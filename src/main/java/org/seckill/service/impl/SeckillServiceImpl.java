package org.seckill.service.impl;

import org.apache.commons.collections.MapUtils;
import org.seckill.dao.SeckillDao;
import org.seckill.dao.SuccessKilledDao;
import org.seckill.dao.cache.RedisDao;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.entity.SuccessKilled;
import org.seckill.enums.SeckillStatEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service业务层接口实现类
 */
@Service
public class SeckillServiceImpl implements SeckillService {

    /**
     * logback记录日志
     */
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillDao seckillDao;

    @Autowired
    private SuccessKilledDao successKilledDao;

    @Autowired
    private RedisDao redisDao;

    /**
     * md5混淆盐值字符串，用于混淆md5
     */
    private final String slat = "skjfweiorjw371937#$#$%^RJEWORJ";

    /**
     * 获取所有商品列表
     *
     * @return
     */
    @Override
    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0, 4);
    }

    /**
     * 根据id获取商品记录
     *
     * @param seckillId
     * @return
     */
    @Override
    public Seckill getById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    /**
     * 暴露秒杀接口地址
     *
     * @param seckillId
     * @return
     */
    @Override
    public Exposer exportSeckillUrl(long seckillId) {
        //暴露接口地址，会先访问redis获取商品（没有再访问mysql）再取md5加密形成秒杀接口地址
        //优化点：缓存优化,由于访问到数据库层，建立一个redisDao
        /**
         * get from cache
         * if null
         *      get from db
         *      put cache
         * else
         *      locgoin
         */
        //首先获取该商品

        //1.访问redis
        Seckill seckill = redisDao.getSeckill(seckillId);
        if (seckill == null) {
            //2.访问数据库
            seckill = seckillDao.queryById(seckillId);
            //是否存在该商品
            if (seckill == null) {
                return new Exposer(false, seckillId);
            } else {
                //3.放入redis
                redisDao.putSeckill(seckill);
            }
        }

        //拿到秒杀开始和结束时间以及当前服务器系统时间
        Date startTime = seckill.getStartTime();
        Date endTime = seckill.getEndTime();
        Date nowTime = new Date();

        //判断秒杀是否开启，以便决定是否需要暴露秒杀接口
        if (nowTime.getTime() < startTime.getTime() || nowTime.getTime() > endTime.getTime()) {
            //秒杀未开启，返回当前服务器系统时间
            return new Exposer(false, seckillId, nowTime.getTime(), startTime.getTime(), endTime.getTime());
        }

        //最后，暴露秒杀接口地址

        //转换特定字符串的过程，不可逆，获取到md5
        String md5 = getMD5(seckillId);
        //暴露秒杀地址
        return new Exposer(true, md5, seckillId);
    }

    /**
     * 获取md5值
     *
     * @param seckillId
     * @return
     */
    private String getMD5(long seckillId) {
        //将商品id与盐值连接
        String base = seckillId + "/" + slat;
        //执行md5散列
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        //返回散列
        return md5;
    }

    @Override
    @Transactional
    /**
     * 执行秒杀操作
     *
     * 使用注解控制事务方法的优点：
     * 1。开发团队达成一致约定，明确标注事务方法的编程风格。
     * 2.保证事务方法的执行时间尽可能短，不要穿插其他的网络操作RPC/HTTP请求或者剥离到事务方法外
     * 3.不是所有的方法都需要事务，只读操作不需要事务
     */
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5) throws SeckillException, RepeatKillException, SeckillCloseException {
        try {
        /*
        通过比对用户拿到的md5和系统生成的md5值判断md5值是否存在或正确，
        判断此次秒杀操作是否合法
         */
            if (md5 == null || !md5.equals(getMD5(seckillId))) {
                //md5值不正确，说明数据被篡改，抛异常
                throw new SeckillException("seckill data rewrite");
            }
            //执行秒杀逻辑：减库存，记录购买行为
            /**
             * 行级锁，减库存，记录购买行为，释放行级锁；-->
             * 记录购买行为，行级锁，减库存，释放行级锁；
             */
            //获取当前服务器系统时间
            Date nowTime = new Date();

            //记录购买行为
            int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);
            if (insertCount <= 0) {
                //重复秒杀
                throw new RepeatKillException("seckill repeated");
            } else {
                //减库存，热点商品竞争
                int updateCount = seckillDao.reduceNumber(seckillId, nowTime);
                if (updateCount <= 0) {
                    //没有更新到记录,秒杀结束,rollback
                    throw new SeckillCloseException("seckill is closed");
                } else {
                    //秒杀成功,commit
                    //在数据库查找订单记录
                    SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                    //返回秒杀操作处理结果
                    return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS, successKilled);
                }

            }

        } catch (SeckillCloseException e1) {
            throw e1;
        } catch (RepeatKillException e2) {
            throw e2;
        } catch (SeckillException e3) {
            throw e3;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            //所有编译期异常转换为运行期异常
            throw new SeckillException("seckill inner error:" + e.getMessage());

        }
    }

    /**
     * 使用Mysql存储过程优化秒杀操作
     * @param seckillId
     * @param userPhone
     * @param md5
     * @return
     * @throws SeckillException
     * @throws RepeatKillException
     * @throws SeckillCloseException
     */
    @Override
    public SeckillExecution executeSeckillProcedure(long seckillId, long userPhone, String md5) throws SeckillException, RepeatKillException, SeckillCloseException {
        if (md5 == null || !md5.equals(getMD5(seckillId))) {
            return new SeckillExecution(seckillId, SeckillStatEnum.DATA_REWRITE);
        }
        Date killTime = new Date();
        Map<String, Object> map = new HashMap<>();
        map.put("seckillId", seckillId);
        map.put("userPhone", userPhone);
        map.put("killTime", killTime);
        map.put("result", null);
        // 执行储存过程,result被复制
        seckillDao.killByProcedure(map);
        // 获取result
        int result = MapUtils.getInteger(map, "result", -2);
        if (result == 1) {
            SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
            return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS, successKilled);
        } else {
            return new SeckillExecution(seckillId, SeckillStatEnum.stateOf(result));
        }
    }
}
