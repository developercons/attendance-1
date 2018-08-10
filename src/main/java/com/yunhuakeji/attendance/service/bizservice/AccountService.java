package com.yunhuakeji.attendance.service.bizservice;

import com.github.pagehelper.PageInfo;
import com.yunhuakeji.attendance.dao.bizdao.model.Account;
import com.yunhuakeji.attendance.dao.bizdao.model.AccountBaseInfoDO;

public interface AccountService {

  PageInfo<AccountBaseInfoDO> secondaryCollegeAdminPageQuery(String name, String code, int pageNo, int pageSize);

  PageInfo<AccountBaseInfoDO> dormitoryAdminPageQuery(String name, String code, int pageNo, int pageSize);

  PageInfo<AccountBaseInfoDO> studentOfficeAdminPageQuery(String name, String code, int pageNo, int pageSize);

  Account getAccountByUserId(Long userId);

  void updateAccount(Account account);
}
