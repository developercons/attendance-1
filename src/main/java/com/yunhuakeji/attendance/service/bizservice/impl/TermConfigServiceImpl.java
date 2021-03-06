package com.yunhuakeji.attendance.service.bizservice.impl;

import com.yunhuakeji.attendance.dao.bizdao.TermConfigMapper;
import com.yunhuakeji.attendance.dao.bizdao.model.StudentDeviceRef;
import com.yunhuakeji.attendance.dao.bizdao.model.TermConfig;
import com.yunhuakeji.attendance.service.bizservice.TermConfigService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;

import tk.mybatis.mapper.entity.Example;

@Service
public class TermConfigServiceImpl implements TermConfigService {

  @Autowired
  private TermConfigMapper termConfigMapper;

  @Override
  public TermConfig getCurrTermConfig() {
    Example example = new Example(TermConfig.class);
    Example.Criteria criteria = example.createCriteria();
    Date d = new Date();
    criteria.andLessThanOrEqualTo("startDate", d);
    criteria.andGreaterThanOrEqualTo("endDate", d);
    List<TermConfig> termConfigList = termConfigMapper.selectByExample(example);
    if (CollectionUtils.isEmpty(termConfigList)) {
      return null;
    }
    return termConfigList.get(0);
  }

  @Override
  public TermConfig getLastTermConfig() {
    Example example = new Example(TermConfig.class);
    example.setOrderByClause("START_DATE");
    List<TermConfig> termConfigList = termConfigMapper.selectByExample(example);
    if (CollectionUtils.isEmpty(termConfigList)) {
      return null;
    }
    Date nowDate = new Date();
    for (TermConfig termConfig : termConfigList) {
      if (termConfig.getStartDate().after(nowDate) || termConfig.getEndDate().after(nowDate)) {
        return termConfig;
      }
    }
    return null;
  }

  @Override
  public void insert(TermConfig termConfig) {
    termConfigMapper.insert(termConfig);
  }

  @Override
  public List<TermConfig> listAll() {
    Example example = new Example(TermConfig.class);
    example.setOrderByClause("START_YEAR,TERM_NUMBER");
    return termConfigMapper.selectByExample(example);
  }
}
