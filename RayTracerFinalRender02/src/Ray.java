public class Ray {
    public Vector3D origin;
    public Vector3D direction;
    public Ray(Vector3D origin, Vector3D direction) {
        this.origin = origin;
        //keeps the ray direction as a unit vector
        this.direction = direction.normalize();
    }
    public Vector3D pointAtParameter(double t) {
        //returns a point along the ray at distance t
        return origin.add(direction.multiply(t));
    }
}
