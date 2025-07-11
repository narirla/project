package com.KDT.mosi.domain.board.bbsReport.dao;

import com.KDT.mosi.domain.entity.board.BbsReport;

public interface BbsReportDAO {
  // 신고 클릭
  String report(BbsReport bbsReport);

  // 게시글의 좋아요 갯수
  int getTotalCountReport(Long bbsId);
}
