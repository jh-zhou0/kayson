package cn.zjh.kayson.framework.common.exception;

import cn.zjh.kayson.framework.common.exception.enums.GlobalErrorCodeConstants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author zjh - kayson
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor // 空构造，避免反序列化问题
@AllArgsConstructor
public class ServerException extends RuntimeException {

    /**
     * 全局错误码
     *
     * @see GlobalErrorCodeConstants
     */
    private Integer code;
    /**
     * 错误提示
     */
    private String message;

    public ServerException(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMsg();
    }
}
