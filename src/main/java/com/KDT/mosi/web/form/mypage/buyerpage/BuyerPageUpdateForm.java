package com.KDT.mosi.web.form.mypage.buyerpage;

import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class BuyerPageUpdateForm {

  // 마이페이지 고유 ID (hidden으로 전달)
  private Long pageId;

  // 회원 ID (수정 불가, hidden)
  private Long memberId;

  // 새 프로필 이미지 파일 (선택 시 덮어씀)
  private MultipartFile imageFile;

  // 닉네임 (별명 등, 최대 20자 등 설정 가능)
  @Size(max = 20, message = "닉네임은 20자 이내로 입력해주세요.")
  private String nickname;

  // 자기소개 (최대 500자)
  @Size(max = 500, message = "자기소개는 500자 이내로 입력해주세요.")
  private String intro;


}
