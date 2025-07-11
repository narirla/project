package com.KDT.mosi.domain.board.rbbsReport.dao;

import com.KDT.mosi.domain.entity.board.RbbsReport;

public interface RBbsReportDAO {
  // 신고 클릭
  String report(RbbsReport rbbsReport);

  // 게시글의 좋아요 갯수
  int getTotalCountReport(Long rbbsId);
}
