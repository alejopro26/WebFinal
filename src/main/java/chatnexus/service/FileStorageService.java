package chatnexus.service;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {
    private final Path root = Path.of("uploads");

    public FileStorageService() throws IOException {
        if (!Files.exists(root)) Files.createDirectories(root);
    }

    public String save(String originalFilename, byte[] bytes, String contentType) throws IOException {
        String ext = extractExt(originalFilename);
        if (!isAllowed(contentType, ext)) throw new IOException("Tipo de archivo no permitido");
        String name = UUID.randomUUID().toString() + (ext != null ? "." + ext : "");
        Path dest = root.resolve(name);
        Files.write(dest, bytes);
        return name;
    }

    public Resource load(String name) {
        Path p = root.resolve(name);
        return new FileSystemResource(p.toFile());
    }

    private String extractExt(String filename) {
        int i = filename.lastIndexOf('.');
        return i >= 0 ? filename.substring(i + 1).toLowerCase() : null;
    }

    private boolean isAllowed(String contentType, String ext) {
        if (contentType == null && ext == null) return false;
        String ct = contentType == null ? "" : contentType.toLowerCase();
        if (ct.startsWith("image/") || (ext != null && (ext.equals("png") || ext.equals("jpg") || ext.equals("jpeg") || ext.equals("gif")))) return true;
        if (ct.equals("application/pdf") || (ext != null && ext.equals("pdf"))) return true;
        return false;
    }
}