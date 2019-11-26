package org.seckill.web;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.dto.SeckillResult;
import org.seckill.entity.Seckill;
import org.seckill.enums.SeckillStatEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 请求处理层
 */
@Controller
@RequestMapping("/seckill")//url:模块/资源/{}/细分
public class SeckillController
{
    @Autowired
    private SeckillService seckillService;

    /**
     * 返回商品列表页
     * @param model
     * @return
     */
    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public String list(Model model)
    {
        //list.jsp+mode=ModelAndView
        //获取列表页
        List<Seckill> list=seckillService.getSeckillList();
        model.addAttribute("list",list);
        return "list";
    }

    /**
     * 商品秒杀详情页
     * @param seckillId
     * @param model
     * @return
     */
    @RequestMapping(value = "/{seckillId}/detail",method = RequestMethod.GET)
    public String detail(@PathVariable("seckillId") Long seckillId, Model model)
    {
        if (seckillId == null)
        {
            return "redirect:/seckill/list";
        }

        Seckill seckill=seckillService.getById(seckillId);
        if (seckill==null)
        {
            return "forward:/seckill/list";
        }

        model.addAttribute("seckill",seckill);

        return "detail";
    }

    /**
     * ajax ,json暴露秒杀接口的方法
     * @param seckillId
     * @return
     */
    @RequestMapping(value = "/{seckillId}/exposer",
            method = RequestMethod.GET,
            produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public SeckillResult<Exposer> exposer(@PathVariable("seckillId") Long seckillId)
    {
        SeckillResult<Exposer> result;
        try{
            //获取秒杀接口地址
            Exposer exposer=seckillService.exportSeckillUrl(seckillId);
            //将秒杀地址封装到json返回结果
            result=new SeckillResult<Exposer>(true,exposer);
        }catch (Exception e)
        {
            //获取秒杀接口地址失败
            e.printStackTrace();
            result=new SeckillResult<Exposer>(false,e.getMessage());
        }

        //返回json数据（秒杀接口地址获取失败）
        return result;
    }

    /**
     * 执行秒杀操作
     * @param seckillId
     * @param md5
     * @param userPhone
     * @return
     */
    @RequestMapping(value = "/{seckillId}/{md5}/execution",
            method = RequestMethod.POST,
            produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public SeckillResult<SeckillExecution> execute(@PathVariable("seckillId") Long seckillId,
                                                   @PathVariable("md5") String md5,
                                                   @CookieValue(value = "userPhone",required = false) Long userPhone)
    {
        //判断用户是否登录（cookie是否存有用户手机号）
        if (userPhone==null)
        {
            return new SeckillResult<SeckillExecution>(false,"未注册");
        }

        try {
            /*
            这里改为调用存储过程，将事务操作从Jav客户端转移到mysql服务器端
            SeckillExecution execution = seckillService.executeSeckill(seckillId, userPhone, md5);
             */

            //执行秒杀操作
            SeckillExecution execution = seckillService.executeSeckillProcedure(seckillId, userPhone, md5);
            //将秒杀结果封装为json返回
            return new SeckillResult<SeckillExecution>(true, execution);
        }catch (RepeatKillException e1)
        {
            SeckillExecution execution=new SeckillExecution(seckillId, SeckillStatEnum.REPEAT_KILL);
            //将秒杀结果封装返回
            return new SeckillResult<SeckillExecution>(true,execution);
        }catch (SeckillCloseException e2)
        {
            SeckillExecution execution=new SeckillExecution(seckillId, SeckillStatEnum.END);
            //将秒杀结果封装返回
            return new SeckillResult<SeckillExecution>(true,execution);
        }
        catch (Exception e)
        {
            SeckillExecution execution=new SeckillExecution(seckillId, SeckillStatEnum.INNER_ERROR);
            //将秒杀结果封装返回
            return new SeckillResult<SeckillExecution>(true,execution);
        }
    }

    //获取系统时间
    @RequestMapping(value = "/time/now",method = RequestMethod.GET)
    @ResponseBody
    public SeckillResult<Long> time()
    {
        Date now=new Date();
        return new SeckillResult<Long>(true,now.getTime());
    }
}
