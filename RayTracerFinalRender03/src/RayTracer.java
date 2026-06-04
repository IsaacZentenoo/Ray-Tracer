import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class RayTracer {
    static final int AA_SAMPLES = 6;
    static final int MAX_DEPTH  = 6;

    public static List<Triangle> rotateY(List<Triangle> tris, double deg) {
        double c = Math.cos(Math.toRadians(deg)), s = Math.sin(Math.toRadians(deg));
        List<Triangle> out = new ArrayList<>();
        for (Triangle t : tris) {
            Vector3D[] v = t.getVertices(), r = new Vector3D[3];
            for (int i = 0; i < 3; i++)
                r[i] = new Vector3D(v[i].x*c + v[i].z*s, v[i].y, -v[i].x*s + v[i].z*c);
            Triangle nt = new Triangle(r[0], r[1], r[2], t.getColor());
            nt.setFullMaterial(t.getReflectivity(), t.getShininess(),
                t.getSpecularStrength(), t.getTransparency(), t.getRefractiveIndex());
            nt.setUVs(t.uv0, t.uv1, t.uv2, t.material);
            out.add(nt);
        }
        return out;
    }
    public static List<Triangle> rotateX(List<Triangle> tris, double deg) {
        double c = Math.cos(Math.toRadians(deg)), s = Math.sin(Math.toRadians(deg));
        List<Triangle> out = new ArrayList<>();
        for (Triangle t : tris) {
            Vector3D[] v = t.getVertices(), r = new Vector3D[3];
            for (int i = 0; i < 3; i++)
                r[i] = new Vector3D(v[i].x, v[i].y*c - v[i].z*s, v[i].y*s + v[i].z*c);
            Triangle nt = new Triangle(r[0], r[1], r[2], t.getColor());
            nt.setFullMaterial(t.getReflectivity(), t.getShininess(),
                t.getSpecularStrength(), t.getTransparency(), t.getRefractiveIndex());
            nt.setUVs(t.uv0, t.uv1, t.uv2, t.material);
            out.add(nt);
        }
        return out;
    }
    public static List<Triangle> rotateZ(List<Triangle> tris, double deg) {
        double c = Math.cos(Math.toRadians(deg)), s = Math.sin(Math.toRadians(deg));
        List<Triangle> out = new ArrayList<>();
        for (Triangle t : tris) {
            Vector3D[] v = t.getVertices(), r = new Vector3D[3];
            for (int i = 0; i < 3; i++)
                r[i] = new Vector3D(v[i].x*c - v[i].y*s, v[i].x*s + v[i].y*c, v[i].z);
            Triangle nt = new Triangle(r[0], r[1], r[2], t.getColor());
            nt.setFullMaterial(t.getReflectivity(), t.getShininess(),
                t.getSpecularStrength(), t.getTransparency(), t.getRefractiveIndex());
            nt.setUVs(t.uv0, t.uv1, t.uv2, t.material);
            out.add(nt);
        }
        return out;
    }
    public static List<Triangle> scale(List<Triangle> tris, double sx, double sy, double sz) {
        List<Triangle> out = new ArrayList<>();
        for (Triangle t : tris) {
            Vector3D[] v = t.getVertices(), r = new Vector3D[3];
            for (int i = 0; i < 3; i++)
                r[i] = new Vector3D(v[i].x*sx, v[i].y*sy, v[i].z*sz);
            Triangle nt = new Triangle(r[0], r[1], r[2], t.getColor());
            nt.setFullMaterial(t.getReflectivity(), t.getShininess(),
                t.getSpecularStrength(), t.getTransparency(), t.getRefractiveIndex());
            nt.setUVs(t.uv0, t.uv1, t.uv2, t.material);
            out.add(nt);
        }
        return out;
    }

    public static void main(String[] args) throws Exception {
        int width  = 4096;
        int height = 2160;

        Camera camera = new Camera(
            new Vector3D(0, 1.5, 3.0),
            4.5, 2.5, 2.5, 0.1, 200);
        Scene scene = new Scene(camera);

        final double floorY = 0.0;

        Color asphalt = new Color(18, 18, 22);
        Triangle f1 = new Triangle(
            new Vector3D(-30,floorY,-5), new Vector3D(30,floorY,-5),
            new Vector3D(30,floorY,50), asphalt);
        Triangle f2 = new Triangle(
            new Vector3D(-30,floorY,-5), new Vector3D(30,floorY,50),
            new Vector3D(-30,floorY,50), asphalt);
        f1.setFullMaterial(0.6, 256, 0.7, 0.0, 1.0);
        f2.setFullMaterial(0.6, 256, 0.7, 0.0, 1.0);
        scene.addObject(f1);
        scene.addObject(f2);

        double waterY = floorY + 0.02;
        Color waterCol = new Color(30, 50, 80);

        Triangle w1 = new Triangle(
            new Vector3D(-3.5, waterY, 3.5),
            new Vector3D( 3.5, waterY, 3.5),
            new Vector3D( 3.5, waterY, 9.0), waterCol);
        Triangle w2 = new Triangle(
            new Vector3D(-3.5, waterY, 3.5),
            new Vector3D( 3.5, waterY, 9.0),
            new Vector3D(-3.5, waterY, 9.0), waterCol);
        w1.setFullMaterial(0.4, 512, 0.8, 0.7, 1.33);
        w2.setFullMaterial(0.4, 512, 0.8, 0.7, 1.33);
        scene.addObject(w1);
        scene.addObject(w2);

        List<Triangle> city = Objreader.loadOBJ(
            "./models/city.obj", null, new Vector3D(0,0,0), 1.0);
        city = scale(city, 10.0, 10.0, 10.0);
        city = centerAndPlace(city, 0, floorY, 18.0);
        for (Triangle t : city) scene.addObject(t);

        List<Triangle> spider = Objreader.loadOBJ(
            "./models/spiderman.obj", null, new Vector3D(0,0,0), 1.0);
        double ss = 1.8 / 1.88;
        spider = scale(spider, ss, ss, ss);
        spider = rotateY(spider, 180);
        spider = centerAndPlace(spider, 0, floorY, 6.0);
        for (Triangle t : spider) scene.addObject(t);

        scene.setBackgroundColor(new Color(5, 8, 20));
        scene.setAmbient(0.20);

        scene.addLight(new PointLight(
            new Vector3D(5, 20.0, 10.0),
            new Color(180, 200, 255), 20.0));
        scene.addLight(new PointLight(
            new Vector3D(0, 5.0, 15.0),
            new Color(255, 180, 80), 8.0));
        scene.addLight(new SpotLight(
            new Vector3D(0, 8.0, 2.0),
            new Vector3D(0, floorY+0.9, 6.0),
            new Color(255, 240, 200), 30.0, 35.0));
        scene.addLight(new PointLight(
            new Vector3D(0, 3.0, 1.0),
            new Color(200, 210, 255), 5.0));

        BVH bvh = BVH.build(scene.objects);
        int[] pixels = new int[width * height];
        System.out.println("Rendering " + width + "x" + height + "...");
        long t0 = System.currentTimeMillis();

        IntStream.range(0, height).parallel().forEach(y -> {
            java.util.Random rng = new java.util.Random();
            for (int x = 0; x < width; x++) {
                double tR=0, tG=0, tB=0;
                for (int s = 0; s < AA_SAMPLES; s++) {
                    Ray ray = scene.camera.getRayAA(
                        x + rng.nextDouble()-0.5,
                        y + rng.nextDouble()-0.5,
                        width, height);
                    Color c = trace(ray, scene, bvh, 0);
                    tR+=c.getRed(); tG+=c.getGreen(); tB+=c.getBlue();
                }
                pixels[y*width+x] = new Color(
                    clamp(tR/AA_SAMPLES),
                    clamp(tG/AA_SAMPLES),
                    clamp(tB/AA_SAMPLES)).getRGB();
            }
        });

        System.out.println("Done in " + (System.currentTimeMillis()-t0)/1000.0 + "s");
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < pixels.length; i++) img.setRGB(i%width, i/width, pixels[i]);
        ImageIO.write(img, "png", new File("./output/render.png"));
        System.out.println("Saved: output/render.png");
    }

    public static Color trace(Ray ray, Scene scene, BVH bvh, int depth) {
        if (depth > MAX_DEPTH) return Color.BLACK;
        Intersection hit = bvh.intersect(ray, scene.camera.near, scene.camera.far);
        if (!hit.hit) return scene.backgroundColor;
        return shade(hit, ray, scene, bvh, depth);
    }

    public static Color shade(Intersection hit, Ray ray, Scene scene, BVH bvh, int depth) {
        Vector3D normal = (hit.normal!=null)?hit.normal.normalize():new Vector3D(0,0,1);
        if (normal.dot(ray.direction) > 0) normal = normal.multiply(-1);
        double bw = 1.0 - hit.baryU - hit.baryV;
        Color col = (hit.object instanceof Triangle)
            ? ((Triangle) hit.object).sampleAt(bw, hit.baryU, hit.baryV)
            : hit.object.getColor();
        double refl = hit.object.getReflectivity();
        double trans = hit.object.getTransparency();
        double ior = hit.object.getRefractiveIndex();
        double specStr = hit.object.getSpecularStrength();
        double shin = hit.object.getShininess();

        double r = col.getRed()  *scene.ambient;
        double g = col.getGreen()*scene.ambient;
        double b = col.getBlue() *scene.ambient;
        Vector3D vd = ray.origin.subtract(hit.point).normalize();

        for (Light light : scene.lights) {
            if (isInShadow(hit, light, bvh)) continue;
            Vector3D ld = light.getDirectionFrom(hit.point);
            double dot = Math.max(0, normal.dot(ld));
            double fo = light.getFalloff(hit.point);
            if (fo <= 0) continue;
            double lr = light.color.getRed()  /255.0;
            double lg = light.color.getGreen()/255.0;
            double lb = light.color.getBlue() /255.0;
            r += col.getRed()  *lr*fo*dot;
            g += col.getGreen()*lg*fo*dot;
            b += col.getBlue() *lb*fo*dot;
            double sp = Math.pow(Math.max(0,
                normal.dot(ld.add(vd).normalize())), shin)*specStr*fo;
            r += 255*lr*sp; g += 255*lg*sp; b += 255*lb*sp;
        }

        double lr = clampD(r), lg = clampD(g), lb = clampD(b);
        double fr = lr, fg = lg, fb = lb;

        if (refl>0 && depth<MAX_DEPTH) {
            Vector3D ro = hit.point.add(normal.multiply(1e-3));
            Color rc = trace(new Ray(ro, reflect(ray.direction, normal)), scene, bvh, depth+1);
            fr = lr*(1-refl)+rc.getRed()  *refl;
            fg = lg*(1-refl)+rc.getGreen()*refl;
            fb = lb*(1-refl)+rc.getBlue() *refl;
        }
        if (trans>0 && depth<MAX_DEPTH) {
            boolean en = ray.direction.dot(normal)<0;
            Vector3D n  = en?normal:normal.multiply(-1);
            Vector3D rd = refract(ray.direction, n, en?1.0:ior, en?ior:1.0);
            if (rd!=null) {
                Color rc = trace(new Ray(hit.point.add(n.multiply(-1e-4)), rd), scene, bvh, depth+1);
                double fr2 = schlick(Math.abs(ray.direction.dot(n)), en?1.0:ior, en?ior:1.0);
                double op  = 1-trans;
                fr = fr*(op+trans*fr2)+rc.getRed()  *trans*(1-fr2);
                fg = fg*(op+trans*fr2)+rc.getGreen()*trans*(1-fr2);
                fb = fb*(op+trans*fr2)+rc.getBlue() *trans*(1-fr2);
            }
        }
        return new Color(clamp(fr), clamp(fg), clamp(fb));
    }

    private static Vector3D reflect(Vector3D d, Vector3D n) {
        return d.subtract(n.multiply(2*d.dot(n)));
    }
    private static Vector3D refract(Vector3D d, Vector3D n, double n1, double n2) {
        double r = n1/n2, ci = -d.dot(n), s2 = r*r*(1-ci*ci);
        return s2>1 ? null : d.multiply(r).add(n.multiply(r*ci-Math.sqrt(1-s2)));
    }
    private static double schlick(double c, double n1, double n2) {
        double r = (n1-n2)/(n1+n2); r*=r;
        return r+(1-r)*Math.pow(1-c,5);
    }
    public static boolean isInShadow(Intersection hit, Light light, BVH bvh) {
        Vector3D n = (hit.normal!=null)?hit.normal.normalize():new Vector3D(0,0,1);
        Ray sr = new Ray(hit.point.add(n.multiply(0.1)), light.getDirectionFrom(hit.point));
        double md = light.getDistanceFrom(hit.point);
        Intersection sh = bvh.intersect(sr, 0.1, md);
        return sh.hit && sh.object!=hit.object && sh.object.getTransparency()<=0.5;
    }
    public static List<Triangle> centerAndPlace(List<Triangle> tris, double tx, double ty, double tz) {
        double minX=Double.MAX_VALUE, minY=Double.MAX_VALUE, minZ=Double.MAX_VALUE;
        double maxX=-Double.MAX_VALUE, maxY=-Double.MAX_VALUE, maxZ=-Double.MAX_VALUE;
        for (Triangle t : tris)
            for (Vector3D v : t.getVertices()) {
                minX=Math.min(minX,v.x); maxX=Math.max(maxX,v.x);
                minY=Math.min(minY,v.y); maxY=Math.max(maxY,v.y);
                minZ=Math.min(minZ,v.z); maxZ=Math.max(maxZ,v.z);
            }
        double cx=(minX+maxX)/2, cy=minY, cz=(minZ+maxZ)/2;
        double dx=tx-cx, dy=ty-cy, dz=tz-cz;
        List<Triangle> out = new ArrayList<>();
        for (Triangle t : tris) {
            Vector3D[] v=t.getVertices(), r=new Vector3D[3];
            for (int i=0;i<3;i++) r[i]=new Vector3D(v[i].x+dx, v[i].y+dy, v[i].z+dz);
            Triangle nt = new Triangle(r[0],r[1],r[2],t.getColor());
            nt.setFullMaterial(t.getReflectivity(), t.getShininess(),
                t.getSpecularStrength(), t.getTransparency(), t.getRefractiveIndex());
            nt.setUVs(t.uv0, t.uv1, t.uv2, t.material);
            out.add(nt);
        }
        return out;
    }
    public static int    clamp(double v){return(int)Math.max(0,Math.min(255,v));}
    public static double clampD(double v){return Math.max(0,Math.min(255,v));}
}