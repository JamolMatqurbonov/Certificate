package jamol.certificate.repository;

import jamol.certificate.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    Certificate findByQrId(String qrId);

}