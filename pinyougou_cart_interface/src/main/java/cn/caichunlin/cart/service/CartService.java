package cn.caichunlin.cart.service;

import cn.caichunlin.pojogroup.Cart;

import java.util.List;

public interface CartService {
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num);

    public List<Cart> findCartListFromRedis(String username);

    public void saveCartListToRedis(String username, List<Cart> cartList);

    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2);

}
