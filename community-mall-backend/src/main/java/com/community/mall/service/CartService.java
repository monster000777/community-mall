package com.community.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.mall.entity.Cart;
import com.community.mall.entity.Product;
import com.community.mall.mapper.CartMapper;
import com.community.mall.mapper.ProductMapper;
import com.community.mall.vo.CartVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 购物车服务类
 */
@Service
public class CartService {
    
    @Autowired
    private CartMapper cartMapper;
    
    @Autowired
    private ProductMapper productMapper;
    
    /**
     * 添加商品到购物车
     */
    public void addToCart(Long userId, Long productId, Integer quantity) {
        // 检查购物车中是否已有该商品
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId)
               .eq(Cart::getProductId, productId);
        Cart existCart = cartMapper.selectOne(wrapper);
        
        if (existCart != null) {
            // 已存在，更新数量
            existCart.setQuantity(existCart.getQuantity() + quantity);
            cartMapper.updateById(existCart);
        } else {
            // 不存在，新增
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(productId);
            cart.setQuantity(quantity);
            cart.setSelected(1);
            cartMapper.insert(cart);
        }
    }
    
    /**
     * 获取购物车列表
     */
    public List<CartVo> getCartList(Long userId) {
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        List<Cart> cartList = cartMapper.selectList(wrapper);
        
        List<CartVo> cartVoList = new ArrayList<>();
        for (Cart cart : cartList) {
            Product product = productMapper.selectById(cart.getProductId());
            if (product != null) {
                CartVo cartVo = new CartVo();
                BeanUtils.copyProperties(cart, cartVo);
                cartVo.setProductName(product.getProductName());
                cartVo.setPrice(product.getPrice());
                cartVo.setMainImage(product.getMainImage());
                cartVo.setStock(product.getStock());
                cartVoList.add(cartVo);
            }
        }
        
        return cartVoList;
    }
    
    /**
     * 更新购物车商品数量
     */
    public void updateCartQuantity(Long userId, Long cartId, Integer quantity) {
        Cart cart = cartMapper.selectById(cartId);
        if (cart != null && cart.getUserId().equals(userId)) {
            cart.setQuantity(quantity);
            cartMapper.updateById(cart);
        }
    }
    
    /**
     * 删除购物车商品
     */
    public void deleteCart(Long userId, Long cartId) {
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getId, cartId)
               .eq(Cart::getUserId, userId);
        cartMapper.delete(wrapper);
    }
    
    /**
     * 清空购物车
     */
    public void clearCart(Long userId) {
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        cartMapper.delete(wrapper);
    }
}

