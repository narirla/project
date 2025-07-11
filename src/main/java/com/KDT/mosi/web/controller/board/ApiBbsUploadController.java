package com.KDT.mosi.web.controller.board;

import com.KDT.mosi.domain.board.bbsUpload.svc.BbsUploadSVC;
import com.KDT.mosi.domain.entity.board.UploadResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/bbs/upload")
@RequiredArgsConstructor
public class ApiBbsUploadController {
  private final BbsUploadSVC bbsUploadSVC;

  /**
   * 본문 INLINED 이미지 목록 조회
   */
  @GetMapping("/{bbsId}/images")
  public ResponseEntity<List<UploadResult>> getInlineImages(@PathVariable("bbsId") Long bbsId) {
    List<UploadResult> images = bbsUploadSVC.findInlineByBbsIdOrderBySort(bbsId)
        .stream()
        .map(u -> new UploadResult(u.getUploadId(), u.getFilePath(),u.getUploadGroup()))
        .toList();
    return ResponseEntity.ok(images);
  }

  /**
   * 첨부파일 목록 조회
   */
  @GetMapping("/{bbsId}/attachments")
  public ResponseEntity<List<UploadResult>> getAttachments(@PathVariable("bbsId") Long bbsId) {
    List<UploadResult> attachments = bbsUploadSVC.findAttachmentsByBbsIdOrderBySort(bbsId)
        .stream()
        .map(u -> new UploadResult(u.getUploadId(), u.getFilePath(),u.getUploadGroup()))
        .toList();
    return ResponseEntity.ok(attachments);
  }

  /**
   * 본문 이미지 업로드
   */
  @PostMapping(
      value = "/images",
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE
  )
  public ResponseEntity<List<UploadResult>> uploadInline(
      @RequestParam(value="uploadGroup", required=false) Long uploadGroup,
      @RequestParam("files") List<MultipartFile> files
  ) {
    if (files == null || files.isEmpty()) return ResponseEntity.badRequest().build();
    List<UploadResult> results = bbsUploadSVC.saveAll(uploadGroup, "INLINE", files);
    return ResponseEntity.status(HttpStatus.CREATED).body(results);
  }

  /**
   * 첨부파일 업로드
   */
  @PostMapping(value = "/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<List<UploadResult>> uploadAttachments(
      @RequestParam(value="uploadGroup", required=false) Long uploadGroup,
      @RequestParam("files") List<MultipartFile> files
  ) {
    if (files == null || files.isEmpty()) return ResponseEntity.badRequest().build();
    List<UploadResult> results = bbsUploadSVC.saveAll(uploadGroup, "ATTACHMENT", files);
    return ResponseEntity.status(HttpStatus.CREATED).body(results);
  }

  /**
   * 개별 업로드 아이템 삭제
   */
  @DeleteMapping("/del/{uploadId}")
  public ResponseEntity<Void> deleteUpload(
      @PathVariable("uploadId") Long uploadId
  ) {
    bbsUploadSVC.deleteById(uploadId);
    return ResponseEntity.noContent().build();
  }

  /**
   * 게시글의 모든 업로드 삭제
   */
  @DeleteMapping("/del/all/{bbsId}")
  public ResponseEntity<Void> deleteAllUploads(@PathVariable("bbsId") Long bbsId) {
    bbsUploadSVC.deleteByBbsId(bbsId);
    return ResponseEntity.noContent().build();
  }

  /**
   * 업로드 순서 재정렬
   * 요청 바디: [{ "uploadId":1, "sortOrder":0 }, ...]
   */
  @PutMapping("/order")
  public ResponseEntity<Void> reorderUploads(
      @RequestBody List<Map<String, Integer>> orders
  ) {
    orders.forEach(item -> {
      Long uploadId = item.get("uploadId").longValue();
      int sortOrder = item.get("sortOrder");
      bbsUploadSVC.updateSortOrder(uploadId, sortOrder);
    });
    return ResponseEntity.noContent().build();
  }
}
