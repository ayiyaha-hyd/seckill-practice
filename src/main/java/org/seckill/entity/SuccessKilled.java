package org.seckill.entity;

import java.util.Date;

/**
 *订单实体
 * @author 22719
 */
public class SuccessKilled {
    /**
     * 订单商品id
     */
    private long seckillId;
    /**
     * 用户手机号
     */
    private long userPhone;
    /**
     * 订单状态
     */
    private short state;
    /**
     * 订单创建时间
     */
    private Date createTime;
    /**
     * 订单对应的商品实体，多对一（多个订单对应一个商品）
     */
    private Seckill seckill;

    public long getSeckillId() {
        return seckillId;
    }

    public void setSeckillId(long seckillId) {
        this.seckillId = seckillId;
    }

    public long getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(long userPhone) {
        this.userPhone = userPhone;
    }

    public short getState() {
        return state;
    }

    public void setState(short state) {
        this.state = state;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Seckill getSeckill() {
        return seckill;
    }

    public void setSeckill(Seckill seckill) {
        this.seckill = seckill;
    }

    @Override
    public String toString() {
        return "SuccessKilled{" +
                "seckillId=" + seckillId +
                ", userPhone=" + userPhone +
                ", state=" + state +
                ", createTime=" + createTime +
                '}';
    }
}
