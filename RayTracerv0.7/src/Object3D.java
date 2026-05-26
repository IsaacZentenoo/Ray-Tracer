import java.awt.Color;

public abstract class Object3D {
    protected Color color;
    protected double specularStrength;
    protected double shininess;
    public Object3D(Color color) {
        this.color = color;
        this.specularStrength = 0.8;
        this.shininess = 64;
    }
    public Color getColor() {
        return color;
    }
    public double getSpecularStrength() {
        return specularStrength;
    }
    public double getShininess() {
        return shininess;
    }
    public void setMaterial(double specularStrength, double shininess) {
        this.specularStrength = specularStrength;
        this.shininess = shininess;
    }
    public abstract Intersection intersect(Ray ray, double near, double far);
}
