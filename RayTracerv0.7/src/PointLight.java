import java.awt.Color;

public class PointLight extends Light {
    public Vector3D position;

    public PointLight(Vector3D position, Color color, double intensity) {
        super(color, intensity);
        this.position = position;
    }

    @Override
    public Vector3D getDirectionFrom(Vector3D point) {
        return position.subtract(point).normalize();
    }

    @Override
    public double getDistanceFrom(Vector3D point) {
        return position.subtract(point).length();
    }

    @Override
    public double getFalloff(Vector3D point) {
        double distance = getDistanceFrom(point);

        if (distance == 0) {
            return intensity;
        }

        return intensity / (distance * distance);
    }
}