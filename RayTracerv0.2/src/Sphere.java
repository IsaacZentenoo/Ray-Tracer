import java.awt.Color;

public class Sphere extends Object3D {
    private Vector3D center;
    private double radius;

    public Sphere(Vector3D center, double radius, Color color) {
        super(color);
        this.center = center;
        this.radius = radius;
    }
    @Override
    public Intersection intersect(Ray ray, double near, double far) {
        Vector3D oc = ray.origin.subtract(center);
        double a = ray.direction.dot(ray.direction);
        double b = 2.0 * oc.dot(ray.direction);
        double c = oc.dot(oc) - radius * radius;
        double discriminant = b * b - 4 * a * c;

        if (discriminant < 0) {
            return new Intersection(false, null, -1, null);
        }
        double sqrtDiscriminant = Math.sqrt(discriminant);
        double t0 = (-b - sqrtDiscriminant) / (2.0 * a);
        double t1 = (-b + sqrtDiscriminant) / (2.0 * a);
        
        double t = -1;

        if (t0 >= near && t1 <= far) {
            t = t0;
        } else if (t1 >= near && t1 <= far) {
            t = t1;
        }
        if (t < 0) {
            return new Intersection(false, null, -1, null);
        }
        Vector3D hitPoint = ray.pointAtParameter(t);
        return new Intersection(true, hitPoint, t, this);
    }
}
