import java.awt.Color;
public class Triangle extends Object3D {
    private Vector3D v0;
    private Vector3D v1;
    private Vector3D v2;

    private Vector3D n0;
    private Vector3D n1;
    private Vector3D n2;

    public Triangle(Vector3D v0, Vector3D v1, Vector3D v2, Color color) {
        super(color);
        this.v0 = v0;
        this.v1 = v1;
        this.v2 = v2;


        Vector3D V = v1.subtract(v0);
        Vector3D W = v2.subtract(v0);
        Vector3D facenormal = V.cross(W).normalize();

        this.n0 = facenormal;
        this.n1 = facenormal;
        this.n2 = facenormal;
    }
    public Triangle(Vector3D v0, Vector3D v1, Vector3D v2, Vector3D n0, Vector3D n1, Vector3D n2, Color color) {
        super(color);
        this.v0 = v0;
        this.v1 = v1;
        this.v2 = v2;
        this.n0 = n0.normalize();
        this.n1 = n1.normalize();
        this.n2 = n2.normalize();
    }
    public Vector3D getNormal(){
        Vector3D V = v1.subtract(v0);
        Vector3D W = v2.subtract(v0);
        return V.cross(W).normalize();
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
        Vector3D s = ray.origin.subtract(v0);

        double u = invDeterminant * s.dot(h);

        if (u < 0.0 || u > 1.0) {
            return new Intersection(false, null, -1, null);
        }

        Vector3D q = s.cross(edge1);
        double v = invDeterminant * ray.direction.dot(q);

        if (v < 0.0 || u + v > 1.0) {
            return new Intersection(false, null, -1, null);
        }
        double t = invDeterminant * edge2.dot(q);
        if (t< near  || t> far){
            return new Intersection(false, null, -1, null);
        }
        Vector3D point = ray.pointAtParameter(t);
        double w = 1.0 - u - v;

        Vector3D phongNormal = n0.multiply(w).add(n1.multiply(u)).add(n2.multiply(v)).normalize();

        return new Intersection(true, point, t, this, phongNormal);
    }}