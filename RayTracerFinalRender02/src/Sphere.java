import java.awt.Color;

public class Sphere extends Object3D {
    public final Vector3D center;  
    public final double radius;

    public Sphere(Vector3D center, double radius, Color color) {
        super(color);
        this.center = center;
        this.radius = radius;
    }

    @Override
    public Intersection intersect(Ray ray, double near, double far) {
        //calculates the intersection of the ray with the sphere using the quadratic formula
        Vector3D oc = ray.origin.subtract(center);
        double a = ray.direction.dot(ray.direction);
        double b = 2.0 * oc.dot(ray.direction);
        double c = oc.dot(oc) - radius * radius;
        double discriminant = b * b - 4 * a * c;
        //if the discriminant is negative, the ray does not intersect the sphere
        if (discriminant < 0) {
            return new Intersection(false, null, -1, null);
        }
        //calculates the two possible intersection distances along the ray
        double sqrtDiscriminant = Math.sqrt(discriminant);
        double t0 = (-b - sqrtDiscriminant) / (2.0 * a);
        double t1 = (-b + sqrtDiscriminant) / (2.0 * a);

        double t = -1;
        //checks if t0 is within the valid range, if not checks t1
        if (t0 >= near && t0 <= far) {
            t = t0;
        } else if (t1 >= near && t1 <= far) {
            t = t1;
        }
        //if neither t0 nor t1 is within the valid range, return no intersection
        if (t < 0) {
            return new Intersection(false, null, -1, null);
        }
        //calculates the intersection point and normal at the hit location on the sphere
        Vector3D hitPoint = ray.pointAtParameter(t);
        Vector3D normal = hitPoint.subtract(center).normalize();
        return new Intersection(true, hitPoint, t, this, normal);
    }
}