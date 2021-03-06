package com.kuke.videomeeting.controller.sign;

import com.kuke.videomeeting.advice.exception.NotRegisteredProviderException;
import com.kuke.videomeeting.config.security.JwtTokenProvider;
import com.kuke.videomeeting.model.auth.CustomUserDetails;
import com.kuke.videomeeting.model.dto.response.Result;
import com.kuke.videomeeting.model.dto.user.*;
import com.kuke.videomeeting.service.common.ResponseService;
import com.kuke.videomeeting.service.sign.SignService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Objects;


@Api(value = "Sign Controller", tags = {"Sign"})
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SignController {
    private final ResponseService responseService;
    private final SignService signService;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${cookie.domain}") private String cookieDomain;
    @Value("${cookie.secure}") private boolean cookieSecure;

    @ApiOperation(value="회원가입", notes = "회원가입을 한다.")
    @PostMapping(value = "/sign/register")
    public Result register(@Valid @RequestBody UserRegisterRequestDto requestDto) {
        signService.register(requestDto);
        return responseService.getSuccessResult();
    }

    @ApiOperation(value="로그인", notes = "로그인을 한다.")
    @PostMapping(value = "/sign/login")
    public Result login(
             @ApiIgnore HttpServletResponse response,
            @Valid @RequestBody UserLoginRequestDto requestDto) {
        UserLoginResponseDto result = signService.login(requestDto);
        response.addCookie(createTokenCookie(result.getAccessToken(), "kuke-access-token", (int) jwtTokenProvider.getTokenValidMillisecond() / 1000));
        response.addCookie(createTokenCookie(result.getRefreshToken(), "kuke-refresh-token", (int) jwtTokenProvider.getRefreshTokenValidMillisecond() / 1000));
        return responseService.getSingleResult(result);
    }

    @ApiOperation(value="소셜 로그인", notes = "소셜 로그인을 한다.")
    @PostMapping(value = "/sign/login-by-provider")
    public Result loginByProvider(
            @ApiIgnore HttpServletResponse response,
            @Valid @RequestBody UserLoginByProviderRequestDto requestDto) {
        UserLoginResponseDto result;
        if(Objects.equals(requestDto.getProvider(), "kakao")) {
            result = signService.loginByKakao(requestDto);
        } else {
            throw new NotRegisteredProviderException();
        }
        response.addCookie(createTokenCookie(result.getAccessToken(), "kuke-access-token", (int) jwtTokenProvider.getTokenValidMillisecond() / 1000));
        response.addCookie(createTokenCookie(result.getRefreshToken(), "kuke-refresh-token", (int) jwtTokenProvider.getRefreshTokenValidMillisecond() / 1000));
        return responseService.getSingleResult(result);
    }

    @ApiOperation(value="소셜 회원가입", notes = "소셜 회원가입을 한다.")
    @PostMapping(value = "/sign/register-by-provider")
    public Result registerByProvider(
            @ApiIgnore HttpServletResponse response,
            @Valid @RequestBody UserRegisterByProviderRequestDto requestDto) {
        UserLoginResponseDto result;
        if(Objects.equals(requestDto.getProvider(), "kakao")) {
            result = signService.registerByKakao(requestDto);
        } else {
            throw new NotRegisteredProviderException();
        }
        response.addCookie(createTokenCookie(result.getAccessToken(), "kuke-access-token", (int) jwtTokenProvider.getTokenValidMillisecond() / 1000));
        response.addCookie(createTokenCookie(result.getRefreshToken(), "kuke-refresh-token", (int) jwtTokenProvider.getRefreshTokenValidMillisecond() / 1000));
        return responseService.getSingleResult(result);
    }

    @ApiOperation(value="토큰 재발급", notes = "Access Token과 Refresh Token을 재발급한다.")
    @PostMapping(value = "/sign/refresh-token")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "refresh-token", required = true, dataType = "String", paramType = "header")
    })
public Result refreshToken(
            @ApiIgnore HttpServletResponse response,
            @RequestHeader(value="Authorization") String refreshToken) {
        UserLoginResponseDto result = signService.refreshToken(refreshToken);
        response.addCookie(createTokenCookie(result.getAccessToken(), "kuke-access-token", (int) jwtTokenProvider.getTokenValidMillisecond() / 1000));
        response.addCookie(createTokenCookie(result.getRefreshToken(), "kuke-refresh-token", (int) jwtTokenProvider.getRefreshTokenValidMillisecond() / 1000));
        return responseService.getSingleResult(result);
    }

    @ApiOperation(value="로그아웃", notes = "로그아웃을 한다.")
    @PostMapping(value = "/sign/logout")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "access-token", required = true, dataType = "String", paramType = "header")
    })
    public Result logout(
            @ApiIgnore HttpServletResponse response,
            @ApiIgnore @AuthenticationPrincipal CustomUserDetails userDetails) {
        signService.logout(userDetails.getId());
        response.addCookie(createTokenCookie("", "kuke-access-token", 0));
        response.addCookie(createTokenCookie("", "kuke-refresh-token", 0));
        return responseService.getSuccessResult();
    }

    @ApiOperation(value="비밀번호 변경", notes = "비밀번호를 변경한다.")
    @PutMapping(value = "/sign/change-password")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "access-token", required = true, dataType = "String", paramType = "header")
    })
    public Result changePassword(
            @ApiIgnore @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UserChangePasswordRequestDto requestDto) {
        signService.changePassword(userDetails.getId(), requestDto);
        return responseService.getSuccessResult();
    }

    @ApiOperation(value="비밀번호 분실 이메일 전송", notes = "비밀번호 분실자에게 이메일을 전송한다.")
    @PostMapping(value = "/sign/send-code-email-for-forgotten-password")
    public Result sendCodeEmailForForgottenPassword(@Valid @RequestBody UserSendEmailRequestDto requestDto) {
        signService.handleCodeEmailForForgottenPasswordUser(requestDto);
        return responseService.getSuccessResult();
    }

    @ApiOperation(value="분실한 비밀번호 변경", notes = "분실한 비밀번호를 변경한다.")
    @PutMapping(value = "/sign/change-forgotten-password")
    public Result changeForgottenPassword(@Valid @RequestBody UserChangeForgottenPasswordRequestDto requestDto) {
        signService.changeForgottenPassword(requestDto);
        return responseService.getSuccessResult();
    }

    public Cookie createTokenCookie(String token, String name, int maxAge) {
        Cookie cookie = new Cookie(name, token);
        cookie.setMaxAge(maxAge);
        cookie.setSecure(cookieSecure);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        if(cookieSecure) cookie.setDomain(cookieDomain);
        return cookie;
    }
}
