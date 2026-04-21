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

        Camera camera = new Camera(new Vector3D(0, 0, 0), 2.0, 2.0, 1.0);
        Scene scene = new Scene(camera);
        scene.addObject(new Sphere(new Vector3D(-0.3, 0.2, 3), 0.25, Color.RED));
        scene.addObject(new Sphere(new Vector3D(0.4, 0.2, 3), 0.15, Color.BLUE));

        Color backgroundColor = Color.WHITE;

        for(int y = 0 ; y < height; y++) {
            for(int x = 0; x < width; x++) {
                Ray ray = camera.getRay(x, y, width, height);
                Intersection closeIntersection = null;
                double closestDistance = Double.MAX_VALUE;

                for (Object3D object : scene.objects){
                    Intersection intersection = object.intersect(ray);
                    if (intersection.hit && intersection.distance < closestDistance) {
                        closestDistance = intersection.distance;
                        closeIntersection = intersection;
                    }
                }
                if (closeIntersection!=null){
                    image.setRGB(x,y, closeIntersection.object.getColor().getRGB());
                } else {
                    image.setRGB(x,y, backgroundColor.getRGB());
                }
            }
        }
        try {
            ImageIO.write(image, "png", new File("RayTracev0.1.png"));
            System.out.println("Image saved as RayTracev0.1.png");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
