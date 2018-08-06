package com.yunhuakeji.attendance.service.bizservice.impl;

import com.yunhuakeji.attendance.dao.bizdao.StudentClockMapper;
import com.yunhuakeji.attendance.dao.bizdao.model.StudentClock;
import com.yunhuakeji.attendance.service.bizservice.StudentClockService;
import com.yunhuakeji.attendance.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class StudentClockServiceImpl implements StudentClockService {

    @Autowired
    private StudentClockMapper studentClockMapper;

    @Override
    public List<StudentClock> list(Long studentId, Long clockDate) {
        Example example = new Example(StudentClock.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("userId", studentId);
        criteria.andEqualTo("clockDate", clockDate);
        return studentClockMapper.selectByExample(example);
    }

    @Override
    public List<StudentClock> listByMonth(Long studentId, Integer year, Integer month) {
        Example example = new Example(StudentClock.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("userId", studentId);
        long startDay = DateUtil.getYearMonthStartDay(year, month);
        long endDay = DateUtil.getYearMonthEndDay(year, month);
        criteria.andBetween("clockDate", startDay, endDay);
        return studentClockMapper.selectByExample(example);
    }

    @Override
    public void batchInsert(List<StudentClock> studentClockList) {
        studentClockMapper.insertBatchSelective(studentClockList);
    }

    @Override
    public int count(Long studentId, Byte clockStatus) {
        Example example = new Example(StudentClock.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("userId", studentId);
        criteria.andEqualTo("clockStatus", clockStatus);
        return studentClockMapper.selectCountByExample(example);
    }
}
