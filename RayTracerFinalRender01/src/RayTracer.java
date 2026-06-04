import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class RayTracer {
    static final int AA_SAMPLES = 6;
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

       Camera camera = new Camera(new Vector3D(0, 0.0, -3.0), 2.5, 2.5, 2.0, 0.1, 100);
       Scene  scene  = new Scene(camera);
       final double L  = 3.5;
       final double Z0 = 2.5;
       final double Z1 = 8.5;

       Color wall  = new Color(18, 22, 35);
       Color floor = new Color(15, 18, 28);

       addWall(scene, new Vector3D(-L,-L,Z0), new Vector3D( L,-L,Z1), new Vector3D( L,-L,Z0), floor, 0.15,32,0.3);
       addWall(scene, new Vector3D(-L,-L,Z0), new Vector3D(-L,-L,Z1), new Vector3D( L,-L,Z1), floor, 0.15,32,0.3);
       addWall(scene, new Vector3D(-L,L,Z0), new Vector3D(L,L,Z0), new Vector3D(L,L,Z1), wall, 0.02,8,0);
       addWall(scene, new Vector3D(-L,L,Z0), new Vector3D(L,L,Z1), new Vector3D(-L,L,Z1), wall, 0.02,8,0);
       addWall(scene, new Vector3D(-L,-L,Z1), new Vector3D(-L,L,Z1), new Vector3D( L,L,Z1), wall, 0.02,8,0);
       addWall(scene, new Vector3D(-L,-L,Z1), new Vector3D( L,L,Z1), new Vector3D( L,-L,Z1), wall, 0.02,8,0);
       addWall(scene, new Vector3D(-L,-L,Z0), new Vector3D(-L,L,Z1), new Vector3D(-L,-L,Z1), new Color(30,35,50), 0.02,8,0);
       addWall(scene, new Vector3D(-L,-L,Z0), new Vector3D(-L,L,Z0), new Vector3D(-L, L,Z1), new Color(30,35,50), 0.02,8,0);
       addWall(scene, new Vector3D(L,-L,Z0), new Vector3D(L,-L,Z1), new Vector3D(L, L,Z1), new Color(30,35,50), 0.02,8,0);
       addWall(scene, new Vector3D(L,-L,Z0), new Vector3D(L, L,Z1), new Vector3D(L, L,Z0), new Color(30,35,50), 0.02,8,0);

       List<Triangle> water = Objreader.loadOBJ("./models/water.obj",new Color(20, 35, 65), new Vector3D(0,0,0), 1.0);
       water = rotateX(water, -90);
       water = scale(water, 4.5, 1.0, 3.5);
       water = centerAndPlace(water, 0, -L + 0.02, (Z0+Z1)/2.0);
       for (Triangle t : water) {
           t.setFullMaterial(0.55, 256, 0.8, 0.25, 1.33);
           scene.addObject(t);
       }

       Color ped = new Color(180, 178, 175);
       double peH = 2.0, peW = 0.6;
       double peY = -L + peH;
       double cx1 = -1.8, pz1 = 4.5;
       double cx2 =  0.0, pz2 = 4.5;
       double cx3 =  1.8, pz3 = 4.5;

       addPedestal(scene, cx1, pz1, peW, -L, peY, ped);
       addPedestal(scene, cx2, pz2, peW, -L, peY, ped);
       addPedestal(scene, cx3, pz3, peW, -L, peY, ped);

       List<Triangle> shoes = Objreader.loadOBJ("./models/tenis.obj",
           Color.WHITE, new Vector3D(0,0,0), 0.45);
       shoes = rotateX(shoes, 0);
       shoes = rotateY(shoes, 0);
       shoes = centerAndPlace(shoes, cx1, peY, pz1);
       for (Triangle t : shoes) {
           Color c = t.getColor();
           int r = Math.max(c.getRed(),   80);
           int g = Math.max(c.getGreen(), 80);
           int b = Math.max(c.getBlue(),  85);
           Triangle bright = new Triangle(
               t.getVertices()[0], t.getVertices()[1], t.getVertices()[2], new Color(r, g, b));
           bright.setFullMaterial(0.3, 64, 0.3, 0.0, 1.0);
           scene.addObject(bright);
       }

       List<Triangle> goggles = Objreader.loadOBJ("./models/goggles.obj",
           new Color(220, 218, 215), new Vector3D(0,0,0), 0.45);
       goggles = rotateX(goggles, 90);
       goggles = rotateY(goggles, 0);
       goggles = centerAndPlace(goggles, cx2, peY, pz2);
       for (Triangle t : goggles) { t.setFullMaterial(0.4,128,0.6,0.0,1.0); scene.addObject(t); }

       List<Triangle> helmet = Objreader.loadOBJ("./models/helmet.obj",
           new Color(245, 243, 240), new Vector3D(0,0,0), 0.40);
       helmet = rotateY(helmet, 80);
       helmet = rotateX(helmet, 70);
       helmet = rotateZ(helmet, 100);
       helmet = centerAndPlace(helmet, cx3-0.4, peY, pz3-0.4);
       for (Triangle t : helmet) { t.setFullMaterial(0.3,128,0.5,0.0,1.0); scene.addObject(t); }

       List<Triangle> bicycle = Objreader.loadOBJ("./models/bicycle.obj",
           new Color(200, 198, 195), new Vector3D(0,0,0), 0.9);
       bicycle = rotateX(bicycle, 270);
       bicycle = rotateY(bicycle, -25);
       bicycle = centerAndPlace(bicycle, cx3, peY, pz3);
       for (Triangle t : bicycle) { t.setFullMaterial(0.5,128,0.7,0.0,1.0); scene.addObject(t); }

       // Cubo de vidrio — refraccion visible enfrente de los pedestales
       addGlassBox(scene,
           -0.4, -L,       3.2,
            0.4, -L+1.4,   3.8,
           new Color(210, 235, 255));

       scene.setBackgroundColor(new Color(5, 8, 15));
       scene.setAmbient(0.6);
       scene.addLight(new SpotLight(new Vector3D(cx1, L-0.1, pz1),new Vector3D(cx1, -L, pz1),new Color(255, 248, 230), 20.0, 200.0));
       scene.addLight(new SpotLight(new Vector3D(cx2, L-0.1, pz2),new Vector3D(cx2, -L, pz2),new Color(255, 248, 230), 20.0, 200.0));
       scene.addLight(new SpotLight(new Vector3D(cx3, L-0.1, pz3),new Vector3D(cx3, -L, pz3),new Color(255, 248, 230), 20.0, 200.0));
       scene.addLight(new PointLight(new Vector3D(0, peY+0.8, Z0+0.3),new Color(180, 190, 210), 2.5));
       scene.addLight(new PointLight(new Vector3D(0, 2.5, (Z0+Z1)/2.0),new Color(40, 50, 80), 5.0));

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
       long t1 = System.currentTimeMillis();
       System.out.println("Done in " + ((t1-t0)/1000.0) + " seconds.");
       ImageIO.write(img, "png", new File("./output/render.png"));
   }

   private static void addGlassBox(Scene sc,
                                    double x0, double y0, double z0,
                                    double x1, double y1, double z1,
                                    Color c) {
       sc.addObject(makeGlassTri(x0,y1,z0, x1,y1,z0, x1,y1,z1, c));
       sc.addObject(makeGlassTri(x0,y1,z0, x1,y1,z1, x0,y1,z1, c));
       sc.addObject(makeGlassTri(x0,y0,z0, x1,y0,z1, x1,y0,z0, c));
       sc.addObject(makeGlassTri(x0,y0,z0, x0,y0,z1, x1,y0,z1, c));
       sc.addObject(makeGlassTri(x0,y0,z0, x1,y1,z0, x0,y1,z0, c));
       sc.addObject(makeGlassTri(x0,y0,z0, x1,y0,z0, x1,y1,z0, c));
       sc.addObject(makeGlassTri(x0,y0,z1, x0,y1,z1, x1,y1,z1, c));
       sc.addObject(makeGlassTri(x0,y0,z1, x1,y1,z1, x1,y0,z1, c));
       sc.addObject(makeGlassTri(x0,y0,z0, x0,y1,z1, x0,y0,z1, c));
       sc.addObject(makeGlassTri(x0,y0,z0, x0,y1,z0, x0,y1,z1, c));
       sc.addObject(makeGlassTri(x1,y0,z0, x1,y0,z1, x1,y1,z1, c));
       sc.addObject(makeGlassTri(x1,y0,z0, x1,y1,z1, x1,y1,z0, c));
   }

   private static Triangle makeGlassTri(double x1,double y1,double z1,
                                         double x2,double y2,double z2,
                                         double x3,double y3,double z3,
                                         Color c) {
       Triangle t = new Triangle(
           new Vector3D(x1,y1,z1),
           new Vector3D(x2,y2,z2),
           new Vector3D(x3,y3,z3), c);
       t.setFullMaterial(0.05, 512, 0.95, 0.92, 1.52);
       return t;
   }

   private static void addWall(Scene sc, Vector3D a, Vector3D b, Vector3D c,
                                 Color col, double refl, double shin, double spec) {
       Triangle t = new Triangle(a, b, c, col);
       t.setFullMaterial(refl, shin, spec, 0.0, 1.0);
       sc.addObject(t);
   }

   private static void addPedestal(Scene sc, double cx, double pz, double pw,
                                    double floorY, double topY, Color c) {
       sc.addObject(makeTri(cx-pw,topY,pz-pw, cx+pw,topY,pz-pw, cx+pw,topY,pz+pw, c,0.05,64,0.5));
       sc.addObject(makeTri(cx-pw,topY,pz-pw, cx+pw,topY,pz+pw, cx-pw,topY,pz+pw, c,0.05,64,0.5));
       sc.addObject(makeTri(cx-pw,floorY,pz-pw, cx+pw,topY,pz-pw, cx-pw,topY,pz-pw, c,0.05,16,0.2));
       sc.addObject(makeTri(cx-pw,floorY,pz-pw, cx+pw,floorY,pz-pw, cx+pw,topY,pz-pw, c,0.05,16,0.2));
       sc.addObject(makeTri(cx-pw,floorY,pz+pw, cx-pw,topY,pz-pw, cx-pw,floorY,pz-pw, c,0.05,16,0.2));
       sc.addObject(makeTri(cx-pw,floorY,pz+pw, cx-pw,topY,pz+pw, cx-pw,topY,pz-pw, c,0.05,16,0.2));
       sc.addObject(makeTri(cx+pw,floorY,pz-pw, cx+pw,topY,pz+pw, cx+pw,floorY,pz+pw, c,0.05,16,0.2));
       sc.addObject(makeTri(cx+pw,floorY,pz-pw, cx+pw,topY,pz-pw, cx+pw,topY,pz+pw, c,0.05,16,0.2));
   }

   private static Triangle makeTri(double x1,double y1,double z1,
                                    double x2,double y2,double z2,
                                    double x3,double y3,double z3,
                                    Color c,double refl,double shin,double spec) {
       Triangle t = new Triangle(new Vector3D(x1,y1,z1),new Vector3D(x2,y2,z2),new Vector3D(x3,y3,z3),c);
       t.setFullMaterial(refl,shin,spec,0.0,1.0);
       return t;
   }

   public static Color trace(Ray ray, Scene scene, BVH bvh, int depth) {
       if (depth > MAX_DEPTH) return Color.BLACK;
       Intersection hit = bvh.intersect(ray, scene.camera.near, scene.camera.far);
       if (!hit.hit) return Color.BLACK;
       return shade(hit, ray, scene, bvh, depth);
   }

   public static Color shade(Intersection hit, Ray ray, Scene scene, BVH bvh, int depth) {
       Vector3D normal = (hit.normal!=null)?hit.normal.normalize():new Vector3D(0,0,1);
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
           double sp = Math.pow(Math.max(0, normal.dot(ld.add(vd).normalize())), shin)*specStr*fo;
           r += 255*lr*sp;
           g += 255*lg*sp;
           b += 255*lb*sp;
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
       Ray sr = new Ray(hit.point.add(n.multiply(0.15)),light.getDirectionFrom(hit.point));
       double md = light.getDistanceFrom(hit.point);
       Intersection sh = bvh.intersect(sr, 0.15, md);
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