package org.seckill.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.*;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml",
        "classpath:spring/spring-service.xml"})
public class SeckillServiceTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillService seckillService;
    @Test
    public void getSeckillList() {
        List<Seckill> list = seckillService.getSeckillList();
        logger.info("list={}",list);
        /**
         * 13:51:34.716 [main] INFO  o.seckill.service.SeckillServiceTest - list=[
         * Seckill{seckillId=1000, name='1000元秒杀iphone11', number=99, startTime=Wed Nov 06 21:51:45 CST 2019, endTime=Thu Nov 07 00:00:00 CST 2019, createTime=Tue Nov 05 15:45:39 CST 2019},
         * Seckill{seckillId=1001, name='500元秒杀iphone watch5', number=200, startTime=Tue Nov 05 00:00:00 CST 2019, endTime=Thu Nov 07 00:00:00 CST 2019, createTime=Tue Nov 05 15:45:39 CST 2019},
         * Seckill{seckillId=1002, name='300元秒杀小米9', number=300, startTime=Tue Nov 05 00:00:00 CST 2019, endTime=Thu Nov 07 00:00:00 CST 2019, createTime=Tue Nov 05 15:45:39 CST 2019},
         * Seckill{seckillId=1003, name='200元秒杀Redmi Note 8', number=400, startTime=Tue Nov 05 00:00:00 CST 2019, endTime=Thu Nov 07 00:00:00 CST 2019, createTime=Tue Nov 05 15:45:39 CST 2019}]
         */
    }

    @Test
    public void getById() {
        long id = 1000L;
        Seckill seckill = seckillService.getById(id);
        logger.info("seckill={}",seckill);
        /**
         * 13:50:55.077 [main] INFO  o.seckill.service.SeckillServiceTest - seckill=Seckill{seckillId=1000, name='1000元秒杀iphone11', number=99, startTime=Wed Nov 06 21:51:45 CST 2019, endTime=Thu Nov 07 00:00:00 CST 2019, createTime=Tue Nov 05 15:45:39 CST 2019}
         */
    }

    //测试代码完整逻辑，注意可重复执行
    @Test
    public void seckillLogic() {
        long id = 1000L;
        Exposer exposer = seckillService.exportSeckillUrl(id);
        if(exposer.isExposed()){
            //秒杀开启
            logger.info("exposer={}",exposer);
            long phone = 111111112;
            String md5 = "bb8e80ede185b693ad1011b59622a9a2";
            try {
                SeckillExecution execution = seckillService.executeSeckill(id, phone, md5);
                logger.info("execution={}", execution);
            }catch (RepeatKillException e){
                logger.error(e.getMessage());
            }
            catch (SeckillCloseException e){
                logger.error(e.getMessage());
            }
            /**
             * 第一次：
             * 14:16:57.779 [main] INFO  o.seckill.service.SeckillServiceTest
             * - execution=SeckillExecution{
             * seckillId=1000, state=1, stateInfo='秒杀成功',
             * successKilled=SuccessKilled{
             * seckillId=1000, userPhone=111111112, state=0, createTime=Thu Nov 07 14:16:57 CST 2019}}
             * 第二次：
             * 14:21:34.818 [main] ERROR o.seckill.service.SeckillServiceTest - seckill repeated
             */
        }else {
            //秒杀未开启
            logger.warn("exposer={}",exposer);
        }
        /**
         * 14:06:46.937 [main] INFO  o.seckill.service.SeckillServiceTest - exposer=Exposer{
         * exposed=true, md5='bb8e80ede185b693ad1011b59622a9a2',
         * seckillId=1000, now=0, start=0, end=0}
         */
    }
    @Test
    public void executeSeckillProcedure(){
        long seckillId = 1001;
        long phone = 15512063930L;
        Exposer exposer = seckillService.exportSeckillUrl(seckillId);
        if (exposer.isExposed()) {
            String md5 = exposer.getMd5();
            SeckillExecution execution = seckillService.executeSeckillProcedure(seckillId, phone, md5);
            logger.info("execution={}", execution);
            /**
             * 16:22:16.972 [main] INFO  o.seckill.service.SeckillServiceTest -
             * execution=SeckillExecution{
             * seckillId=1001, state=1, stateInfo='秒杀成功',
             * successKilled=SuccessKilled{
             * seckillId=1001, userPhone=15512063930,
             * state=0, createTime=Fri Nov 08 16:22:16 CST 2019}}
             */
        }
    }

}