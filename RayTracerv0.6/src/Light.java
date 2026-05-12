import java.awt.Color;

public class Light {

    public static final int DIRECTIONAL = 0;
    public static final int POINT = 1;

    public int type;
    public Vector3D position;
    public Vector3D direction;
    public Color color;
    public double intensity;

    public Light(int type, Vector3D vector, Color color, double intensity) {
        this.type = type;
        this.color = color;
        this.intensity = intensity;

        if (type == DIRECTIONAL) {
            this.direction = vector.normalize();
            this.position = null;
        } else {
            this.position = vector;
            this.direction = null;
        }
    }

    public Vector3D getDirectionFromPoint(Vector3D point) {
        if (type == DIRECTIONAL) {
            return direction;
        }
        return position.subtract(point).normalize();
    }

    public Vector3D getDirectionFrom(Vector3D point) {
        return getDirectionFromPoint(point);
    }

}
