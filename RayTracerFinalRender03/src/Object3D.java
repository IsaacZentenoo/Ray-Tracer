import java.awt.Color;

public abstract class Object3D {
    protected Color color;
    protected double specularStrength;
    protected double shininess;
    protected double reflectivity;   
    protected double transparency;  
    protected double refractiveIndex; 

    public Object3D(Color color) {
        //default material properties for objects
        this.color = color;
        this.specularStrength = 0.8;
        this.shininess = 64;
        this.reflectivity = 0.0;
        this.transparency = 0.0;
        this.refractiveIndex = 1.5;
    }
    public Color   getColor(){ return color; }
    public double  getSpecularStrength() { return specularStrength; }
    public double  getShininess(){return shininess; }
    public double  getReflectivity(){ return reflectivity; }
    public double  getTransparency(){ return transparency; }
    public double  getRefractiveIndex(){ return refractiveIndex; }

    public void setMaterial(double specularStrength, double shininess) {
        //Sets the basic specular material properties
        this.specularStrength = specularStrength;
        this.shininess        = shininess;
    }
    public void setFullMaterial(double specularStrength, double shininess,double reflectivity, double transparency,double refractiveIndex) {
        //Sets all material properties for the object
        this.specularStrength = specularStrength;
        this.shininess = shininess;
        this.reflectivity = reflectivity;
        this.transparency  = transparency;
        this.refractiveIndex  = refractiveIndex;
    }
    public void setReflectivity(double r) { this.reflectivity    = r; }
    public void setTransparency(double t)  { this.transparency    = t; }
    public void setRefractiveIndex(double ri) { this.refractiveIndex = ri; }
    //abstract method that all 3D objects must implement to calculate ray intersections
    public abstract Intersection intersect(Ray ray, double near, double far);
}