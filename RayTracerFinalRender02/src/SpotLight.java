import java.awt.Color;

public class SpotLight extends Light {
    public Vector3D position;
    public Vector3D direction;
    public double   cutoffAngle;

    public SpotLight(Vector3D position, Vector3D target,
                     Color color, double cutoffAngle, double intensity) {
        super(color, intensity);
        this.position    = position;
        this.direction   = target.subtract(position).normalize();
        this.cutoffAngle = cutoffAngle;
    }

    @Override
    public Vector3D getDirectionFrom(Vector3D point) {
        return position.subtract(point).normalize();
    }

    @Override
    public double getFalloff(Vector3D point) {
        // direccion desde el spot hacia el punto
        Vector3D spotToPoint = point.subtract(position).normalize();
        // angulo entre la direccion del spot y el vector al punto
        double cosAngle = spotToPoint.dot(direction);
        double cosCut   = Math.cos(Math.toRadians(cutoffAngle));

        // si el punto esta fuera del cono retorna 0
        if (cosAngle < cosCut) return 0.0;

        // falloff suave
        double t    = (cosAngle - cosCut) / (1.0 - cosCut);
        double smooth = t * t;

        double dist = getDistanceFrom(point);
        return smooth * intensity / (1.0 + 0.15*dist + 0.03*dist*dist);
    }

    @Override
    public double getDistanceFrom(Vector3D point) {
        return position.subtract(point).length();
    }
}