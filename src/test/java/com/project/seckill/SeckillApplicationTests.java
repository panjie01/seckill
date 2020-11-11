package com.project.seckill;

import com.project.seckill.entity.Result;
import com.project.seckill.service.ISeckillDistributedService;
import com.project.seckill.service.ISeckillService;
import com.project.seckill.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
class SeckillApplicationTests {

    private static int corePoolSize = Runtime.getRuntime().availableProcessors();
    //调整队列数 拒绝服务
    private static ThreadPoolExecutor executor  = new ThreadPoolExecutor(corePoolSize, corePoolSize+1, 10l, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(10000));

    @Autowired
    private ISeckillDistributedService seckillDistributedService;

    @Autowired
    private ISeckillService seckillService;

    @Autowired
    private RedisUtil redisUtil;

    @Test
    void contextLoads() {
    }

    public static final int THREAD_NUM = 1000;

    /**
     * 测试redis分布式锁
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

    /**
     * 测试redis原子递减
     */
    @Test
    public void restRedisCount() {
        int secKillId = 1000;
        /**
         * 初始化商品个数
         */
        redisUtil.cacheValue("seckillNum", 50);
        // 并发数
        CountDownLatch countDownLatch = new CountDownLatch(THREAD_NUM);
        long start = System.currentTimeMillis();
        for (int i = 0;i<THREAD_NUM;i++) {
            final long userId = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        /**
                         * 原子递减
                         */
                        long number = redisUtil.decr("seckillNum",1);
                        if(number>=0){
                            seckillService.startSeckilAopLock(secKillId, userId);
                            log.info("用户:{}秒杀商品成功", userId);
                        }else{
                            log.error("用户:{}秒杀商品失败", userId);
                        }
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
