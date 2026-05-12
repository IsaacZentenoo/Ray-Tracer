import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.List;
import java.util.ArrayList;

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
        List <Light> lights =  new ArrayList<>();
        lights.add(new Light(Light.DIRECTIONAL,new Vector3D(0, 0.3, -1.0),Color.WHITE,0.8));
        lights.add(new Light(Light.POINT,new Vector3D(0.0, 1.0, 2.0),Color.WHITE,1.0));

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
                    Color finalColor = phongShade(closest, lights);
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
    public static Color phongShade(Intersection intersection, List<Light> lights) {
        Vector3D normal = intersection.normal;

        if (normal == null) {
            normal = new Vector3D(0, 0, 1);
        }

        normal = normal.normalize();

        Color objectColor = intersection.object.getColor();
        double ambient = 0.1;

        double red = objectColor.getRed() * ambient;
        double green = objectColor.getGreen() * ambient;
        double blue = objectColor.getBlue() * ambient;

        for (Light light : lights) {
            Vector3D lightDirection = light.getDirectionFrom(intersection.point);

            double dot = normal.dot(lightDirection);
            dot = Math.max(0.0, dot);

            double lightRed = light.color.getRed() / 255.0;
            double lightGreen = light.color.getGreen() / 255.0;
            double lightBlue = light.color.getBlue() / 255.0;

            red += objectColor.getRed() * lightRed * light.intensity * dot;
            green += objectColor.getGreen() * lightGreen * light.intensity * dot;
            blue += objectColor.getBlue() * lightBlue * light.intensity * dot;
        }

        int r = clamp((int) red);
        int g = clamp((int) green);
        int b = clamp((int) blue);

        return new Color(r,g,b);
    }

    private static int clamp(int v) {
        if (v < 0) return 0;
        if (v > 255) return 255;
        return v;
    }
}
