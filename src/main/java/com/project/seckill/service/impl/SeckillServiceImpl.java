package com.project.seckill.service.impl;


import com.project.seckill.aop.Servicelock;
import com.project.seckill.dao.SeckillMapper;
import com.project.seckill.dao.SuccessKilledMapper;
import com.project.seckill.entity.Result;
import com.project.seckill.entity.Seckill;
import com.project.seckill.entity.SuccessKilled;
import com.project.seckill.enums.SeckillStatEnum;
import com.project.seckill.exception.RrException;
import com.project.seckill.service.ISeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service("seckillService")
public class SeckillServiceImpl implements ISeckillService {

    @Autowired
    private SeckillMapper seckillMapper;

    @Autowired
    private SuccessKilledMapper successKilledMapper;
    /**
     * 思考：为什么不用synchronized
     * service 默认是单例的，并发下lock只有一个实例
	 * 互斥锁 参数默认false，不公平锁
     */
	private Lock lock = new ReentrantLock(true);

    @Override
    public List<Seckill> getSeckillList() {
        return null;
    }

    @Override
    public Seckill getById(long seckillId) {
        return null;
    }

    @Override
    public Long getSeckillCount(long seckillId) {
        return null;
    }

    @Override
    public void deleteSeckill(long seckillId) {

    }

    @Override
    public Result startSeckil(long seckillId, long userId) {
        return null;
    }

    @Override
    public Result startSeckilLock(long seckillId, long userId) {
        return null;
    }

    @Override
	@Servicelock
	@Transactional(rollbackFor = Exception.class)
	public Result startSeckilAopLock(long seckillId, long userId) {
		//来自码云码友<马丁的早晨>的建议 使用AOP + 锁实现
        Seckill seckill = seckillMapper.selectById(seckillId);
        Long number = Long.valueOf(seckill.getNumber());
        if(number>0){
            seckillMapper.updateSeckill(seckillId);
			SuccessKilled killed = new SuccessKilled();
			killed.setSeckillId(seckillId);
			killed.setUserId(userId);
			killed.setState(Short.parseShort(number+""));
			killed.setCreateTime(new Timestamp(System.currentTimeMillis()));
            successKilledMapper.insert(killed);
		}else{
			return Result.error(SeckillStatEnum.END);
		}
		return Result.ok(SeckillStatEnum.SUCCESS);
	}

    @Override
    public Result startSeckilDBPCC_ONE(long seckillId, long userId) {
        return null;
    }

    @Override
    public Result startSeckilDBPCC_TWO(long seckillId, long userId) {
        return null;
    }

    @Override
    public Result startSeckilDBOCC(long seckillId, long userId, long number) {
        return null;
    }


    @Override
	public Result startSeckilTemplate(long seckillId, long userId, long number) {
		return null;
	}

}
