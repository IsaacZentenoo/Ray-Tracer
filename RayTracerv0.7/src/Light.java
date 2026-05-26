import java.awt.Color;

public abstract class Light {
    public Color color;
    public double intensity;

    public Light(Color color, double intensity) {
        this.color = color;
        this.intensity = intensity;
    }

    public abstract Vector3D getDirectionFrom(Vector3D point);
    public abstract double getDistanceFrom(Vector3D point);
    public abstract double getFalloff(Vector3D point);
}




