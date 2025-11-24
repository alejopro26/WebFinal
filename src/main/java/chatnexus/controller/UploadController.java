package chatnexus.controller;

import chatnexus.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class UploadController {

    private final FileStorageService storage;
    private static final long MAX_SIZE = 5 * 1024 * 1024; // 5MB

    public UploadController(FileStorageService storage) { this.storage = storage; }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> upload(@RequestPart("file") MultipartFile file, Authentication authentication) {
        if (authentication == null) return ResponseEntity.status(401).body("No autenticado");
        if (file == null || file.isEmpty()) return ResponseEntity.badRequest().body("Archivo vacío");
        if (file.getSize() > MAX_SIZE) return ResponseEntity.status(413).body("Archivo supera 5MB");
        try {
            String name = storage.save(file.getOriginalFilename(), file.getBytes(), file.getContentType());
            return ResponseEntity.ok().body(java.util.Map.of("url", "/api/files/" + name));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Tipo de archivo no permitido");
        }
    }

    @GetMapping("/{name}")
    public ResponseEntity<Resource> get(@PathVariable String name) {
        Resource res = storage.load(name);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "max-age=31536000")
                .body(res);
    }
}