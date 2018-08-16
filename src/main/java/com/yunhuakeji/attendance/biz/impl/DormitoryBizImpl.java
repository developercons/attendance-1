package com.yunhuakeji.attendance.biz.impl;

import com.yunhuakeji.attendance.aspect.RequestLog;
import com.yunhuakeji.attendance.biz.BusinessUtil;
import com.yunhuakeji.attendance.biz.DormitoryBiz;
import com.yunhuakeji.attendance.cache.BuildingCacheService;
import com.yunhuakeji.attendance.cache.ClassCacheService;
import com.yunhuakeji.attendance.constants.ErrorCode;
import com.yunhuakeji.attendance.constants.Result;
import com.yunhuakeji.attendance.dao.basedao.model.BuildingInfo;
import com.yunhuakeji.attendance.dao.basedao.model.DormitoryInfo;
import com.yunhuakeji.attendance.dao.basedao.model.DormitoryUser;
import com.yunhuakeji.attendance.dao.basedao.model.User;
import com.yunhuakeji.attendance.dao.bizdao.model.*;
import com.yunhuakeji.attendance.dto.request.DormitoryCheckOverReqDTO;
import com.yunhuakeji.attendance.dto.response.BuildingQueryRspDTO;
import com.yunhuakeji.attendance.dto.response.DormitoryCheckDayStatListRspDTO;
import com.yunhuakeji.attendance.dto.response.DormitoryCheckDayStatRspDTO;
import com.yunhuakeji.attendance.dto.response.DormitoryCheckWeekStatListRspDTO;
import com.yunhuakeji.attendance.dto.response.DormitoryCheckWeekStatRspDTO;
import com.yunhuakeji.attendance.dto.response.DormitoryClockDetailStatDTO;
import com.yunhuakeji.attendance.dto.response.DormitoryClockStatDTO;
import com.yunhuakeji.attendance.dto.response.DormitorySimpleRspDTO;
import com.yunhuakeji.attendance.dto.response.StudentDormitoryRsqDTO;
import com.yunhuakeji.attendance.dto.response.WeekInfoRspDTO;
import com.yunhuakeji.attendance.enums.AppType;
import com.yunhuakeji.attendance.enums.ClockStatus;
import com.yunhuakeji.attendance.enums.RoleType;
import com.yunhuakeji.attendance.exception.BusinessException;
import com.yunhuakeji.attendance.service.baseservice.BuildingInfoService;
import com.yunhuakeji.attendance.service.baseservice.DormitoryInfoService;

import com.yunhuakeji.attendance.service.baseservice.DormitoryUserService;
import com.yunhuakeji.attendance.service.baseservice.StudentInfoService;
import com.yunhuakeji.attendance.service.baseservice.UserService;
import com.yunhuakeji.attendance.service.bizservice.AccountService;
import com.yunhuakeji.attendance.service.bizservice.CheckDormitoryService;
import com.yunhuakeji.attendance.service.bizservice.ClockDaySettingService;
import com.yunhuakeji.attendance.service.bizservice.StudentClockService;
import com.yunhuakeji.attendance.service.bizservice.TermConfigService;
import com.yunhuakeji.attendance.service.bizservice.UserBuildingService;
import com.yunhuakeji.attendance.util.DateUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.management.relation.Role;

import java.util.*;

@Service
public class DormitoryBizImpl implements DormitoryBiz {

  private static final Logger logger = LoggerFactory.getLogger(DormitoryBizImpl.class);


  @Autowired
  private BuildingInfoService buildingInfoService;

  @Autowired
  private DormitoryInfoService dormitoryInfoService;

  @Autowired
  private UserBuildingService userBuildingService;

  @Autowired
  private BuildingCacheService buildingCacheService;

  @Autowired
  private DormitoryUserService dormitoryUserService;

  @Autowired
  private StudentClockService studentClockService;

  @Autowired
  private CheckDormitoryService checkDormitoryService;

  @Autowired
  private StudentInfoService studentInfoService;

  @Autowired
  private AccountService accountService;

  @Autowired
  private ClassCacheService classCacheService;

  @Autowired
  private UserService userService;

