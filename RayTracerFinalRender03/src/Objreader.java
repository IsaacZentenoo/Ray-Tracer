import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Objreader {

    public static List<Triangle> loadOBJ(String filename, Color defaultColor,
                                          Vector3D position, double scaleFactor) {
        List<Vector3D> vertices = new ArrayList<>();
        List<double[]> uvCoords = new ArrayList<>();
        List<Triangle> triangles = new ArrayList<>();
        List<int[]> faces = new ArrayList<>();
        List<int[]> faceUVs = new ArrayList<>();
        List<Integer> faceSmoothGroups = new ArrayList<>();
        List<String> faceMaterials = new ArrayList<>();

        Map<String, MtlReader.Material> materials = new HashMap<>();
        String currentMaterial   = null;
        int currentSmoothGroup = 1;

        double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY, minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;

        try {
            Path   objPath = Paths.get(filename);
            String folder  = objPath.getParent() != null
                ? objPath.getParent().toString() + "/" : "./models/";
            BufferedReader reader = new BufferedReader(new FileReader(objPath.toFile()));
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.startsWith("mtllib ")) {
                    String mtlFile = line.substring(7).trim();
                    materials = MtlReader.load(folder + mtlFile);

                } else if (line.startsWith("usemtl ")) {
                    currentMaterial = line.substring(7).trim();

                } else if (line.startsWith("vt ")) {
                    String[] p = line.split("\\s+");
                    double u = Double.parseDouble(p[1]);
                    double v = p.length > 2 ? Double.parseDouble(p[2]) : 0.0;
                    uvCoords.add(new double[]{u, v});

                } else if (line.startsWith("v ")) {
                    String[] p = line.split("\\s+");
                    double x = Double.parseDouble(p[1]);
                    double y = Double.parseDouble(p[2]);
                    double z = Double.parseDouble(p[3]);
                    vertices.add(new Vector3D(x, y, z));
                    minX=Math.min(minX,x); minY=Math.min(minY,y); minZ=Math.min(minZ,z);
                    maxX=Math.max(maxX,x); maxY=Math.max(maxY,y); maxZ=Math.max(maxZ,z);

                } else if (line.startsWith("s ")) {
                    String val = line.substring(2).trim();
                    if (val.equalsIgnoreCase("off") || val.equals("0")) currentSmoothGroup = 0;
                    else { try { currentSmoothGroup = Integer.parseInt(val); }
                           catch (NumberFormatException e) { currentSmoothGroup = 1; } }

                } else if (line.startsWith("f ")) {
                    String[] parts = line.split("\\s+");
                    int[]vIdx  = new int[parts.length - 1];
                    int[]uvIdx = new int[parts.length - 1];
                    for (int i = 1; i < parts.length; i++) {
                        String[] tok = parts[i].split("/");
                        vIdx[i-1] = Integer.parseInt(tok[0]) - 1;
                        if (tok.length > 1 && !tok[1].isEmpty())
                            uvIdx[i-1] = Integer.parseInt(tok[1]) - 1;
                        else
                            uvIdx[i-1] = -1;
                    }
                    faces.add(vIdx);
                    faceUVs.add(uvIdx);
                    faceSmoothGroups.add(currentSmoothGroup);
                    faceMaterials.add(currentMaterial);
                }
            }
            reader.close();

            double cx = (minX+maxX)/2, cy = (minY+maxY)/2, cz = (minZ+maxZ)/2;
            double biggest = Math.max(maxX-minX, Math.max(maxY-minY, maxZ-minZ));
            double sc = biggest == 0 ? 1.0 : 2.0 / biggest;
            List<Vector3D> fv = new ArrayList<>();
            for (Vector3D v : vertices)
                fv.add(new Vector3D(
                    (v.x-cx)*sc*scaleFactor + position.x,
                    (v.y-cy)*sc*scaleFactor + position.y,
                    (v.z-cz)*sc*scaleFactor + position.z));

            List<java.util.List<int[]>> vfg = new ArrayList<>();
            for (int i = 0; i < fv.size(); i++) vfg.add(new ArrayList<>());
            List<Vector3D[]> faceNormals = new ArrayList<>();

            for (int fi = 0; fi < faces.size(); fi++) {
                int[] face = faces.get(fi);
                if (face.length < 3) { faceNormals.add(null); continue; }
                Vector3D e1 = fv.get(face[1]).subtract(fv.get(face[0]));
                Vector3D e2 = fv.get(face[2]).subtract(fv.get(face[0]));
                faceNormals.add(new Vector3D[]{e1.cross(e2).normalize()});
                int sg = faceSmoothGroups.get(fi);
                for (int idx : face) vfg.get(idx).add(new int[]{fi, sg});
            }
            List<java.util.Map<Integer, Vector3D>> vnf = new ArrayList<>();
            for (int i = 0; i < fv.size(); i++) vnf.add(new java.util.HashMap<>());

            for (int vi = 0; vi < fv.size(); vi++) {
                java.util.Map<Integer, List<Integer>> g2f = new java.util.HashMap<>();
                for (int[] e : vfg.get(vi))
                    g2f.computeIfAbsent(e[1], k -> new ArrayList<>()).add(e[0]);
                for (java.util.Map.Entry<Integer, List<Integer>> e : g2f.entrySet()) {
                    int sg = e.getKey();
                    if (sg == 0) {
                        for (int fi : e.getValue())
                            if (faceNormals.get(fi) != null)
                                vnf.get(vi).put(fi, faceNormals.get(fi)[0]);
                    } else {
                        Vector3D acc = new Vector3D(0,0,0);
                        for (int fi : e.getValue())
                            if (faceNormals.get(fi) != null)
                                acc = acc.add(faceNormals.get(fi)[0]);
                        Vector3D sn = acc.normalize();
                        for (int fi : e.getValue()) vnf.get(vi).put(fi, sn);
                    }
                }
            }
            for (int fi = 0; fi < faces.size(); fi++) {
                int[] face = faces.get(fi);
                int[] uvs  = faceUVs.get(fi);
                if (face.length < 3 || faceNormals.get(fi) == null) continue;

                String matName = faceMaterials.get(fi);
                MtlReader.Material mat = null;
                Color color = defaultColor != null ? defaultColor : new Color(200, 200, 200);

                if (matName != null && materials.containsKey(matName)) {
                    mat   = materials.get(matName);
                    color = mat.diffuse;
                }
                for (int i = 1; i < face.length - 1; i++) {
                    int i0 = face[0],  i1 = face[i],  i2 = face[i+1];
                    int u0 = uvs[0],   u1 = uvs[i],   u2 = uvs[i+1];

                    Vector3D v0 = fv.get(i0), v1 = fv.get(i1), v2 = fv.get(i2);
                    Vector3D n0 = getNorm(i0, fi, vnf, faceNormals);
                    Vector3D n1 = getNorm(i1, fi, vnf, faceNormals);
                    Vector3D n2 = getNorm(i2, fi, vnf, faceNormals);

                    double[] uv0 = (u0 >= 0 && u0 < uvCoords.size()) ? uvCoords.get(u0) : new double[]{0,0};
                    double[] uv1 = (u1 >= 0 && u1 < uvCoords.size()) ? uvCoords.get(u1) : new double[]{0,0};
                    double[] uv2 = (u2 >= 0 && u2 < uvCoords.size()) ? uvCoords.get(u2) : new double[]{0,0};

                    Triangle tri = new Triangle(v0, v1, v2, n0, n1, n2, color);
                    tri.setUVs(uv0, uv1, uv2, mat);
                    if (mat != null)
                        tri.setFullMaterial(mat.getSpecularStrength(), mat.getShininess(),
                            mat.getReflectivity(), mat.getTransparency(), mat.ior);
                    triangles.add(tri);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return triangles;
    }
    private static Vector3D getNorm(int vi, int fi,List<java.util.Map<Integer, Vector3D>> vnf,List<Vector3D[]> fn) {
        Vector3D n = vnf.get(vi).get(fi);
        if (n != null) return n;
        if (fn.get(fi) != null) return fn.get(fi)[0];
        return new Vector3D(0, 0, 1);
    }
}