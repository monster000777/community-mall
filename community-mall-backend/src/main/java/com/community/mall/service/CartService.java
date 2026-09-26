package com.community.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.mall.entity.Cart;
import com.community.mall.entity.Product;
import com.community.mall.exception.BusinessException;
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

    /** 购物车单项数量上限 */
    private static final int MAX_QUANTITY = 999;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    /**
     * 添加商品到购物车
     */
    public void addToCart(Long userId, Long productId, Integer quantity) {
        // 数量校验：必须为正数且不超过上限
        if (quantity == null || quantity <= 0) {
            throw new BusinessException("商品数量必须大于 0");
        }
        if (quantity > MAX_QUANTITY) {
            throw new BusinessException("商品数量不能超过 " + MAX_QUANTITY);
        }

        // 商品必须存在且处于上架状态
        Product product = productMapper.selectById(productId);
        if (product == null || product.getIsOnSale() == null || product.getIsOnSale() == 0) {
            throw new BusinessException("商品不存在或已下架");
        }

        // 检查购物车中是否已有该商品
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId)
               .eq(Cart::getProductId, productId);
        Cart existCart = cartMapper.selectOne(wrapper);

        if (existCart != null) {
            // 已存在，更新数量
            int newQuantity = existCart.getQuantity() + quantity;
            if (newQuantity > MAX_QUANTITY) {
                throw new BusinessException("购物车内该商品总数不能超过 " + MAX_QUANTITY);
            }
            existCart.setQuantity(newQuantity);
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
        // 数量校验：必须为正数且不超过上限
        if (quantity == null || quantity <= 0) {
            throw new BusinessException("商品数量必须大于 0");
        }
        if (quantity > MAX_QUANTITY) {
            throw new BusinessException("商品数量不能超过 " + MAX_QUANTITY);
        }

        Cart cart = cartMapper.selectById(cartId);
        if (cart == null) {
            throw new BusinessException("购物车项不存在");
        }
        if (!cart.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此购物车项");
        }
        cart.setQuantity(quantity);
        cartMapper.updateById(cart);
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

