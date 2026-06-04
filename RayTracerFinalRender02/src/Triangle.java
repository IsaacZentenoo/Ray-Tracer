import java.awt.Color;
 
public class Triangle extends Object3D {
    public final Vector3D v0;  
    public final Vector3D v1;
    public final Vector3D v2;
    private final String sourceObjectName;
    private final Vector3D n0;
    private final Vector3D n1;
    private final Vector3D n2;
    private final Vector3D edge1;
    private final Vector3D edge2;

    public Triangle(Vector3D v0, Vector3D v1, Vector3D v2, Color color) {
        this(v0, v1, v2, color, null);
    }
    //constructor that takes three vertices, a color, and an optional source object name. It calculates the edge vectors and the face normal for the triangle, which are used in the intersection calculations and shading.
    public Triangle(Vector3D v0, Vector3D v1, Vector3D v2, Color color, String sourceObjectName) {
        super(color);
        this.v0 = v0;
        this.v1 = v1;
        this.v2 = v2;
        this.sourceObjectName = sourceObjectName;
        this.edge1 = v1.subtract(v0);
        this.edge2 = v2.subtract(v0);
        //calculates the face normal using the cross product of the edge vectors and normalizes it, then assigns this normal to all three vertices for flat shading
        Vector3D facenormal = edge1.cross(edge2).normalize();
        this.n0 = facenormal;
        this.n1 = facenormal;
        this.n2 = facenormal;
    }
    //constructor that takes three vertices, three vertex normals, a color, and an optional source object name. It calculates the edge vectors for the triangle and normalizes the vertex normals for smooth shading.
    public Triangle(Vector3D v0, Vector3D v1, Vector3D v2,Vector3D n0, Vector3D n1, Vector3D n2, Color color) {
        this(v0, v1, v2, n0, n1, n2, color, null);
    }
    //constructor that takes three vertices, three vertex normals, a color, and an optional source object name. It calculates the edge vectors for the triangle and normalizes the vertex normals for smooth shading.
    public Triangle(Vector3D v0, Vector3D v1, Vector3D v2,Vector3D n0, Vector3D n1, Vector3D n2, Color color,String sourceObjectName) {
        super(color);
        this.v0 = v0;
        this.v1 = v1;
        this.v2 = v2;
        this.sourceObjectName = sourceObjectName;
        this.n0 = n0.normalize();
        this.n1 = n1.normalize();
        this.n2 = n2.normalize();
        this.edge1 = v1.subtract(v0);
        this.edge2 = v2.subtract(v0);
    }
    public Vector3D getNormal() {
        return edge1.cross(edge2).normalize();
    }
    @Override
    public Intersection intersect(Ray ray, double near, double far) {
        double epsilon = 1e-6;
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
        if (t < near || t > far) {
            return new Intersection(false, null, -1, null);
        }
        Vector3D point = ray.pointAtParameter(t);
        double w = 1.0 - u - v;
        Vector3D phongNormal = n0.multiply(w).add(n1.multiply(u)).add(n2.multiply(v)).normalize();
 
        return new Intersection(true, point, t, this, phongNormal);
    }
    public Vector3D[] getVertices() {
    return new Vector3D[]{ v0, v1, v2 };
}
    public String getSourceObjectName() {
        return sourceObjectName;
    }
}