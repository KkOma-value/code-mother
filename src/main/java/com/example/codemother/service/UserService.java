package com.example.codemother.service;

import com.mybatisflex.core.service.IService;
import com.example.codemother.model.entity.User;

/**
 * 用户 服务层。
 *
 * @author kkoma
 * @since 1.0
 */
public interface UserService extends IService<User> {


    long userAdd(String Account, String password, String checkPassword);
}