  @Autowired
  private TermConfigService termConfigService;

  @Autowired
  private ClockDaySettingService clockDaySettingService;


  @Override
  public Result<List<BuildingQueryRspDTO>> listAllBuilding() {
    List<BuildingInfo> buildingInfoList = buildingInfoService.listAll();
    List<BuildingQueryRspDTO> rspDTOList = new ArrayList<>();
    if (!CollectionUtils.isEmpty(buildingInfoList)) {
      for (BuildingInfo buildingInfo : buildingInfoList) {
        rspDTOList.add(convertToDormitoryBuildingQueryRspDTO(buildingInfo));
      }
    }
    return Result.success(rspDTOList);
  }

  @Override
  public Result<List<DormitorySimpleRspDTO>> listDormitory(Long buildingId, Integer floorNumber) {

    List<DormitorySimpleRspDTO> resultList = new ArrayList<>();
    List<DormitoryInfo> dormitoryInfoList = dormitoryInfoService.list(buildingId, floorNumber);
    if (!CollectionUtils.isEmpty(dormitoryInfoList)) {
      for (DormitoryInfo dormitoryInfo : dormitoryInfoList) {
        resultList.add(convertToDormitoryQueryRspDTO(dormitoryInfo));
      }
    }
    return Result.success(resultList);
  }

  private byte getRoleTypeByUserId(Long userId) {
    Account account = accountService.getAccountByUserId(userId);
    if (account == null) {
      return -1;
    }
    return account.getRoleType();
  }


  @Override
  public Result<List<BuildingQueryRspDTO>> listBuildingForApp(Long userId) {

    byte roleType = getRoleTypeByUserId(userId);
    List<Long> instructorIds = classCacheService.getInstructorIds();

    if (instructorIds != null && instructorIds.contains(userId)) {
      List<DormitoryInfo> dormitoryInfoList = dormitoryInfoService.listDormitoryByInstructorId(userId);
      Map<Long, BuildingInfo> buildingInfoMap = buildingCacheService.getBuildingInfoMap();
      List<BuildingQueryRspDTO> rspDTOList = new ArrayList<>();
      if (!CollectionUtils.isEmpty(dormitoryInfoList)) {
        for (DormitoryInfo dormitoryInfo : dormitoryInfoList) {
          BuildingQueryRspDTO dto = new BuildingQueryRspDTO();
          dto.setBuildingId(dormitoryInfo.getBuildingId());
          if (buildingInfoMap.get(dormitoryInfo.getBuildingId()) != null) {
            dto.setBuildingName(buildingInfoMap.get(dormitoryInfo.getBuildingId()).getName());
          }
        }
      }
      return Result.success(rspDTOList);
    } else if (RoleType.DormitoryAdmin.getType() == roleType) {
      List<UserBuildingRef> userBuildingRefList = userBuildingService.listByUserId(userId);
      Map<Long, BuildingInfo> buildingInfoMap = buildingCacheService.getBuildingInfoMap();
      List<BuildingQueryRspDTO> rspDTOList = new ArrayList<>();
      if (!CollectionUtils.isEmpty(userBuildingRefList)) {
        for (UserBuildingRef userBuildingRef : userBuildingRefList) {
          BuildingQueryRspDTO dto = new BuildingQueryRspDTO();
          dto.setBuildingId(userBuildingRef.getBuildingId());
          if (buildingInfoMap.get(userBuildingRef.getBuildingId()) != null) {
            dto.setBuildingName(buildingInfoMap.get(userBuildingRef.getBuildingId()).getName());
          }
        }
      }
      return Result.success(rspDTOList);

    } else if (RoleType.StudentsAffairsAdmin.getType() == roleType) {
      List<BuildingInfo> buildingInfoList = buildingInfoService.listAll();
      List<BuildingQueryRspDTO> rspDTOList = new ArrayList<>();
      if (!CollectionUtils.isEmpty(buildingInfoList)) {
        for (BuildingInfo buildingInfo : buildingInfoList) {
          rspDTOList.add(convertToDormitoryBuildingQueryRspDTO(buildingInfo));
        }
      }
      return Result.success(rspDTOList);
    }
    return Result.success(new ArrayList<>());
  }

