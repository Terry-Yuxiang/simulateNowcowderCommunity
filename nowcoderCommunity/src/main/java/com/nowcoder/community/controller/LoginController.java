package com.nowcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.utils.CommunityConstant;
import com.nowcoder.community.utils.CommunityUtil;
import com.nowcoder.community.utils.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Controller
public class  LoginController implements CommunityConstant {

   private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);
   @Autowired
   private Producer kaptchaProducer;
   @Autowired
   private UserService userService;
   @Value("${server.servlet.context-path}")
   private String contextPath;

   @Autowired
   private RedisTemplate redisTemplate;

   @RequestMapping(path="/register",method = RequestMethod.GET)
   public String getRegisterPage(){
      return "/site/register";
   }

   @RequestMapping(path="/login",method = RequestMethod.GET)
   public String getLoginPage(){
      return "/site/login";
   }
   @RequestMapping(path="/register",method=RequestMethod.POST)
   public String register(Model model, User user){

      Map<String, Object> map = userService.register(user);  //map???????????????
      if(map == null || map.isEmpty()){  //????????????
         model.addAttribute("msg","???????????????????????????????????????????????????????????????????????????????????????");
         model.addAttribute("target","/index");
         return "/site/operate-result";
      }else{
         model.addAttribute("usernameMsg",map.get("usernameMsg"));
         model.addAttribute("passwordMsg",map.get("passwordMsg"));
         model.addAttribute("emailMsg",map.get("emailMsg"));

         return "/site/register";
      }
   }

   /**
    * ??????
    * @param model
    * @param userId
    * @param code
    * @return
    */
   @RequestMapping(path="/activation/{userId}/{code}",method = RequestMethod.GET)
   public String activation(Model model,
                            @PathVariable("userId") int userId,
                            @PathVariable("code") String code){

      int result = userService.activation(userId, code);
      if(result == ACTIVATION_SUCCESS){
         model.addAttribute("msg","?????????????????????????????????????????????");
         model.addAttribute("target","/login");
      }else if(result == ACTIVATION_REPEAT){
         model.addAttribute("msg","?????????????????????????????????????????????");
         model.addAttribute("target","/index");
      }else{
         model.addAttribute("msg","????????????????????????????????????");
         model.addAttribute("target","/index");
      }
      return "/site/operate-result";
   }

   /**
    * ???????????????
    * @param response
    * @param session
    */
   @RequestMapping(path = "/kaptcha",method = RequestMethod.GET)
   public void getKaptcha(HttpServletResponse response/*, HttpSession session*/){
      // ???????????????
      String text = kaptchaProducer.createText();
      BufferedImage image = kaptchaProducer.createImage(text);

//        //??????????????????session
//        session.setAttribute("kaptcha",text);

      //??????????????????
      String kaptchaOwner = CommunityUtil.generateUUID();
      Cookie cookie = new Cookie("kaptchaOwner",kaptchaOwner);
      cookie.setMaxAge(60);
      cookie.setPath(contextPath);
      response.addCookie(cookie);  //??????cookie

      //??????????????????Redis
      String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
      redisTemplate.opsForValue().set(kaptchaKey,text,60, TimeUnit.SECONDS);
      //???????????????????????????
      response.setContentType("image/png");
      try {
         //?????????????????????
         OutputStream outputStream = response.getOutputStream();
         ImageIO.write(image, "png",outputStream);
      } catch (IOException e) {
         LOGGER.error("????????????????????????"+e.getMessage());
      }

   }


   /**
    * ??????
    * @param username
    * @param password
    * @param code  //?????????
    * @param rememberMe  //???????????????
    * @param model
    * @param session   //????????????????????????
    * @param response  //?????????????????????cookie
    * @return
    */
   @RequestMapping(path = "/login",method = RequestMethod.POST)
   public String login(String username, String password, String code, boolean rememberMe,
                       Model model/*,HttpSession session*/, HttpServletResponse response,
                       @CookieValue("kaptchaOwner") String kaptchaOwner){
      String kaptcha = null;

      if(StringUtils.isNotBlank(kaptchaOwner)){
         String kaptchaKey = RedisKeyUtil.getKaptchaKey(kaptchaOwner);
         kaptcha = (String) redisTemplate.opsForValue().get(kaptchaKey);

      }
      if(StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)){
         model.addAttribute("codeMsg","??????????????????");
         return "/site/login";
      }

      //??????????????????
      int expiredTime = rememberMe ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
      Map<String, Object> map = userService.login(username, password, expiredTime);
      if(map.containsKey("ticket")){  //??????????????????????????????cookie????????????????????????
         Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
         cookie.setPath(contextPath);
         cookie.setMaxAge(expiredTime);
         response.addCookie(cookie);
         return "redirect:/index";
      }else{
         model.addAttribute("usernameMsg",map.get("usernameMsg"));
         model.addAttribute("passwordMsg",map.get("passwordMsg"));
         return "/site/login";
      }

   }

   @RequestMapping(path = "/logout",method = RequestMethod.GET)
   public String logout(@CookieValue("ticket") String ticket){
      userService.logout(ticket);
      return "redirect:/login";
   }
}
