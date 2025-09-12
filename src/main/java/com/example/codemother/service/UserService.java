package com.example.codemother.service;

import com.example.codemother.model.dto.user.UserQueryRequest;
import com.example.codemother.model.vo.LoginUserVO;
import com.example.codemother.model.vo.UserVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.example.codemother.model.entity.User;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 用户 服务层。
 *
 * @author kkoma
 * @since 1.0
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param Account
     * @param password
     * @param checkPassword
     * @return
     */
    long userAdd(String Account, String password, String checkPassword);

    /**
     * 获取已经脱敏的已经登录的用户信息
     *
     * @param user
     * @return
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 用户登录
     *
     * @param Account
     * @param password
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String Account, String password, HttpServletRequest request);

    /**
     * 获取当前登录用户
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取单个用户信息
     *
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏后的数据列表
     *
     * @param userList
     * @return
     */
    List<UserVO> getUserVOList(List<User> userList);

    //盐值加密
    String newPassword(String password);

    QueryWrapper getQueryWrapper(UserQueryRequest userQueryRequest);


}
