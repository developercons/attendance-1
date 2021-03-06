package com.yunhuakeji.attendance.biz;

import com.yunhuakeji.attendance.constants.PagedResult;
import com.yunhuakeji.attendance.constants.Result;
import com.yunhuakeji.attendance.dto.response.*;

import java.util.Date;
import java.util.List;

public interface AnalysisBiz {

  Result<AnalysisExceptionStatByDayRsqDTO> getAnalysisExceptionStatByDay(Long orgId, Date date);

  PagedResult<AnalysisExceptionClockByDayRsqDTO> getAnalysisExceptionClockByDay(String nameOrCode,
                                                                                Long orgId,
                                                                                Long majorId,
                                                                                Long instructor,
                                                                                Byte clockStatus,
                                                                                Date date,
                                                                                String orderBy,
                                                                                String descOrAsc,Integer pageNo,Integer pageSize);

  Result<AnalysisExceptionStatByWeekRsqDTO> getAnalysisExceptionStatByWeek(
      Long orgId,
      int weekNumber
  );


  Result<List<AnalysisDayExceptionDTO>> getAnalysisExceptionStatListByWeek(
      Long orgId,
      int weekNum
  );


  PagedResult<AnalysisExceptionClockByWeekRsqDTO> getAnalysisExceptionClockByWeek(
      String nameOrCode,
      Long orgId,
      Long majorId,
      Long instructor,
      int weekNum,
      String orderBy,
      String descOrAsc,Integer pageNo,Integer pageSize
  );
}
