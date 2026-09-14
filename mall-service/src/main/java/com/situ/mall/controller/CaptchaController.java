package com.situ.mall.controller;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.situ.mall.utils.Result;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/service")
public class CaptchaController {
    @Autowired
    private DefaultKaptcha defaultKaptcha;
    @Autowired
    private RedisTemplate redisTemplate;


    //图片以json形式返回
    @GetMapping("/captcha")
    public Result captcha(HttpServletResponse response) throws IOException {
        String captcha = defaultKaptcha.createText();
        //redis存储
        String uuid = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set("captcha:" + uuid, captcha, 2, TimeUnit.MINUTES);
        BufferedImage image = defaultKaptcha.createImage(captcha);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", out);
        // 将图片转换为base64编码的字符串
        String base64Code = Base64.encodeBase64String(out.toByteArray());
        Map<String, Object> map = new HashMap<>();
        map.put("captcha", "data:image/png;base64," + base64Code);
        map.put("uuid", uuid);
        return Result.ok("", map);
    }
}
