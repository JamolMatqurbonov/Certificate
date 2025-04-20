package jamol.certificate.entity;


import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Certificate implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    private String course;

    @Column(name = "given_at")
    private LocalDateTime givenAt;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "u_id")
    private String uId;

    @Column(name = "qr_id")
    private String qrId;
}
