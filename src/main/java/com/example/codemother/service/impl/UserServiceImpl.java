package com.example.codemother.service.impl;


import cn.hutool.core.util.StrUtil;
import com.example.codemother.exception.BusinessException;
import com.example.codemother.exception.ErrorCode;
import com.example.codemother.model.enums.UserRoleEnum;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.example.codemother.model.entity.User;
import com.example.codemother.mapper.UserMapper;
import com.example.codemother.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.Queue;

/**
 * 用户 服务层实现。
 *
 * @author kkoma
 * @since 1.0
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public long userAdd(String userAccount, String password, String checkPassword) {
        //1.检验
        if (StrUtil.hasBlank(userAccount, password, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        if (userAccount.length() < 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户长度过短");
        }

        if (password.length() < 5 || checkPassword.length() < 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度过短");
        }

        if (!checkPassword.equals(password)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次密码不一致");
        }

        //2.查看是否重复
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("userAccount", userAccount);
        long cnt = this.mapper.selectCountByQuery(queryWrapper);
        if (cnt > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户重复");
        }
        //3.加密
        String new_password = newPassword(password);
        //4.加入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(new_password);
        user.setUserName("测试");
        user.setUserRole(UserRoleEnum.USER.getValue());
        boolean ans = this.save(user);
        if (ans) {
            return user.getId();
        } else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "数据库不存在");
        }
    }

    public String newPassword(String password) {
        final String as = "Joseph_KkOma";
        return DigestUtils.md5DigestAsHex((password + as).getBytes());
    }
}
