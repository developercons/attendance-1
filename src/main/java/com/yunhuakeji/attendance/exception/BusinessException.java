package com.yunhuakeji.attendance.exception;

import com.yunhuakeji.attendance.constants.ErrorCode;

public class BusinessException extends RuntimeException  {

    private ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode){
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

}
