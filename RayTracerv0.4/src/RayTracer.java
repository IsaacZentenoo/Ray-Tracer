import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.List;

public class RayTracer {
    public static void main(String[] args) throws Exception {
        int width = 400;
        int height = 400;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        Camera camera = new Camera(
            new Vector3D(0, 0, 0), 2, 2, 1,0.1, 100
        );
        Scene scene = new Scene(camera);
        List<Triangle> plant =
            Objreader.loadOBJ("indoor_plant_02.obj", Color.CYAN);
        for (Triangle t : plant) {
            scene.addObject(t);
        }

        Vector3D lightDirection = new Vector3D(0, 0.3, -1.0).normalize();
        double lightIntensity = 1.;
        Color lightColor = Color.WHITE;

        Color backgroundColor = Color.GRAY;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Ray ray = scene.camera.getRay(x, y, width, height);

                Intersection closest = null;
                double closestDistance = Double.MAX_VALUE;
                
                for (Object3D object : scene.objects){
                    Intersection intesection = object.intersect(ray,scene.camera.near,scene.camera.far);
                    if (intesection.hit && intesection.distance < closestDistance) {
                        closestDistance = intesection.distance;
                        closest = intesection;
                    }
                }
                if (closest != null) {
                    Color finalColor = shade(closest.object, lightDirection, lightIntensity, lightColor);
                    image.setRGB(x, y, finalColor.getRGB());    
                } else {
                    image.setRGB(x, y, backgroundColor.getRGB());
                }
            }
        }
        try {
            ImageIO.write(image, "png", new File("render.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static Color shade(Object3D object, Vector3D lightDirection, double lightIntensity, Color lightColor) {
       Vector3D normal;
       if (object instanceof Triangle){
        normal = ((Triangle) object).getNormal();
       } else {
        normal = new Vector3D(0,0,1);
       }
       double ambient = 0.1;
       double dot = Math.abs(normal.dot(lightDirection));
       dot = Math.max(dot, 0);
       double intensity = ambient + (1 - ambient) * dot;

       Color objectColor = object.getColor();
       int r = (int)(objectColor.getRed() * (lightColor.getRed() / 255.0) * lightIntensity * intensity);
       int g = (int)(objectColor.getGreen() * (lightColor.getGreen() / 255.0) * lightIntensity * intensity);
       int b = (int)(objectColor.getBlue() * (lightColor.getBlue() / 255.0) * lightIntensity * intensity);

       r  = Math.min(255, Math.max(0, r));
       g  = Math.min(255, Math.max(0, g));
       b = Math.min(255, Math.max(0, b));
       
       return new Color(r, g, b);
    }
}
