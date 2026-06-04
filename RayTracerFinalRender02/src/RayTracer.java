import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class RayTracer {
    static final int AA_SAMPLES =6;
    static final int MAX_DEPTH  = 5;
    public static List<Triangle> rotateY(List<Triangle> tris, double deg) {
        double c = Math.cos(Math.toRadians(deg)), s = Math.sin(Math.toRadians(deg));
        List<Triangle> out = new ArrayList<>();
        for (Triangle t : tris) {
            Vector3D[] v = t.getVertices(), r = new Vector3D[3];
            for (int i = 0; i < 3; i++)
                r[i] = new Vector3D(v[i].x*c + v[i].z*s, v[i].y, -v[i].x*s + v[i].z*c);
            out.add(new Triangle(r[0], r[1], r[2], t.getColor()));
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
            out.add(new Triangle(r[0], r[1], r[2], t.getColor()));
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
            out.add(new Triangle(r[0], r[1], r[2], t.getColor()));
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
            nt.setFullMaterial(
                t.getReflectivity(), t.getShininess(),
                t.getSpecularStrength(), t.getTransparency(),
                t.getRefractiveIndex());
            out.add(nt);
        }
        return out;
    }
    public static void main(String[] args) throws Exception {
        int width  = 4096;
        int height = 2160;

        Camera camera = new Camera(new Vector3D(0, 2.0, -2.0), 5.0, 2.8, 2.5, 0.1, 100);
        Scene  scene  = new Scene(camera);

        final double floorY = -2.0;
        final double L =  6.0;
        final double Z0 =  1.5;
        final double Z1 = 10.0;
        final double ceilY =  5.0;

        Color bg = new Color(18, 22, 35);
        addBox(scene, -L-0.05, floorY, Z0, -L,ceilY, Z1, bg, 0.02, 8, 0);
        addBox(scene,  L,floorY, Z0,  L+0.05, ceilY, Z1, bg, 0.02, 8, 0);
        addBox(scene, -L, ceilY-0.05, Z0, L, ceilY, Z1, bg, 0.02, 8, 0);
        addBox(scene, -L, floorY, Z1-0.05,L, ceilY, Z1,bg, 0.05, 8, 0);
        Color floorCol = new Color(25, 28, 42);
        Triangle fp1 = new Triangle(new Vector3D(-L, floorY, Z0),new Vector3D( L, floorY, Z0),new Vector3D( L, floorY, Z1), floorCol);
        Triangle fp2 = new Triangle(new Vector3D(-L, floorY, Z0),new Vector3D( L, floorY, Z1),new Vector3D(-L, floorY, Z1), floorCol);
        fp1.setFullMaterial(0.85, 512, 0.9, 0.0, 1.0);
        fp2.setFullMaterial(0.85, 512, 0.9, 0.0, 1.0);
        scene.addObject(fp1);
        scene.addObject(fp2);
        
        List<Triangle> logo = Objreader.loadOBJ("./models/realmadrid.obj",new Color(212, 175, 55), new Vector3D(0,0,0), 1.5);
        logo = rotateY(logo, 180);
        logo = centerAndPlace(logo, 0, -0.2, Z1-0.12);
        for (Triangle t : logo) {
            Triangle golden = new Triangle(
                t.getVertices()[0], t.getVertices()[1], t.getVertices()[2],
                new Color(212, 175, 55));
            golden.setFullMaterial(0.7, 512, 0.8, 0.0, 1.0);
            scene.addObject(golden);
        }
        //parameters for trophy placement and pedestal dimensions, it calculates the spacing and positions for each trophy and their corresponding pedestals, and loads different trophy models based on their index to create a visually appealing display in the scene
        int nTrophies = 10;
        double spacing = 1.05;
        double totalW = spacing * (nTrophies - 1);
        double startX = -totalW / 2.0;
        double trophyZ = 6.5;
        double pedH = 0.9;
        double pedW = 0.30;
        Color  pedCol = new Color(20, 20, 25);

        for (int i = 0; i < nTrophies; i++) {
            double px = startX + i * spacing;

            addBox(scene,px-pedW, floorY,trophyZ-pedW,px+pedW, floorY+pedH, trophyZ+pedW,pedCol, 0.55, 256, 0.65);
            String path;
            double rotX = 0;
            //loads different trophy models based on the index
            if (i < 4) {
                path = "./models/champions.obj";
            } else if (i < 7) {
                path = "./models/copadelrey.obj";
                rotX = 180;
            } else {
                path = "./models/mundialdeclubes.obj";
            }
            loadTrophy(scene, path, px, floorY+pedH, trophyZ, rotX, 0, 0.65);
            scene.addLight(new SpotLight(
                new Vector3D(px, ceilY-0.3, trophyZ-1.0),
                new Vector3D(px, floorY+pedH+0.5, trophyZ),
                new Color(255, 250, 220), 18.0, 30.0));
        }
        scene.setBackgroundColor(new Color(5, 8, 15));
        scene.setAmbient(0.30);

        scene.addLight(new PointLight(
            new Vector3D(0, ceilY-0.8, trophyZ),
            new Color(255, 248, 220), 5.0));

        scene.addLight(new SpotLight(
            new Vector3D(0, ceilY-0.5, Z1-1.5),
            new Vector3D(0, -0.2, Z1-0.12),
            new Color(255, 240, 180), 15.0, 35.0));

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
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < pixels.length; i++) img.setRGB(i%width, i/width, pixels[i]);
        ImageIO.write(img, "png", new File("./output/render.png"));
        System.out.println("Guardado: output/render.png");
    }
    private static void loadTrophy(Scene sc, String path,double x, double y, double z, double rotXDeg, double rotYDeg, double sc2) {
        try {
            List<Triangle> trophy = Objreader.loadOBJ(path,Color.WHITE, new Vector3D(0,0,0), sc2);
            if (rotXDeg != 0) trophy = rotateX(trophy, rotXDeg);
            if (rotYDeg != 0) trophy = rotateY(trophy, rotYDeg);
            trophy = centerAndPlace(trophy, x, y, z);
            for (Triangle t : trophy) {
                t.setFullMaterial(0.75, 1024, 0.95, 0.0, 1.0);
                sc.addObject(t);
            }
        } catch (Exception e) {
        }
    }
    //helper method to create a box from two opposite corners, it generates 12 triangles to form the six faces of the box and assigns the specified color and material properties to each triangle, this method is used to create the walls and floor of the scene
    private static void addBox(Scene sc,double x0, double y0, double z0,double x1, double y1, double z1,Color c, double refl, double shin, double spec) {
        sc.addObject(makeTri(x0,y1,z0, x1,y1,z0, x1,y1,z1, c,refl,shin,spec));
        sc.addObject(makeTri(x0,y1,z0, x1,y1,z1, x0,y1,z1, c,refl,shin,spec));
        sc.addObject(makeTri(x0,y0,z0, x1,y0,z1, x1,y0,z0, c,refl,shin,spec));
        sc.addObject(makeTri(x0,y0,z0, x0,y0,z1, x1,y0,z1, c,refl,shin,spec));
        sc.addObject(makeTri(x0,y0,z0, x1,y1,z0, x0,y1,z0, c,refl,shin,spec));
        sc.addObject(makeTri(x0,y0,z0, x1,y0,z0, x1,y1,z0, c,refl,shin,spec));
        sc.addObject(makeTri(x0,y0,z1, x0,y1,z1, x1,y1,z1, c,refl,shin,spec));
        sc.addObject(makeTri(x0,y0,z1, x1,y1,z1, x1,y0,z1, c,refl,shin,spec));
        sc.addObject(makeTri(x0,y0,z0, x0,y1,z1, x0,y0,z1, c,refl,shin,spec));
        sc.addObject(makeTri(x0,y0,z0, x0,y1,z0, x0,y1,z1, c,refl,shin,spec));
        sc.addObject(makeTri(x1,y0,z0, x1,y0,z1, x1,y1,z1, c,refl,shin,spec));
        sc.addObject(makeTri(x1,y0,z0, x1,y1,z1, x1,y1,z0, c,refl,shin,spec));
    }

    private static Triangle makeTri(double x1,double y1,double z1,
                                     double x2,double y2,double z2,
                                     double x3,double y3,double z3,
                                     Color c,double refl,double shin,double spec) {
        Triangle t = new Triangle(
            new Vector3D(x1,y1,z1),
            new Vector3D(x2,y2,z2),
            new Vector3D(x3,y3,z3),c);
        t.setFullMaterial(refl,shin,spec,0.0,1.0);
        return t;
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
        Color    col    = hit.object.getColor();
        double refl     = hit.object.getReflectivity();
        double trans    = hit.object.getTransparency();
        double ior      = hit.object.getRefractiveIndex();
        double specStr  = hit.object.getSpecularStrength();
        double shin     = hit.object.getShininess();

        double r = col.getRed()  *scene.ambient;
        double g = col.getGreen()*scene.ambient;
        double b = col.getBlue() *scene.ambient;
        Vector3D vd = ray.origin.subtract(hit.point).normalize();

        for (Light light : scene.lights) {
            if (isInShadow(hit, light, bvh)) continue;
            Vector3D ld = light.getDirectionFrom(hit.point);
            double dot  = Math.max(0, normal.dot(ld));
            double fo   = light.getFalloff(hit.point);
            if (fo <= 0) continue;
            double lr   = light.color.getRed()  /255.0;
            double lg   = light.color.getGreen()/255.0;
            double lb   = light.color.getBlue() /255.0;
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
            Color rc = trace(new Ray(ro, reflect(ray.direction, normal)),
                             scene, bvh, depth+1);
            fr = lr*(1-refl)+rc.getRed()  *refl;
            fg = lg*(1-refl)+rc.getGreen()*refl;
            fb = lb*(1-refl)+rc.getBlue() *refl;
        }
        if (trans>0 && depth<MAX_DEPTH) {
            boolean en = ray.direction.dot(normal)<0;
            Vector3D n  = en?normal:normal.multiply(-1);
            Vector3D rd = refract(ray.direction, n, en?1.0:ior, en?ior:1.0);
            if (rd!=null) {
                Color rc = trace(
                    new Ray(hit.point.add(n.multiply(-1e-4)), rd),
                    scene, bvh, depth+1);
                double fr2 = schlick(Math.abs(ray.direction.dot(n)),
                                     en?1.0:ior, en?ior:1.0);
                double op  = 1-trans;
                fr = fr*(op+trans*fr2)+rc.getRed()  *trans*(1-fr2);
                fg = fg*(op+trans*fr2)+rc.getGreen()*trans*(1-fr2);
                fb = fb*(op+trans*fr2)+rc.getBlue() *trans*(1-fr2);
            }
        }
        return new Color(clamp(fr), clamp(fg), clamp(fb));
    }
    //calculates the reflection direction of a ray given the incident direction and the surface normal, it uses the formula R = D - 2(D·N)N, where D is the incident direction, N is the normal vector, and R is the reflected direction, this function is essential for implementing reflective surfaces in the ray tracer
    private static Vector3D reflect(Vector3D d, Vector3D n) {
        return d.subtract(n.multiply(2*d.dot(n)));
    }
    //calculates the refraction of a ray using Snell's law, it takes the incident direction, the surface normal, and the refractive indices of the two materials to compute the refracted ray direction, it also checks for total internal reflection and returns null if refraction is not possible
    private static Vector3D refract(Vector3D d, Vector3D n, double n1, double n2) {
        double r = n1/n2, ci = -d.dot(n), s2 = r*r*(1-ci*ci);
        return s2>1 ? null : d.multiply(r).add(n.multiply(r*ci-Math.sqrt(1-s2)));
    }
    //Schlicks approximation for reflectance, which gives a more accurate reflection coefficient based on the angle of incidence and the refractive indices of the materials, it calculates the base reflectance at normal incidence and then adjusts it based on the angle to provide a more realistic rendering of reflections, especially for transparent materials
    private static double schlick(double c, double n1, double n2) {
        double r = (n1-n2)/(n1+n2); r*=r;
        return r+(1-r)*Math.pow(1-c,5);
    }
    //checks if the hit point is in shadow by casting a ray towards the light and checking for intersections with other objects in the scene, it also accounts for a small bias to prevent self-shadowing and checks if the intersected object is not the same as the hit object and has low transparency to determine if it blocks the light
    public static boolean isInShadow(Intersection hit, Light light, BVH bvh) {
        Vector3D n = (hit.normal!=null)?hit.normal.normalize():new Vector3D(0,0,1);
        Ray sr     = new Ray(hit.point.add(n.multiply(0.1)),
                             light.getDirectionFrom(hit.point));
        double md  = light.getDistanceFrom(hit.point);
        Intersection sh = bvh.intersect(sr, 0.1, md);
        return sh.hit && sh.object!=hit.object && sh.object.getTransparency()<=0.5;
    }

    public static List<Triangle> centerAndPlace(List<Triangle> tris, double tx, double ty, double tz) {
        double minX=Double.MAX_VALUE,  minY=Double.MAX_VALUE,  minZ=Double.MAX_VALUE;
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
            nt.setFullMaterial(
                t.getReflectivity(), t.getShininess(),
                t.getSpecularStrength(), t.getTransparency(),
                t.getRefractiveIndex());
            out.add(nt);
        }
        return out;
    }
    public static int    clamp(double v){return(int)Math.max(0,Math.min(255,v));}
    public static double clampD(double v){return Math.max(0,Math.min(255,v));}
}