import java.awt.Color;

public abstract class Light {
    public Color color;
    public double intensity;
    //base class for all light types, defines common properties and abstract methods for direction, distance, and falloff calculations
    public Light(Color color, double intensity) {
        this.color = color;
        this.intensity = intensity;
    }
    //abstract methods that must be implemented by specific light types to provide the direction from a point to the light, the distance from the light to a point, and the intensity falloff based on distance and angle
    public abstract Vector3D getDirectionFrom(Vector3D point);
    public abstract double getDistanceFrom(Vector3D point);
    public abstract double getFalloff(Vector3D point);
}




