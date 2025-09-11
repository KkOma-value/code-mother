package com.example.codemother.controller;

import com.example.codemother.common.BaseResponse;
import com.example.codemother.common.ResultUtils;
import com.example.codemother.exception.ErrorCode;
import com.example.codemother.exception.ThrowUtils;
import com.example.codemother.model.dto.UserLogging;
import com.mybatisflex.core.paginate.Page;
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

import java.util.List;

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
    public BaseResponse addUser(@RequestBody UserLogging userLogging) {
        ThrowUtils.throwIf(userLogging == null, ErrorCode.PARAMS_ERROR);
        String userAccount = userLogging.getUserAccount();
        String userPassword = userLogging.getUserPassword();
        String checkPassword = userLogging.getCheckPassword();
        long result = userService.userLogin(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

}
