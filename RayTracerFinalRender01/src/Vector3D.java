public class Vector3D {
    public double x;
    public double y;
    public double z;
    public Vector3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    public Vector3D add(Vector3D other) { //adds two vectors component-wise and returns the result as a new Vector3D object
        return new Vector3D(this.x + other.x, this.y + other.y, this.z + other.z);
    }
    public Vector3D subtract(Vector3D other) { //subtracts another vector from this vector component-wise and returns the result as a new Vector3D object
        return new Vector3D(this.x - other.x, this.y - other.y, this.z - other.z);
    }
    public Vector3D multiply(double scalar) { //multiplies this vector by a scalar value, scaling each component of the vector by the given scalar and returns the result as a new Vector3D object
        return new Vector3D(this.x * scalar, this.y * scalar, this.z * scalar);
    }
    public double dot(Vector3D other) { //calculates the dot product of this vector with another vector by multiplying the corresponding components of the two vectors and summing the results, returning a single scalar value that represents the dot product
        return this.x * other.x + this.y * other.y + this.z * other.z;
    }
    public double length() { //calculates the length  of the vector by taking the square root of the sum of the squares of its components, which is equivalent to the Euclidean distance from the origin to the point represented by the vector in 3D space
        return Math.sqrt(this.dot(this));
    }
    public Vector3D normalize() { //avoids division by zero 
        double len = this.length();
        if (len == 0) return new Vector3D(0, 0, 0);
        //converts the vector into a unit vector 
        return new Vector3D(this.x / len, this.y / len, this.z / len);
    }
    //cross product, used to get a perpendicular vector
    public Vector3D cross(Vector3D other) {
        return new Vector3D(
            this.y * other.z - this.z * other.y,
            this.z * other.x - this.x * other.z,
            this.x * other.y - this.y * other.x
        );
    }
}
