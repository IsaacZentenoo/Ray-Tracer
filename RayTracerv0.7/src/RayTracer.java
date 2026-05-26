import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.List;
import java.util.stream.IntStream;

public class RayTracer {
    public static void main(String[] args) throws Exception {
        int width = 800;
        int height = 800;

        Camera camera = new Camera(
            new Vector3D(0, 0, 0), 2, 2, 1, 0.1, 100
        );
        Scene scene = new Scene(camera);

        List<Triangle> teapot = Objreader.loadOBJ("Ball.obj", new Color(190, 15, 15), new Vector3D(0, 0, 3.5), 1.2);
        for (Triangle t : teapot) {
            t.setMaterial(1.2, 96);
            scene.addObject(t);
        }
        // Cargar otro modelo a la izquierda
        List<Triangle> objeto2 = Objreader.loadOBJ("taza.obj",new Color(30, 100, 200),new Vector3D(-2.5, 0, 4.0),1.0);
        for (Triangle t : objeto2) {t.setMaterial(0.8, 64);
        scene.addObject(t);
        }
        Triangle floor1 = new Triangle(
            new Vector3D(-4.0, -1.2, 1.8),
            new Vector3D( 4.0, -1.2, 8.0),
            new Vector3D( 4.0, -1.2, 1.8),
            new Color(85, 85, 85)
        );
        floor1.setMaterial(0.1, 8);
        scene.addObject(floor1);

        Triangle floor2 = new Triangle(
            new Vector3D(-4.0, -1.2, 1.8),
            new Vector3D(-4.0, -1.2, 8.0),  
            new Vector3D( 4.0, -1.2, 8.0), 
            new Color(85, 85, 85)
        );
        floor2.setMaterial(0.1, 8);
        scene.addObject(floor2);

        scene.setBackgroundColor(Color.BLACK);
        scene.setAmbient(0.08);
        scene.addLight(new PointLight(
            new Vector3D(-2.5, 1.5, 2.2), Color.WHITE, 45.0
        ));
        int[] pixels = new int[width * height];

        IntStream.range(0, height).parallel().forEach(y -> {
            for (int x = 0; x < width; x++) {
                Ray ray = scene.camera.getRay(x, y, width, height);

                Intersection closest = null;
                double closestDistance = Double.MAX_VALUE;

                for (Object3D object : scene.objects) {
                    Intersection intersection = object.intersect(ray, scene.camera.near, scene.camera.far);
                    if (intersection.hit && intersection.distance < closestDistance) {
                        closest = intersection;
                        closestDistance = intersection.distance;
                    }
                }

                pixels[y * width + x] = (closest != null)
                    ? phongShade(closest, scene).getRGB()
                    : scene.backgroundColor.getRGB();
            }
        });

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < pixels.length; i++) {
            image.setRGB(i % width, i / width, pixels[i]);
        }

        try {
            ImageIO.write(image, "png", new File("render.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Color phongShade(Intersection intersection, Scene scene) {
        Vector3D normal = intersection.normal;
        if (normal == null) {
            normal = new Vector3D(0, 0, 1);
        }
        normal = normal.normalize();

        Color objectColor = intersection.object.getColor();
        double red   = objectColor.getRed()   * scene.ambient;
        double green = objectColor.getGreen() * scene.ambient;
        double blue  = objectColor.getBlue()  * scene.ambient;

        double specularStrength = intersection.object.getSpecularStrength();
        double shininess        = intersection.object.getShininess();

        Vector3D viewDirection = scene.camera.position.subtract(intersection.point).normalize();

        for (Light light : scene.lights) {
            if (isInShadow(intersection, light, scene)) {
                continue;
            }
            Vector3D lightDir = light.getDirectionFrom(intersection.point);
            double dot = Math.max(0, normal.dot(lightDir));
            double falloff = light.getFalloff(intersection.point);

            double lightRed   = light.color.getRed()   / 255.0;
            double lightGreen = light.color.getGreen() / 255.0;
            double lightBlue  = light.color.getBlue()  / 255.0;

            red   += objectColor.getRed()   * lightRed   * falloff * dot;
            green += objectColor.getGreen() * lightGreen * falloff * dot;
            blue  += objectColor.getBlue()  * lightBlue  * falloff * dot;

            Vector3D halfVector = lightDir.add(viewDirection).normalize();
            double specAngle = Math.max(0.0, normal.dot(halfVector));
            double specular  = Math.pow(specAngle, shininess) * specularStrength * falloff;

            red   += 255 * lightRed   * specular;
            green += 255 * lightGreen * specular;
            blue  += 255 * lightBlue  * specular;
        }

        return new Color(clamp(red), clamp(green), clamp(blue));
    }

    public static int clamp(double value) {
        return (int) Math.max(0, Math.min(255, value));
    }

    public static boolean isInShadow(Intersection intersection, Light light, Scene scene) {
        double epsilon = 0.001;
        Vector3D lightDirection = light.getDirectionFrom(intersection.point);
        Vector3D normal = (intersection.normal != null) ? intersection.normal : new Vector3D(0, 0, 1);

        Vector3D shadowOrigin = intersection.point.add(normal.normalize().multiply(epsilon));
        Ray shadowRay = new Ray(shadowOrigin, lightDirection);

        double maxDistance = (light instanceof PointLight)
            ? light.getDistanceFrom(intersection.point)
            : 1000.0;

        for (Object3D object : scene.objects) {
            if (object == intersection.object) continue;
            Intersection shadowIntersection = object.intersect(shadowRay, epsilon, maxDistance);
            if (shadowIntersection.hit) return true;
        }
        return false;
    }
}