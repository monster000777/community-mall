-- ====================================
-- 社区团购系统数据库设计
-- ====================================

DROP DATABASE IF EXISTS community_mall;
CREATE DATABASE community_mall CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE community_mall;

-- 用户表
CREATE TABLE `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码（加密）',
    `phone` VARCHAR(20) NOT NULL UNIQUE COMMENT '手机号',
    `email` VARCHAR(100) COMMENT '邮箱',
    `nickname` VARCHAR(50) COMMENT '昵称',
    `avatar` VARCHAR(255) COMMENT '头像URL',
    `role_id` BIGINT DEFAULT 2 COMMENT '角色ID（1-管理员，2-普通用户）',
    `status` TINYINT DEFAULT 1 COMMENT '状态（0-禁用，1-启用）',
    `deleted_flag` TINYINT DEFAULT 0 COMMENT '删除标记（0-未删除，1-已删除）',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (`username`),
    INDEX idx_phone (`phone`),
    INDEX idx_role_id (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE `role` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `role_key` VARCHAR(50) NOT NULL UNIQUE COMMENT '角色标识',
    `status` TINYINT DEFAULT 1 COMMENT '状态',
    `deleted_flag` TINYINT DEFAULT 0 COMMENT '删除标记',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 权限表
CREATE TABLE `permission` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
    `permission_name` VARCHAR(50) NOT NULL COMMENT '权限名称',
    `permission_key` VARCHAR(100) NOT NULL UNIQUE COMMENT '权限标识',
    `status` TINYINT DEFAULT 1,
    `deleted_flag` TINYINT DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 角色权限关联表
CREATE TABLE `role_permission` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `role_id` BIGINT NOT NULL,
    `permission_id` BIGINT NOT NULL,
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 商品分类表
CREATE TABLE `category` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    `category_name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父分类ID（0表示一级分类）',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `icon` VARCHAR(255) COMMENT '图标',
    `status` TINYINT DEFAULT 1,
    `deleted_flag` TINYINT DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_parent_id (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 商品表
CREATE TABLE `product` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商品ID',
    `product_name` VARCHAR(100) NOT NULL COMMENT '商品名称',
    `category_id` BIGINT NOT NULL COMMENT '分类ID',
    `description` TEXT COMMENT '商品描述',
    `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
    `stock` INT NOT NULL DEFAULT 0 COMMENT '库存',
    `sales` INT DEFAULT 0 COMMENT '销量',
    `main_image` VARCHAR(255) COMMENT '主图',
    `images` TEXT COMMENT '商品图片（多张，JSON数组）',
    `is_on_sale` TINYINT DEFAULT 1 COMMENT '是否上架（0-下架，1-上架）',
    `status` TINYINT DEFAULT 1,
    `deleted_flag` TINYINT DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category_id (`category_id`),
    INDEX idx_product_name (`product_name`),
    INDEX idx_is_on_sale (`is_on_sale`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 购物车表
CREATE TABLE `cart` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '购物车ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `quantity` INT NOT NULL DEFAULT 1 COMMENT '数量',
    `selected` TINYINT DEFAULT 1 COMMENT '是否选中（0-未选，1-已选）',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
    INDEX idx_user_id (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- 收货地址表
CREATE TABLE `address` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '地址ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `receiver_name` VARCHAR(50) NOT NULL COMMENT '收货人姓名',
    `receiver_phone` VARCHAR(20) NOT NULL COMMENT '收货人电话',
    `province` VARCHAR(50) NOT NULL COMMENT '省份',
    `city` VARCHAR(50) NOT NULL COMMENT '城市',
    `district` VARCHAR(50) NOT NULL COMMENT '区县',
    `detail` VARCHAR(200) NOT NULL COMMENT '详细地址',
    `is_default` TINYINT DEFAULT 0 COMMENT '是否默认地址',
    `status` TINYINT DEFAULT 1,
    `deleted_flag` TINYINT DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货地址表';

-- 订单主表
CREATE TABLE `order_master` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    `order_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '订单编号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `total_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    `actual_amount` DECIMAL(10,2) NOT NULL COMMENT '实付金额',
    `payment_type` TINYINT DEFAULT 1 COMMENT '支付方式（1-在线支付，2-货到付款）',
    `payment_time` DATETIME COMMENT '支付时间',
    `order_status` TINYINT DEFAULT 1 COMMENT '订单状态（1-待支付，2-已支付，3-已发货，4-已完成，5-已取消，6-退款中，7-已退款）',
    `receiver_name` VARCHAR(50) NOT NULL COMMENT '收货人',
    `receiver_phone` VARCHAR(20) NOT NULL COMMENT '收货电话',
    `receiver_address` VARCHAR(300) NOT NULL COMMENT '收货地址',
    `remark` VARCHAR(500) COMMENT '订单备注',
    `status` TINYINT DEFAULT 1,
    `deleted_flag` TINYINT DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_order_no (`order_no`),
    INDEX idx_user_id (`user_id`),
    INDEX idx_order_status (`order_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单主表';

-- 订单明细表
CREATE TABLE `order_item` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单明细ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `product_name` VARCHAR(100) NOT NULL COMMENT '商品名称',
    `product_image` VARCHAR(255) COMMENT '商品图片',
    `price` DECIMAL(10,2) NOT NULL COMMENT '商品单价',
    `quantity` INT NOT NULL COMMENT '购买数量',
    `total_price` DECIMAL(10,2) NOT NULL COMMENT '小计金额',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_order_id (`order_id`),
    INDEX idx_product_id (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- 团购活动表
CREATE TABLE `group_activity` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '活动ID',
    `activity_name` VARCHAR(100) NOT NULL COMMENT '活动名称',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `group_price` DECIMAL(10,2) NOT NULL COMMENT '团购价格',
    `min_people` INT DEFAULT 2 COMMENT '成团最少人数',
    `limit_per_user` INT DEFAULT 1 COMMENT '每人限购数量',
    `stock` INT NOT NULL COMMENT '活动库存',
    `start_time` DATETIME NOT NULL COMMENT '开始时间',
    `end_time` DATETIME NOT NULL COMMENT '结束时间',
    `status` TINYINT DEFAULT 1 COMMENT '活动状态（0-未开始，1-进行中，2-已结束）',
    `deleted_flag` TINYINT DEFAULT 0,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_product_id (`product_id`),
    INDEX idx_time (`start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团购活动表';

-- 团购订单表（关联团购活动和普通订单）
CREATE TABLE `group_order` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '团购订单ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `activity_id` BIGINT NOT NULL COMMENT '团购活动ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `quantity` INT NOT NULL COMMENT '购买数量',
    `group_price` DECIMAL(10,2) NOT NULL COMMENT '团购价格（订单时的价格）',
    `total_price` DECIMAL(10,2) NOT NULL COMMENT '总价',
    `status` TINYINT DEFAULT 1 COMMENT '状态（1-待支付，2-已支付，3-已完成，4-已取消）',
    `deleted_flag` TINYINT DEFAULT 0 COMMENT '删除标记',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_order_id (`order_id`),
    INDEX idx_activity_id (`activity_id`),
    INDEX idx_user_id (`user_id`),
    INDEX idx_status (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团购订单表';

-- 团购参与者表（统计用户参与情况）
CREATE TABLE `group_participant` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '参与者ID',
    `activity_id` BIGINT NOT NULL COMMENT '团购活动ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `total_quantity` INT DEFAULT 0 COMMENT '累计购买数量',
    `last_order_time` DATETIME COMMENT '最后下单时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '首次参与时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_activity_user` (`activity_id`, `user_id`),
    INDEX idx_activity_id (`activity_id`),
    INDEX idx_user_id (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='团购参与者表';

-- 初始化数据
INSERT INTO `role` (`id`, `role_name`, `role_key`) VALUES
(1, '管理员', 'admin'),
(2, '普通用户', 'user');

INSERT INTO `permission` (`permission_name`, `permission_key`) VALUES
('用户管理', 'system:user:manage'),
('商品管理', 'system:product:manage'),
('订单管理', 'system:order:manage'),
('活动管理', 'system:activity:manage');

INSERT INTO `role_permission` (`role_id`, `permission_id`) VALUES
(1, 1), (1, 2), (1, 3), (1, 4);

-- 插入默认管理员账号（密码：123456，需要加密）
INSERT INTO `user` (`username`, `password`, `phone`, `nickname`, `role_id`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '13800138000', '系统管理员', 1);

-- 插入测试分类
INSERT INTO `category` (`category_name`, `parent_id`, `sort_order`) VALUES
('生鲜水果', 0, 1),
('蔬菜豆制品', 0, 2),
('肉禽蛋品', 0, 3),
('休闲零食', 0, 4),
('粮油调味', 0, 5),
('酒水饮料', 0, 6);

-- 插入测试商品(使用真实网络图片)
INSERT INTO `product` (`product_name`, `category_id`, `description`, `price`, `stock`, `main_image`, `is_on_sale`) VALUES
-- 生鲜水果 (category_id = 1)
('新鲜苹果', 1, '新鲜红富士苹果,香甜可口', 12.80, 100, 'https://images.unsplash.com/photo-1560806887-1e4cd0b6cbd6?w=400&h=400&fit=crop', 1),
('新鲜草莓', 1, '当季新鲜草莓,甜度高', 28.00, 60, 'https://images.unsplash.com/photo-1464965911861-746a04b4bca6?w=400&h=400&fit=crop', 1),
('进口香蕉', 1, '菲律宾进口香蕉,软糯香甜', 9.90, 150, 'https://images.unsplash.com/photo-1603833665858-e61d17a86224?w=400&h=400&fit=crop', 1),
('新鲜橙子', 1, '赣南脐橙,果肉饱满多汁', 15.80, 80, 'https://images.unsplash.com/photo-1547514701-42782101795e?w=400&h=400&fit=crop', 1),
('猕猴桃', 1, '新西兰奇异果,酸甜可口', 32.00, 45, 'https://images.unsplash.com/photo-1585059895524-72359e06133a?w=400&h=400&fit=crop', 1),
('新鲜葡萄', 1, '阳光玫瑰葡萄,粒大饱满', 45.00, 35, 'https://images.unsplash.com/photo-1537640538966-79f369143f8f?w=400&h=400&fit=crop', 1),
('新鲜芒果', 1, '海南贵妃芒,香甜多汁', 22.00, 55, 'https://images.unsplash.com/photo-1601493700631-2b16ec4b4716?w=400&h=400&fit=crop', 1),

-- 蔬菜豆制品 (category_id = 2)
('有机西红柿', 2, '本地有机西红柿,新鲜采摘', 8.50, 80, 'https://images.unsplash.com/photo-1546094096-0df4bcaaa337?w=400&h=400&fit=crop', 1),
('有机黄瓜', 2, '新鲜有机黄瓜,脆嫩可口', 6.50, 90, 'https://images.unsplash.com/photo-1604977042946-1eecc30f269e?w=400&h=400&fit=crop', 1),
('新鲜生菜', 2, '水培生菜,清脆爽口', 5.80, 100, 'https://images.unsplash.com/photo-1622206151226-18ca2c9ab4a1?w=400&h=400&fit=crop', 1),
('有机菠菜', 2, '有机种植菠菜,营养丰富', 7.50, 70, 'https://images.unsplash.com/photo-1576045057995-568f588f82fb?w=400&h=400&fit=crop', 1),
('新鲜土豆', 2, '优质土豆,面甜可口', 4.50, 200, 'https://images.unsplash.com/photo-1518977676601-b53f82aba655?w=400&h=400&fit=crop', 1),
('嫩豆腐', 2, '新鲜嫩豆腐,口感细腻', 3.50, 60, 'https://images.unsplash.com/photo-1541529086526-db283c563270?w=400&h=400&fit=crop', 1),
('新鲜胡萝卜', 2, '有机胡萝卜,脆甜多汁', 5.00, 120, 'https://images.unsplash.com/photo-1598170845058-32b9d6a5da37?w=400&h=400&fit=crop', 1),

-- 肉禽蛋品 (category_id = 3)
('土鸡蛋', 3, '农家散养土鸡蛋,营养丰富', 25.00, 50, 'https://images.unsplash.com/photo-1582722872445-44dc5f7e3c8f?w=400&h=400&fit=crop', 1),
('五花肉', 3, '新鲜五花肉,肥瘦适中', 32.00, 40, 'https://images.unsplash.com/photo-1602470520998-f4a52199a3d6?w=400&h=400&fit=crop', 1),
('鸡胸肉', 3, '新鲜鸡胸肉,低脂高蛋白', 18.00, 65, 'https://images.unsplash.com/photo-1604503468506-a8da13d82791?w=400&h=400&fit=crop', 1),
('新鲜排骨', 3, '精选猪排骨,肉质鲜嫩', 38.00, 45, 'https://images.unsplash.com/photo-1529692236671-f1f6cf9683ba?w=400&h=400&fit=crop', 1),
('鸭蛋', 3, '新鲜鸭蛋,个大饱满', 20.00, 55, 'https://images.unsplash.com/photo-1506976785307-8732e854ad03?w=400&h=400&fit=crop', 1),
('牛腱子肉', 3, '优质牛腱子,适合炖煮', 68.00, 30, 'https://images.unsplash.com/photo-1607623814075-e51df1bdc82f?w=400&h=400&fit=crop', 1),

-- 休闲零食 (category_id = 4)
('坚果礼盒', 4, '精选混合坚果,健康零食', 58.00, 30, 'https://images.unsplash.com/photo-1599599810769-bcde5a160d32?w=400&h=400&fit=crop', 1),
('每日坚果', 4, '每日坚果小包装,方便携带', 39.90, 80, 'https://images.unsplash.com/photo-1608797178974-15b35a64ede9?w=400&h=400&fit=crop', 1),
('薯片', 4, '原味薯片,香脆可口', 12.50, 100, 'https://images.unsplash.com/photo-1566478989037-eec170784d0b?w=400&h=400&fit=crop', 1),
('牛肉干', 4, '手撕牛肉干,劲道美味', 45.00, 50, 'https://images.unsplash.com/photo-1529692236671-f1f6cf9683ba?w=400&h=400&fit=crop', 1),
('巧克力', 4, '进口黑巧克力,浓郁醇香', 28.00, 70, 'https://images.unsplash.com/photo-1511381939415-e44015466834?w=400&h=400&fit=crop', 1),
('饼干礼盒', 4, '多口味饼干组合,送礼佳品', 52.00, 40, 'https://images.unsplash.com/photo-1558961363-fa8fdf82db35?w=400&h=400&fit=crop', 1),

-- 粮油调味 (category_id = 5)
('东北大米', 5, '优质东北大米5kg装', 38.00, 120, 'https://images.unsplash.com/photo-1586201375761-83865001e31c?w=400&h=400&fit=crop', 1),
('泰国香米', 5, '进口泰国香米,米粒饱满', 45.00, 80, 'https://images.unsplash.com/photo-1516684732162-798a0062be99?w=400&h=400&fit=crop', 1),
('花生油', 5, '压榨花生油5L装,香味浓郁', 88.00, 60, 'https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=400&h=400&fit=crop', 1),
('生抽酱油', 5, '特级生抽,提鲜调味', 15.80, 100, 'https://images.unsplash.com/photo-1626200419199-391ae4be7a41?w=400&h=400&fit=crop', 1),
('食用盐', 5, '精制食用盐,纯净健康', 3.50, 200, 'https://images.unsplash.com/photo-1532336414038-cf19250c5757?w=400&h=400&fit=crop', 1),
('白砂糖', 5, '优质白砂糖,烘焙必备', 8.00, 150, 'https://images.unsplash.com/photo-1587735243615-c03f25aaff15?w=400&h=400&fit=crop', 1),

-- 酒水饮料 (category_id = 6)
('矿泉水', 6, '天然矿泉水整箱24瓶', 28.00, 100, 'https://images.unsplash.com/photo-1548839140-29a749e1cf4d?w=400&h=400&fit=crop', 1),
('橙汁饮料', 6, '100%纯果汁,无添加', 18.00, 80, 'https://images.unsplash.com/photo-1600271886742-f049cd451bba?w=400&h=400&fit=crop', 1),
('牛奶', 6, '纯牛奶1L装,营养丰富', 12.50, 120, 'https://images.unsplash.com/photo-1563636619-e9143da7973b?w=400&h=400&fit=crop', 1),
('绿茶饮料', 6, '无糖绿茶,清爽解渴', 8.50, 150, 'https://images.unsplash.com/photo-1556679343-c7306c1976bc?w=400&h=400&fit=crop', 1),
('酸奶', 6, '风味酸奶,益生菌发酵', 15.00, 90, 'https://images.unsplash.com/photo-1488477181946-6428a0291777?w=400&h=400&fit=crop', 1),
('可乐', 6, '经典可乐整箱12罐', 32.00, 70, 'https://images.unsplash.com/photo-1554866585-cd94860890b7?w=400&h=400&fit=crop', 1);

-- ====================================
-- 团购活动测试数据
-- ====================================

-- 插入团购活动测试数据
INSERT INTO `group_activity` (`activity_name`, `product_id`, `group_price`, `min_people`, `limit_per_user`, `stock`, `start_time`, `end_time`, `status`) VALUES
-- 进行中的活动
('新鲜草莓限时拼团', 2, 19.90, 3, 2, 100, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 2 DAY), 1),
('进口香蕉团购特惠', 3, 6.90, 2, 3, 200, DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_ADD(NOW(), INTERVAL 1 DAY), 1),
('阳光玫瑰葡萄拼团', 6, 32.00, 5, 1, 50, DATE_SUB(NOW(), INTERVAL 3 HOUR), DATE_ADD(NOW(), INTERVAL 3 DAY), 1),
('土鸡蛋拼团优惠', 15, 18.00, 3, 2, 80, DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 2 DAY), 1),
('坚果礼盒团购', 23, 42.00, 4, 1, 60, NOW(), DATE_ADD(NOW(), INTERVAL 5 DAY), 1),

-- 未开始的活动
('东北大米限时团', 31, 28.00, 3, 2, 150, DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 7 DAY), 0),
('花生油团购活动', 33, 68.00, 5, 1, 80, DATE_ADD(NOW(), INTERVAL 2 DAY), DATE_ADD(NOW(), INTERVAL 9 DAY), 0),
('矿泉水整箱拼团', 37, 22.00, 2, 3, 200, DATE_ADD(NOW(), INTERVAL 3 DAY), DATE_ADD(NOW(), INTERVAL 10 DAY), 0),

-- 已结束的活动
('新鲜苹果团购', 1, 9.80, 2, 2, 0, DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), 2),
('有机西红柿拼团', 8, 6.50, 3, 2, 0, DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY), 2);
