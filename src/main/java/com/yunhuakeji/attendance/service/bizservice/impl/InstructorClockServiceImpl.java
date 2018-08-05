package com.yunhuakeji.attendance.service.bizservice.impl;

import com.yunhuakeji.attendance.dao.bizdao.InstructorClockMapper;
import com.yunhuakeji.attendance.dao.bizdao.model.InstructorClock;
import com.yunhuakeji.attendance.service.bizservice.InstructorClockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class InstructorClockServiceImpl implements InstructorClockService {

    @Autowired
    private InstructorClockMapper instructorClockMapper;

    @Override
    public int statByInstructor(long instructorId) {
        Example example = new Example(InstructorClock.class);
        example.createCriteria().andEqualTo("instructorId", instructorId);
        return instructorClockMapper.selectCountByExample(example);
    }

    @Override
    public List<InstructorClock> list(long instructorId, long clockDate) {
        Example example = new Example(InstructorClock.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("instructorId", instructorId);
        criteria.andEqualTo("statDate", clockDate);
        return instructorClockMapper.selectByExample(example);
    }

    @Override
    public int save(InstructorClock instructorClock) {
        return instructorClockMapper.insertSelective(instructorClock);
    }
}
