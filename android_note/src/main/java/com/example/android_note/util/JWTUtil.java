package com.example.android_note.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * @className: JWTUtil
 * @description: TODO 类描述
 * @date: 2023/5/100:37
 **/
@Slf4j
public class JWTUtil {
   private static final String scretkey = "dazuoye";
   public static final int time = 3600*24*7;//以秒为单位
   public static String createJWT(Map<String,String> msg){
      //设置生效时间
      Calendar calendar = Calendar.getInstance();
      calendar.add(Calendar.SECOND,time);//second是以秒为单位
      JWTCreator.Builder builder = JWT.create();
      //存放数据
      msg.forEach(new BiConsumer<String, String>() {
         @Override
         public void accept(String s, String s2) {
            builder.withClaim(s,s2);

         }
      });
      //加载生效时间,进行签名
      String token = builder.withExpiresAt(calendar.getTime())
                             .sign(Algorithm.HMAC384(scretkey));
      log.info("创建的token是"+token);
      return token;
   }
   public static String parseJWT(String token,String key){
      JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC384(scretkey)).build();
      DecodedJWT decodedJWT = jwtVerifier.verify(token);
      Claim claim = decodedJWT.getClaim(key);
      return claim.asString();

   }
   public static boolean isTokenNearExpired(String token){
      JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC384(scretkey)).build();
      DecodedJWT decodedJWT = jwtVerifier.verify(token);
      Calendar calendar = Calendar.getInstance();
      return decodedJWT.getExpiresAt().getTime()-calendar.getTime().getTime()<=3600*24*1000;
   }
   public static boolean isPasswordChangedBeforeToken(String token,String password_change_time) throws ParseException {
      //密码是否有过修改
      if(password_change_time==null||password_change_time.equals("")){
         return false;
      }

      JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC384(scretkey)).build();
      DecodedJWT decodedJWT = jwtVerifier.verify(token);
      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      Date date = format.parse(password_change_time);
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(new Date(decodedJWT.getExpiresAt().getTime()));
      System.out.println("令牌过期时间"+calendar.getTime());
      System.out.println("密码修改时间"+format.format(date));
      return (decodedJWT.getExpiresAt().getTime()-date.getTime()) < 3600*24*1000*7;//如果过期时间与密码修改时间大于7天，说明密码修改之后发放的 则返回true
   }
   public static boolean isTokenExpired(String token){
      JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC384(scretkey)).build();
      DecodedJWT decodedJWT = jwtVerifier.verify(token);
      return decodedJWT.getExpiresAt().before(Calendar.getInstance().getTime());
   }
}
