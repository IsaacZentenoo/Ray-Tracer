import java.awt.Color;

public class Light {
    public Vector3D direction;
    public Color color;
    public double intensity;
    public Light(Vector3D direction, Color color, double intensity) {
        this.direction = direction.normalize();
        this.color = color;
        this.intensity = intensity;

    }
}
