package com.KDT.mosi.web.controller;

import com.KDT.mosi.web.form.member.MemberJoinForm;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class MemberControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @LocalServerPort
  private int port;

  @Test
  @DisplayName("회원가입 폼 페이지 반환")
  void testJoinFormPage() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get("/members/join"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.view().name("member/joinForm"))
        .andDo(print());
  }

  @Test
  @DisplayName("이메일 중복 확인 API")
  void testEmailCheckApi() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get("/members/emailCheck")
            .param("email", "test@mosi.com"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.exists").isBoolean())
        .andDo(print());
  }

  @Test
  @DisplayName("회원가입 처리 요청")
  void testJoinProcess() throws Exception {
    MemberJoinForm form = new MemberJoinForm();
    form.setEmail("test999@mosi.com");
    form.setPasswd("test1234");
    form.setConfirmPasswd("test1234");
    form.setName("테스트");
    form.setNickname("테스터");

    mockMvc.perform(MockMvcRequestBuilders.post("/members/join")
            .param("email", form.getEmail())
            .param("passwd", form.getPasswd())
            .param("confirmPasswd", form.getConfirmPasswd())
            .param("name", form.getName())
            .param("nickname", form.getNickname())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED))
        .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
        .andDo(print());
  }

  // 추가적으로 editForm, edit, view 등도 통합 테스트로 확장 가능
}
