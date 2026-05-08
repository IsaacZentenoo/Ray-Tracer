import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.List;

public class RayTracer {
    public static void main(String[] args) throws Exception {
        int width = 800;
        int height = 800;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        Camera camera = new Camera(
            new Vector3D(0, 0, 0), 2, 2, 1,0.1, 100
        );
        Scene scene = new Scene(camera);
        List<Triangle> plant =
            Objreader.loadOBJ("indoor_plant_02.obj", new Color(20, 140, 70));
        for (Triangle t : plant) {
            scene.addObject(t);
        }

        Light light = new Light(
        new Vector3D(1, -1, -1),
        Color.WHITE,
        1.0);

        Color backgroundColor = Color.BLACK;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Ray ray = scene.camera.getRay(x, y, width, height);

                Intersection closest = null;
                double closestDistance = Double.MAX_VALUE;
                
                  for (Object3D object : scene.objects) {
                    Intersection intersection = object.intersect(ray,scene.camera.near,scene.camera.far);

                    if (intersection.hit && intersection.distance < closestDistance) {
                        closest = intersection;
                        closestDistance = intersection.distance;
                    }
                }
                if (closest != null) {
                    Color finalColor = phongShade(closest, light);
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
    public static Color phongShade(Intersection intersection, Light light) {
        Vector3D normal = intersection.normal;

        if (normal == null) {
            normal = new Vector3D(0, 0, 1);
        }

        normal = normal.normalize();

        double ambient = 0.35;
        double dot = Math.abs(normal.dot(light.direction));

        double finalIntensity = ambient + (1.0 - ambient) * dot;
        finalIntensity = finalIntensity * light.intensity;

        Color objectColor = intersection.object.getColor();
        Color lightColor = light.color;

        int r = (int)(objectColor.getRed() * (lightColor.getRed() / 255.0) * finalIntensity);
        int g = (int)(objectColor.getGreen() * (lightColor.getGreen() / 255.0) * finalIntensity);
        int b = (int)(objectColor.getBlue() * (lightColor.getBlue() / 255.0) * finalIntensity);

        r = clamp(r);
        g = clamp(g);
        b = clamp(b);

        return new Color(r, g, b);
    }

    public static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
