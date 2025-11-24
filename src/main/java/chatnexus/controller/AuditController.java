package chatnexus.controller;

import chatnexus.service.AuditService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit")
public class AuditController {
    private final AuditService auditService;
    public AuditController(AuditService auditService) { this.auditService = auditService; }

    @GetMapping
    public ResponseEntity<java.util.List<AuditService.Event>> list(@RequestParam(required = false) String type) {
        return ResponseEntity.ok(auditService.list(type));
    }
}