import java.awt.Color;
public class Triangle extends Object3D {
    private Vector3D v0;
    private Vector3D v1;
    private Vector3D v2;

    public Triangle(Vector3D v0, Vector3D v1, Vector3D v2, Color color) {
        super(color);
        this.v0 = v0;
        this.v1 = v1;
        this.v2 = v2;
    }

    @Override
    public Intersection intersect(Ray ray, double near, double far) {
        double epsilon = 1e-6;
        Vector3D edge1 = v1.subtract(v0);
        Vector3D edge2 = v2.subtract(v0);
        Vector3D h = ray.direction.cross(edge2);

        double determinant = edge1.dot(h);

        if (Math.abs(determinant) < epsilon) {
            return new Intersection(false, null, -1, null);
        }
        double invDeterminant = 1.0 / determinant;
        Vector3D tvector = ray.origin.subtract(v0);
        double u = tvector.dot(h) * invDeterminant;

        if (u < 0.0 || u > 1.0) {
            return new Intersection(false, null, -1, null);
        }
        Vector3D qvector = tvector.cross(edge1);
        double v = invDeterminant * ray.direction.dot(qvector);

        if (v < 0.0 || u + v > 1.0) {
            return new Intersection(false, null, -1, null);
        }
        double t = invDeterminant * edge2.dot(qvector);

        if (t < near || t > far) {
            return new Intersection(false, null, -1, null);
        }
        Vector3D hitPoint = ray.pointAtParameter(t);
        return new Intersection(true, hitPoint, t, this);
    }
}
