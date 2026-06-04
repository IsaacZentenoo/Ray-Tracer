import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class MtlReader {

    public static class Material {
        public Color  diffuse;      
        public Color  specular;      
        public double shininess;   
        public double transparency;  
        public double ior;           
        public int    illum;         
        public Material() {
            //default material values
            diffuse = new Color(200, 200, 200);
            specular = new Color(128, 128, 128);
            shininess = 50.0;
            transparency = 0.0;
            ior = 1.0;
            illum = 2;
        }
        public double getShininess() {
            return Math.max(1.0, shininess);
        }
        public double getSpecularStrength() {
            return (specular.getRed() + specular.getGreen() + specular.getBlue()) / (3.0 * 255.0);
        }
        public double getReflectivity() {
            if (illum >= 3) return 0.3;
            return 0.05;
        }
        public double getTransparency() {
            return Math.max(0.0, 1.0 - transparency);
        }
    }  
    //loads materials from a .mtl file and returns a map of material names to Material objects
    public static Map<String, Material> load(String mtlPath) {
        Map<String, Material> materials = new HashMap<>();
        Material current = null;
        String   currentName = null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(mtlPath));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("\\s+");
                String   key   = parts[0].toLowerCase();

                switch (key) {
                    case "newmtl":
                        if (currentName != null && current != null)
                            materials.put(currentName, current);
                        currentName = parts[1];
                        current     = new Material();
                        break;
                   case "kd":
    
    if (current != null && parts.length >= 4) {
        float r = Float.parseFloat(parts[1]);
        float g = Float.parseFloat(parts[2]);
        float b = Float.parseFloat(parts[3]);
        if (r < 0.05f && g < 0.05f && b < 0.05f)
            current.diffuse = new Color(60, 60, 65);
        else if (b > r * 1.5f && b > g * 1.2f && b > 0.5f)
            current.diffuse = new Color(
                clamp(r * 0.7f + 0.15f),
                clamp(g * 0.7f + 0.15f),
                clamp(b * 0.5f + 0.2f));
        else
            current.diffuse = new Color(clamp(r), clamp(g), clamp(b));
    }
    break;
             case "ks": 
                if (current != null && parts.length >= 4) {
                    current.specular = new Color(
                    clamp(Float.parseFloat(parts[1])),
                    clamp(Float.parseFloat(parts[2])),
                    clamp(Float.parseFloat(parts[3])));
                    }
                     break;
                        case "ns"://shininess
                            if (current != null && parts.length >= 2)
                                current.shininess = Double.parseDouble(parts[1]);
                                break;
                        case "d"://transparency
                            if (current != null && parts.length >= 2)
                                current.transparency = Double.parseDouble(parts[1]);
                                break;  
                        case "ni"://index of refraction
                            if (current != null && parts.length >= 2)
                                current.ior = Double.parseDouble(parts[1]);
                                break;
                        case "illum"://illumination 
                            if (current != null && parts.length >= 2)
                                current.illum = Integer.parseInt(parts[1]);
                                break;
                            default:
                            break;
                }}
            if (currentName != null && current != null)
                materials.put(currentName, current);
            reader.close();
        } catch (Exception e){
        }return materials;
    }
    private static int clamp(float v) {
        return (int)(Math.max(0, Math.min(1, v)) * 255);
    }
}