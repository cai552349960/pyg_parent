package cn.caichunlin.cart.controller;

import cn.caichiunlin.pay.service.WeiXinPayService;
import cn.caichunlin.order.service.OrderService;
import cn.caichunlin.pojo.TbPayLog;
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
    private WeiXinPayService weiXinPayService;
    @Reference
    private OrderService orderService;

    @RequestMapping("/createNative")
    public Map createNaative() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        TbPayLog payLog = orderService.searchPayLogFromRedis(userId);
        if (payLog != null) {
            return weiXinPayService.createNative(payLog.getOutTradeNo(), payLog.getTotalFee() + "");
        } else {
            return new HashMap();
        }

    }
    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
        Result result=null;
        int x = 0;
        while(true){
            //调用查询接口
            Map<String,String> map = weiXinPayService.queryPayStatus(out_trade_no);
            if(map==null){//出错
                result = new Result(false, "支付出错");
                orderService.updateOrderStatus(out_trade_no,map.get("transaction_id"));

                break;
            }
            if(map.get("trade_state").equals("SUCCESS")){//如果成功
                result=new  Result(true, "支付成功");
                break;
            }
            try {
                Thread.sleep(3000);//间隔三秒
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            x++;
            if (x >= 100) {
                result = new Result(false, "二维码超时");
                break;
            }
        }

        return result;
    }
}
