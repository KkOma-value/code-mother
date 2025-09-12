package com.example.codemother.controller;

import com.example.codemother.common.BaseResponse;
import com.example.codemother.common.ResultUtils;
import com.example.codemother.exception.ErrorCode;
import com.example.codemother.exception.ThrowUtils;
import com.example.codemother.model.dto.UserAdd;
import com.example.codemother.model.dto.UserLogin;
import com.example.codemother.model.entity.User;
import com.example.codemother.model.vo.LoginUserVO;
import com.mybatisflex.core.paginate.Page;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.codemother.service.UserService;
import org.springframework.web.bind.annotation.RestController;


/**
 * 用户 控制层。
 *
 * @author kkoma
 * @since 1.0
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/add")
    public BaseResponse<Long> addUser(@RequestBody UserAdd userAdd) {
        ThrowUtils.throwIf(userAdd == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userAdd.getUserAccount();
        String userPassword = userAdd.getUserPassword();
        String checkPassword = userAdd.getCheckPassword();
        long result = userService.userAdd(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }


    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLogin userLogin, HttpServletRequest request) {
        ThrowUtils.throwIf(userLogin == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userLogin.getUserAccount();
        String userPassword = userLogin.getUserPassword();
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(loginUserVO);
    }

    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(loginUser));
    }

    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }


}
