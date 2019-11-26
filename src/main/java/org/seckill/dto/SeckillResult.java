package org.seckill.dto;

/**
 * 封装json结果
 * @param <T>
 */
public class SeckillResult<T> {
    /**
     * success表示秒杀请求是否成功，不表示秒杀结果是否成功
     */
    private boolean success;
    /**
     * 数据
     */
    private T data;
    /**
     * 错误信息
     */
    private String error;

    //两种构造方法

    /**
     *成功，返回数据
     * @param success
     * @param data
     */
    public SeckillResult(boolean success, T data) {
        this.success = success;
        this.data = data;
    }

    /**
     *失败，返回错误信息
     * @param success
     * @param error
     */
    public SeckillResult(boolean success, String error) {
        this.success = success;
        this.error = error;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
