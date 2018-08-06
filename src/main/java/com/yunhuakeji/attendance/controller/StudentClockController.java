package com.yunhuakeji.attendance.controller;

import com.yunhuakeji.attendance.biz.StudentClockBiz;
import com.yunhuakeji.attendance.constants.Result;
import com.yunhuakeji.attendance.dto.request.StudentClockAddReqDTO;
import com.yunhuakeji.attendance.dto.request.StudentClockUpdateReqDTO;
import com.yunhuakeji.attendance.dto.response.StudentClockQueryRsqDTO;
import com.yunhuakeji.attendance.dto.response.StudentClockStatRspDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

@Api(value = "学生考勤操作接口")
@Controller
public class StudentClockController {

    @Autowired
    private StudentClockBiz studentClockBiz;

    @PostMapping("/student-clock")
    @ApiOperation(value = "学生打卡")
    public Result clock(@Valid @RequestBody StudentClockAddReqDTO req) {
        return studentClockBiz.clock(req);
    }

    @GetMapping("/student-clock/{studentId}/stat")
    @ApiOperation(value = "统计学生累计晚归，到勤，未归次数")
    public Result<StudentClockStatRspDTO> statClockByStudent(
            @ApiParam(name = "学生ID", required = true)
            @PathVariable(name = "studentId")
            @NotNull(message = "学生ID不能为空")
                    Long studentId) {
        return studentClockBiz.statClockByStudent(studentId);
    }

    @GetMapping("/student-clock")
    @ApiOperation(value = "查询打卡记录")
    public Result<List<StudentClockQueryRsqDTO>> listByYearMonth(
            @ApiParam(name = "学生ID", required = true)
            @RequestParam(name = "studentId")
            @NotNull(message = "学生ID不能为空")
                    Long studentId,
            @ApiParam(name = "年份", required = true)
            @RequestParam(name = "year")
            @NotNull(message = "年份不能为空")
            @Min(value = 1000, message = "不合法的年份")
            @Max(value = 9999, message = "不合法的年份")
                    Integer year,
            @ApiParam(name = "月份", required = true)
            @RequestParam(name = "month")
            @NotNull(message = "月份不能为空")
            @Min(value = 1, message = "不合法的月份")
            @Max(value = 12, message = "不合法的月份")
                    Integer month
    ) {
        return studentClockBiz.listByYearMonth(studentId, year, month);
    }


    @PutMapping("/student-clock")
    @ApiOperation(value = "更新学生打卡记录")
    public Result update(@Valid @RequestBody StudentClockUpdateReqDTO reqDTO) {
        return null;
    }


}