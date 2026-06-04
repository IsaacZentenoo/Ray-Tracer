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

    public static List<Triangle> loadOBJ(String filename, Color defaultColor,Vector3D position, double scaleFactor) {
        //lists to store the vertices, faces, and materials defined in the .obj file, as well as the precomputed face normals and vertex normals for smooth shading
        List<Vector3D>  vertices  = new ArrayList<>();
        List<Triangle>  triangles = new ArrayList<>();
        List<int[]>     faces     = new ArrayList<>();
        List<Integer>   faceSmoothGroups  = new ArrayList<>();
        List<String>    faceMaterials     = new ArrayList<>();
        //precomputes the bounding box of the model to center and scale it appropriately in the scene
        Map<String, MtlReader.Material> materials    = new HashMap<>();
        String currentMaterial  = null;
        int currentSmoothGroup = 1;

        double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY, minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY, maxZ = Double.NEGATIVE_INFINITY;
        try {
            Path objPath = Paths.get(filename);
            String folder = objPath.getParent() != null
                ? objPath.getParent().toString() + "/" : "./models/";
            BufferedReader reader  = new BufferedReader(new FileReader(objPath.toFile()));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("mtllib ")) {
                    String mtlFile = line.substring(7).trim();
                    materials = MtlReader.load(folder + mtlFile);
                } else if (line.startsWith("usemtl ")) {
                    currentMaterial = line.substring(7).trim();
                } else if (line.startsWith("v ")) {
                    String[] parts = line.split("\\s+");
                    double x = Double.parseDouble(parts[1]);
                    double y = Double.parseDouble(parts[2]);
                    double z = Double.parseDouble(parts[3]);
                    vertices.add(new Vector3D(x, y, z));
                    minX = Math.min(minX, x); minY = Math.min(minY, y); minZ = Math.min(minZ, z);
                    maxX = Math.max(maxX, x); maxY = Math.max(maxY, y); maxZ = Math.max(maxZ, z);
                } else if (line.startsWith("s ")) {
                    String val = line.substring(2).trim();
                    if (val.equalsIgnoreCase("off") || val.equals("0"))
                        currentSmoothGroup = 0;
                    else {
                        try { currentSmoothGroup = Integer.parseInt(val); }
                        catch (NumberFormatException e) { currentSmoothGroup = 1; }
                    }
                } else if (line.startsWith("f ")) {
                    String[] parts   = line.split("\\s+");
                    int[]    indices = new int[parts.length - 1];
                    for (int i = 1; i < parts.length; i++) {
                        String[] values = parts[i].split("/");
                        indices[i - 1] = Integer.parseInt(values[0]) - 1;
                    }
                    faces.add(indices);
                    faceSmoothGroups.add(currentSmoothGroup);
                    faceMaterials.add(currentMaterial);
                }
            }
            reader.close();
            
            double centerX = (minX + maxX) / 2.0;
            double centerY = (minY + maxY) / 2.0;
            double centerZ = (minZ + maxZ) / 2.0;
            double biggestSize = Math.max(maxX - minX, Math.max(maxY - minY, maxZ - minZ));
            double scale = biggestSize == 0.0 ? 1.0 : 2.0 / biggestSize;

            List<Vector3D> finalVertices = new ArrayList<>();
            for (Vector3D v : vertices) {
                finalVertices.add(new Vector3D(
                    (v.x - centerX) * scale * scaleFactor + position.x,
                    (v.y - centerY) * scale * scaleFactor + position.y,
                    (v.z - centerZ) * scale * scaleFactor + position.z));
            }
            //precomputes face normals and groups faces by vertex and smooth group to compute vertex normals for smooth shading, storing them in a list of maps where each map corresponds to a vertex and maps face indices to normal vectors for that vertex in those faces
            List<java.util.List<int[]>> vertexFacesByGroup = new ArrayList<>();
            for (int i = 0; i < finalVertices.size(); i++)
                vertexFacesByGroup.add(new ArrayList<>());
            List<Vector3D[]> faceNormals = new ArrayList<>();
            for (int fi = 0; fi < faces.size(); fi++) {
                int[] face = faces.get(fi);
                if (face.length < 3) { faceNormals.add(null); continue; }
                Vector3D v0    = finalVertices.get(face[0]);
                Vector3D v1    = finalVertices.get(face[1]);
                Vector3D v2    = finalVertices.get(face[2]);
                Vector3D edge1 = v1.subtract(v0);
                Vector3D edge2 = v2.subtract(v0);
                Vector3D fn    = edge1.cross(edge2).normalize();
                faceNormals.add(new Vector3D[]{fn});
                int sg = faceSmoothGroups.get(fi);
                for (int idx : face)
                    vertexFacesByGroup.get(idx).add(new int[]{fi, sg});
            }
            //list of maps, one for each vertex
            List<java.util.Map<Integer, Vector3D>> vertexNormalForFace = new ArrayList<>();
            for (int i = 0; i < finalVertices.size(); i++)
                vertexNormalForFace.add(new java.util.HashMap<>());
            //for each vertex, it groups the faces that share that vertex by their smooth group, and if the smooth group is 0 it assigns the face normal to that vertex for those faces, otherwise it averages the face normals of all faces in the same smooth group that share that vertex to compute a smooth normal for that vertex and assigns it to those faces
            for (int vi = 0; vi < finalVertices.size(); vi++) {
                List<int[]> faceEntries = vertexFacesByGroup.get(vi);
                java.util.Map<Integer, List<Integer>> groupToFaces = new java.util.HashMap<>();
                for (int[] entry : faceEntries)
                    groupToFaces.computeIfAbsent(entry[1], k -> new ArrayList<>()).add(entry[0]);
                //for each vertex, it groups the faces that share that vertex by their smooth group, and if the smooth group is 0 it assigns the face normal to that vertex for those faces, otherwise it averages the face normals of all faces in the same smooth group that share that vertex to compute a smooth normal for that vertex and assigns it to those faces
                for (java.util.Map.Entry<Integer, List<Integer>> entry : groupToFaces.entrySet()) {
                    int sg = entry.getKey();
                    List<Integer> faceIndices = entry.getValue();
                    if (sg == 0) {
                        for (int fi : faceIndices)
                            if (faceNormals.get(fi) != null)
                                vertexNormalForFace.get(vi).put(fi, faceNormals.get(fi)[0]);
                    } else {
                        Vector3D accumulated = new Vector3D(0, 0, 0);
                        for (int fi : faceIndices)
                            if (faceNormals.get(fi) != null)
                                accumulated = accumulated.add(faceNormals.get(fi)[0]);
                        Vector3D smoothNormal = accumulated.normalize();
                        for (int fi : faceIndices)
                            vertexNormalForFace.get(vi).put(fi, smoothNormal);
                    }
                }
            }
            //triangulates faces and creates Triangle objects, assigning materials and colors based on the .mtl file if available, and using the precomputed vertex normals for smooth shading
            for (int fi = 0; fi < faces.size(); fi++) {
                int[] face = faces.get(fi);
                if (face.length < 3 || faceNormals.get(fi) == null) continue;
                String matName = faceMaterials.get(fi);
                Color  triColor = defaultColor;
                MtlReader.Material mat = null;

                if (matName != null && materials.containsKey(matName)) {
                    mat      = materials.get(matName);
                    triColor = mat.diffuse;
                }
                //triangulates faces with more than 3 vertices by creating a fan of triangles around the first vertex of the face, using the precomputed vertex normals for smooth shading and assigning the material properties if available
                int first = face[0];
                for (int i = 1; i < face.length - 1; i++) {
                    int second = face[i];
                    int third  = face[i + 1];
                    //creates a triangle for each face, using the precomputed vertex normals for smooth shading and assigning the material properties if available
                    Vector3D v0 = finalVertices.get(first);
                    Vector3D v1 = finalVertices.get(second);
                    Vector3D v2 = finalVertices.get(third);
                    Vector3D n0 = getNormalForVertex(first,  fi, vertexNormalForFace, faceNormals);
                    Vector3D n1 = getNormalForVertex(second, fi, vertexNormalForFace, faceNormals);
                    Vector3D n2 = getNormalForVertex(third,  fi, vertexNormalForFace, faceNormals);

                    Triangle tri = new Triangle(v0, v1, v2, n0, n1, n2, triColor);
                    if (mat != null) {
                        tri.setFullMaterial(mat.getSpecularStrength(),mat.getShininess(),mat.getReflectivity(),
                            mat.getTransparency(),
                            mat.ior
                        );
                    }
                    triangles.add(tri);
                }}
        } catch (Exception e) {
            e.printStackTrace();
        }
        return triangles;
    }
    //helper method to get the normal vector for a vertex in a specific face, using the precomputed vertex normals for smooth shading or falling back to the face normal if no vertex normal is available
    private static Vector3D getNormalForVertex(int vertexIdx, int faceIdx,List<java.util.Map<Integer, Vector3D>> vertexNormalForFace,
        List<Vector3D[]> faceNormals) {
        Vector3D n = vertexNormalForFace.get(vertexIdx).get(faceIdx);
        if (n != null) return n;
        if (faceNormals.get(faceIdx) != null) return faceNormals.get(faceIdx)[0];
        return new Vector3D(0, 0, 1);
    }
}