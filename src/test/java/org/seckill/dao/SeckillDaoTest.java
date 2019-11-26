package org.seckill.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.entity.Seckill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class SeckillDaoTest {
    @Autowired
    private SeckillDao seckillDao;

    @Test
    public void queryById() {
        long id = 1000L;
        Seckill seckill = seckillDao.queryById(id);
        System.out.println(seckill.getName());
        System.out.println(seckill);
        /**
         * 1000元秒杀iphone11
         * Seckill{seckillId=1000, name='1000元秒杀iphone11', number=100, startTime=Tue Nov 05 00:00:00 CST 2019, endTime=Thu Nov 07 00:00:00 CST 2019, createTime=Tue Nov 05 15:45:39 CST 2019}
         */
    }

    @Test
    public void queryAll() {
        List<Seckill> seckills = seckillDao.queryAll(0, 100);
        for (Seckill seckill:seckills){
            System.out.println(seckill);
        }
        /**
         * Seckill{seckillId=1000, name='1000元秒杀iphone11', number=100, startTime=Tue Nov 05 00:00:00 CST 2019, endTime=Thu Nov 07 00:00:00 CST 2019, createTime=Tue Nov 05 15:45:39 CST 2019}
         * Seckill{seckillId=1001, name='500元秒杀iphone watch5', number=200, startTime=Tue Nov 05 00:00:00 CST 2019, endTime=Thu Nov 07 00:00:00 CST 2019, createTime=Tue Nov 05 15:45:39 CST 2019}
         * Seckill{seckillId=1002, name='300元秒杀小米9', number=300, startTime=Tue Nov 05 00:00:00 CST 2019, endTime=Thu Nov 07 00:00:00 CST 2019, createTime=Tue Nov 05 15:45:39 CST 2019}
         * Seckill{seckillId=1003, name='200元秒杀Redmi Note 8', number=400, startTime=Tue Nov 05 00:00:00 CST 2019, endTime=Thu Nov 07 00:00:00 CST 2019, createTime=Tue Nov 05 15:45:39 CST 2019}
         */
    }

    @Test
    public void reduceNumber() {
        Date killTime = new Date();
        int updateCount = seckillDao.reduceNumber(1000, killTime);
        System.out.println("updateCount:"+updateCount);
        /**
         * updateCount:1
         */
    }
}