import java.util.ArrayList;
import java.util.List;
import java.awt.Color;

public class Scene {
    public Camera camera;
    public List<Object3D> objects;
    public List<Light> lights;
    public Color backgroundColor;
    public double ambient;
    public Scene(Camera camera) {
        this.camera = camera;
        this.objects = new ArrayList<>();
        this.lights = new ArrayList<>();
        this.backgroundColor = Color.BLACK;
        this.ambient = 0.8;
    }
    public void addObject(Object3D object) {
        this.objects.add(object);
    }
    public void addLight(Light light) {
        this.lights.add(light);
    }
    public void setBackgroundColor(Color color) {
        this.backgroundColor = color;
    }
    public void setAmbient(double ambient) {
        this.ambient = ambient;
    }
}