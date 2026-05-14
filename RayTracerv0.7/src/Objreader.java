import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.awt.Color;

public class Objreader {

    public static List<Triangle> loadOBJ(String filename, Color color) {
        List<Vector3D> vertices = new ArrayList<>();
        List<Triangle> triangles = new ArrayList<>();
        List<int[]> faces = new ArrayList<>();
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;
        
        try {
            Path objPath = Paths.get(filename);
            BufferedReader reader = new BufferedReader(
                new FileReader(objPath.toFile())
            );
            String line;
            while ((line = reader.readLine()) != null) {

                line = line.trim();
                if (line.startsWith("v ")) {

                    String[] parts = line.split("\\s+");

                    double x = Double.parseDouble(parts[1]);
                    double y = Double.parseDouble(parts[2]);
                    double z = Double.parseDouble(parts[3]);

                    vertices.add(new Vector3D(x, y, z));
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    minZ = Math.min(minZ, z);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                    maxZ = Math.max(maxZ, z);
                }
                else if (line.startsWith("f ")) {

                    String[] parts = line.split("\\s+");
                    int[] indices = new int[parts.length - 1];

                    for (int i = 1; i < parts.length; i++) {

                        String[] values = parts[i].split("/");

                        indices[i - 1] = Integer.parseInt(values[0]) - 1;
                    }

                    faces.add(indices);
                }
            }
            reader.close();
            double centerX = (minX + maxX) / 2.0;
            double centerY = (minY + maxY) / 2.0;
            double centerZ = (minZ + maxZ) / 2.0;

            double sizeX = maxX - minX;
            double sizeY = maxY - minY;
            double sizeZ = maxZ - minZ;
            double biggestsize = Math.max(sizeX, Math.max(sizeY, sizeZ));
            double scale = biggestsize == 0.0 ? 1.0 : 2.0 / biggestsize;

            List <Vector3D>finalVertices = new ArrayList<>();
            for (Vector3D vertex : vertices) {
                double scaleFactor = 1.2;
                double x = (vertex.x - centerX) * scale * scaleFactor;
                double y = (vertex.y - centerY) * scale * scaleFactor;
                double z = (vertex.z - centerZ) * scale * scaleFactor + 3.5;
                finalVertices.add(new Vector3D(x, y, z));
            }

            List <Vector3D> vertexNormals= new ArrayList<>();

            for (int i = 0; i < finalVertices.size(); i++) {
                vertexNormals.add(new Vector3D(0, 0, 0));
            }
            for (int[] face : faces) {
                if (face.length < 3) {
                    continue;
                }
                int first = face[0];

                for (int i = 1; i < face.length - 1; i++) {
                    int second = face[i];
                    int third = face[i + 1];

                    Vector3D v0 = finalVertices.get(first);
                    Vector3D v1 = finalVertices.get(second);
                    Vector3D v2 = finalVertices.get(third);

                    Vector3D edge1 = v1.subtract(v0);
                    Vector3D edge2 = v2.subtract(v0);
                    Vector3D faceNormal = edge1.cross(edge2).normalize();

                    vertexNormals.set(first, vertexNormals.get(first).add(faceNormal));
                    vertexNormals.set(second, vertexNormals.get(second).add(faceNormal));
                    vertexNormals.set(third, vertexNormals.get(third).add(faceNormal));
                }
            }
            for (int i = 0; i < vertexNormals.size(); i++) {
                vertexNormals.set(i, vertexNormals.get(i).normalize());
            }
            for (int[] face : faces) {
                if (face.length < 3) {
                    continue;
                }
                int first = face[0];

                for (int i = 1; i < face.length - 1; i++) {
                    int second = face[i];
                    int third = face[i + 1];

                    Vector3D v0 = finalVertices.get(first);
                    Vector3D v1 = finalVertices.get(second);
                    Vector3D v2 = finalVertices.get(third);

                    Vector3D n0 = vertexNormals.get(first);
                    Vector3D n1 = vertexNormals.get(second);
                    Vector3D n2 = vertexNormals.get(third);

                    triangles.add(new Triangle(v0, v1, v2, n0, n1, n2, color));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return triangles;
    }}
            