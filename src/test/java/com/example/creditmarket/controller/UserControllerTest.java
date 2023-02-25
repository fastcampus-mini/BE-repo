package com.example.creditmarket.controller;

import com.example.creditmarket.dto.request.UserLoginRequestDTO;
import com.example.creditmarket.dto.request.UserSignUpRequestDTO;
import com.example.creditmarket.dto.response.LoginResponseDTO;
import com.example.creditmarket.entity.EntityUser;
import com.example.creditmarket.exception.AppException;
import com.example.creditmarket.exception.ErrorCode;
import com.example.creditmarket.openAPI.CrawlingOpenAPI;
import com.example.creditmarket.service.Impl.UserServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;
    @MockBean
    UserServiceImpl userServiceImpl;
    @MockBean
    SearchController searchController;
    @MockBean
    CartController cartController;
    @MockBean
    MyPageController myPageController;
    @MockBean
    ProductController productController;
    @MockBean
    CrawlingOpenAPI crawlingOpenAPI;
    @Autowired
    ObjectMapper objectMapper;


    @Test
    @DisplayName("/usersignup 회원가입 성공")
    @WithMockUser
    void signup() throws Exception{
        String userEmail = "test@Email.com";
        String userPassword = "testPassword";
        String userName ="패캠";
        String userGender = "남";
        String userBirthDate = "20010101";
        String userJob = "학생";
        String userPrefCreditProductTypeName = "마이너스한도대출";
        String userPrefInterestType = "대출금리";
        Long userCreditScore = 700L;

        mockMvc.perform(post("/usersignup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserSignUpRequestDTO(userEmail, userPassword, userName, userGender, userBirthDate, userJob, userPrefCreditProductTypeName, userPrefInterestType, userCreditScore))))
                .andDo(print())
                .andExpect(status().isOk());
    }


    @Test
    @DisplayName("/usersignup 회원가입 실패 - userName 중복")
    @WithMockUser
    void signup_fail() throws Exception{
        String userEmail = "test@Email.com";
        String userPassword = "testPassword";
        String userName ="패캠";
        String userGender = "남";
        String userBirthDate = "20010101";
        String userJob = "학생";
        String userPrefCreditProductTypeName = "마이너스한도대출";
        String userPrefInterestType = "대출금리";
        Long userCreditScore = 700L;

        when(userServiceImpl.signup(any()))
                .thenThrow(new RuntimeException("해당 userId가 중복됩니다."));

        mockMvc.perform(post("/usersignup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserSignUpRequestDTO(userEmail, userPassword,userName, userGender, userBirthDate, userJob, userPrefCreditProductTypeName, userPrefInterestType, userCreditScore))))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("/userlogin 로그인 성공")
    @WithMockUser
    void login_success() throws Exception {

        UserSignUpRequestDTO testUser = new UserSignUpRequestDTO(
                "test@Email.com",
                "testPassword",
                "패캠",
                "남",
                "20010101",
                "학생",
                "마이너스한도대출",
                "대출금리",
                500L);
        userServiceImpl.signup(testUser);

        String userEmail = "test@Email.com";
        String userPassword = "testPassword";


        when(userServiceImpl.login(any(), any()))
                .thenReturn(new LoginResponseDTO(userEmail, "token"));

        mockMvc.perform(post("/userlogin")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new UserLoginRequestDTO(userEmail, userPassword))))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("/userlogin 로그인 실패 - userName 없음")
    @WithMockUser
    void login_fail1() throws Exception {
        String userEmail = "test@Email.com";
        String userPassword = "testPassword";

        when(userServiceImpl.login(any(), any()))
                .thenThrow(new AppException(ErrorCode.USERMAIL_NOT_FOUND, ""));

        mockMvc.perform(post("/userlogin")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new UserLoginRequestDTO(userEmail, userPassword))))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("/userlogin 로그인 실패 - password 틀림")
    @WithMockUser
    void login_fail2() throws Exception {
        String userEmail = "test@Email.com";
        String userPassword = "testPassword";

        when(userServiceImpl.login(any(), any()))
                .thenThrow(new AppException(ErrorCode.INVALID_PASSWORD, ""));

        mockMvc.perform(post("/userlogin")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserLoginRequestDTO(userEmail, userPassword))))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("/userlogout 로그아웃 성공")
    @WithMockUser
    void logout_success() throws Exception {
        mockMvc.perform(post("/userlogout")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("/userlogout 로그아웃 실패 - 로그인 안됨")
    void logout_fail() throws Exception {
        mockMvc.perform(post("/userlogout")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("/userpasswordcheck 유저 비밀번호 확인")
    @WithMockUser
    void checkPassword_success() throws Exception {
        UserSignUpRequestDTO testUser = new UserSignUpRequestDTO(
                "test@Email.com",
                "testPassword",
                "패캠",
                "남",
                "20010101",
                "학생",
                "마이너스한도대출",
                "대출금리",
                500L);
        userServiceImpl.signup(testUser);

        when(userServiceImpl.passwordCheck(any(), any()))
                .thenReturn(EntityUser.builder()
                        .userEmail("test@Email.com")
                        .userPassword("testPassword")
                        .userGender("남")
                        .userBirthdate("20010101")
                        .userJob("학생")
                        .userPrefCreditProductTypeName("마이너스한도대출")
                        .userPrefInterestType("대출금리")
                        .userCreditScore(500L)
                        .build());

        String userEmail = "test@Email.com";
        String userPassword = "testPassword";
        mockMvc.perform(post("/userpasswordcheck")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserLoginRequestDTO(userEmail, userPassword))))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("/userpasswordcheck 유저 비밀번호 실패 - password 틀림")
    @WithMockUser
    void checkPassword_fail() throws Exception {
        UserSignUpRequestDTO testUser = new UserSignUpRequestDTO(
                "test@Email.com",
                "testPassword",
                "패캠",
                "남",
                "20010101",
                "학생",
                "마이너스한도대출",
                "대출금리",
                500L);
        userServiceImpl.signup(testUser);

        when(userServiceImpl.passwordCheck(any(), any()))
                .thenThrow(new AppException(ErrorCode.INVALID_PASSWORD, ""));


        String userEmail = "test@Email.com";
        String userPassword = "testPassword2";
        mockMvc.perform(post("/userpasswordcheck")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserLoginRequestDTO(userEmail, userPassword))))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }


    @Test
    @DisplayName("/userinfoupdate 회원정보 수정 성공")
    @WithMockUser
    void update_success() throws Exception {
        UserSignUpRequestDTO testUser = new UserSignUpRequestDTO(
                "test@Email.com",
                "testPassword",
                "패캠",
                "남",
                "20010101",
                "학생",
                "마이너스한도대출",
                "대출금리",
                500L);

        userServiceImpl.signup(testUser);

        String userEmail = "test@Email.com";
        String userPassword = "testPassword2";
        String userName = "패캠";
        String userGender = "남";
        String userBirthDate = "20020101";
        String userJob = "직장인";
        String userPrefCreditProductTypeName = "마이너스한도대출";
        String userPrefInterestType = "대출금리";
        Long userCreditScore = 800L;

        mockMvc.perform(post("/userinfoupdate")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserSignUpRequestDTO(userEmail, userPassword, userName, userGender, userBirthDate, userJob, userPrefCreditProductTypeName, userPrefInterestType, userCreditScore))))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("/userinfo 회원정보 요청 성공")
    @WithMockUser
    public void testGetUserInfo() {
        UserSignUpRequestDTO testUser = new UserSignUpRequestDTO(
                "test@Email.com",
                "testPassword",
                "패캠",
                "남",
                "20010101",
                "학생",
                "마이너스한도대출",
                "대출금리",
                500L);

        userServiceImpl.signup(testUser);

        String userEmail = "test@Email.com";
        String userPassword = "testPassword";
        String userName = "패캠";
        String userGender = "남";
        String userBirthDate = "20020101";
        String userJob = "직장인";
        String userPrefCreditProductTypeName = "마이너스한도대출";
        String userPrefInterestType = "대출금리";
        Long userCreditScore = 800L;

        when(userServiceImpl.getUserInfo(any()))
                .thenReturn(EntityUser.builder()
                        .userEmail(userEmail)
                        .userPassword(userPassword)
                        .userName(userName)
                        .userGender(userGender)
                        .userBirthdate(userBirthDate)
                        .userJob(userJob)
                        .userPrefCreditProductTypeName(userPrefCreditProductTypeName)
                        .userPrefInterestType(userPrefInterestType)
                        .userCreditScore(userCreditScore)
                        .build());
    }

}