package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.mmall.common.TokenCache.TOKEN_PREFIX;

/**
 * @auther earlman
 * @create 9/12/17
 */
@Service("iUserService")
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String username, String password) {
        ServerResponse<String> response = checkValid(username, Const.USERNAME);
        if (response.isSuccess()) {
            return ServerResponse.createErrorResponse("用户名不存在");
        }

        password = MD5Util.MD5(password);
        User user = userMapper.selectLogin(username, password);
        if (user == null) {
            return ServerResponse.createErrorResponse("password is wrong");
        }

        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createSuccessResponse("login success", user);
    }

    @Override
    public ServerResponse<String> register(User user) {
        ServerResponse<String> serverResponse = checkValid(user.getEmail(), Const.EMAIL);
        if (!serverResponse.isSuccess()) {
            return serverResponse;
        }
        serverResponse = checkValid(user.getUsername(), Const.USERNAME);
        if (!serverResponse.isSuccess()) {
            return serverResponse;
        }

        user.setRole(Const.Role.ROLE_CUSTOMER);
        //MD5 encode
        user.setPassword(MD5Util.MD5(user.getPassword()));

        int resultCount = userMapper.insert(user);
        if (resultCount <= 0) {
            return ServerResponse.createErrorResponse("register fail");
        }
        return ServerResponse.createSuccessResponse("register success");
    }

    @Override
    public ServerResponse<String> checkValid(String str, String type) {
        if (StringUtils.isNotBlank(type)) {
            if (Const.USERNAME.equals(type)) {
                int resultCount = userMapper.checkUsername(str);
                if (resultCount > 0) {
                    return ServerResponse.createErrorResponse("username has existed");
                }
            }
            if (Const.EMAIL.equals(type)) {
                int resultCount = userMapper.checkEmail(str);
                if (resultCount > 0) {
                    return ServerResponse.createErrorResponse("email has existed");
                }
            }
        } else {
            return ServerResponse.createErrorResponse("参数错误");
        }

        return ServerResponse.createSuccessResponse("检验成功");
    }

    @Override
    public ServerResponse<String> selectQuestion(String username) {
        if (checkValid(username, Const.USERNAME).isSuccess()) {
            return ServerResponse.createErrorResponse("用户名不存在");
        }
        String question = userMapper.selectQuestionByUsername(username);
        if (StringUtils.isNotBlank(question)) {
            return ServerResponse.createSuccessResponse(question);
        }
        return ServerResponse.createErrorResponse("找回密码问题为空");
    }

    @Override
    public ServerResponse<String> checkAnswer(String username, String question, String answer) {
        if (userMapper.checkAnswer(username, question, answer) > 0) {
            //当前用户问题和答案匹配成功
            String forgetToken = UUID.randomUUID().toString();
            TokenCache.put(TOKEN_PREFIX + username, forgetToken);
            return ServerResponse.createSuccessResponse(forgetToken);
        }
        return ServerResponse.createErrorResponse("答案错误");
    }

    @Override
    public ServerResponse<String> forgetResetPassword(String username, String newPassword, String forgetToken) {
        if (StringUtils.isBlank(forgetToken)) {
            return ServerResponse.createErrorResponse("参数错误");
        }

        if (checkValid(username, Const.USERNAME).isSuccess()) {
            return ServerResponse.createErrorResponse("用户名不存在");
        }

        String token = TokenCache.get(TOKEN_PREFIX + username);
        if (StringUtils.isBlank(forgetToken) || StringUtils.equals(token, forgetToken)) {
            return ServerResponse.createErrorResponse("token无效");
        }

        newPassword = MD5Util.MD5(newPassword);
        if (userMapper.updatePasswordByUsername(username, newPassword) > 0) {
            return ServerResponse.createSuccessResponse("密码修改成功");
        }
        return ServerResponse.createErrorResponse("密码修改失败，请重试");
    }

    @Override
    public ServerResponse<String> resetPassword(String newPassword, String oldPassword, User user) {
        if (userMapper.checkPassword(MD5Util.MD5(oldPassword), user.getId()) == 0) {
            return ServerResponse.createErrorResponse("旧密码错误");
        }
        user.setPassword(MD5Util.MD5(newPassword));
        if (userMapper.updateByPrimaryKeySelective(user) == 0) {
            return ServerResponse.createErrorResponse("密码修改失败，请重试");
        }
        return ServerResponse.createSuccessResponse("密码修改成功");
    }

    @Override
    public ServerResponse<User> updateInformation(User user) {
        //emil需要校验
        if (StringUtils.isNotBlank(user.getEmail()) && (userMapper.checkEmailByUserId(user.getEmail(), user.getId()) > 0)) {
            return ServerResponse.createErrorResponse("email 已存在");
        }
        //username password不可被更新
        String userName = user.getUsername();
        user.setPassword(StringUtils.EMPTY);
        user.setUsername(StringUtils.EMPTY);

        if (userMapper.updateByPrimaryKeySelective(user) > 0) {
            user.setUsername(userName);
            return ServerResponse.createSuccessResponse("信息更新成功", user);
        }
        return ServerResponse.createErrorResponse("更新失败");
    }

    @Override
    public ServerResponse<User> getInformation(Integer userId) {
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            return ServerResponse.createErrorResponse("找不到当前用户信息");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createSuccessResponse(user);
    }
}
