import java.awt.Color;

public class PointLight extends Light {
    public Vector3D position;

    public PointLight(Vector3D position, Color color, double intensity) {
        super(color, intensity);
        //position of the point light in the scene
        this.position = position;
    }
    @Override
    //direction from the surface point to the light position
    public Vector3D getDirectionFrom(Vector3D point) {
        return position.subtract(point).normalize();
    }
    @Override
    //calculate the distance from the light to a point, used for falloff calculations
    public double getDistanceFrom(Vector3D point) {
        return position.subtract(point).length();
    }
    @Override
    public double getFalloff(Vector3D point) {
        double distance = getDistanceFrom(point);
        //avoids division by zero if the point is exactly at the light position
        if (distance == 0) {
            return intensity;
        }
        //inverse square falloff for point lights
        return intensity / (distance * distance);
    }
}