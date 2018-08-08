package com.yunhuakeji.attendance.biz.impl;

import com.github.pagehelper.PageInfo;
import com.yunhuakeji.attendance.biz.UserRoleManageBiz;
import com.yunhuakeji.attendance.cache.BuildingCacheService;
import com.yunhuakeji.attendance.cache.ClassCacheService;
import com.yunhuakeji.attendance.cache.DormitoryCacheService;
import com.yunhuakeji.attendance.cache.MajorCacheService;
import com.yunhuakeji.attendance.cache.OrgCacheService;
import com.yunhuakeji.attendance.constants.Page;
import com.yunhuakeji.attendance.constants.PagedResult;
import com.yunhuakeji.attendance.constants.Result;
import com.yunhuakeji.attendance.dao.basedao.model.*;
import com.yunhuakeji.attendance.dao.bizdao.model.AccountBaseInfoDO;
import com.yunhuakeji.attendance.dao.bizdao.model.UserBuildingRef;
import com.yunhuakeji.attendance.dao.bizdao.model.UserOrgRef;
import com.yunhuakeji.attendance.dto.request.ClearFrequentlyUsedPhoneReqDTO;
import com.yunhuakeji.attendance.dto.response.*;
import com.yunhuakeji.attendance.service.baseservice.DormitoryUserService;
import com.yunhuakeji.attendance.service.baseservice.UserClassService;
import com.yunhuakeji.attendance.service.baseservice.UserService;
import com.yunhuakeji.attendance.service.bizservice.AccountService;
import com.yunhuakeji.attendance.service.bizservice.StudentDeviceRefService;

