package com.kuke.videomeeting.controller.user;

import com.kuke.videomeeting.advice.exception.NotResourceOwnerException;
import com.kuke.videomeeting.model.auth.CustomUserDetails;
import com.kuke.videomeeting.model.dto.response.Result;
import com.kuke.videomeeting.model.dto.user.UserSearchDto;
import com.kuke.videomeeting.model.dto.user.UserUpdateRequestDto;
import com.kuke.videomeeting.service.common.ResponseService;
import com.kuke.videomeeting.service.user.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.Objects;

@Api(value = "User Controller", tags = {"User"})
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final ResponseService responseService;
    private final UserService userService;

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "access-token", required = true, dataType = "String", paramType = "header")
    })
    @GetMapping("/users/me")
    public Result readMeByAccessToken(@ApiIgnore @AuthenticationPrincipal CustomUserDetails userDetails) {
        return responseService.getSingleResult(userService.readUser(userDetails.getId()));
    }

    @GetMapping("/users")
    public Result readAllUsers(UserSearchDto searchDto) {
        return responseService.getListResult(userService.readAllUsers(searchDto));
    }

    @GetMapping("/users/nickname/{nickname}")
    public Result readUserByNickname(@PathVariable String nickname) {
        return responseService.getSingleResult(userService.readUserByNickname(nickname));
    }

    @GetMapping("/users/{userId}")
    public Result readUser(@PathVariable Long userId) {
        return responseService.getSingleResult(userService.readUser(userId));
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "access-token", required = true, dataType = "String", paramType = "header")
    })
    @PutMapping("/users/{userId}")
    public Result updateUser(
            @ApiIgnore @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequestDto requestDto) {
        if(userDetails.getId() != null && !Objects.equals(userDetails.getId(), userId)) throw new NotResourceOwnerException();
        userService.updateUser(userId, requestDto);
        return responseService.getSuccessResult();
    }

    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "access-token", required = true, dataType = "String", paramType = "header")
    })
    @DeleteMapping("/users/{userId}")
    public Result deleteUser(
            @ApiIgnore @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long userId) {
        if(userDetails.getId() != null && !Objects.equals(userDetails.getId(), userId)) throw new NotResourceOwnerException();
        userService.deleteUser(userId);
        return responseService.getSuccessResult();
    }

}
