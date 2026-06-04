public class Intersection {
    public boolean hit;
    public Vector3D point;
    public double distance;
    public Object3D object;
    public Vector3D normal;
    // constructor for a basic intersection without a normal vector, used for non-sphere objects or when the normal is not needed
    public Intersection(boolean hit, Vector3D point, double distance, Object3D object) {
        this.hit = hit;
        this.point = point;
        this.distance = distance;
        this.object = object;
        this.normal = null;
    }
    // constructor that also takes a normal vector, used for sphere intersections to provide the surface normal at the hit point
    public Intersection(boolean hit, Vector3D point, double distance, Object3D object, Vector3D normal) {
        this.hit = hit;
        this.point = point;
        this.distance = distance;
        this.object = object;
        this.normal = normal;
    }
}