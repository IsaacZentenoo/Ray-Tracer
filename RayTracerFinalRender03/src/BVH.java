import java.awt.Color;
import java.util.List;
import java.util.ArrayList;

public class BVH extends Object3D {

    private Vector3D boxMin; 
    private Vector3D boxMax;  
    private BVH left;
    private BVH right;
    private Object3D leaf;  

    private BVH() {
        super(Color.BLACK);
    }

    public static BVH build(List<Object3D> objects) {
        return buildNode(objects, 0);
    }

    private static BVH buildNode(List<Object3D> objects, int depth) {
        BVH node = new BVH();

        //leaf node which stores only one object
        if (objects.size() == 1) {
            node.leaf = objects.get(0);
            node.boxMin = getBoundsMin(objects.get(0));
            node.boxMax = getBoundsMax(objects.get(0));
            return node;
        }

        //if there are two objects it split them directly into two leaves
        if (objects.size() == 2) {
            node.left  = buildNode(List.of(objects.get(0)), depth + 1);
            node.right = buildNode(List.of(objects.get(1)), depth + 1);
            node.boxMin = minVec(node.left.boxMin, node.right.boxMin);
            node.boxMax = maxVec(node.left.boxMax, node.right.boxMax);
            return node;
        }

        Vector3D centroidMin = new Vector3D(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        Vector3D centroidMax = new Vector3D(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);

        //finds the bounding box of the centroids of the objects to determine the best axis to split
        for (Object3D obj : objects) {
            Vector3D c = getCenter(obj);
            centroidMin = minVec(centroidMin, c);
            centroidMax = maxVec(centroidMax, c);
        }

        double extX = centroidMax.x - centroidMin.x;
        double extY = centroidMax.y - centroidMin.y;
        double extZ = centroidMax.z - centroidMin.z;

        //it choose the axis with the largest extent to split the objects
        int axis = 0;
        if (extY > extX && extY > extZ) axis = 1;
        else if (extZ > extX && extZ > extY) axis = 2;

        final int splitAxis = axis;
        List<Object3D> sorted = new ArrayList<>(objects);

        //sort the objects based on the centroid along the chosen axis
        sorted.sort((a, b) -> {
            double ca = getCentroidOnAxis(a, splitAxis);
            double cb = getCentroidOnAxis(b, splitAxis);
            return Double.compare(ca, cb);
        });

        //split the sorted list into two halves and recursively build the left and right child nodes
        int mid = sorted.size() / 2;
        List<Object3D> leftList  = sorted.subList(0, mid);
        List<Object3D> rightList = sorted.subList(mid, sorted.size());

        node.left  = buildNode(new ArrayList<>(leftList),  depth + 1);
        node.right = buildNode(new ArrayList<>(rightList), depth + 1);

        //the node box is the union of the left and right child boxes
        node.boxMin = minVec(node.left.boxMin, node.right.boxMin);
        node.boxMax = maxVec(node.left.boxMax, node.right.boxMax);

        return node;
    }

    @Override
    public Intersection intersect(Ray ray, double near, double far) {
        //if the ray doesnt intersect the node's bounding box, return no hit
        if (!intersectsBox(ray, boxMin, boxMax, near, far)) {
            return new Intersection(false, null, -1, null);
        }

        //if this is a leaf node, intersect the ray with the stored object
        if (leaf != null) {
            return leaf.intersect(ray, near, far);
        }

        Intersection leftHit  = left.intersect(ray, near, far);
        Intersection rightHit = right.intersect(ray, near, far);

        //return the closest valid intersection
        if (leftHit.hit && rightHit.hit) {
            return leftHit.distance <= rightHit.distance ? leftHit : rightHit;
        }
        if (leftHit.hit)  return leftHit;
        if (rightHit.hit) return rightHit;

        return new Intersection(false, null, -1, null);
    }

    private static boolean intersectsBox(Ray ray, Vector3D bmin, Vector3D bmax, double near, double far) {
        double tmin = near;
        double tmax = far;

        // it performs the slab method for ray-box intersection, testing each axis separately
        double invDx = 1.0 / ray.direction.x;
        double tx0 = (bmin.x - ray.origin.x) * invDx;
        double tx1 = (bmax.x - ray.origin.x) * invDx;
        if (invDx < 0) { double tmp = tx0; tx0 = tx1; tx1 = tmp; }

        tmin = Math.max(tmin, tx0);
        tmax = Math.min(tmax, tx1);
        if (tmax < tmin) return false;

        //it repeats the same process for the Y and Z axes, updating the tmin and tmax values accordingly
        double invDy = 1.0 / ray.direction.y;
        double ty0 = (bmin.y - ray.origin.y) * invDy;
        double ty1 = (bmax.y - ray.origin.y) * invDy;
        if (invDy < 0) { double tmp = ty0; ty0 = ty1; ty1 = tmp; }
        tmin = Math.max(tmin, ty0);
        tmax = Math.min(tmax, ty1);
        if (tmax < tmin) return false;
        double invDz = 1.0 / ray.direction.z;
        double tz0 = (bmin.z - ray.origin.z) * invDz;
        double tz1 = (bmax.z - ray.origin.z) * invDz;
        if (invDz < 0) { double tmp = tz0; tz0 = tz1; tz1 = tmp; }

        tmin = Math.max(tmin, tz0);
        tmax = Math.min(tmax, tz1);
        if (tmax < tmin) return false;

        return true;
    }
    private static Vector3D getBoundsMin(Object3D obj) {
        //bounding box minimum for triangles, it finds the minimum coordinate among the three vertices of the triangle and adds a small epsilon to avoid precision issues
        if (obj instanceof Triangle) {
            Triangle t = (Triangle) obj;
            return new Vector3D(
                Math.min(t.v0.x, Math.min(t.v1.x, t.v2.x)) - 1e-4,
                Math.min(t.v0.y, Math.min(t.v1.y, t.v2.y)) - 1e-4,
                Math.min(t.v0.z, Math.min(t.v1.z, t.v2.z)) - 1e-4
            );
        }
        //if the object is a sphere, it calculates the minimum coordinate by subtracting the radius from the center of the sphere
        if (obj instanceof Sphere) {
            Sphere s = (Sphere) obj;
            return new Vector3D(
                s.center.x - s.radius,
                s.center.y - s.radius,
                s.center.z - s.radius
            );
        }

        return new Vector3D(-1e9, -1e9, -1e9);
    }

    private static Vector3D getBoundsMax(Object3D obj) {
        //bounding box maximum for triangles, it finds the maximum coordinate among the three vertices of the triangle and adds a small epsilon to avoid precision issues
        if (obj instanceof Triangle) {
            Triangle t = (Triangle) obj;
            return new Vector3D(
                Math.max(t.v0.x, Math.max(t.v1.x, t.v2.x)) + 1e-4,
                Math.max(t.v0.y, Math.max(t.v1.y, t.v2.y)) + 1e-4,
                Math.max(t.v0.z, Math.max(t.v1.z, t.v2.z)) + 1e-4
            );
        }
        //bounding box maximum for spheres, it calculates the maximum coordinate by adding the radius to the center of the sphere
        if (obj instanceof Sphere) {
            Sphere s = (Sphere) obj;
            return new Vector3D(
                s.center.x + s.radius,
                s.center.y + s.radius,
                s.center.z + s.radius
            );
        }

        return new Vector3D(1e9, 1e9, 1e9);
    }

    private static Vector3D getCenter(Object3D obj) {
        Vector3D mn = getBoundsMin(obj);
        Vector3D mx = getBoundsMax(obj);

        //it centers the object by calculating the midpoint between the minimum and maximum coordinates of its bounding box, which gives an approximation of the object's center in space
        return new Vector3D(
            (mn.x + mx.x) / 2.0,
            (mn.y + mx.y) / 2.0,
            (mn.z + mx.z) / 2.0
        );
    }

    private static double getCentroidOnAxis(Object3D obj, int axis) {
        Vector3D c = getCenter(obj);

        //returns the center coordinate depending on the chosen axis
        if (axis == 0) return c.x;
        if (axis == 1) return c.y;
        return c.z;
    }
    private static Vector3D minVec(Vector3D a, Vector3D b) {
        return new Vector3D(
            Math.min(a.x, b.x),
            Math.min(a.y, b.y),
            Math.min(a.z, b.z)
        );
    }
    private static Vector3D maxVec(Vector3D a, Vector3D b) {
        return new Vector3D(
            Math.max(a.x, b.x),
            Math.max(a.y, b.y),
            Math.max(a.z, b.z)
        );
    }
    @Override
    public Color getColor() {
        return leaf != null ? leaf.getColor() : Color.BLACK;
    }
}