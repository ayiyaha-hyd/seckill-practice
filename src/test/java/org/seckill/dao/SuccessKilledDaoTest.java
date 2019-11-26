package org.seckill.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.entity.SuccessKilled;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class SuccessKilledDaoTest {

    @Resource
    private SuccessKilledDao successKilledDao;

    @Test
    public void insertSuccessKilled() {
        long id = 1000L;
        long phone = 123456789L;
        int insertCount = successKilledDao.insertSuccessKilled(id, phone);
        System.out.println("insertCount:"+insertCount);
        /**
         * 第一次执行：
         * insertCount:1
         * 第二次执行：
         *insertCount:0
         */
    }

    @Test
    public void queryByIdWithSeckill() {
        long id = 1000L;
        long phone = 123456789L;
        SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(id, phone);
        System.out.println(successKilled);
        System.out.println(successKilled.getSeckill());
        /**
         * SuccessKilled{seckillId=1000, userPhone=123456789, state=0, createTime=Wed Nov 06 21:59:17 CST 2019}
         * Seckill{seckillId=1000, name='1000元秒杀iphone11', number=0, startTime=Wed Nov 06 21:51:45 CST 2019, endTime=Thu Nov 07 00:00:00 CST 2019, createTime=Tue Nov 05 15:45:39 CST 2019}
         */
    }

}