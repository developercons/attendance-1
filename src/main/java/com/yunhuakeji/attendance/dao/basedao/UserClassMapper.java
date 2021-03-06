package com.yunhuakeji.attendance.dao.basedao;

import com.yunhuakeji.attendance.dao.basedao.model.InstructorInfo;
import com.yunhuakeji.attendance.dao.basedao.model.UserClass;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface UserClassMapper extends Mapper<UserClass> {

  List<InstructorInfo> queryInstructorByNameAndCode(String nameOrCode);

  List<UserClass> listStudentByClassIds(List<Long> classIds);
}