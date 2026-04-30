import java.awt.Color;

public abstract class Object3D {
    protected Color color;
    public Object3D(Color color) {
        this.color = color;
    }
    public Color getColor() {
        return color;
    }
    public abstract Intersection intersect(Ray ray, double near, double far);
}
