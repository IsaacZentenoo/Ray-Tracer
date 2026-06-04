import java.awt.Color;

public class DirectionalLight extends Light {
    public Vector3D direction;

    public DirectionalLight(Vector3D direction, Color color, double intensity) {
        super(color, intensity);
        //directional lights use a fixed direction for the whole scene
        this.direction = direction.normalize();
    }
    @Override
    public Vector3D getDirectionFrom(Vector3D point) {
        //the light direction does not depend on the point position
        return direction;
    }

    @Override
    public double getDistanceFrom(Vector3D point) {
        //directional light is treated as infinitely far away
        return Double.POSITIVE_INFINITY;
    }
    @Override
    public double getFalloff(Vector3D point) {
        //directional lights do not lose intensity with distance
        return intensity;
    }
}