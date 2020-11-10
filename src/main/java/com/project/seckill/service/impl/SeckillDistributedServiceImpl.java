package com.project.seckill.service.impl;

import com.project.seckill.dao.SeckillMapper;
import com.project.seckill.dao.SuccessKilledMapper;
import com.project.seckill.entity.Result;
import com.project.seckill.entity.Seckill;
import com.project.seckill.entity.SuccessKilled;
import com.project.seckill.enums.SeckillStatEnum;
import com.project.seckill.exception.RrException;
import com.project.seckill.service.ISeckillDistributedService;
import com.project.seckill.util.RedissLockUtil;
import com.project.seckill.util.ZkLockUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.TimeUnit;
@Slf4j
@Service
public class SeckillDistributedServiceImpl implements ISeckillDistributedService {
	
	@Autowired
	private SeckillMapper seckillMapper;

	@Autowired
    private SuccessKilledMapper successKilledMapper;
	
	@Override
	@Transactional
	public Result startSeckilRedisLock(long seckillId,long userId) {
		boolean res=false;
		try {
		    res = RedissLockUtil.tryLock(seckillId+"", TimeUnit.SECONDS, 3, 10);
			if(res){
                Seckill seckill = seckillMapper.selectById(seckillId);
                Long number =  Long.valueOf(seckill.getNumber());
				if(number>0){
					SuccessKilled killed = new SuccessKilled();
					killed.setSeckillId(seckillId);
					killed.setUserId(userId);
					killed.setState((short)0);
					killed.setCreateTime(new Timestamp(new Date().getTime()));
					// save
                    successKilledMapper.insert(killed);
                    // update
                    seckillMapper.updateSeckill(seckillId);
				}else{
					return Result.error(SeckillStatEnum.END);
				}
			}else{
			    return Result.error(SeckillStatEnum.MUCH);
			}
		} catch (Exception e) {
            log.info("异常了个乖乖 : {}", e);
            throw new RrException("异常了个乖乖");
		} finally{
			if(res){//释放锁
				RedissLockUtil.unlock(seckillId+"");
			}
		}
		return Result.ok(SeckillStatEnum.SUCCESS);
	}
	@Override
	@Transactional
	public Result startSeckilZksLock(long seckillId, long userId) {
		boolean res=false;
		try {
			//基于redis分布式锁 基本就是上面这个解释 但是 使用zk分布式锁 使用本地zk服务 并发到10000+还是没有问题，谁的锅？
			res = ZkLockUtil.acquire(3,TimeUnit.SECONDS);
			if(res){
                Seckill seckill = seckillMapper.selectById(seckillId);
                Long number =  Long.valueOf(seckill.getNumber());
				if(number>0){
					SuccessKilled killed = new SuccessKilled();
					killed.setSeckillId(seckillId);
					killed.setUserId(userId);
					killed.setState((short)0);
					killed.setCreateTime(new Timestamp(new Date().getTime()));
                    // save
                    successKilledMapper.insert(killed);
                    // update
                    seckillMapper.updateSeckill(seckillId);
				}else{
					return Result.error(SeckillStatEnum.END);
				}
			}else{
			    return Result.error(SeckillStatEnum.MUCH);
			}
		} catch (Exception e) {
            throw new RrException("异常了个乖乖");
		} finally{
			if(res){//释放锁
				ZkLockUtil.release();
			}
		}
		return Result.ok(SeckillStatEnum.SUCCESS);
	}

	@Override
	@Transactional
	public Result startSeckilLock(long seckillId, long userId, long number) {
		boolean res=false;
		try {
			//尝试获取锁，最多等待3秒，上锁以后10秒自动解锁（实际项目中推荐这种，以防出现死锁）
			res = RedissLockUtil.tryLock(seckillId+"", TimeUnit.SECONDS, 3, 10);
			if(res){
                Seckill seckill = seckillMapper.selectById(seckillId);
                Long count =  Long.valueOf(seckill.getNumber());
				if(count>=number){
					SuccessKilled killed = new SuccessKilled();
					killed.setSeckillId(seckillId);
					killed.setUserId(userId);
					killed.setState((short)0);
					killed.setCreateTime(new Timestamp(new Date().getTime()));
                    // save
                    successKilledMapper.insert(killed);
                    // update
                    seckillMapper.updateSeckill(seckillId);
				}else{
					return Result.error(SeckillStatEnum.END);
				}
			}else{
				return Result.error(SeckillStatEnum.MUCH);
			}
		} catch (Exception e) {
            throw new RrException("异常了个乖乖");
		} finally{
			if(res){//释放锁
				RedissLockUtil.unlock(seckillId+"");
			}
		}
		return Result.ok(SeckillStatEnum.SUCCESS);
	}

}
