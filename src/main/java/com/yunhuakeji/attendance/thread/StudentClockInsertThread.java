package com.yunhuakeji.attendance.thread;

import com.alibaba.fastjson.JSON;
import com.yunhuakeji.attendance.biz.ConvertUtil;
import com.yunhuakeji.attendance.cache.ClassCacheService;
import com.yunhuakeji.attendance.cache.DormitoryCacheService;
import com.yunhuakeji.attendance.cache.MajorCacheService;
import com.yunhuakeji.attendance.cache.StudentClockCache;
import com.yunhuakeji.attendance.constants.ConfigConstants;
import com.yunhuakeji.attendance.dao.basedao.model.ClassInfo;
import com.yunhuakeji.attendance.dao.basedao.model.DormitoryInfo;
import com.yunhuakeji.attendance.dao.basedao.model.DormitoryUser;
import com.yunhuakeji.attendance.dao.basedao.model.MajorInfo;
import com.yunhuakeji.attendance.dao.basedao.model.User;
import com.yunhuakeji.attendance.dao.basedao.model.UserClass;
import com.yunhuakeji.attendance.dao.bizdao.model.StudentClock;
import com.yunhuakeji.attendance.dao.bizdao.model.StudentClockHistory;
import com.yunhuakeji.attendance.enums.AppName;
import com.yunhuakeji.attendance.service.baseservice.DormitoryUserService;
import com.yunhuakeji.attendance.service.baseservice.UserClassService;
import com.yunhuakeji.attendance.service.baseservice.UserService;
import com.yunhuakeji.attendance.service.baseservice.impl.DormitoryUserServiceImpl;
import com.yunhuakeji.attendance.service.baseservice.impl.UserClassServiceImpl;
import com.yunhuakeji.attendance.service.baseservice.impl.UserServiceImpl;
import com.yunhuakeji.attendance.service.bizservice.StudentClockHistoryService;
import com.yunhuakeji.attendance.service.bizservice.StudentClockService;
import com.yunhuakeji.attendance.service.bizservice.impl.StudentClockHistoryServiceImpl;
import com.yunhuakeji.attendance.service.bizservice.impl.StudentClockServiceImpl;
import com.yunhuakeji.attendance.util.ApplicationUtils;
import com.yunhuakeji.attendance.util.DateUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class StudentClockInsertThread implements Runnable {

  public static final Logger logger = LoggerFactory.getLogger(StudentClockInsertThread.class);

  /**
   * 批量插入数量
   */
  private static final int BATCH_INSERT_SIZE = 200;
  /**
   * 最长等待时间
   */
  private static final int WAIT_SECONDS = 3;

  @Override
  public void run() {

    List<StudentClock> studentClockList = new ArrayList<>();
    long lastTime = System.currentTimeMillis();

    StudentClockService studentClockService = ApplicationUtils.getBean(StudentClockServiceImpl.class);
    StudentClockHistoryService studentClockHistoryService = ApplicationUtils.getBean(StudentClockHistoryServiceImpl.class);

    UserClassService userClassService = ApplicationUtils.getBean(UserClassServiceImpl.class);
    ClassCacheService classCacheService = ApplicationUtils.getBean(ClassCacheService.class);
    MajorCacheService majorCacheService = ApplicationUtils.getBean(MajorCacheService.class);
    UserService userService = ApplicationUtils.getBean(UserServiceImpl.class);
    DormitoryUserService dormitoryUserService = ApplicationUtils.getBean(DormitoryUserServiceImpl.class);
    DormitoryCacheService dormitoryCacheService = ApplicationUtils.getBean(DormitoryCacheService.class);
    try {
      while (true) {
        StudentClock studentClock = StudentClockCache.studentClockBlockingQueue.poll(500, TimeUnit.MILLISECONDS);
        if (studentClock != null) {
          studentClockList.add(studentClock);
        }
        long currTime = System.currentTimeMillis();
        if (studentClockList.size() >= BATCH_INSERT_SIZE || currTime - lastTime >= WAIT_SECONDS * 1000) {

          if (!CollectionUtils.isEmpty(studentClockList)) {
            List<Long> studentIds = ConvertUtil.getStudentIds(studentClockList);
            List<UserClass> userClassList = userClassService.listByUserIds(studentIds);
            Map<Long, Long> userClassMap = ConvertUtil.getUserClassMap(userClassList);
            Map<Long, ClassInfo> classInfoMap = classCacheService.getClassInfoMap();
            Map<Long, MajorInfo> majorInfoMap = majorCacheService.getMajorInfoMap();
            List<User> userList = userService.selectByPrimaryKeyList(studentIds);
            Map<Long, User> userMap = ConvertUtil.getUserMap(userList);
            List<DormitoryUser> dormitoryUserList = dormitoryUserService.listByUserIds(studentIds);
            Map<Long, Long> userDormitoryMap = ConvertUtil.getUserDormitoryMap(dormitoryUserList);
            Map<Long, DormitoryInfo> dormitoryInfoMap = dormitoryCacheService.getDormitoryMap();
            long startUuid = DateUtil.uuid();
            List<StudentClockHistory> studentClockHistoryList = new ArrayList<>();
            for (StudentClock clock : studentClockList) {
              long studentId = clock.getUserId();
              Date d = new Date();
              clock.setCreateTime(d);
              clock.setClockTime(d);
              if (clock.getClockDate() == null) {
                clock.setClockDate(DateUtil.getYearMonthDayByDate(d));
              }
              clock.setUpdateTime(d);
              clock.setId(startUuid++);
              if (clock.getOperatorId() == null) {
                clock.setOperatorId(studentId);
              }

              Long dormitoryId = userDormitoryMap.get(studentId);
              if (dormitoryId != null) {
                DormitoryInfo dormitoryInfo = dormitoryInfoMap.get(dormitoryId);
                if (dormitoryInfo != null) {
                  clock.setBuildingId(dormitoryInfo.getBuildingId());
                }
              }
              User user = userMap.get(studentId);
              if (clock.getAppName() == null) {
                clock.setAppName("就寝打卡");
              }

              if (user != null) {
                if (clock.getOperatorName() == null) {
                  clock.setOperatorName(user.getUserName());
                }
                clock.setGender(user.getGender());
              }
              Long classId = userClassMap.get(studentId);
              clock.setClassId(classId);
              ClassInfo classInfo = classInfoMap.get(classId);
              if (classInfo != null) {
                clock.setInstructorId(classInfo.getInstructorId());
                clock.setMajorId(classInfo.getMajorId());
                MajorInfo majorInfo = majorInfoMap.get(classInfo.getMajorId());
                if (majorInfo != null) {
                  clock.setOrgId(majorInfo.getOrgId());
                }
              }

              StudentClockHistory studentClockHistory = new StudentClockHistory();
              if (clock.getOperatorId() == null) {
                studentClockHistory.setOperatorId(ConfigConstants.ADMIN_USER_ID);
                clock.setOperatorName("管理员");
              } else {
                studentClockHistory.setOperatorId(clock.getOperatorId());
              }
              if (clock.getOperatorName() != null) {
                studentClockHistory.setOperatorName(clock.getOperatorName());
              } else {
                if (user != null) {
                  studentClockHistory.setOperatorName(user.getUserName());
                }
              }
              studentClockHistory.setUserId(studentId);
              studentClockHistory.setStatDate(DateUtil.getYearMonthDayByDate(d));
              studentClockHistory.setOperateTime(d);
              studentClockHistory.setClockStatus(clock.getClockStatus());
              studentClockHistory.setId(startUuid++);
              if (clock.getAppName() != null) {
                studentClockHistory.setAppName(clock.getAppName());
              } else {
                studentClockHistory.setAppName("就寝打卡");
              }
              studentClockHistoryList.add(studentClockHistory);
            }
            logger.info("开始批量写入数据");
            studentClockHistoryService.batchInsert(studentClockHistoryList);
            studentClockService.batchInsert(studentClockList);
            logger.info("批量写入数据完成");
            studentClockList.clear();
            lastTime = System.currentTimeMillis();
          }
        }
      }
    } catch (Exception e) {
      logger.error("插入数据异常.", e);
    }

  }


}
