package com.community.mall.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Set;

/**
 * 图片上传安全组件
 *
 * 统一两个头像上传入口（用户端 / 管理端）的校验与存储逻辑：
 * 1. 扩展名白名单 + 文件头魔数双重校验，防止上传 html/svg 等可执行脚本伪装成图片（存储型 XSS）
 * 2. 上传目录统一走 upload.path 配置，与 UploadResourceConfig 的静态资源映射保持一致
 * 3. 文件名由服务端生成（不含用户输入），随机化避免猜测与覆盖
 */
@Component
public class ImageUploader {

    /** 允许的图片扩展名（小写，不含点） */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");

    /** 单文件大小上限：5MB */
    private static final long MAX_SIZE = 5 * 1024 * 1024L;

    @Value("${upload.path}")
    private String uploadPath;

    /**
     * 校验并保存头像图片，返回可供前端访问的相对 URL
     *
     * @param file     上传的文件
     * @param ownerKey 文件名标识（通常为用户ID）
     * @return 相对 URL，如 /api/uploads/avatar/avatar_1_1698xxxxx.jpg
     */
    public String saveAvatar(MultipartFile file, Long ownerKey) {
        String ext = validate(file);

        // 解析上传根目录（相对路径基于工作目录，与 UploadResourceConfig 逻辑一致）
        String uploadRoot = uploadPath;
        if (!new File(uploadRoot).isAbsolute()) {
            uploadRoot = System.getProperty("user.dir") + File.separator + uploadRoot;
        }
        String uploadRootCleaned = uploadRoot.replaceAll("[/\\\\]+$", "") + File.separator;

        File avatarDir = new File(uploadRootCleaned + "avatar");
        if (!avatarDir.exists() && !avatarDir.mkdirs()) {
            throw new RuntimeException("创建上传目录失败");
        }

        String filename = "avatar_" + ownerKey + "_" + System.currentTimeMillis()
                + "_" + (int) (Math.random() * 10000) + "." + ext;
        File dest = new File(avatarDir, filename);
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            throw new RuntimeException("文件保存失败", e);
        }
        return "/api/uploads/avatar/" + filename;
    }

    /**
     * 校验文件：非空、大小、扩展名白名单、魔数。通过则返回规范化的扩展名
     */
    private String validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new com.community.mall.exception.BusinessException("上传文件不能为空");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new com.community.mall.exception.BusinessException("图片大小不能超过 5MB");
        }

        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase(Locale.ROOT);
        }
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new com.community.mall.exception.BusinessException("仅支持 jpg/jpeg/png/webp 格式的图片");
        }

        // 魔数校验：防止把 html/svg 脚本改名为 .jpg 上传
        try (InputStream in = file.getInputStream()) {
            byte[] head = new byte[12];
            int read = in.read(head);
            if (!matchesImageMagic(head, read)) {
                throw new com.community.mall.exception.BusinessException("文件内容不是有效的图片");
            }
        } catch (IOException e) {
            throw new RuntimeException("读取上传文件失败", e);
        }
        return ext;
    }

    /**
     * 按文件头魔数判断是否为白名单图片格式
     */
    private boolean matchesImageMagic(byte[] head, int len) {
        if (len < 4) {
            return false;
        }
        // JPEG: FF D8 FF
        if ((head[0] & 0xFF) == 0xFF && (head[1] & 0xFF) == 0xD8 && (head[2] & 0xFF) == 0xFF) {
            return true;
        }
        // PNG: 89 50 4E 47
        if ((head[0] & 0xFF) == 0x89 && head[1] == 0x50 && head[2] == 0x4E && head[3] == 0x47) {
            return true;
        }
        // WebP: "RIFF" ??? "WEBP"
        if (len >= 12 && head[0] == 'R' && head[1] == 'I' && head[2] == 'F' && head[3] == 'F'
                && head[8] == 'W' && head[9] == 'E' && head[10] == 'B' && head[11] == 'P') {
            return true;
        }
        return false;
    }
}
