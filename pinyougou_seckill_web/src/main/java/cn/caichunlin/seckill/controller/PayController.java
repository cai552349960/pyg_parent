package cn.caichunlin.seckill.controller;

import cn.caichiunlin.pay.service.WeiXinPayService;
import cn.caichunlin.pojo.TbSeckillOrder;
import cn.caichunlin.seckill.service.SeckillOrderService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Reference
    private  WeiXinPayService weixinPayService;

    @Reference
    private SeckillOrderService seckillOrderService;

    /**
     * 生成二维码
     * @return
     */
    @RequestMapping("/createNative")
    public Map createNative(){
        //获取当前用户
        String userId= SecurityContextHolder.getContext().getAuthentication().getName();
        //到redis查询秒杀订单
        TbSeckillOrder seckillOrder = seckillOrderService.searchOrderFromRedisByUserId(userId);
        //判断秒杀订单存在
        if(seckillOrder!=null){
            long fen=  (long)(seckillOrder.getMoney().doubleValue()*100);//金额（分）
            return weixinPayService.createNative(seckillOrder.getId()+"",+fen+"");
        }else{
            return new HashMap();
        }
    }

    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
        //获取当前用户
        String userId=SecurityContextHolder.getContext().getAuthentication().getName();
        Result result=null;
        int x=0;
        while(true){
            //调用查询接口
            Map<String,String> map = weixinPayService.queryPayStatus(out_trade_no);
            if(map==null){//出错
                result=new  Result(false, "支付出错");
                break;
            }
            if(map.get("trade_state").equals("SUCCESS")){//如果成功
                result=new  Result(true, "支付成功");
                seckillOrderService.saveOrderFromRedisToDb(userId, Long.valueOf(out_trade_no), map.get("transaction_id"));
                break;
            }
            try {
                Thread.sleep(3000);//间隔三秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            x++;//设置超时时间为5分钟
            if(x>100){
                result=new Result(false, "二维码超时");
                break;
            }
        }
        return result;
    }
}
