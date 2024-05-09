package com.example.android_note.controller;


import com.example.android_note.entity.User;
import com.example.android_note.model.HttpResult;
import com.example.android_note.model.ResponseConstant;
import com.example.android_note.service.UserService;
import com.example.android_note.util.DateTime;
import com.example.android_note.util.JWTUtil;
import com.example.android_note.util.RedisUtil;
import com.example.android_note.webSocket.WebSocket;
import com.example.android_note.webSocket.WebSocketImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.socket.WebSocketSession;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static com.example.android_note.util.DateTime.getCurrentTime;

/**
 * @className: UserController
 * @description: TODO 类描述
 * @date: 2023/5/100:11
 **/
@Controller
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    UserService userService;
    @Autowired
    RedisUtil redisUtil;



    @PostMapping("/login")
    @ResponseBody
    HttpResult<String> login(@RequestBody User user, HttpServletRequest httpRequest){

        User user1 = userService.queryUserByName(user.getUsername());
        if(Objects.isNull(user1)){
            return new HttpResult<String>(ResponseConstant.NOT_EXIST.code,ResponseConstant.NOT_EXIST.msg, "");
        } else if (user1.getUsername().equals(user.getUsername())&&user1.getPassword().equals(user.getPassword())) {
            String token = httpRequest.getHeader("Authorization");
            if(token!=null&&!token.equals("")){
                try{
                    String name = JWTUtil.parseJWT(token, "userName");
                }catch (Exception e){
                    return new HttpResult<>(ResponseConstant.TOKEN_EXPIRE.code, ResponseConstant.TOKEN_EXPIRE.msg, "");
                }
            }
            return  loginHandler(token,user.getUsername(),user1.getPassword_change_time());
        }else {
            return new HttpResult<String>(ResponseConstant.LOGIN_FAIL.code,ResponseConstant.LOGIN_FAIL.msg, "");
        }

    }
    @RequestMapping("/SecretFreeLogin")
    @ResponseBody
    HttpResult<String> SecretFreeLogin(@RequestParam String userName,HttpServletRequest httpRequest){
        //免密登录
        String token = httpRequest.getHeader("Authorization");
        //如果token非法或者过期
        String name = null;
        try{
             name = JWTUtil.parseJWT(token, "userName");
        }catch (Exception e){
            return new HttpResult<>(ResponseConstant.TOKEN_EXPIRE.code, ResponseConstant.TOKEN_EXPIRE.msg, "");
        }
        System.out.println("免密登录对应的用户名" + userName);
        System.out.println("免密登录 token"+token);
        User user = userService.queryUserByName(name);
        System.out.println("免密登录对应的用户信息" + user.toString());
        return loginHandler(token,userName,user.getPassword_change_time());
    }

    private HttpResult<String> getStringHttpResult(User user1) {
        Map<String,String> map = new HashMap<>();
        map.put("userName", String.valueOf(user1.getUsername()));
        String token = JWTUtil.createJWT(map);
        return new HttpResult<>(ResponseConstant.SUCESS.code, ResponseConstant.SUCESS.msg, token);
    }

    @RequestMapping("/register")
    @ResponseBody
    HttpResult<String> register(@RequestBody User user){


        if(userService.registerUser(user)){
            return getStringHttpResult(user);
        }else {
            return new HttpResult<>(ResponseConstant.REGISTER_FAIL.code, ResponseConstant.REGISTER_FAIL.msg, "");
        }
    }
    @RequestMapping("/isRepeat")
    @ResponseBody
    HttpResult<String> isRepeat(@RequestParam("username") String username){
        if(isUserNameRepeat(username)){
            return new HttpResult<>(ResponseConstant.USER_NAME_REPEAT.code, ResponseConstant.USER_NAME_REPEAT.msg, "");
        }else return new HttpResult<>(ResponseConstant.SUCESS.code, ResponseConstant.SUCESS.msg, "");

    }
    //退出登录
    @RequestMapping("/login_out")
    @ResponseBody
    HttpResult<String> loginOut(@RequestParam("username") String username){
        log.info("退出登录");
        redisUtil.delete("login_"+username);
        return new HttpResult<>(ResponseConstant.SUCESS.code, ResponseConstant.SUCESS.msg, "");
    }
    //校验密保问题 返回问题和答案
    @RequestMapping("/check_question")
    @ResponseBody
    HttpResult<String> checkQuestion(@RequestParam("username") String username){
        User user = userService.queryUserByName(username);
        if(user.getSecurity_question()!=null&&!user.getSecurity_question().equals("")){
            return new HttpResult<>(ResponseConstant.SUCESS.code, ResponseConstant.SUCESS.msg,user.getSecurity_question());
        }else {
            return new HttpResult<>(ResponseConstant.SECURITY_QUESTION_NOT_EXIST.code, ResponseConstant.SECURITY_QUESTION_NOT_EXIST.msg, "");
        }
    }
    //校验密保答案是否正确
    @RequestMapping("/check_answer")
    @ResponseBody
    HttpResult<String> checkAnswer(@RequestBody User user){
        User user1 = userService.queryUserByName(user.getUsername());
        if(user.getSecurity_answer().equals(user1.getSecurity_answer())){
            return new HttpResult<>(ResponseConstant.SUCESS.code, ResponseConstant.SUCESS.msg, "");
        }else {
            return new HttpResult<>(ResponseConstant.SECURITY_QUESTION_NOT_EXIST.code, ResponseConstant.SECURITY_QUESTION_NOT_EXIST.msg, "");
        }
    }
    //校验密码是否正确
    @RequestMapping("/check_password")
    @ResponseBody
    HttpResult<String> checkPassword(@RequestBody User user){
        User user1 = userService.queryUserByName(user.getUsername());
        if(user.getPassword().equals(user1.getPassword())){
            return new HttpResult<>(ResponseConstant.SUCESS.code, ResponseConstant.SUCESS.msg, "");
        }else {
            return new HttpResult<>(ResponseConstant.LOGIN_FAIL.code, ResponseConstant.LOGIN_FAIL.msg, "");
        }
    }
    //修改密码
    @RequestMapping("/change_password")
    @ResponseBody
    HttpResult<String> changePassword(@RequestBody User user){
        System.out.println("密码修改成功");
        user.setPassword_change_time(DateTime.getCurrentTime());
        userService.updateUserPassword(user);
        return new HttpResult<>(ResponseConstant.SUCESS.code, ResponseConstant.SUCESS.msg, "");

    }
    //设置新的密保问题和密保答案
    @RequestMapping("/set_question")
    @ResponseBody
    HttpResult<String> setQuestion(@RequestBody User user){
        userService.updateQuestion(user);
        return new HttpResult<>(ResponseConstant.SUCESS.code, ResponseConstant.SUCESS.msg, "");
    }
    Boolean isUserNameRepeat(String username){
        if(userService.isUserHasInCache(username)){
           return true;
        }else {
            //在缓存中没有 数据库查询 并添加到缓存
            User user = userService.queryUserByName(username);
            if(user!=null){
                redisUtil.set(user.getUsername(),user);
                return true;
            }else {
                return false;
            }
        }
    }
    HttpResult<String> loginHandler(String token,String userName,String passwordChangeTime){
        //检验token的发放是不是在 密码修改时间之后 是则进入以下流程
        if(token!=null){
            try {
                if(JWTUtil.isPasswordChangedBeforeToken(token,passwordChangeTime)){
                    System.out.println("密码被修改过");
                    return new HttpResult<String> (ResponseConstant.PASSWORD_HAS_CHANGED.code,ResponseConstant.PASSWORD_HAS_CHANGED.msg,"");
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return loginHandler(token,userName);
    }
    HttpResult<String> loginHandler(String token,String userName){
        //用户存在 进行登录操作
        //用户名 缓存中已经登录的是否冲突 冲突则返回给客户端 客户端确认是本人登录后，再发起请求将用户加入缓存，如果没有token或者即将过期 过期则分发token
        // 另一账户登录，没有token。 现有token过期 token合法
        String login_token = (String) redisUtil.get("login_"+userName);
        System.out.println(" 用户携带 token"+token);
        System.out.println(" 缓存中的 token"+login_token);
        if(Objects.isNull(login_token)||login_token.equals("")){
            //用户未登录 发放新token 修改密码后能到这肯定是输入正确的新密码
            String newToken = createNewTokenForToken(null,userName);
            redisUtil.set("login_"+userName,newToken);
            return new HttpResult<>(ResponseConstant.SUCESS.code, ResponseConstant.SUCESS.msg, newToken);
        } else if (token!=null&&login_token.equals(token)) {
            //用户已登录 token一致说明是同一设备
            //如果过期发放新token
            //如果临近一天过期发放新token
            //更新缓存中的登录数据
                String newToken = createNewTokenForToken(token,userName);
                if(newToken !=null){
                    redisUtil.set("login_"+userName,newToken);
                    return new HttpResult<>(ResponseConstant.SUCESS.code, ResponseConstant.SUCESS.msg, newToken);
                    //不需要移除，因为同一台设备的话，重新登录之前的断开连接会清除对应的session
/*                    ConcurrentHashMap<String, WebSocketSession> SESSION_POOL = WebSocketImpl.getSessions();
                    WebSocketSession session = SESSION_POOL.remove(token);
                    try {
                        session.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }*/
                }else {
                    return new HttpResult<>(ResponseConstant.SUCESS.code, ResponseConstant.SUCESS.msg, token);
                }

        } else if(token!=null&&!login_token.equals(token)){

            //用户已经登录 但前后使用的客户端不一致 发生冲突 可能新旧两台设备都登录过 并且token未过期 免密登录时某一台正在登录 客户端确认后发起长连接对缓存进行更新
            //之后长连接时如果集合中有旧token对应的session，即有其他设备使用，将其关闭
            //如果token过期重新生成
                String newToken = createNewTokenForToken(token,userName);
                //不需要在这里更新缓存，如果客户端确定要抢占登录发起长连接再更新缓存
                //redisUtil.set("login_"+user.getUsername(),newToken);
            if(newToken ==null){
                return new HttpResult<String>(ResponseConstant.USER_HAS_LOGIN.code,ResponseConstant.USER_HAS_LOGIN.msg, token);
            }else {
                return new HttpResult<String>(ResponseConstant.USER_HAS_LOGIN.code,ResponseConstant.USER_HAS_LOGIN.msg, newToken);
            }

        } else if (token==null) {
            //用户已经登录，但是离线过久 客户端由于token缓存过期删除了token或者新设备登录 客户端确认是本人 发起长连接更新缓存中的token
            //之后长连接时如果集合中有旧token对应的session，即有其他设备使用，将其关闭
            String newToken = createNewTokenForToken(token,userName);
            //不需要在这里更新缓存，如果客户端确定要抢占登录发起长连接再更新缓存
            //redisUtil.set("login_"+user.getUsername(),newToken);
            return new HttpResult<String>(ResponseConstant.USER_HAS_LOGIN.code,ResponseConstant.USER_HAS_LOGIN.msg, newToken);
        }
        return new HttpResult<String>(ResponseConstant.NOT_EXIST.code,ResponseConstant.NOT_EXIST.msg, "");
    }
    public String createNewTokenForToken(String token,String userName){//如果token过期或者快过期进行更换
        if(token==null||token.equals("")||JWTUtil.isTokenExpired(token)||JWTUtil.isTokenNearExpired(token)){
            System.out.println("创建新token");
            if(token!=null){
                System.out.println(""+JWTUtil.isTokenExpired(token)+JWTUtil.isTokenNearExpired(token));
            }
            Map<String,String> map = new HashMap<>();
            map.put("userName", String.valueOf(userName));
            String newToken = JWTUtil.createJWT(map);
            return newToken;
        }
        return null;
    }

}
