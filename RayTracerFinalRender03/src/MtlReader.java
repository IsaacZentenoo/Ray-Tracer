import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

public class MtlReader {

    public static class Material {
        public Color diffuse;
        public Color specular;
        public double shininess;
        public double transparency;
        public double ior;
        public int illum;
        public BufferedImage diffuseTexture;
        public String texturePath;

        public Material() {

            //default material values
            diffuse = new Color(200, 200, 200);
            specular = new Color(128, 128, 128);
            shininess = 50.0;
            transparency = 0.0;
            ior = 1.0;
            illum = 2;
            diffuseTexture = null;
            texturePath = null;
        }
        public Color sampleTexture(double u, double v) {
            //if there is no texture, return the diffuse color
            if (diffuseTexture == null) return diffuse;
            u = u - Math.floor(u); //keeps u and v in the [0,1] range for tiling
            v = v - Math.floor(v);
            int px = (int)(u * (diffuseTexture.getWidth()  - 1));
            int py = (int)((1.0 - v) * (diffuseTexture.getHeight() - 1));
            //prevents reading outside the image limits
            px = Math.max(0, Math.min(diffuseTexture.getWidth()  - 1, px));
            py = Math.max(0, Math.min(diffuseTexture.getHeight() - 1, py));
            return new Color(diffuseTexture.getRGB(px, py));
        }
        public boolean hasTexture() { return diffuseTexture != null; }
        public double getShininess()        { return Math.max(1.0, shininess); }
        public double getSpecularStrength() {
            return (specular.getRed() + specular.getGreen() + specular.getBlue()) / (3.0 * 255.0);
        }
        public double getReflectivity()  { return illum >= 3 ? 0.3 : 0.05; }
        public double getTransparency()  { return Math.max(0.0, 1.0 - transparency); }
    }
    public static Map<String, Material> load(String mtlPath) {
        Map<String, Material> materials = new HashMap<>();
        Material current     = null;
        String   currentName = null;
        String folder = mtlPath.contains("/")
            ? mtlPath.substring(0, mtlPath.lastIndexOf('/') + 1)
            : "./models/";
        try {
            BufferedReader reader = new BufferedReader(new FileReader(mtlPath));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("\\s+");
                String   key   = parts[0].toLowerCase();
                switch (key) {
                    case "newmtl": // saves the previous material and starts a new one
                        if (currentName != null && current != null)
                            materials.put(currentName, current);
                        currentName = parts[1];
                        current= new Material();
                        break;
                    case "kd": //diffuse color
                        if (current != null && parts.length >= 4)
                            current.diffuse = new Color(
                                clamp(Float.parseFloat(parts[1])),
                                clamp(Float.parseFloat(parts[2])),
                                clamp(Float.parseFloat(parts[3])));
                        break;
                    case "ks": //specular color
                        if (current != null && parts.length >= 4)
                            current.specular = new Color(
                                clamp(Float.parseFloat(parts[1])),
                                clamp(Float.parseFloat(parts[2])),
                                clamp(Float.parseFloat(parts[3])));
                        break;
                    case "ns": //shininess
                        if (current != null && parts.length >= 2)
                            current.shininess = Double.parseDouble(parts[1]);
                        break;
                    case "d": //transparency
                        if (current != null && parts.length >= 2)
                            current.transparency = Double.parseDouble(parts[1]);
                        break;
                    case "ni": //index of refraction
                        if (current != null && parts.length >= 2)
                            current.ior = Double.parseDouble(parts[1]);
                        break;
                    case "illum": //ilumination model
                        if (current != null && parts.length >= 2)
                            current.illum = Integer.parseInt(parts[1]);
                        break;
                    case "map_kd":
                        if (current != null && parts.length >= 2) {
                            //possible texture locations
                            String texName  = line.substring(key.length()).trim();
                            String justFile = new java.io.File(texName).getName();
                            String[] candidates = {folder + texName,folder + justFile,"./models/textures/"  + justFile,
                                "./models/textures2/" + justFile,"./models/" + justFile,texName
                            }; 
                            //tries to load the first texture that exists in the possible locations
                            for (String candidate : candidates) {
                                java.io.File f = new java.io.File(candidate);
                                if (f.exists()) {
                                    try {
                                        current.diffuseTexture = ImageIO.read(f);
                                        current.texturePath    = candidate;
                                        System.out.println("Textura cargada: " + candidate);
                                    } catch (Exception ex) {
                                        System.err.println("Error cargando textura: " + candidate);
                                    }
                                    break;
                                }
                            }
                            if (current.diffuseTexture == null)
                                System.err.println("Textura no encontrada: " + texName);
                        }
                        break;
                    default:
                        break;
                }
            }
            if (currentName != null && current != null) //saves the last material read
                materials.put(currentName, current);
            reader.close();
            String[] texFolders = { "./models/textures/", "./models/textures2/" };
        String[]exts= { "_baseColor.jpeg", "_baseColor.png", "_baseColor.jpg" };
        //tries to assign textures automatically if map_kd was not found
            for (Map.Entry<String, Material> entry : materials.entrySet()) {
                if (entry.getValue().diffuseTexture != null) continue;
                    String name = entry.getKey();
                    outer:
                        for (String tf : texFolders) {
                            for (String ext : exts) {
                                java.io.File f = new java.io.File(tf + name + ext);
                                if (!f.exists()) {
                                    String base = name.replaceAll("\\.(\\d{3})$", "");
                                    f = new java.io.File(tf + base + ".001" + ext);
                                    if (!f.exists()) f = new java.io.File(tf + base + ext);
                            }
            if (f.exists()) {
                try {
                    entry.getValue().diffuseTexture = ImageIO.read(f);
                    entry.getValue().texturePath    = f.getPath();
                } catch (Exception ex) {
                }
                break outer;
            }}}}
        } catch (Exception e) {
            System.err.println("Error leyendo MTL: " + mtlPath + " — " + e.getMessage());
        }
        return materials;
    }
    //converts a 0-1 value into a 0-255 color value
    private static int clamp(float v) {
        return (int)(Math.max(0, Math.min(1, v)) * 255);
    }
}