package com.project.seckill;

import com.project.seckill.entity.Result;
import com.project.seckill.service.ISeckillDistributedService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.CountDownLatch;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
class SeckillApplicationTests {

    @Autowired
    private ISeckillDistributedService seckillDistributedService;

    @Test
    void contextLoads() {
    }

    public static final int THREAD_NUM = 1000;

    /**
     * 测试redis
     */
    @Test
    public void testSeckillDistributed() {
        CountDownLatch countDownLatch = new CountDownLatch(THREAD_NUM);
        long start = System.currentTimeMillis();
        for (int i = 0;i<THREAD_NUM;i++) {
            final long userId = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        log.info("----------程序开始准备----------");
//                        countDownLatch.await();
                        Result result = seckillDistributedService.startSeckilRedisLock(1000, userId);
                        log.info("程序执行结果为 ：{}", userId);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }finally {
                        countDownLatch.countDown();
                    }
                }
            }).start();
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("程序执行总时间： {}", System.currentTimeMillis()-start);
    }

}
