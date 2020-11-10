package com.project.seckill.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.seckill.entity.Seckill;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Component;

/**
 * @author panjie
 * @date 2020/11/10 16:14
 */
@Component
public interface SeckillMapper extends BaseMapper<Seckill> {

    @Update("UPDATE seckill  SET number=number-1 WHERE seckill_id=#{seckillId} AND number>0")
    Boolean updateSeckill(Long seckillId);

}
