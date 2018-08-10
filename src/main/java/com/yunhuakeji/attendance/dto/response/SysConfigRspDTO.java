package com.yunhuakeji.attendance.dto.response;

import com.yunhuakeji.attendance.dao.bizdao.model.ClockAddressSetting;
import com.yunhuakeji.attendance.dto.request.AddressReqDTO;

import java.util.Date;
import java.util.List;

import io.swagger.annotations.ApiParam;

/**
 * 系统配置
 */
public class SysConfigRspDTO {

  @ApiParam(name = "打卡开始时间,格式HH:mm:ss", required = true)
  private String clockStartTime;

  @ApiParam(name = "打卡结束时间,格式HH:mm:ss", required = true)
  private String clockEndTime;

  @ApiParam(name = "查寝开始时间,格式HH:mm:ss", required = true)
  private String checkClockStartTime;

  @ApiParam(name = "查寝结束时间,格式HH:mm:ss", required = true)
  private String checkClockEndTime;

  @ApiParam(name = "年", required = true)
  private Integer year;

  @ApiParam(name = "月", required = true)
  private Integer month;

  @ApiParam(name = "日期列表", required = true)
  private List<Integer> dayList;

  @ApiParam(name = "地址列表", required = true)
  private List<ClockAddressSetting> clockAddressSettingList;

  @ApiParam(name = "是否校验设备", required = true)
  private Byte checkDevice;

  public String getClockStartTime() {
    return clockStartTime;
  }

  public void setClockStartTime(String clockStartTime) {
    this.clockStartTime = clockStartTime;
  }

  public String getClockEndTime() {
    return clockEndTime;
  }

  public void setClockEndTime(String clockEndTime) {
    this.clockEndTime = clockEndTime;
  }

  public String getCheckClockStartTime() {
    return checkClockStartTime;
  }

  public void setCheckClockStartTime(String checkClockStartTime) {
    this.checkClockStartTime = checkClockStartTime;
  }

  public String getCheckClockEndTime() {
    return checkClockEndTime;
  }

  public void setCheckClockEndTime(String checkClockEndTime) {
    this.checkClockEndTime = checkClockEndTime;
  }

  public Integer getYear() {
    return year;
  }

  public void setYear(Integer year) {
    this.year = year;
  }

  public Integer getMonth() {
    return month;
  }

  public void setMonth(Integer month) {
    this.month = month;
  }

  public List<Integer> getDayList() {
    return dayList;
  }

  public void setDayList(List<Integer> dayList) {
    this.dayList = dayList;
  }

  public List<ClockAddressSetting> getClockAddressSettingList() {
    return clockAddressSettingList;
  }

  public void setClockAddressSettingList(List<ClockAddressSetting> clockAddressSettingList) {
    this.clockAddressSettingList = clockAddressSettingList;
  }

  public Byte getCheckDevice() {
    return checkDevice;
  }

  public void setCheckDevice(Byte checkDevice) {
    this.checkDevice = checkDevice;
  }
}