  @Override
  public Result<List<DormitorySimpleRspDTO>> listDormitoryForApp(Long userId, Long buildingId, Integer floorNumber) {
    byte roleType = getRoleTypeByUserId(userId);
    List<Long> instructorIds = classCacheService.getInstructorIds();
    if (instructorIds != null && instructorIds.contains(userId)) {
      List<DormitoryInfo> dormitoryInfoList = dormitoryInfoService.listDormitoryByInstructorId(userId);
      List<DormitorySimpleRspDTO> rspDTOList = new ArrayList<>();
      if (!CollectionUtils.isEmpty(dormitoryInfoList)) {
        for (DormitoryInfo dormitoryInfo : dormitoryInfoList) {
          if (dormitoryInfo.getBuildingId().equals(buildingId) && (dormitoryInfo.getFloorNumber() == null
              || dormitoryInfo.getFloorNumber().intValue() == floorNumber)) {
            DormitorySimpleRspDTO dto = new DormitorySimpleRspDTO();
            dto.setDormitoryId(dormitoryInfo.getDormitoryId());
            dto.setDormitoryName(dormitoryInfo.getName());
            rspDTOList.add(dto);
          }
        }
      }
      return Result.success(rspDTOList);
    } else if (RoleType.DormitoryAdmin.getType() == roleType || RoleType.StudentsAffairsAdmin.getType() == roleType) {
      List<DormitoryInfo> dormitoryInfoList = dormitoryInfoService.list(buildingId, floorNumber);
      List<DormitorySimpleRspDTO> rspDTOList = new ArrayList<>();
      if (!CollectionUtils.isEmpty(dormitoryInfoList)) {
        for (DormitoryInfo dormitoryInfo : dormitoryInfoList) {
          DormitorySimpleRspDTO dto = new DormitorySimpleRspDTO();
          dto.setDormitoryId(dormitoryInfo.getDormitoryId());
          dto.setDormitoryName(dormitoryInfo.getName());
          rspDTOList.add(dto);
        }
      }
      return Result.success(rspDTOList);
    }
    return Result.success(new ArrayList<>());
  }

  @Override
  public Result<List<DormitoryClockStatDTO>> listDormitoryClockStatForApp(Long userId, Long buildingId, Integer floorNumber, Long dormitoryId) {

    List<DormitoryInfo> dormitoryInfoList = getDormitoryInfo(userId, buildingId, floorNumber, dormitoryId);
    if (CollectionUtils.isEmpty(dormitoryInfoList)) {
      return Result.success(new ArrayList<>());
    }
    List<Long> dormitoryIds = getDormitoryIds(dormitoryInfoList);
    List<DormitoryUser> dormitoryUserList = dormitoryUserService.listByDormitoryIds(dormitoryIds);
    List<CheckDormitory> checkDormitoryList = checkDormitoryService.list(dormitoryIds, 1L);//TODO 日期
    Map<Long, List<Long>> dormitoryToUserIdsMap = getDormitoryToUserIdsMap(dormitoryUserList);
    List<Long> studentIds = getStudentIds(dormitoryUserList);
    List<StudentClock> studentClockList = studentClockService.list(studentIds, 1L); //TODO 日期确定 分组查，考虑学生数量过多
    Map<Long, StudentClock> studentIdToStatusMap = getStudentIdToStatusMap(studentClockList);
    Set<Long> sormitoryIdSet = getDormitoryIdSet(checkDormitoryList);
    List<DormitoryClockStatDTO> dormitoryClockStatDTOList = new ArrayList<>();
    for (DormitoryInfo dormitoryInfo : dormitoryInfoList) {
      DormitoryClockStatDTO dto = new DormitoryClockStatDTO();
      dto.setBuildingId(dormitoryInfo.getBuildingId());
      //TODO dto.setBuildingName();
      dto.setDormitoryId(dormitoryInfo.getDormitoryId());
      dto.setDormitoryName(dormitoryInfo.getName());
      dormitoryClockStatDTOList.add(dto);
      // TODO 其他的統計工作
    }
    return Result.success(dormitoryClockStatDTOList);
  }

