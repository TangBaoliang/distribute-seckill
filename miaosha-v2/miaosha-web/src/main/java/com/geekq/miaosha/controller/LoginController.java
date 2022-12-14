package com.geekq.miaosha.controller;

import com.geekq.miaosha.annotation.StreamLimit;
import com.geekq.miaosha.redis.redismanager.RedisLua;
import com.geekq.miaosha.service.MiaoShaUserService;
import com.geekq.miasha.enums.resultbean.ResultGeekQ;
import com.geekq.miasha.vo.LoginVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import static com.geekq.miasha.enums.Constanst.COUNTLOGIN;

@Controller
@RequestMapping("/login")
public class LoginController {

    private static Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private MiaoShaUserService userService;

    /**
     * 登录界面
     */
    @RequestMapping("/to_login")
    public String tologin(LoginVo loginVo, Model model) {
        logger.info(loginVo.toString());

        //todo 网站访问次数统计
        RedisLua.vistorCount(COUNTLOGIN);
        String count = RedisLua.getVistorCount(COUNTLOGIN).toString();
        logger.info("访问网站的次数为:{}", count);
        model.addAttribute("count", count);
        return "login";
    }

    /**
     * 用户登录接口
     */
    @RequestMapping("/loginin")
    @ResponseBody
    @StreamLimit(name = "登录接口限流", key = "login_limit", period = 20, count = 1)
    public ResultGeekQ<String> dologin(HttpServletResponse response, @Valid LoginVo loginVo) {
        ResultGeekQ<String> result = ResultGeekQ.build();
        logger.info(loginVo.toString());
        userService.login(response, loginVo);
        return result;
    }


    @RequestMapping("/create_token")
    @ResponseBody
    public String createToken(HttpServletResponse response, @Valid LoginVo loginVo) {
        logger.info(loginVo.toString());
        String token = userService.createToken(response, loginVo);
        return token;
    }
}
