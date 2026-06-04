public class Camera {
    public Vector3D position;
    public double viewportWidth;
    public double viewportHeight;
    public double projectionplanez;
    public double near;
    public double far;

    public Camera(Vector3D position, double viewportWidth, double viewportHeight, double projectionplanez, double near, double far) {
        this.position = position;
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        this.projectionplanez = projectionplanez;
        this.near= near;
        this.far = far;
    }
    //method that converts pixel coordinates to a ray in world space, it calculates the direction of the ray based on the position of the pixel on the viewport and the projection plane
    public Ray getRay(int pixelx, int pixely, int imageWidth, int imageHeight) {
        double x = (pixelx+0.5) * (viewportWidth / imageWidth) - viewportWidth / 2.0;
        double y = (viewportHeight / 2.0) - (pixely+0.5) * (viewportHeight / imageHeight);
        //the z coordinate of the ray direction is set to the projection plane distance, which determines how far the ray will travel before it intersects with objects in the scene
        double z = projectionplanez;
        //creates the ray direction from the camera to the viewport point
        Vector3D rayDirection = new Vector3D(x, y, z);
        return new Ray(position, rayDirection);
    }
    public Ray getRayAA(double pixelx, double pixely, int imageWidth, int imageHeight) {
        //same ray calculation, but with decimal pixel positions for anti-aliasing
        double x = (pixelx + 0.5) * (viewportWidth / imageWidth) - viewportWidth / 2.0;
        double y = (viewportHeight / 2.0) - (pixely + 0.5) * (viewportHeight / imageHeight);
        return new Ray(position, new Vector3D(x, y, projectionplanez));
    }
}

