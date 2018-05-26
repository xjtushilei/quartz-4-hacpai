package com.xjtushilei.quartz4hacpai.service;

import com.alibaba.fastjson.JSON;
import com.xjtushilei.quartz4hacpai.entity.Log;
import com.xjtushilei.quartz4hacpai.repository.LogRepository;
import okhttp3.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashMap;

/**
 * @author scriptshi
 * 2018/5/26
 */
@Service
public class HacpaiService {

    @Autowired
    private LogRepository logRepository;

    @Value("${userName}")
    private String userName;
    @Value("${userPassword}")
    private String userPassword;

    private String loginBody;


    private static String LOG_URL = "https://hacpai.com/api/v2/login";
    private static String DAILY = "https://hacpai.com/activity/daily-checkin";
    private static String YESTERDAY = "https://hacpai.com/activity/yesterday-liveness-reward";

    private OkHttpClient okHttpClient = new OkHttpClient();


    public void autoSign() {
        String token = getToken();
        if (token != null) {
            Arrays.asList(DAILY, YESTERDAY).forEach(url -> {
                try {
                    Document document = Jsoup.connect(url).cookie("symphony", token).get();
                    String status = document.select("div.points > div:nth-child(1)").text();
                    logRepository.save(new Log(status, "签到或领取成功"));
                } catch (Exception e) {
                    logRepository.save(new Log(e.getMessage(), "签到或领取失败"));
                }
            });
        }
    }

    private String getToken() {
        try {
            RequestBody requestBody = RequestBody.create(MediaType.parse("json"), loginBody);
            Request request = new Request.Builder()
                    .url(LOG_URL)
                    .post(requestBody)
                    .build();
            Response response = okHttpClient.newCall(request).execute();
            String bodyString = response.body().string();
            String token = JSON.parseObject(bodyString).getString("token");
            if (token == null) {
                logRepository.save(new Log(bodyString, "登录失败"));
            } else {
                logRepository.save(new Log(token, "登录成功"));
            }
            return token;
        } catch (Exception e) {
            logRepository.save(new Log(e.getMessage(), "登录失败"));
            return null;
        }


    }

    @PostConstruct
    private void setLoginBody() {
        HashMap<String, String> logBody = new HashMap<>();
        logBody.put("userName", userName);
        logBody.put("userPassword", DigestUtils.md5Hex(userPassword));
        logBody.put("captcha", "");
        this.loginBody = JSON.toJSONString(logBody);
    }
}