  @Override
  public Result<DormitoryClockDetailStatDTO> getDormitoryClockDetailStatForApp(Long userId, Long dormitoryId) {
    //TODO 参考上面的
    List<Long> dormitoryIds = new ArrayList<>();
    dormitoryIds.add(dormitoryId);
    List<DormitoryUser> dormitoryUserList = dormitoryUserService.listByDormitoryIds(dormitoryIds);
    List<CheckDormitory> checkDormitoryList = checkDormitoryService.list(dormitoryIds, 1L);//TODO 日期
    Map<Long, List<Long>> dormitoryToUserIdsMap = getDormitoryToUserIdsMap(dormitoryUserList);
    List<Long> studentIds = getStudentIds(dormitoryUserList);
    List<StudentClock> studentClockList = studentClockService.list(studentIds, 1L); //TODO 日期确定 分组查，考虑学生数量过多
    Map<Long, StudentClock> studentIdToStatusMap = getStudentIdToStatusMap(studentClockList);
    Set<Long> sormitoryIdSet = getDormitoryIdSet(checkDormitoryList);

    DormitoryClockDetailStatDTO dto = new DormitoryClockDetailStatDTO();
    //dto.setBuildingId();


    return null;
  }


  @Override
  public Result<DormitoryCheckDayStatRspDTO> dayStat(Long userId, Integer year, Integer month, Integer day) {
    int totalCount = 0;
    Map<String, Object> queryMap = new HashMap<>();
    byte roleType = getRoleTypeByUserId(userId);
    List<Long> instructorIds = classCacheService.getInstructorIds();

    queryMap.put("clockDate", DateUtil.ymdToint(year, month, day));
    if (instructorIds != null && instructorIds.contains(userId)) {
      //总人数
      totalCount = studentInfoService.countClockStudentByInstructorId(userId);
      queryMap.put("instructorId", userId);

    } else if (RoleType.DormitoryAdmin.getType() == roleType) {
      List<UserBuildingRef> userBuildingRefList = userBuildingService.listByUserId(userId);
      List<Long> buildingIds = getBuildingIds(userBuildingRefList);
      totalCount = studentInfoService.countClockStudentByBuildingIds(buildingIds);
      queryMap.put("buildingIds", buildingIds);

    } else if (RoleType.StudentsAffairsAdmin.getType() == roleType) {
      totalCount = studentInfoService.countAllClockStudent();
    }
    List<ClockStatByStatusDO> clockStatByStatusDOList = studentClockService.statByStatus(queryMap);
    Map<Byte, Integer> statusCountMap = getStatusCountMap(clockStatByStatusDOList);

    DormitoryCheckDayStatRspDTO dto = new DormitoryCheckDayStatRspDTO();
    dto.setTotalNum(totalCount);
    dto.setClockNum(statusCountMap.get(ClockStatus.CLOCK.getType()) != null ? statusCountMap.get(ClockStatus.CLOCK.getType()) : 0);
    dto.setStayOutNum(statusCountMap.get(ClockStatus.STAYOUT.getType()) != null ? statusCountMap.get(ClockStatus.STAYOUT.getType()) : 0);
    dto.setStayOutLateNum(statusCountMap.get(ClockStatus.STAYOUT_LATE.getType()) != null ? statusCountMap.get(ClockStatus.STAYOUT_LATE.getType()) : 0);

    return Result.success(dto);
  }

