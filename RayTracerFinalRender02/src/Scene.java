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
        //list that objects and lights in the scene are stored in
        this.objects = new ArrayList<>();
        this.lights = new ArrayList<>();

        this.backgroundColor = Color.BLACK;
        this.ambient = 0.08;
    }
    //add objects and lights to the scene
    public void addObject(Object3D object) {
        this.objects.add(object);
    }
    //add lights to the scene
    public void addLight(Light light) {
        this.lights.add(light);
    }//set the background color of the scene
    public void setBackgroundColor(Color color) {
        this.backgroundColor = color;
    }//set the ambient light intensity in the scene
    public void setAmbient(double ambient) {
        this.ambient = ambient;
    }
}