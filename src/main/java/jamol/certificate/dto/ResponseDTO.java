package jamol.certificate.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@JsonInclude(value = JsonInclude.Include.NON_NULL)
@ToString
@Getter
@Setter
public class ResponseDTO<T> {
    private boolean success;
    private String reason;
    private long recordsTotal = 0L;
    private T data;
    private String message;

    public ResponseDTO() {
    }

    public ResponseDTO(boolean success, String reason) {
        this.success = success;
        this.reason = reason;
    }

    public ResponseDTO(boolean success, String reason, T data) {
        this.success = success;
        this.reason = reason;
        this.data = data;
    }

    public static <T> ResponseDTO<T> ok(T data) {
        ResponseDTO<T> responseDto = new ResponseDTO<>();
        responseDto.setSuccess(true);
        responseDto.setReason(null);
        responseDto.setData(data);
        return responseDto;
    }

    public static <T> ResponseDTO<T> ok(T data, String message) {
        ResponseDTO<T> responseDto = new ResponseDTO<>();
        responseDto.setSuccess(true);
        responseDto.setMessage(message);
        responseDto.setReason(null);
        responseDto.setData(data);
        return responseDto;
    }

    public static <T> ResponseDTO<T> error(String reason, T data) {
        ResponseDTO<T> responseDto = new ResponseDTO<>();
        responseDto.setSuccess(false);
        responseDto.setReason(reason);
        responseDto.setData(data);
        return responseDto;
    }

}