  @Override
  public Result<List<DormitoryCheckDayStatListRspDTO>> dayStatStudentList(Long userId, Integer year, Integer month, Integer day, Byte clockStatus) {
    byte roleType = getRoleTypeByUserId(userId);
    List<Long> instructorIds = classCacheService.getInstructorIds();
    List<Long> studentIds = null;
    if (instructorIds != null && instructorIds.contains(userId)) {
      //总人数
      studentIds = studentInfoService.listClockStudentByInstructorId(userId);

    } else if (RoleType.DormitoryAdmin.getType() == roleType) {
      List<UserBuildingRef> userBuildingRefList = userBuildingService.listByUserId(userId);
      List<Long> buildingIds = getBuildingIds(userBuildingRefList);
      studentIds = studentInfoService.listClockStudentByBuildingIds(buildingIds);

    } else if (RoleType.StudentsAffairsAdmin.getType() == roleType) {
    } else {
      return Result.success(Collections.emptyList());
    }
    if (RoleType.StudentsAffairsAdmin.getType() != roleType && CollectionUtils.isEmpty(studentIds)) {
      return Result.success(Collections.emptyList());
    }
    List<Long> resultIds =
        studentClockService.listStudentIdsByIdsAndStatusAndDate(studentIds, DateUtil.ymdToint(year, month, day), clockStatus);
    List<User> userList = userService.selectByPrimaryKeyList(resultIds);
    List<DormitoryCheckDayStatListRspDTO> dormitoryCheckDayStatListRspDTOS = new ArrayList<>();
    if (!CollectionUtils.isEmpty(userList)) {
      for (User user : userList) {
        DormitoryCheckDayStatListRspDTO dto = new DormitoryCheckDayStatListRspDTO();
        dto.setStudentId(user.getUserId());
        dto.setStudentName(user.getUserName());
        dormitoryCheckDayStatListRspDTOS.add(dto);
      }
    }

    return Result.success(dormitoryCheckDayStatListRspDTOS);
  }

  private Map<Byte, Integer> getStatusCountMap(List<ClockStatByStatusDO> clockStatByStatusDOList) {
    Map<Byte, Integer> statusCountMap = new HashMap<>();
    if (!CollectionUtils.isEmpty(clockStatByStatusDOList)) {
      for (ClockStatByStatusDO d : clockStatByStatusDOList) {
        statusCountMap.put(d.getClockStatus(), d.getStatCount());
      }
    }
    return statusCountMap;
  }

  @Override
  public Result<DormitoryCheckWeekStatRspDTO> weekStat(Long userId, Integer weekNumber) {
    TermConfig termConfig = termConfigService.getCurrTermConfig();
    if (termConfig == null) {
      logger.warn("不在学期内");
      return Result.success();
    }
    Date startDate = termConfig.getStartDate();
    Date endDate = termConfig.getEndDate();
    WeekInfoRspDTO weekInfoRspDTO = BusinessUtil.getWeek(startDate, endDate, weekNumber);
    if (weekInfoRspDTO == null) {
      logger.warn("周数不存在");
      return Result.success();
    }
    byte roleType = getRoleTypeByUserId(userId);
    List<Long> instructorIds = classCacheService.getInstructorIds();

    int totalCount = 0;
    Map<String, Object> queryMap = new HashMap<>();
    Date startStatDate = DateUtil.add(weekInfoRspDTO.getStartDate(), Calendar.DAY_OF_YEAR, -1);
    Date endStatDate = DateUtil.add(weekInfoRspDTO.getEndDate(), Calendar.DAY_OF_YEAR, -1);

    List<ClockDaySetting> clockDaySettingList = clockDaySettingService.list(startStatDate, endStatDate);
    if (CollectionUtils.isEmpty(clockDaySettingList)) {
      logger.warn("本周不需要打卡");
      return Result.success();
    }

    queryMap.put("startClockDate", startStatDate);
    queryMap.put("endClockDate", endStatDate);
    if (instructorIds != null && instructorIds.contains(userId)) {
      //总人数
      totalCount = studentInfoService.countClockStudentByInstructorId(userId);
      queryMap.put("instructorId", userId);

    } else if (RoleType.DormitoryAdmin.getType() == roleType) {
      List<UserBuildingRef> userBuildingRefList = userBuildingService.listByUserId(userId);
      List<Long> buildingIds = getBuildingIds(userBuildingRefList);
      totalCount = studentInfoService.countClockStudentByBuildingIds(buildingIds);
      queryMap.put("buildingIds", buildingIds);

    } else if (RoleType.StudentsAffairsAdmin.getType() == roleType) {
      totalCount = studentInfoService.countAllClockStudent();
    }
    List<ClockStatByStatusDO> clockStatByStatusDOList = studentClockService.statByStatus(queryMap);
    DormitoryCheckWeekStatRspDTO dto = new DormitoryCheckWeekStatRspDTO();
    dto.setTotalNum(totalCount * clockDaySettingList.size());
    Map<Byte, Integer> statusCountMap = getStatusCountMap(clockStatByStatusDOList);
    dto.setStayOutNum(statusCountMap.get(ClockStatus.STAYOUT.getType()) != null ? statusCountMap.get(ClockStatus.STAYOUT.getType()) : 0);
    dto.setStayOutLateNum(statusCountMap.get(ClockStatus.STAYOUT_LATE.getType()) != null ? statusCountMap.get(ClockStatus.STAYOUT_LATE.getType()) : 0);
    return Result.success(dto);
  }

