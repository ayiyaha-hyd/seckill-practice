package org.seckill.dto;

/**
 * 封装暴露的秒杀地址
 */
public class Exposer {
    /**
     * 秒杀是否可以暴露
     */
    private boolean exposed;
    /**
     * 一种加密措施
     */
    private String md5;
    /**
     * 商品id
     */
    private long seckillId;
    /**
     * 系统当前时间，如果秒杀未开启，返回当前系统时间，方便客户端定位服务器时间，控制秒杀计时
     */
    private long now;
    /**
     * 秒杀开启时间
     */
    private long start;
    /**
     * 秒杀结束时间
     */
    private long end;

    //三种构造方法用于返回不同结果
    /**
     *可以暴露秒杀地址
     * @param exposed
     * @param md5
     * @param seckillId
     */
    public Exposer(boolean exposed, String md5, long seckillId) {
        this.exposed = exposed;
        this.md5 = md5;
        this.seckillId = seckillId;
    }

    /**
     *秒杀未开启
     * @param exposed
     * @param seckillId
     * @param now
     * @param start
     * @param end
     */
    public Exposer(boolean exposed,long seckillId, long now, long start, long end) {
        this.exposed = exposed;
        this.seckillId = seckillId;
        this.now = now;
        this.start = start;
        this.end = end;
    }

    /**
     *找不到对应的商品id
     * @param exposed
     * @param seckillId
     */
    public Exposer(boolean exposed, long seckillId) {
        this.exposed = exposed;
        this.seckillId = seckillId;
    }

    public boolean isExposed() {
        return exposed;
    }

    public void setExposed(boolean exposed) {
        this.exposed = exposed;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public long getSeckillId() {
        return seckillId;
    }

    public void setSeckillId(long seckillId) {
        this.seckillId = seckillId;
    }

    public long getNow() {
        return now;
    }

    public void setNow(long now) {
        this.now = now;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    @Override
    public String toString() {
        return "Exposer{" +
                "exposed=" + exposed +
                ", md5='" + md5 + '\'' +
                ", seckillId=" + seckillId +
                ", now=" + now +
                ", start=" + start +
                ", end=" + end +
                '}';
    }
}
