package com.community.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.mall.entity.Address;
import com.community.mall.mapper.AddressMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 收货地址服务类
 */
@Service
public class AddressService {
    
    @Autowired
    private AddressMapper addressMapper;
    
    /**
     * 获取用户的所有地址
     */
    public List<Address> getUserAddressList(Long userId) {
        LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Address::getUserId, userId)
               .orderByDesc(Address::getIsDefault)
               .orderByDesc(Address::getCreatedAt);
        return addressMapper.selectList(wrapper);
    }
    
    /**
     * 获取地址详情
     */
    public Address getAddressById(Long id, Long userId) {
        Address address = addressMapper.selectById(id);
        if (address != null && !address.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问此地址");
        }
        return address;
    }
    
    /**
     * 添加地址
     */
    @Transactional(rollbackFor = Exception.class)
    public void addAddress(Address address, Long userId) {
        address.setUserId(userId);
        address.setStatus(1);
        
        // 如果设置为默认地址，先取消其他默认地址
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            cancelOtherDefault(userId);
        } else {
            address.setIsDefault(0);
        }
        
        addressMapper.insert(address);
    }
    
    /**
     * 更新地址
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateAddress(Address address, Long userId) {
        Address existAddress = addressMapper.selectById(address.getId());
        if (existAddress == null || !existAddress.getUserId().equals(userId)) {
            throw new RuntimeException("地址不存在或无权修改");
        }
        
        // 如果设置为默认地址，先取消其他默认地址
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            cancelOtherDefault(userId);
        }
        
        addressMapper.updateById(address);
    }
    
    /**
     * 删除地址
     */
    public void deleteAddress(Long id, Long userId) {
        Address address = addressMapper.selectById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new RuntimeException("地址不存在或无权删除");
        }
        addressMapper.deleteById(id);
    }
    
    /**
     * 设置默认地址
     */
    @Transactional(rollbackFor = Exception.class)
    public void setDefaultAddress(Long id, Long userId) {
        Address address = addressMapper.selectById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new RuntimeException("地址不存在或无权修改");
        }
        
        // 取消其他默认地址
        cancelOtherDefault(userId);
        
        // 设置当前地址为默认
        address.setIsDefault(1);
        addressMapper.updateById(address);
    }
    
    /**
     * 获取默认地址
     */
    public Address getDefaultAddress(Long userId) {
        LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Address::getUserId, userId)
               .eq(Address::getIsDefault, 1)
               .last("LIMIT 1");
        return addressMapper.selectOne(wrapper);
    }
    
    /**
     * 取消其他默认地址
     */
    private void cancelOtherDefault(Long userId) {
        LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Address::getUserId, userId)
               .eq(Address::getIsDefault, 1);
        List<Address> defaultAddresses = addressMapper.selectList(wrapper);
        
        for (Address addr : defaultAddresses) {
            addr.setIsDefault(0);
            addressMapper.updateById(addr);
        }
    }
}

