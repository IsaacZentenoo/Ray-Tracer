import java.awt.Color;

public class DirectionalLight extends Light {
    public Vector3D direction;

    public DirectionalLight(Vector3D direction, Color color, double intensity) {
        super(color, intensity);
        this.direction = direction.normalize();
    }

    @Override
    public Vector3D getDirectionFrom(Vector3D point) {
        return direction;
    }

    @Override
    public double getDistanceFrom(Vector3D point) {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public double getFalloff(Vector3D point) {
        return intensity;
    }
}