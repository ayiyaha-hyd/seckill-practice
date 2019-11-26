package org.seckill.dao.cache;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.seckill.entity.Seckill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 *访问redis的数据库访问层
 */
public class RedisDao {
    /**
     * 日志记录
     */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    /**
     * 相当于数据库连接池
     */
    private final JedisPool jedisPool;

    private RuntimeSchema<Seckill> schema = RuntimeSchema.createFrom(Seckill.class);

    /**
     * 初始化redis
     * @param ip
     * @param port
     */
    public RedisDao(String ip,int port){
        jedisPool = new JedisPool(ip,port);
    }

    /**
     * 从redis中获取商品
     * @param seckillId
     * @return
     */
    public Seckill getSeckill(long seckillId){
        //redis操作逻辑
        try {
            Jedis jedis = jedisPool.getResource();
            try {
                /*
                redis通过键值对存取数据：key:value；
                其中实体value放入时序列化，取出时反序列化；
                 */
                String key = "seckill:"+seckillId;
                //并没有实现内部序列化问题,可以通过让RedisDao实现Serializable接口（不推荐），
                // 我们采用自定义序列化方式 protostuff，创建快，内存占用低
                //get->bytes()->反序列化->Object(seckill)
                byte[] bytes = jedis.get(key.getBytes());
                //从缓存中获取
                if(bytes!=null){
                    //创建一个空对象
                    Seckill seckill = schema.newMessage();
                    //seckill被反序列化,空对象成为实体：Object(null)->Object(seckill)
                    ProtostuffIOUtil.mergeFrom(bytes,seckill,schema);
                    return seckill;
                }

            } finally {
                jedis.close();
            }

        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
        return null;
    }

    /**
     * 将商品存入redis
     * @param seckill
     * @return
     */
    public String putSeckill(Seckill seckill){
        //put->Object(seckill)->序列化->bytes()
        try {
            Jedis jedis = jedisPool.getResource();
            try {
                String key = "seckill:"+seckill.getSeckillId();
                byte[] bytes = ProtostuffIOUtil.toByteArray(seckill, schema, LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
                //超时缓存
                int timeout = 60*60;//一小时
                String result = jedis.setex(key.getBytes(), timeout, bytes);
                return result;
            } finally {
                jedis.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
        return null;
    }

}
