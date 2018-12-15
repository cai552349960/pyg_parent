package cn.caichiunlin.pay.service;

import java.util.Map;

public interface WeiXinPayService {
    public Map createNative(String out_trade_no, String total_fee);
    public Map queryPayStatus(String out_trade_no);
}