  @Override
  public Result<List<DormitoryCheckWeekStatListRspDTO>> weekStatStudentList(Long userId, Integer weekNumber, Byte clockStatus) {
    TermConfig termConfig = termConfigService.getCurrTermConfig();
    if (termConfig == null) {
      logger.warn("不在学期内");
      return Result.success(Collections.emptyList());
    }
    Date startDate = termConfig.getStartDate();
    Date endDate = termConfig.getEndDate();
    WeekInfoRspDTO weekInfoRspDTO = BusinessUtil.getWeek(startDate, endDate, weekNumber);
    if (weekInfoRspDTO == null) {
      logger.warn("周数不存在");
      return Result.success(Collections.emptyList());
    }
    byte roleType = getRoleTypeByUserId(userId);
    List<Long> instructorIds = classCacheService.getInstructorIds();

    List<Long> studentIds = null;
    Date startStatDate = DateUtil.add(weekInfoRspDTO.getStartDate(), Calendar.DAY_OF_YEAR, -1);
    Date endStatDate = DateUtil.add(weekInfoRspDTO.getEndDate(), Calendar.DAY_OF_YEAR, -1);

    List<ClockDaySetting> clockDaySettingList = clockDaySettingService.list(startStatDate, endStatDate);
    if (CollectionUtils.isEmpty(clockDaySettingList)) {
      logger.warn("本周不需要打卡");
      return Result.success(Collections.emptyList());
    }

    if (instructorIds != null && instructorIds.contains(userId)) {
      //总人数
      studentIds = studentInfoService.listClockStudentByInstructorId(userId);
    } else if (RoleType.DormitoryAdmin.getType() == roleType) {
      List<UserBuildingRef> userBuildingRefList = userBuildingService.listByUserId(userId);
      List<Long> buildingIds = getBuildingIds(userBuildingRefList);
      studentIds = studentInfoService.listClockStudentByBuildingIds(buildingIds);
    } else if (RoleType.StudentsAffairsAdmin.getType() == roleType) {
    } else {
      return Result.success(Collections.emptyList());
    }
    if (RoleType.StudentsAffairsAdmin.getType() != roleType && CollectionUtils.isEmpty(studentIds)) {
      return Result.success(Collections.emptyList());
    }

    List<User> userList = userService.selectByPrimaryKeyList(studentIds);
    Map<Long, User> userMap = BusinessUtil.getUserMap(userList);
    List<StudentClockStatusCountStatDO> studentClockStatusCountStatDOS =
        studentClockService.listStudentClockStatusCountStat(studentIds, startStatDate, endStatDate, clockStatus);

    List<DormitoryCheckWeekStatListRspDTO> dormitoryCheckWeekStatListRspDTOS = new ArrayList<>();
    if (!CollectionUtils.isEmpty(studentClockStatusCountStatDOS)) {
      for (StudentClockStatusCountStatDO d : studentClockStatusCountStatDOS) {
        DormitoryCheckWeekStatListRspDTO dto = new DormitoryCheckWeekStatListRspDTO();
        dto.setStudentId(d.getStudentId());
        dto.setCount(d.getStatCount());
        User user = userMap.get(d.getStudentId());
        if (user != null) {
          dto.setStudentName(user.getUserName());
        }
        dormitoryCheckWeekStatListRspDTOS.add(dto);
      }
    }

    return Result.success(dormitoryCheckWeekStatListRspDTOS);
  }

