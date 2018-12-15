package cn.caichunlin.cart.controller;

import cn.caichunlin.Utils.CookieUtil;
import cn.caichunlin.cart.service.CartService;
import cn.caichunlin.order.service.OrderService;
import cn.caichunlin.pojo.TbOrder;
import cn.caichunlin.pojogroup.Cart;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {
    @Reference(timeout = 6000)
    private CartService cartService;
    @Autowired
    private HttpServletRequest request;
    @Reference
    private OrderService orderService;
    @Autowired
    private HttpServletResponse response;

    @RequestMapping("/findCartList")
    public List<Cart> findCartList() {
        String cartListString = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
        if (cartListString == null || cartListString.equals("")) {
            cartListString = "[]";
        }
        List<Cart> cartList_cookie = JSON.parseArray(cartListString, Cart.class);
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登录账户："+username+";执行了findCartList方法");
        if ("anonymousUser".equals(username)) {
            return cartList_cookie;
        } else {
            List<Cart> cartList_redis = cartService.findCartListFromRedis(username);
            if (cartList_cookie.size() > 0) {
                cartList_redis = cartService.mergeCartList(cartList_redis, cartList_cookie);
                CookieUtil.deleteCookie(request, response, "cartList");
                cartService.saveCartListToRedis(username, cartList_redis);
            }
            return cartList_redis;
        }

    }

    @RequestMapping("/addGoodsToCartList")
    @CrossOrigin(origins = "http://localhost:9105",allowCredentials = "true")
    public Result addGoodsToCartList(Long itemId, Integer num) {
       /* response.setHeader("Access-Control-Allow-Origin", "http://localhost:9105");
        response.setHeader("Access-Control-Allow-Credentials", "true");*/
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前登录账户："+name+";执行了addGoodsToCartList方法");

        try {
            List<Cart> cartList = findCartList();
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);
            if ("anonymousUser".equals(name)) {
                CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cartList), 3600 * 24, "UTF-8");
            } else {
                cartService.saveCartListToRedis(name, cartList);
            }
            return new Result(true, "添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "添加失败");
        }

    }

    @RequestMapping("/add")
    public Result add(@RequestBody TbOrder order) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        order.setUserId(username);
        order.setSourceType("2");
        try {
            orderService.add(order);
            return new Result(true, "增加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "增加失败");
        }
    }
}
