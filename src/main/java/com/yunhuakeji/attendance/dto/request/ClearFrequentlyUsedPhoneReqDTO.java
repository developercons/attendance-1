package com.yunhuakeji.attendance.dto.request;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiParam;

/**
 * 清除常用手机
 */
public class ClearFrequentlyUsedPhoneReqDTO {

  @ApiParam(name = "学生ID列表", required = true)
  @NotNull(message = "学生ID列表不能为空")
  @Size(min = 1, max = 1000, message = "学生ID列表长度1-1000")
  private List<Long> studentId;

  public List<Long> getStudentId() {
    return studentId;
  }

  public void setStudentId(List<Long> studentId) {
    this.studentId = studentId;
  }
}