  @Override
  public Result addDormitoryCheck(DormitoryCheckOverReqDTO reqDTO) {
    CheckDormitory checkDormitory = new CheckDormitory();
    checkDormitory.setDormitoryId(reqDTO.getDormitoryId());
    checkDormitory.setOperatorName(reqDTO.getOperatorName());
    checkDormitory.setOperateDate(new Date());
    checkDormitory.setStatDate(DateUtil.currHhmmssToLong());
    checkDormitoryService.insert(checkDormitory);
    return Result.success();
  }

  @Override
  public Result<List<StudentDormitoryRsqDTO>> queryStudent(Long userId, String nameOrCode) {
    byte roleType = getRoleTypeByUserId(userId);
    List<Long> instructorIds = classCacheService.getInstructorIds();
    List<Long> studentIds = null;
    if (instructorIds != null && instructorIds.contains(userId)) {
      //总人数
      studentIds = studentInfoService.listStudentIdsByInstructorIdAndNOC(userId, nameOrCode);

    } else if (RoleType.DormitoryAdmin.getType() == roleType) {
      List<UserBuildingRef> userBuildingRefList = userBuildingService.listByUserId(userId);
      List<Long> buildingIds = getBuildingIds(userBuildingRefList);
      studentIds = studentInfoService.listClockStudentByBuildingIdsAndNOC(buildingIds, nameOrCode);

    } else if (RoleType.StudentsAffairsAdmin.getType() == roleType) {
      studentIds = studentInfoService.listClockStudentByNOC(nameOrCode);
    } else {
      return Result.success(Collections.emptyList());
    }
    if (CollectionUtils.isEmpty(studentIds)) {
      return Result.success(Collections.emptyList());
    }

    List<User> userList = userService.selectByPrimaryKeyList(studentIds);
    List<StudentDormitoryRsqDTO> studentDormitoryRsqDTOS = new ArrayList<>();
    if (!CollectionUtils.isEmpty(userList)) {
      for (User user : userList) {
        StudentDormitoryRsqDTO dto = new StudentDormitoryRsqDTO();
        dto.setStudentId(user.getUserId());
        dto.setName(user.getUserName());
        dto.setCode(user.getCode());
        studentDormitoryRsqDTOS.add(dto);
      }
    }
    return Result.success(studentDormitoryRsqDTOS);
  }

  private List<Long> getBuildingIds(List<UserBuildingRef> userBuildingRefList) {
    List<Long> buildingIds = new ArrayList<>();
    if (!CollectionUtils.isEmpty(userBuildingRefList)) {
      for (UserBuildingRef userBuildingRef : userBuildingRefList) {
        buildingIds.add(userBuildingRef.getBuildingId());
      }
    }
    return buildingIds;
  }

  private Set<Long> getDormitoryIdSet(List<CheckDormitory> checkDormitoryList) {
    Set<Long> sormitoryIdSet = new HashSet<>();
    if (!CollectionUtils.isEmpty(checkDormitoryList)) {
      for (CheckDormitory checkDormitory : checkDormitoryList) {
        sormitoryIdSet.add(checkDormitory.getDormitoryId());
      }
    }
    return sormitoryIdSet;
  }

  private Map<Long, StudentClock> getStudentIdToStatusMap(List<StudentClock> studentClockList) {
    Map<Long, StudentClock> studentIdToStatusMap = new HashMap<>();
    if (CollectionUtils.isEmpty(studentClockList)) {
      for (StudentClock studentClock : studentClockList) {
        studentIdToStatusMap.put(studentClock.getUserId(), studentClock);
      }
    }
    return studentIdToStatusMap;
  }

  private List<Long> getStudentIds(List<DormitoryUser> dormitoryUserList) {
    List<Long> studentIds = new ArrayList<>();
    if (!CollectionUtils.isEmpty(dormitoryUserList)) {
      for (DormitoryUser dormitoryUser : dormitoryUserList) {
        studentIds.add(dormitoryUser.getUserId());
      }
    }
    return studentIds;
  }

