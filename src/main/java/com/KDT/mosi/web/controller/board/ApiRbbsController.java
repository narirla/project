package com.KDT.mosi.web.controller.board;

import com.KDT.mosi.domain.board.rbbs.svc.RbbsSVC;
import com.KDT.mosi.domain.entity.board.Rbbs;
import com.KDT.mosi.web.api.ApiResponse;
import com.KDT.mosi.web.api.ApiResponseCode;
import com.KDT.mosi.web.form.board.rbbs.SaveApi;
import com.KDT.mosi.web.form.board.rbbs.UpdateApi;
import com.KDT.mosi.web.form.login.LoginMember;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@RestController
@RequestMapping("/api/bbs/{bbsId}/comments")
@RequiredArgsConstructor
public class ApiRbbsController {
  private final RbbsSVC rbbsSVC;

  // 댓글 추가
  @PostMapping
  public ResponseEntity<ApiResponse<Rbbs>> add(
      @PathVariable("bbsId") Long bbsId,
      @RequestBody @Valid SaveApi form,
      HttpSession session
  ) {
    // 로그인 체크
    LoginMember login = (LoginMember) session.getAttribute("loginMember");
    if (login == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
    }

    Rbbs rbbs = new Rbbs();
    BeanUtils.copyProperties(form, rbbs);
    rbbs.setBbsId(bbsId);
    rbbs.setMemberId(login.getMemberId());

    Long id = rbbsSVC.save(rbbs);
    Rbbs saved = rbbsSVC.findById(id).orElseThrow();

    ApiResponse<Rbbs> resp = ApiResponse.of(ApiResponseCode.SUCCESS, saved);
    return ResponseEntity.status(HttpStatus.CREATED).body(resp);
  }

  // 댓글 목록 (페이징 없이)
  @GetMapping
  public ResponseEntity<ApiResponse<List<Rbbs>>> list(
      @PathVariable("bbsId") Long bbsId
  ) {
    List<Rbbs> list = rbbsSVC.findAll(bbsId);
    ApiResponse<List<Rbbs>> resp = ApiResponse.of(ApiResponseCode.SUCCESS, list);
    return ResponseEntity.ok(resp);
  }

  // 댓글 목록 – 페이징
  @GetMapping("/paging")
  public ResponseEntity<ApiResponse<List<Rbbs>>> listPage(
      @PathVariable("bbsId") Long bbsId,
      @RequestParam(value="pageNo",   defaultValue="1") int pageNo,
      @RequestParam(value="numOfRows",defaultValue="10") int numOfRows
  ) {
    log.info("bbsId={}, pageNo={}, numOfRows={}", bbsId, pageNo, numOfRows);
    List<Rbbs> list      = rbbsSVC.findAll(bbsId, pageNo, numOfRows);
    int         totalCnt = rbbsSVC.getTotalCount(bbsId);

    ApiResponse<List<Rbbs>> resp = ApiResponse.of(
        ApiResponseCode.SUCCESS,
        list,
        new ApiResponse.Paging(pageNo, numOfRows, totalCnt)
    );
    return ResponseEntity.ok(resp);
  }

  // 댓글 단건 조회
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<Rbbs>> get(
      @PathVariable("bbsId") Long bbsId,
      @PathVariable("id")    Long id
  ) {
    Rbbs comment = rbbsSVC.findById(id)
        .filter(r -> r.getBbsId().equals(bbsId))
        .orElseThrow(() -> new NoSuchElementException("댓글을 찾을 수 없습니다. id=" + id));

    ApiResponse<Rbbs> resp = ApiResponse.of(ApiResponseCode.SUCCESS, comment);
    return ResponseEntity.ok(resp);
  }

  // 댓글 수정
  @PatchMapping("/{id}")
  public ResponseEntity<ApiResponse<Rbbs>> update(
      @PathVariable("bbsId") Long bbsId,
      @PathVariable("id")    Long id,
      @RequestBody @Valid UpdateApi form,
      HttpSession session
  ) {
    // 1) 원본 댓글 조회
    Rbbs orig = rbbsSVC.findById(id)
        .filter(r -> r.getBbsId().equals(bbsId))
        .orElseThrow(() -> new NoSuchElementException("댓글을 찾을 수 없습니다. id=" + id));

    // 2) 로그인 & 작성자 확인
    LoginMember login = (LoginMember) session.getAttribute("loginMember");
    if (login == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
    }
    if (!orig.getMemberId().equals(login.getMemberId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "작성자만 수정할 수 있습니다.");
    }

    // 3) 수정 실행
    Rbbs update = new Rbbs();
    BeanUtils.copyProperties(form, update);
    rbbsSVC.updateById(id, update);

    // 4) 결과 조회 및 반환
    Rbbs updated = rbbsSVC.findById(id).orElseThrow();
    ApiResponse<Rbbs> resp = ApiResponse.of(ApiResponseCode.SUCCESS, updated);
    return ResponseEntity.ok(resp);
  }

  // 댓글 삭제 (status 코드만 변경)
  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Rbbs>> delete(
      @PathVariable("bbsId") Long bbsId,
      @PathVariable("id")    Long id,
      HttpSession session
  ) {
    // 1) 원본 댓글 조회
    Rbbs orig = rbbsSVC.findById(id)
        .filter(r -> r.getBbsId().equals(bbsId))
        .orElseThrow(() -> new NoSuchElementException("댓글을 찾을 수 없습니다. id=" + id));

    // 2) 로그인 & 작성자 확인
    LoginMember login = (LoginMember) session.getAttribute("loginMember");
    if (login == null) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
    }
    if (!orig.getMemberId().equals(login.getMemberId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "작성자만 삭제할 수 있습니다.");
    }

    // 3) 삭제(status 변경)
    rbbsSVC.deleteById(id);

    ApiResponse<Rbbs> resp = ApiResponse.of(ApiResponseCode.SUCCESS, orig);
    return ResponseEntity.ok(resp);
  }

  @GetMapping("/totCnt")
  public ResponseEntity<ApiResponse<Integer>> totalCount(@PathVariable("bbsId") Long bbsId) {

    int totalCount = rbbsSVC.getTotalCount(bbsId);
    ApiResponse<Integer> bbsApiResponse = ApiResponse.of(ApiResponseCode.SUCCESS, totalCount);

    return ResponseEntity.ok(bbsApiResponse);  //상태코드 200, 응답메세지Body:bbsApiResponse객채가 json포맷 문자열로 변환됨
  }





}

