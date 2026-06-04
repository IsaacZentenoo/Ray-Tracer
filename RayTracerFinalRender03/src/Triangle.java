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

    public double[] uv0, uv1, uv2;
    public MtlReader.Material material;

    public Triangle(Vector3D v0, Vector3D v1, Vector3D v2, Color color) {
        this(v0, v1, v2, color, null);
    }

    public Triangle(Vector3D v0, Vector3D v1, Vector3D v2, Color color, String sourceObjectName) {
        super(color);
        this.v0 = v0;
        this.v1 = v1;
        this.v2 = v2;
        this.sourceObjectName = sourceObjectName;
        //precomputed edges used for normals and intersection tests
        this.edge1 = v1.subtract(v0);
        this.edge2 = v2.subtract(v0);
        //face normal used when the triangle doesn't have vertex normals
        Vector3D facenormal = edge1.cross(edge2).normalize();
        this.n0 = facenormal;
        this.n1 = facenormal;
        this.n2 = facenormal;
        //default uvs coordinates 
        this.uv0 = new double[]{0,0};
        this.uv1 = new double[]{1,0};
        this.uv2 = new double[]{0,1};
        this.material = null;
    }

    public Triangle(Vector3D v0, Vector3D v1, Vector3D v2,
                    Vector3D n0, Vector3D n1, Vector3D n2, Color color) {
        this(v0, v1, v2, n0, n1, n2, color, null);
    }

    public Triangle(Vector3D v0, Vector3D v1, Vector3D v2,
                    Vector3D n0, Vector3D n1, Vector3D n2, Color color,
                    String sourceObjectName) {
        super(color);
        this.v0 = v0;
        this.v1 = v1;
        this.v2 = v2;
        this.sourceObjectName = sourceObjectName;
        //vertex normals for smooth shading
        this.n0 = n0.normalize();
        this.n1 = n1.normalize();
        this.n2 = n2.normalize();
        this.edge1 = v1.subtract(v0);
        this.edge2 = v2.subtract(v0);
        this.uv0 = new double[]{0,0};
        this.uv1 = new double[]{1,0};
        this.uv2 = new double[]{0,1};
        this.material = null;
    }
    //stores texture coordinates and material for this triangle
    public void setUVs(double[] uv0, double[] uv1, double[] uv2, MtlReader.Material mat) {
        this.uv0 = uv0;
        this.uv1 = uv1;
        this.uv2 = uv2;
        this.material = mat;
    }

    public Color sampleAt(double w0, double w1, double w2) {
        //if the triangle has no texture, use its base color
        if (material == null || !material.hasTexture()) return getColor();
        //interpolates UV coordinates using barycentric weights
        double u = w0 * uv0[0] + w1 * uv1[0] + w2 * uv2[0];
        double v = w0 * uv0[1] + w1 * uv1[1] + w2 * uv2[1];
        return material.sampleTexture(u, v);
    }

    public Vector3D getNormal() {
        // calculates the triangle face normal
        return edge1.cross(edge2).normalize();
    }

    @Override
    public Intersection intersect(Ray ray, double near, double far) {
        double epsilon = 1e-6;
        Vector3D h = ray.direction.cross(edge2); //moller-Trumbore ray-triangle intersection algorithm
        double determinant = edge1.dot(h);
        //if determinant is close to 0, the ray is parallel to the triangle
        if (Math.abs(determinant) < epsilon)
            return new Intersection(false, null, -1, null);

        double invDeterminant = 1.0 / determinant;
        Vector3D s = ray.origin.subtract(v0);
        double u = invDeterminant * s.dot(h);

        //checks if the intersection is outside the triangle
        if (u < 0.0 || u > 1.0)
            return new Intersection(false, null, -1, null);

        Vector3D q = s.cross(edge1);
        double v = invDeterminant * ray.direction.dot(q);
        //checks the second barycentric coordinate
        if (v < 0.0 || u + v > 1.0)
            return new Intersection(false, null, -1, null);

        double t = invDeterminant * edge2.dot(q);
        //checks if the intersection is inside the valid ray range
        if (t < near || t > far)
            return new Intersection(false, null, -1, null);

        Vector3D point = ray.pointAtParameter(t);
        double w = 1.0 - u - v;
        //interpolates the normal for smooth Phong shading
        Vector3D phongNormal = n0.multiply(w).add(n1.multiply(u)).add(n2.multiply(v)).normalize();

        return new Intersection(true, point, t, this, phongNormal, u, v);
    }
    public Vector3D[] getVertices() {
        return new Vector3D[]{ v0, v1, v2 };
    }

    public String getSourceObjectName() {
        return sourceObjectName;
    }
}