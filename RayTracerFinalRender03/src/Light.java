import java.awt.Color;

public abstract class Light {
    public Color color;
    public double intensity;

    public Light(Color color, double intensity) {
        this.color = color;
        this.intensity = intensity;
    }

    public abstract Vector3D getDirectionFrom(Vector3D point);    //returns the direction from a point in the scene to the light source
    public abstract double getDistanceFrom(Vector3D point); //returns the distance from a point to the light
    public abstract double getFalloff(Vector3D point); //returns the light intensity after applying distance attenuation
}