  private Map<Long, List<Long>> getDormitoryToUserIdsMap(List<DormitoryUser> dormitoryUserList) {
    Map<Long, List<Long>> dormitoryToUserIdsMap = new HashMap<>();
    if (!CollectionUtils.isEmpty(dormitoryUserList)) {
      for (DormitoryUser dormitoryUser : dormitoryUserList) {
        List<Long> userIds = dormitoryToUserIdsMap.get(dormitoryUser.getDormitoryId());
        if (userIds == null) {
          userIds = new ArrayList<>();
        }
        userIds.add(dormitoryUser.getUserId());
        dormitoryToUserIdsMap.put(dormitoryUser.getDormitoryId(), userIds);
      }
    }
    return dormitoryToUserIdsMap;
  }

  /**
   * 从dormitoryInfoList获取DormitoryIds
   *
   * @param dormitoryInfoList :
   * @return : java.util.List<java.lang.Long>
   */
  private List<Long> getDormitoryIds(List<DormitoryInfo> dormitoryInfoList) {
    List<Long> dormitoryIds = new ArrayList<>();
    if (!CollectionUtils.isEmpty(dormitoryInfoList)) {
      for (DormitoryInfo dormitoryInfo : dormitoryInfoList) {
        dormitoryIds.add(dormitoryInfo.getDormitoryId());
      }
    }
    return dormitoryIds;
  }

  private BuildingQueryRspDTO convertToDormitoryBuildingQueryRspDTO(BuildingInfo buildingInfo) {
    BuildingQueryRspDTO dto = new BuildingQueryRspDTO();
    dto.setBuildingId(buildingInfo.getBuildingId());
    dto.setBuildingName(buildingInfo.getName());
    dto.setFloorNumber(buildingInfo.getTotalFloor());
    return dto;
  }


  private DormitorySimpleRspDTO convertToDormitoryQueryRspDTO(DormitoryInfo dormitoryInfo) {
    DormitorySimpleRspDTO dto = new DormitorySimpleRspDTO();
    dto.setDormitoryId(dormitoryInfo.getDormitoryId());
    dto.setDormitoryName(dormitoryInfo.getName());
    return dto;
  }

  /**
   * 获取宿舍列表
   *
   * @param userId      :
   * @param buildingId  :
   * @param floorNumber :
   * @return : java.util.List<com.yunhuakeji.attendance.dao.basedao.model.DormitoryInfo>
   */
  private List<DormitoryInfo> getDormitoryInfo(Long userId, Long buildingId, Integer floorNumber, Long dormitoryId) {
    byte roleType = getRoleTypeByUserId(userId);
    List<Long> instructorIds = classCacheService.getInstructorIds();

    List<DormitoryInfo> dormitoryInfoList = null;
    if (instructorIds != null && instructorIds.contains(userId)) {
      dormitoryInfoList = dormitoryInfoService.listDormitoryByInstructorId(userId);
      if (!CollectionUtils.isEmpty(dormitoryInfoList)) {
        Iterator<DormitoryInfo> dormitoryInfoIterable = dormitoryInfoList.iterator();
        while (dormitoryInfoIterable.hasNext()) {
          DormitoryInfo dormitoryInfo = dormitoryInfoIterable.next();
          if (!dormitoryInfo.getBuildingId().equals(buildingId)) {
            dormitoryInfoIterable.remove();
          }
          if (floorNumber != null && dormitoryInfo.getFloorNumber().intValue() != floorNumber) {
            dormitoryInfoIterable.remove();
          }
        }
      }
    } else if (RoleType.DormitoryAdmin.getType() == roleType || RoleType.StudentsAffairsAdmin.getType() == roleType) {
      dormitoryInfoList = dormitoryInfoService.list(buildingId, floorNumber);
    }
    if (!CollectionUtils.isEmpty(dormitoryInfoList) && dormitoryId != null) {
      Iterator<DormitoryInfo> dormitoryInfoIterable = dormitoryInfoList.iterator();
      while (dormitoryInfoIterable.hasNext()) {
        DormitoryInfo dormitoryInfo = dormitoryInfoIterable.next();
        if (!dormitoryInfo.getDormitoryId().equals(dormitoryId)) {
          dormitoryInfoIterable.remove();
        }
      }
    }
    return dormitoryInfoList;
  }

}
