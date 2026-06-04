public class Intersection {
    public boolean hit;
    public Vector3D point;
    public double distance;
    public Object3D object;
    public Vector3D normal;
    public double baryU;
    public double baryV;

    public Intersection(boolean hit, Vector3D point, double distance, Object3D object) {
        this.hit = hit;
        this.point = point;
        this.distance = distance;
        this.object = object;
        this.normal = null; //default values when no normal or barycentric data is needed
        this.baryU = 0;
        this.baryV = 0;
    }

    public Intersection(boolean hit, Vector3D point, double distance, Object3D object, Vector3D normal) {
        this.hit = hit;
        this.point = point;
        this.distance = distance;
        this.object = object;
        this.normal = normal; //stores the surface normal at the intersection point
        this.baryU = 0;
        this.baryV = 0;
    }

    public Intersection(boolean hit, Vector3D point, double distance, Object3D object, Vector3D normal, double baryU, double baryV) {
        this.hit = hit;
        this.point = point;
        this.distance = distance;
        this.object = object;
        this.normal = normal;

        //barycentric coordinates used mainly for triangle interpolation
        this.baryU = baryU;
        this.baryV = baryV;
    }
}