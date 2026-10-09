package utils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ManejoImagenes {

    // 1. Selector de archivo con filtro de imágenes (.jpg, .png)
    public static File seleccionarImagen() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar Póster de la Película");
        fileChooser.setAcceptAllFileFilterUsed(false);
        
        FileNameExtensionFilter filtro = new FileNameExtensionFilter("Imágenes (JPG, PNG)", "jpg", "jpeg", "png");
        fileChooser.addChoosableFileFilter(filtro);

        int resultado = fileChooser.showOpenDialog(null);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }

    // 2. Almacenamiento Local (Copia a src/assets/posters/ y renombra)
    public static String guardarPosterLocal(File archivoOrigen, int idPelicula) {
        if (archivoOrigen == null) return null;

        try {
            // Extraer extensión del archivo original (.jpg o .png)
            String nombreOriginal = archivoOrigen.getName();
            String extension = nombreOriginal.substring(nombreOriginal.lastIndexOf("."));

            // Carpeta destino dentro del proyecto
            String carpetaDestino = "src/assets/posters/";
            Path rutaDirectorio = Paths.get(carpetaDestino);

            if (!Files.exists(rutaDirectorio)) {
                Files.createDirectories(rutaDirectorio);
            }

            // Nombre estandarizado: id_pelicula_poster.ext
            String nuevoNombre = idPelicula + "_poster" + extension;
            Path rutaFinal = rutaDirectorio.resolve(nuevoNombre);

            Files.copy(archivoOrigen.toPath(), rutaFinal, StandardCopyOption.REPLACE_EXISTING);

            // Retorna la ruta relativa que se guarda en el campo imagen_url
            return carpetaDestino + nuevoNombre;

        } catch (IOException e) {
            System.err.println("Error al guardar la imagen local: " + e.getMessage());
            return null;
        }
    }

    // 3. (Opcional) Subida directa al Storage de Supabase vía HTTP
    public static String subirPosterASupabase(File archivo, int idPelicula, String supabaseUrl, String anonOrServiceKey, String bucketName) {
        if (archivo == null) return null;

        try {
            String extension = archivo.getName().substring(archivo.getName().lastIndexOf("."));
            String nombreArchivoEnNube = idPelicula + "_poster" + extension;

            // Endpoint REST de Supabase Storage: POST /storage/v1/object/{bucket}/{nombre}
            String endpoint = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + nombreArchivoEnNube;
            String contentType = extension.equalsIgnoreCase(".png") ? "image/png" : "image/jpeg";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Authorization", "Bearer " + anonOrServiceKey)
                    .header("apikey", anonOrServiceKey)
                    .header("Content-Type", contentType)
                    .header("x-upsert", "true") // Sobreescribe si ya existe
                    .POST(HttpRequest.BodyPublishers.ofFile(archivo.toPath()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // Retorna la URL pública directa del archivo
                return supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + nombreArchivoEnNube;
            } else {
                System.err.println("Fallo al subir a Supabase Storage (" + response.statusCode() + "): " + response.body());
                return null;
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("Error al conectar con Supabase Storage: " + e.getMessage());
            return null;
        }
    }
}