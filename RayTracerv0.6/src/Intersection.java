public class Intersection {
    public boolean hit;
    public Vector3D point;
    public double distance;
    public Object3D object;
    public Vector3D normal;

    public Intersection(boolean hit, Vector3D point, double distance, Object3D object) {
        this.hit = hit;
        this.point = point;
        this.distance = distance;
        this.object = object;
        this.normal = null;
    }
    public Intersection(boolean hit, Vector3D point, double distance, Object3D object, Vector3D normal) {
        this.hit = hit;
        this.point = point;
        this.distance = distance;
        this.object = object;
        this.normal = normal;
    }
}
