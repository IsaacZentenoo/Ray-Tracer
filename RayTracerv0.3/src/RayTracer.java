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
                    image.setRGB(x, y, closest.object.getColor().getRGB());
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
}
