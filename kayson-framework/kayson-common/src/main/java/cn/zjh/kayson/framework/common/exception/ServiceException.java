package cn.zjh.kayson.framework.common.exception;

import cn.zjh.kayson.framework.common.exception.enums.ServiceErrorCodeRange;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 业务逻辑异常 Exception
 *
 * @author zjh - kayson
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor // 空构造，避免反序列化问题
@AllArgsConstructor
public class ServiceException extends RuntimeException {

    /**
     * 业务错误码
     *
     * @see ServiceErrorCodeRange
     */
    private Integer code;
    /**
     * 错误提示
     */
    private String message;

    public ServiceException(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMsg();
    }
}
