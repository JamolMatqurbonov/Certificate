package jamol.certificate.service;

import com.itextpdf.text.Image; // ItextImage importi
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.itextpdf.text.BadElementException;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import jamol.certificate.dto.StudentReceiverDto;
import jamol.certificate.entity.Certificate;
import jamol.certificate.exception.CertificateNotFoundException;
import jamol.certificate.repository.CertificateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Hashtable;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final S3Service s3Service;

    /**
     * Talabaning ma'lumotlari asosida sertifikat yaratadi va S3'ga yuklab, DB'ga saqlaydi.
     */
    public void generateCertificate(StudentReceiverDto dto) throws IOException, DocumentException, WriterException {
        // Sertifikat shablon faylini o'qish
        ClassPathResource resource = new ClassPathResource("Certificate nusxasi.pdf");

        // Faylni tekshirish
        if (!resource.exists()) {
            throw new IOException("Certificate nusxasi.pdf fayli topilmadi!");
        }

        PdfReader reader = new PdfReader(resource.getInputStream());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfStamper stamper = new PdfStamper(reader, baos);

        // Foydalaniladigan shriftni o'rnatish
        BaseFont font = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.WINANSI, BaseFont.EMBEDDED);
        PdfContentByte canvas = stamper.getOverContent(1);

        // Talabaning to‘liq ismini PDF faylga joylashtirish (markazga)
        String fullName = dto.firstName() + " " + dto.lastName();
        Rectangle pageSize = reader.getPageSize(1);
        float centerX = pageSize.getWidth() / 2;
        canvas.beginText();
        canvas.setFontAndSize(font, 30);
        canvas.showTextAligned(Element.ALIGN_CENTER, fullName, centerX, 330, 0); // Y = 330 pozitsiyada
        canvas.endText();

        // Joriy sanani pastki markazga joylashtirish
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        canvas.beginText();
        canvas.setFontAndSize(font, 14);
        canvas.showTextAligned(Element.ALIGN_CENTER, date, 452, 40, 0);
        canvas.endText();

        // QR kodni yaratish (tasodifiy kod asosida)
        String qrCode = generateRandomCode(6);
        BufferedImage qrImage = generateQRCodeImage(qrCode, 110, 110);
        Image qr = convertBufferedImageToItextImage(qrImage);
        qr.setAbsolutePosition(650, 58); // QR kodning pozitsiyasi
        qr.scaleToFit(110, 110);         // QR kod o‘lchami
        stamper.getOverContent(1).addImage(qr); // PDF'ga qo‘shish

        // PDF faylni yakunlash
        stamper.close();
        reader.close();

        // PDF faylni S3'ga yuklash
        String uuid = UUID.randomUUID().toString(); // Har bir fayl uchun noyob ID
        String key = s3Service.uploadFile(baos.toByteArray(), "certificate", uuid);

        // Sertifikatni bazaga saqlash
        Certificate certificate = new Certificate();
        certificate.setUId(uuid);
        certificate.setFilePath(key);
        certificate.setFirstName(dto.firstName());
        certificate.setLastName(dto.lastName());
        certificate.setQrId(qrCode); // QR kodni bazada saqlash (QR orqali qidiruv uchun)
        certificate.setGivenAt(LocalDateTime.now());

        certificateRepository.save(certificate);

        log.info("Certificate generated for {} {} with QR code {}", dto.firstName(), dto.lastName(), qrCode);
    }

    /**
     * QR kod tasvirini yaratadi.
     */
    private BufferedImage generateQRCodeImage(String text, int width, int height) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
        hints.put(EncodeHintType.MARGIN, 0); // Chegarasiz QR
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H); // Yuqori aniqlik

        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    /**
     * Tasodifiy QR kod uchun alfanumerik kod yaratadi.
     */
    private String generateRandomCode(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random rand = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(rand.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * BufferedImage turidagi rasmni iText Image turiga o‘tkazadi.
     */
    private Image convertBufferedImageToItextImage(BufferedImage bufferedImage) throws IOException, BadElementException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        javax.imageio.ImageIO.write(bufferedImage, "png", baos);
        return Image.getInstance(baos.toByteArray());
    }

    /**
     * QR ID asosida sertifikatni olish (bazadan).
     */
    public Certificate getOne(String qrId) {
        Certificate certificate = certificateRepository.findByQrId(qrId);
        if (certificate == null) {
            throw new CertificateNotFoundException("Certificate with the given QR ID was not found");
        }
        return certificate;
    }
}
