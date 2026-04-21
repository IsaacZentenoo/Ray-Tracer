public class Camera {
    public Vector3D position;
    public double viewportWidth;
    public double viewportHeight;
    public double projectionplanez;

    public Camera(Vector3D position, double viewportWidth, double viewportHeight, double projectionplanez) {
        this.position = position;
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        this.projectionplanez = projectionplanez;
    }
    public Ray getRay(int pixelx, int pixely, int imageWidth, int imageHeight) {
        double x = (pixelx+0.5) * (viewportWidth / imageWidth) - viewportWidth / 2.0;
        double y = (viewportHeight / 2.0) - (pixely+0.5) * (viewportHeight / imageHeight);
        double z = projectionplanez;
        Vector3D rayDirection = new Vector3D(x, y, z);
        return new Ray(position, rayDirection);
    }
}
