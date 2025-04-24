package jamol.certificate.controller;

import jamol.certificate.dto.StudentReceiverDto;
import jamol.certificate.entity.Certificate;
import jamol.certificate.exception.CertificateNotFoundException;
import jamol.certificate.service.CertificateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;

    @PostMapping("/creat")  // Correct method mapping
    public ResponseEntity<String> createCertificate(@RequestBody StudentReceiverDto dto) {
        try {
            certificateService.generateCertificate(dto);
            return ResponseEntity.ok("Sertifikat muvaffaqiyatli yaratildi!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Sertifikat yaratishda xatolik: " + e.getMessage());
        }
    }


    /**
     * QR ID asosida sertifikat ma'lumotlarini olish.
     * @param key QR kod orqali qidiriladigan ID
     */
    @GetMapping("/{key}")
    public ResponseEntity<Certificate> getCertificateByQrId(@PathVariable String key) {
        try {
            Certificate certificate = certificateService.getOne(key);
            return ResponseEntity.ok(certificate);
        } catch (CertificateNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null); // Sertifikat topilmagan holat
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null); // Umumiy xatolik holati
        }
    }

    /**
     * Sertifikatlarni olish bo'yicha global exception handler (masalan, validation xatoliklari)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGlobalException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Global xatolik: " + ex.getMessage());
    }
}