import com.yunhuakeji.attendance.service.bizservice.UserBuildingService;
import com.yunhuakeji.attendance.service.bizservice.UserOrgRefService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserRoleManageBizImpl implements UserRoleManageBiz {

    @Autowired
    private UserService userService;

    @Autowired
    private DormitoryUserService dormitoryUserService;

    @Autowired
    private DormitoryCacheService dormitoryCacheService;

    @Autowired
    private BuildingCacheService buildingCacheService;

    @Autowired
    private UserClassService userClassService;

    @Autowired
    private ClassCacheService classCacheService;

    @Autowired
    private MajorCacheService majorCacheService;

    @Autowired
    private OrgCacheService orgCacheService;

    @Autowired
    private StudentDeviceRefService studentDeviceRefService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserOrgRefService userOrgRefService;

    @Autowired
    private UserBuildingService userBuildingService;

    @Override
    public PagedResult<StudentBaseInfoDTO> studentPageQuery(String name, String code, Integer pageNo, Integer pageSize) {

        PageInfo pageInfo = userService.getStudentForPage(name, code, pageNo, pageSize);
        List<User> userList = pageInfo.getList();

        List<StudentBaseInfoDTO> studentBaseInfoDTOList = new ArrayList<>();
        List<Long> instructorIds = new ArrayList<>();
        if (!CollectionUtils.isEmpty(userList)) {
            //获取批量UserIds
            List<Long> userIds = getUserIds(userList);

            List<DormitoryUser> dormitoryUserList = dormitoryUserService.listByUserIds(userIds);
            Map<Long, DormitoryUser> userToDormitoryMap = getUserToDormitoryMap(dormitoryUserList);
            Map<Long, DormitoryInfo> dormitoryInfoMap = dormitoryCacheService.getDormitoryMap();
            Map<Long, BuildingInfo> buildingInfoMap = buildingCacheService.getBuildingInfoMap();
            List<UserClass> userClassList = userClassService.listByUserIds(userIds);
            Map<Long, Long> userClassMap = getUserClassMap(userClassList);
            Map<Long, ClassInfo> classInfoMap = classCacheService.getClassInfoMap();
            Map<Long, MajorInfo> majorInfoMap = majorCacheService.getMajorInfoMap();
            Map<Long, CollegeInfo> collegeInfoMap = orgCacheService.getCollegeInfoMap();
            //dormitoryInfoService.
            for (User user : userList) {
                StudentBaseInfoDTO dto = new StudentBaseInfoDTO();
                dto.setStudentCode(user.getCode());
                DormitoryUser dormitoryUser = userToDormitoryMap.get(user.getUserId());
                if (dormitoryUser != null) {
                    dto.setDormitoryId(dormitoryUser.getDormitoryId());
                    dto.setBedCode(dormitoryUser.getBedCode());
                    DormitoryInfo dormitoryInfo = dormitoryInfoMap.get(dormitoryUser.getDormitoryId());
                    if (dormitoryInfo != null) {
                        dto.setDormitoryName(dormitoryInfo.getName());
                        dto.setBuildingId(dormitoryInfo.getBuildingId());
                        BuildingInfo buildingInfo = buildingInfoMap.get(dormitoryInfo.getBuildingId());
                        if (buildingInfo != null) {
                            dto.setBuildingName(buildingInfo.getName());
                        }
                    }
                }
                dto.setClassId(userClassMap.get(user.getUserId()));
                ClassInfo classInfo = classInfoMap.get(userClassMap.get(user.getUserId()));
                if (classInfo != null) {
                    dto.setClassName(classInfo.getClassCode());
                    dto.setInstructorId(classInfo.getInstructorId());
                    instructorIds.add(classInfo.getInstructorId());
                    dto.setMajorId(classInfo.getMajorId());
                    MajorInfo majorInfo = majorInfoMap.get(classInfo.getMajorId());
                    if (majorInfo != null) {
                        dto.setMajorName(majorInfo.getName());
                        dto.setCollegeId(majorInfo.getOrgId());
                        CollegeInfo collegeInfo = collegeInfoMap.get(majorInfo.getOrgId());
                        if (collegeInfo != null) {
                            dto.setCollegeName(collegeInfo.getName());
                        }
                    }
                }
                dto.setProfilePhoto(user.getHeadPortraitPath());
                dto.setStudentId(user.getUserId());
                dto.setStudentName(user.getUserName());
                studentBaseInfoDTOList.add(dto);
            }
        }

        if (!CollectionUtils.isEmpty(instructorIds) && !CollectionUtils.isEmpty(studentBaseInfoDTOList)) {
            List<User> instructorList = userService.selectByPrimaryKeyList(instructorIds);
            Map<Long, User> instructorMap = getInstructorMap(instructorList);
            for (StudentBaseInfoDTO dto : studentBaseInfoDTOList) {
                User user = instructorMap.get(dto.getInstructorId());
                dto.setInstructorName(user.getUserName());
            }
        }

        //3.组装返回结果
        Page<StudentBaseInfoDTO> studentBaseInfoDTOPage = new Page<>();
        studentBaseInfoDTOPage.setResult(studentBaseInfoDTOList);
        studentBaseInfoDTOPage.setTotalCount((int) pageInfo.getTotal());
        studentBaseInfoDTOPage.setPageSize(pageSize);
        studentBaseInfoDTOPage.setPageNo(pageNo);
        studentBaseInfoDTOPage.setTotalPages(pageInfo.getPages());

        return PagedResult.success(studentBaseInfoDTOPage);
    }

    @Override
    public Result clearFrequentlyUsedPhone(ClearFrequentlyUsedPhoneReqDTO reqDTO) {
        studentDeviceRefService.deleteByStudentIds(reqDTO.getStudentIds());
        return Result.success(null);
    }

    @Override
    public PagedResult<InstructorManageQueryDTO> instructorPageQuery(String name, String code, Integer pageNo, Integer pageSize) {

        PageInfo pageInfo = userClassService.listInstructorInfo(name, code, pageNo, pageSize);
        List<InstructorInfo> instructorInfoList = pageInfo.getList();

        List<InstructorManageQueryDTO> instructorManageQueryDTOList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(instructorInfoList)) {
            Map<Long, ClassInfo> classInfoMap = classCacheService.getClassInfoMap();
            Map<Long, MajorInfo> majorInfoMap = majorCacheService.getMajorInfoMap();
            Map<Long, CollegeInfo> collegeInfoMap = orgCacheService.getCollegeInfoMap();
            for (InstructorInfo instructorInfo : instructorInfoList) {
                InstructorManageQueryDTO dto = new InstructorManageQueryDTO();
                dto.setUserId(instructorInfo.getUserId());
                dto.setName(instructorInfo.getName());
                dto.setCode(instructorInfo.getCode());
                ClassInfo classInfo = classInfoMap.get(instructorInfo.getClassId());
                if (classInfo != null) {
                    MajorInfo majorInfo = majorInfoMap.get(classInfo.getMajorId());
                    if (majorInfo != null) {
                        CollegeInfo collegeInfo = collegeInfoMap.get(majorInfo.getOrgId());
                        dto.setCollegeId(majorInfo.getOrgId());
                        if (collegeInfo != null) {
                            dto.setCollegeName(collegeInfo.getName());
                        }
                    }
                }
            }
        }

        //3.组装返回结果
        Page<InstructorManageQueryDTO> instructorManageQueryDTOPage = new Page<>();
        instructorManageQueryDTOPage.setResult(instructorManageQueryDTOList);
        instructorManageQueryDTOPage.setTotalCount((int) pageInfo.getTotal());
        instructorManageQueryDTOPage.setPageSize(pageSize);
        instructorManageQueryDTOPage.setPageNo(pageNo);
        instructorManageQueryDTOPage.setTotalPages(pageInfo.getPages());

        return PagedResult.success(instructorManageQueryDTOPage);
    }

    @Override
    public PagedResult<SecondaryCollegeAdminQueryRspDTO> secondaryCollegeAdminPage(String name, String code, Integer pageNo, Integer pageSize) {

        PageInfo pageInfo = accountService.secondaryCollegeAdminPageQuery(name, code, pageNo, pageSize);
        List<AccountBaseInfoDO> accountBaseInfoDOList = pageInfo.getList();
        List<SecondaryCollegeAdminQueryRspDTO> secondaryCollegeAdminQueryRspDTOList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(accountBaseInfoDOList)) {
            List<Long> userIds = new ArrayList<>();
            for (AccountBaseInfoDO accountBaseInfoDO : accountBaseInfoDOList) {
                SecondaryCollegeAdminQueryRspDTO dto = new SecondaryCollegeAdminQueryRspDTO();
                dto.setUserId(accountBaseInfoDO.getUserId());
                dto.setName(accountBaseInfoDO.getName());
                dto.setCode(accountBaseInfoDO.getCode());
                userIds.add(accountBaseInfoDO.getUserId());
                secondaryCollegeAdminQueryRspDTOList.add(dto);
            }
            List<UserOrgRef> userOrgRefList = userOrgRefService.listByUserIds(userIds);
            Map<Long, List<Long>> userOrgMap = getUserOrgMap(userOrgRefList);
            Map<Long, CollegeInfo> collegeInfoMap = orgCacheService.getCollegeInfoMap();
            for (SecondaryCollegeAdminQueryRspDTO dto : secondaryCollegeAdminQueryRspDTOList) {
                List<Long> orgIds = userOrgMap.get(dto.getUserId());
                if (orgIds != null) {
                    List<CollegeBaseInfoDTO> collegeBaseInfoDTOList = new ArrayList<>();
                    for (long id : orgIds) {
                        CollegeBaseInfoDTO collegeBaseInfoDTO = new CollegeBaseInfoDTO();
                        collegeBaseInfoDTO.setCollegeId(id);
                        CollegeInfo collegeInfo = collegeInfoMap.get(id);
                        if (collegeInfo != null) {
                            collegeBaseInfoDTO.setCollegeName(collegeInfo.getName());
                        }
                        collegeBaseInfoDTOList.add(collegeBaseInfoDTO);
                    }
                    dto.setCollegeBaseInfoList(collegeBaseInfoDTOList);
                }
            }

        }

        //3.组装返回结果
        Page<SecondaryCollegeAdminQueryRspDTO> secondaryCollegeAdminQueryRspDTOPage = new Page<>();
        secondaryCollegeAdminQueryRspDTOPage.setResult(secondaryCollegeAdminQueryRspDTOList);
        secondaryCollegeAdminQueryRspDTOPage.setTotalCount((int) pageInfo.getTotal());
        secondaryCollegeAdminQueryRspDTOPage.setPageSize(pageSize);
        secondaryCollegeAdminQueryRspDTOPage.setPageNo(pageNo);
        secondaryCollegeAdminQueryRspDTOPage.setTotalPages(pageInfo.getPages());

        return PagedResult.success(secondaryCollegeAdminQueryRspDTOPage);
    }

    @Override
    public PagedResult<DormitoryAdminQueryRspDTO> dormitoryAdminPage(String name, String code, Integer pageNo, Integer pageSize) {
        PageInfo pageInfo = accountService.dormitoryAdminPageQuery(name, code, pageNo, pageSize);
        List<AccountBaseInfoDO> accountBaseInfoDOList = pageInfo.getList();
        List<DormitoryAdminQueryRspDTO> dormitoryAdminQueryRspDTOList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(accountBaseInfoDOList)) {
            List<Long> userIds = new ArrayList<>();
            for (AccountBaseInfoDO accountBaseInfoDO : accountBaseInfoDOList) {
                DormitoryAdminQueryRspDTO dto = new DormitoryAdminQueryRspDTO();
                dto.setUserId(accountBaseInfoDO.getUserId());
                dto.setName(accountBaseInfoDO.getName());
                dto.setCode(accountBaseInfoDO.getCode());
                userIds.add(accountBaseInfoDO.getUserId());
                dormitoryAdminQueryRspDTOList.add(dto);
            }
            List<UserBuildingRef> userBuildingRefList = userBuildingService.listByUserIds(userIds);
            Map<Long, List<Long>> useBuildingMap = getUserBuildingMap(userBuildingRefList);
            Map<Long, BuildingInfo> buildingInfoMap = buildingCacheService.getBuildingInfoMap();
            for (DormitoryAdminQueryRspDTO dto : dormitoryAdminQueryRspDTOList) {
                List<Long> buildingIds = useBuildingMap.get(dto.getUserId());
                if (!CollectionUtils.isEmpty(buildingIds)) {
                    List<BuildingBaseInfoDTO> buildingBaseInfoDTOS = new ArrayList<>();
                    for (long id : buildingIds) {
                        BuildingBaseInfoDTO buildingBaseInfoDTO = new BuildingBaseInfoDTO();
                        buildingBaseInfoDTO.setBuildingId(id);
                        BuildingInfo buildingInfo = buildingInfoMap.get(id);
                        if (buildingInfo != null) {
                            buildingBaseInfoDTO.setBuildingName(buildingInfo.getName());
                        }
                        buildingBaseInfoDTOS.add(buildingBaseInfoDTO);

                    }
                    dto.setBuildingBaseInfoDTOList(buildingBaseInfoDTOS);
                }
            }
        }

        //3.组装返回结果
        Page<DormitoryAdminQueryRspDTO> dormitoryAdminQueryRspDTOPage = new Page<>();
        dormitoryAdminQueryRspDTOPage.setResult(dormitoryAdminQueryRspDTOList);
        dormitoryAdminQueryRspDTOPage.setTotalCount((int) pageInfo.getTotal());
        dormitoryAdminQueryRspDTOPage.setPageSize(pageSize);
        dormitoryAdminQueryRspDTOPage.setPageNo(pageNo);
        dormitoryAdminQueryRspDTOPage.setTotalPages(pageInfo.getPages());

        return PagedResult.success(dormitoryAdminQueryRspDTOPage);
    }

    @Override
    public PagedResult<StudentOfficeAdminQueryRspDTO> studentOfficeAdminPage(String name, String code, Integer pageNo, Integer pageSize) {
        PageInfo pageInfo = accountService.studentOfficeAdminPageQuery(name, code, pageNo, pageSize);
        List<AccountBaseInfoDO> accountBaseInfoDOList = pageInfo.getList();
        List<StudentOfficeAdminQueryRspDTO> studentOfficeAdminQueryRspDTOList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(accountBaseInfoDOList)) {
            List<Long> userIds = new ArrayList<>();
            for (AccountBaseInfoDO accountBaseInfoDO : accountBaseInfoDOList) {
                StudentOfficeAdminQueryRspDTO dto = new StudentOfficeAdminQueryRspDTO();
                dto.setUserId(accountBaseInfoDO.getUserId());
                dto.setName(accountBaseInfoDO.getName());
                dto.setCode(accountBaseInfoDO.getCode());
                userIds.add(accountBaseInfoDO.getUserId());
                studentOfficeAdminQueryRspDTOList.add(dto);
            }
        }

        //3.组装返回结果
        Page<StudentOfficeAdminQueryRspDTO> studentOfficeAdminQueryRspDTOPage = new Page<>();
        studentOfficeAdminQueryRspDTOPage.setResult(studentOfficeAdminQueryRspDTOList);
        studentOfficeAdminQueryRspDTOPage.setTotalCount((int) pageInfo.getTotal());
        studentOfficeAdminQueryRspDTOPage.setPageSize(pageSize);
        studentOfficeAdminQueryRspDTOPage.setPageNo(pageNo);
        studentOfficeAdminQueryRspDTOPage.setTotalPages(pageInfo.getPages());

        return PagedResult.success(studentOfficeAdminQueryRspDTOPage);
    }


    private Map<Long, List<Long>> getUserBuildingMap(List<UserBuildingRef> userOrgRefList) {
        Map<Long, List<Long>> useBuildingMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(userOrgRefList)) {
            for (UserBuildingRef userBuildingRef : userOrgRefList) {
                List<Long> buildingIds = useBuildingMap.get(userBuildingRef.getUserId());
                if (buildingIds == null) {
                    buildingIds = new ArrayList<>();
                }
                buildingIds.add(userBuildingRef.getBuildingId());
                useBuildingMap.put(userBuildingRef.getUserId(), buildingIds);
            }
        }
        return useBuildingMap;
    }

    private Map<Long, List<Long>> getUserOrgMap(List<UserOrgRef> userOrgRefList) {
        Map<Long, List<Long>> userOrgMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(userOrgRefList)) {
            for (UserOrgRef userOrgRef : userOrgRefList) {
                List<Long> orgIds = userOrgMap.get(userOrgRef.getUserId());
                if (orgIds == null) {
                    orgIds = new ArrayList<>();
                }
                orgIds.add(userOrgRef.getOrgId());
                userOrgMap.put(userOrgRef.getUserId(), orgIds);
            }
        }
        return userOrgMap;
    }

    private Map<Long, User> getInstructorMap(List<User> instructorList) {
        Map<Long, User> instructorMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(instructorList)) {
            for (User user : instructorList) {
                instructorMap.put(user.getUserId(), user);
            }
        }
        return instructorMap;
    }

    private Map<Long, Long> getUserClassMap(List<UserClass> userClassList) {
        Map<Long, Long> userClassMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(userClassList)) {
            for (UserClass userClass : userClassList) {
                userClassMap.put(userClass.getUserId(), userClass.getClassId());
            }
        }
        return userClassMap;
    }

    private Map<Long, DormitoryUser> getUserToDormitoryMap(List<DormitoryUser> dormitoryUserList) {
        Map<Long, DormitoryUser> userToDormitoryMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(dormitoryUserList)) {
            for (DormitoryUser dormitoryUser : dormitoryUserList) {
                userToDormitoryMap.put(dormitoryUser.getUserId(), dormitoryUser);
            }
        }
        return userToDormitoryMap;
    }

    private List<Long> getUserIds(List<User> userList) {
        List<Long> userIds = new ArrayList<>();
        for (User user : userList) {
            userIds.add(user.getUserId());
        }
        return userIds;
    }
}
