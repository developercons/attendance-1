package com.yunhuakeji.attendance.service.bizservice;

import com.yunhuakeji.attendance.dao.bizdao.model.UserBuildingRef;

import java.util.List;

public interface UserBuildingService {

  List<UserBuildingRef> listByUserId(Long userId);

  List<UserBuildingRef> listByUserIds(List<Long> userIds);

  void deleteByUserIds(List<Long> userIds);

  void batchInsert(List<Long> userIds, List<UserBuildingRef> userBuildingRefList);
}
