package com.example.codemother.service.impl;


import cn.hutool.core.util.StrUtil;
import com.example.codemother.exception.BusinessException;
import com.example.codemother.exception.ErrorCode;
import com.example.codemother.model.enums.UserRoleEnum;
import com.example.codemother.model.vo.LoginUserVO;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.example.codemother.model.entity.User;
import com.example.codemother.mapper.UserMapper;
import com.example.codemother.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;


import static com.example.codemother.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户 服务层实现。
 *
 * @author kkoma
 * @since 1.0
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    //注册
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

    //将User中的属性复制到LoginUserVO中，不存在的字段就被过滤掉
    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    //登录
    @Override
    public LoginUserVO userLogin(String Account, String password, HttpServletRequest request) {

        // ==================== 参数校验阶段 ====================
        // 检查账号和密码是否为空
        if (StrUtil.hasBlank(Account, password)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        // 校验账号长度（至少5位）
        if (Account.length() < 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }

        // 校验密码长度（至少5位）
        if (password.length() < 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }

        // ==================== 密码加密处理 ====================
        // 对原始密码进行加密处理（使用自定义的加密方法）
        String new_password = newPassword(password);

        // ==================== 数据库查询阶段 ====================
        // 创建查询条件包装器
        QueryWrapper queryWrapper = new QueryWrapper();
        // 添加账号查询条件
        queryWrapper.eq("userAccount", Account);
        // 添加密码查询条件（使用加密后的密码）
        queryWrapper.eq("userPassword", new_password);

        // 执行数据库查询，根据账号和密码查找用户
        User user = this.mapper.selectOneByQuery(queryWrapper);

        // ==================== 登录结果处理 ====================
        // 如果用户不存在，抛出异常
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }

        // ==================== 会话管理阶段 ====================
        // 将用户信息存入Session，标记用户登录状态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);

        // ==================== 返回结果处理 ====================
        // 将User实体转换为前端需要的LoginUserVO对象并返回
        return this.getLoginUserVO(user);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {

        // ==================== 从Session中获取用户信息 ====================
        // 从HTTP Session中获取用户登录状态属性
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        // 将Object类型强制转换为User类型
        User currentUser = (User) userObj;

        // ==================== 初步校验用户登录状态 ====================
        // 检查Session中的用户对象是否存在或用户ID是否为空
        if (currentUser == null || currentUser.getId() == null) {
            // 如果未找到用户信息，抛出未登录异常
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // ==================== 重新查询数据库获取最新用户信息 ====================
        // 获取用户ID
        long userId = currentUser.getId();
        // 根据用户ID从数据库中重新查询用户信息（确保获取最新数据）
        currentUser = this.getById(userId);

        // ==================== 最终校验用户有效性 ====================
        // 检查数据库中的用户是否存在（防止用户被删除等情况）
        if (currentUser == null) {
            // 如果数据库中没有该用户，抛出未登录异常
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        // ==================== 返回有效的用户信息 ====================
        // 返回从数据库中获取的最新用户信息
        return currentUser;
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {

        // ==================== 登录状态检查 ====================
        // 从Session中获取用户登录状态属性
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);

        // 检查用户是否已登录：如果Session中没有用户信息，说明未登录
        if (userObj == null) {
            // 抛出业务异常，提示用户未登录，无需执行登出操作
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }

        // ==================== 执行登出操作 ====================
        // 移除Session中的用户登录状态属性，清除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);

        // ==================== 返回登出结果 ====================
        // 返回true表示登出操作成功完成
        return true;
    }


    //盐值加密
    public String newPassword(String password) {
        final String as = "Joseph_KkOma";
        return DigestUtils.md5DigestAsHex((password + as).getBytes());
    }
}
