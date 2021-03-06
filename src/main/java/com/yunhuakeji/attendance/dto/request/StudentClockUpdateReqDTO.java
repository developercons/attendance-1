package com.yunhuakeji.attendance.dto.request;

import javax.validation.constraints.*;

import io.swagger.annotations.ApiModelProperty;

public class StudentClockUpdateReqDTO {

  @ApiModelProperty(value = "ID", required = true)
  @NotNull(message = "ID不能为空")
  private Long id;
  @ApiModelProperty(value = "状态", required = true)
  @NotNull(message = "状态不能为空")
  private Byte status;
  @ApiModelProperty(value = "备注")
  @Size(max = 30, message = "长度不能超过30")
  private String remark;
  @ApiModelProperty(value = "操作人ID", required = true)
  private Long operatorId;
  @ApiModelProperty(value = "操作人名称", required = true)
  private String operatorName;
  @ApiModelProperty(value = "操作应用类型 1.晚归查寝 2平台后台", required = true)
  @NotNull(message = "操作应用类型不能为空")
  @Min(value = 1, message = "范围1-2")
  @Max(value = 2, message = "范围1-2")
  private Byte appType;
  @ApiModelProperty(value = "打卡日期 格式 yyyyMMdd", required = true)
  private String clockDate;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Byte getStatus() {
    return status;
  }

  public void setStatus(Byte status) {
    this.status = status;
  }

  public String getRemark() {
    return remark;
  }

  public void setRemark(String remark) {
    this.remark = remark;
  }

  public Long getOperatorId() {
    return operatorId;
  }

  public void setOperatorId(Long operatorId) {
    this.operatorId = operatorId;
  }

  public Byte getAppType() {
    return appType;
  }

  public void setAppType(Byte appType) {
    this.appType = appType;
  }

  public String getOperatorName() {
    return operatorName;
  }

  public void setOperatorName(String operatorName) {
    this.operatorName = operatorName;
  }

  public String getClockDate() {
    return clockDate;
  }

  public void setClockDate(String clockDate) {
    this.clockDate = clockDate;
  }
}
