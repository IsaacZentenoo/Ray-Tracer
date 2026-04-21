import java.util.ArrayList;
import java.util.List;

public class Scene {
    public Camera camera;
    public List<Object3D> objects;
    public Scene(Camera camera) {
        this.camera = camera;
        this.objects = new ArrayList<>();
    }
    public void addObject(Object3D object) {
        objects.add(object);
    }
}
