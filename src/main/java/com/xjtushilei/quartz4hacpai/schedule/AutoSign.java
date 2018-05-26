package com.xjtushilei.quartz4hacpai.schedule;

import com.xjtushilei.quartz4hacpai.service.HacpaiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author scriptshi
 * 2018/5/27
 */
@Component
@EnableScheduling
public class AutoSign {

    @Autowired
    HacpaiService hacpaiService;

    //每天8点5分签到和领取昨天礼物
    @Scheduled(cron = "0 5 8 ? * *")
    public void autoSign() {
        hacpaiService.autoSign();
    }

}
