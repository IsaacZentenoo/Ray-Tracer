import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
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
            Path objPath = resolvePath(filename);
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
            double maxDimension = Math.max(
                maxX - minX,
                Math.max(maxY - minY, maxZ - minZ)
            );
            double scale = maxDimension == 0.0 ? 1.0 : 2.0 / maxDimension;

            List<Vector3D> transformedVertices = new ArrayList<>();
            for (Vector3D vertex : vertices) {
                transformedVertices.add(
                    new Vector3D(
                        (vertex.x - centerX) * scale,
                        (vertex.y - centerY) * scale,
                        (vertex.z - centerZ) * scale + 3.0
                    )
                );
            }

            for (int[] face : faces) {
                if (face.length < 3) {
                    continue;
                }
                Vector3D firstVertex = transformedVertices.get(face[0]);
                for (int i = 1; i < face.length - 1; i++) {
                    Vector3D secondVertex = transformedVertices.get(face[i]);
                    Vector3D thirdVertex = transformedVertices.get(face[i + 1]);
                    triangles.add(new Triangle(firstVertex, secondVertex, thirdVertex, color));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return triangles;
    }

    private static Path resolvePath(String filename) {
        Path directPath = Paths.get(filename);
        if (Files.exists(directPath)) {
            return directPath;
        }

        Path srcPath = Paths.get("src", filename);
        if (Files.exists(srcPath)) {
            return srcPath;
        }

        return directPath;
    }
}