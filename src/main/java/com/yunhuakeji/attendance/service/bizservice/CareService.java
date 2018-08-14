package com.yunhuakeji.attendance.service.bizservice;

import com.github.pagehelper.PageInfo;
import com.yunhuakeji.attendance.dao.bizdao.model.Care;

import java.util.List;

public interface CareService {

  PageInfo pageByInstructor(long instructorId, Byte careStatus, int pageNo, int pageSize);

  void update(Care care);

  PageInfo pageByStudent(long studentId,int pageNo, int pageSize);

  void batchInsert(List<Care> careList);

  List<Care> listByIds(List<Long> ids);

  void batchDelete(List<Long> ids);
}
