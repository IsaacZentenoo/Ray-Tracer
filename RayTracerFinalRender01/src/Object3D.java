import java.awt.Color;

public abstract class Object3D {
    protected Color color;
    protected double specularStrength;
    protected double shininess;
    protected double reflectivity;   
    protected double transparency;   
    protected double refractiveIndex;
    //base class for all 3D objects, defines common material properties and an abstract method for ray intersection calculations
    public Object3D(Color color) {
        this.color = color;
        this.specularStrength = 0.8;
        this.shininess = 64;
        this.reflectivity = 0.0;
        this.transparency = 0.0;
        this.refractiveIndex  = 1.5;
    }
    //getter and setter methods for material properties
    public Color getColor(){ return color; }
    public double getSpecularStrength() { return specularStrength; }
    public double getShininess(){ return shininess; }
    public double getReflectivity(){ return reflectivity; }
    public double  getTransparency(){ return transparency; }
    public double  getRefractiveIndex(){ return refractiveIndex; }
    //sets the basic specular material properties for the object
    public void setMaterial(double specularStrength, double shininess) {
        this.specularStrength = specularStrength;
        this.shininess        = shininess;
    }
    //sets all material properties for the object, including reflectivity, transparency, and refractive index
    public void setFullMaterial(double specularStrength, double shininess,double reflectivity, double transparency,double refractiveIndex) {
        this.specularStrength = specularStrength;
        this.shininess        = shininess;
        this.reflectivity     = reflectivity;
        this.transparency     = transparency;
        this.refractiveIndex  = refractiveIndex;
    }
    //individual setters for reflectivity, transparency, and refractive index to allow for more flexible material adjustments
    public void setReflectivity(double r)     { this.reflectivity    = r; }
    public void setTransparency(double t)     { this.transparency    = t; }
    public void setRefractiveIndex(double ri) { this.refractiveIndex = ri; }
    //abstract method that all 3D objects must implement to calculate ray intersections, taking a ray and near/far distance bounds as parameters
    public abstract Intersection intersect(Ray ray, double near, double far);
}