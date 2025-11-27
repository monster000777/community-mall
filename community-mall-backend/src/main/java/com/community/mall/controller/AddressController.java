package com.community.mall.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.community.mall.common.Result;
import com.community.mall.entity.Address;
import com.community.mall.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 收货地址控制器
 */
@Tag(name = "收货地址", description = "收货地址管理相关接口")
@RestController
@RequestMapping("/address")
@Validated
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    /**
     * 获取用户的所有地址
     */
    @Operation(summary = "获取地址列表", description = "获取当前用户的所有收货地址")
    @GetMapping("/list")
    public Result<List<Address>> getAddressList() {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            List<Address> addressList = addressService.getUserAddressList(userId);
            return Result.success(addressList);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取地址详情
     */
    @Operation(summary = "获取地址详情", description = "根据ID获取收货地址详细信息")
    @GetMapping("/{id}")
    public Result<Address> getAddressById(@Parameter(description = "地址ID") @PathVariable Long id) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            Address address = addressService.getAddressById(id, userId);
            if (address == null) {
                return Result.error("地址不存在");
            }
            return Result.success(address);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 添加地址
     */
    @Operation(summary = "添加收货地址", description = "添加新的收货地址")
    @PostMapping("/add")
    public Result<Void> addAddress(@RequestBody @Validated AddressForm form) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            Address address = new Address();
            address.setReceiverName(form.getReceiverName());
            address.setReceiverPhone(form.getReceiverPhone());
            address.setProvince(form.getProvince());
            address.setCity(form.getCity());
            address.setDistrict(form.getDistrict());
            address.setDetail(form.getDetail());
            address.setIsDefault(form.getIsDefault() != null ? form.getIsDefault() : 0);

            addressService.addAddress(address, userId);
            return Result.success("添加成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新地址
     */
    @Operation(summary = "更新收货地址", description = "更新指定的收货地址信息")
    @PutMapping("/{id}")
    public Result<Void> updateAddress(@Parameter(description = "地址ID") @PathVariable Long id,
            @RequestBody @Validated AddressForm form) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            Address address = new Address();
            address.setId(id);
            address.setReceiverName(form.getReceiverName());
            address.setReceiverPhone(form.getReceiverPhone());
            address.setProvince(form.getProvince());
            address.setCity(form.getCity());
            address.setDistrict(form.getDistrict());
            address.setDetail(form.getDetail());
            address.setIsDefault(form.getIsDefault() != null ? form.getIsDefault() : 0);

            addressService.updateAddress(address, userId);
            return Result.success("更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除地址
     */
    @Operation(summary = "删除收货地址", description = "删除指定的收货地址")
    @DeleteMapping("/{id}")
    public Result<Void> deleteAddress(@Parameter(description = "地址ID") @PathVariable Long id) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            addressService.deleteAddress(id, userId);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 设置默认地址
     */
    @Operation(summary = "设置默认地址", description = "将指定地址设为默认收货地址")
    @PutMapping("/{id}/default")
    public Result<Void> setDefaultAddress(@Parameter(description = "地址ID") @PathVariable Long id) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            addressService.setDefaultAddress(id, userId);
            return Result.success("设置成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取默认地址
     */
    @Operation(summary = "获取默认地址", description = "获取当前用户的默认收货地址")
    @GetMapping("/default")
    public Result<Address> getDefaultAddress() {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            Address address = addressService.getDefaultAddress(userId);
            return Result.success(address);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 地址表单
     */
    public static class AddressForm {
        @NotBlank(message = "收货人姓名不能为空")
        private String receiverName;

        @NotBlank(message = "收货人电话不能为空")
        private String receiverPhone;

        @NotBlank(message = "省份不能为空")
        private String province;

        @NotBlank(message = "城市不能为空")
        private String city;

        @NotBlank(message = "区县不能为空")
        private String district;

        @NotBlank(message = "详细地址不能为空")
        private String detail;

        private Integer isDefault;

        // Getters and Setters
        public String getReceiverName() {
            return receiverName;
        }

        public void setReceiverName(String receiverName) {
            this.receiverName = receiverName;
        }

        public String getReceiverPhone() {
            return receiverPhone;
        }

        public void setReceiverPhone(String receiverPhone) {
            this.receiverPhone = receiverPhone;
        }

        public String getProvince() {
            return province;
        }

        public void setProvince(String province) {
            this.province = province;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getDistrict() {
            return district;
        }

        public void setDistrict(String district) {
            this.district = district;
        }

        public String getDetail() {
            return detail;
        }

        public void setDetail(String detail) {
            this.detail = detail;
        }

        public Integer getIsDefault() {
            return isDefault;
        }

        public void setIsDefault(Integer isDefault) {
            this.isDefault = isDefault;
        }
    }
}
