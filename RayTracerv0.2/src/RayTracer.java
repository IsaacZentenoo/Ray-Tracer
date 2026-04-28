import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class RayTracer {
    public static void main(String[] args) throws Exception {
        int width = 800;
        int height = 800;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        Camera camera = new Camera(new Vector3D(0, 0, 0), 2, 2, 1, 0.1, 100);
        Scene scene = new Scene(camera);
        
        scene.addObject(new Triangle( new Vector3D(-0.8, -0.6, 2.5), new Vector3D(0.0, 0.8, 2.5), new Vector3D(0.8, -0.6, 2.5),Color.ORANGE));
        Color BackgroundColor = Color.BLACK;

        for (int y = 0; y<height; y++) {
            for (int x = 0; x < width; x++) {
                Ray ray = scene.camera.getRay(x, y, width, height);
                Intersection closest = null;
                double closestDistance = Double.MAX_VALUE;
                
                for (Object3D object : scene.objects) {
                    Intersection intersection = object.intersect(ray,scene.camera.near,scene.camera.far);

                    if (intersection.hit && intersection.distance < closestDistance) {
                        closestDistance = intersection.distance;
                        closest = intersection;
                    }
                }
                if (closest != null) {
                    image.setRGB(x, y, closest.object.getColor().getRGB());
                } else {
                    image.setRGB(x, y, BackgroundColor.getRGB());
                }
            }
        }
        try {
            ImageIO.write(image, "png", new File("RayTracev0.2.png"));
            System.out.println("Image saved as RayTracev0.2.png");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
