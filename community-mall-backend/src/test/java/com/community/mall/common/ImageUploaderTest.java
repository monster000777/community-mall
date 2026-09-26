package com.community.mall.common;

import com.community.mall.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 图片上传安全组件单元测试：白名单 + 魔数双重校验（存储型 XSS 防护）
 */
@ExtendWith(MockitoExtension.class)
class ImageUploaderTest {

    @TempDir
    Path tempDir;

    private ImageUploader uploader;

    @BeforeEach
    void setUp() {
        uploader = new ImageUploader();
        ReflectionTestUtils.setField(uploader, "uploadPath", tempDir.toAbsolutePath().toString());
    }

    private MockMultipartFile file(String name, String ext, byte[] content) {
        return new MockMultipartFile("file", name + "." + ext,
                "application/octet-stream", content);
    }

    /** PNG 文件头 */
    private static final byte[] PNG_HEAD = new byte[]{
            (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};

    /** JPEG 文件头 */
    private static final byte[] JPG_HEAD = new byte[]{
            (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0, 0, 0, 0, 0, 0, 0};

    /** WebP 文件头（RIFF....WEBP） */
    private static final byte[] WEBP_HEAD = new byte[]{
            'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P'};

    @Test
    void 合法PNG_保存成功并返回URL() {
        String url = uploader.saveAvatar(file("photo", "png", PNG_HEAD), 1L);

        assertNotNull(url);
        assertTrue(url.startsWith("/api/uploads/avatar/avatar_1_"));
        assertTrue(url.endsWith(".png"));
        File saved = new File(tempDir.toFile(), "avatar");
        assertEquals(1, saved.listFiles().length);
    }

    @Test
    void 合法WebP_保存成功() {
        String url = uploader.saveAvatar(file("img", "webp", WEBP_HEAD), 2L);
        assertTrue(url.endsWith(".webp"));
    }

    @Test
    void 扩展名不在白名单_拒绝() {
        // html/svg 不在白名单，第一步就会被拦截
        BusinessException ex = assertThrows(BusinessException.class,
                () -> uploader.saveAvatar(file("evil", "html", "<script>alert(1)</script>".getBytes()), 1L));
        assertTrue(ex.getMessage().contains("格式"));
    }

    @Test
    void 白名单扩展名但魔数是脚本内容_拒绝() {
        // 重点场景：把 html 改名为 .jpg —— 扩展名通过但魔数校验失败
        BusinessException ex = assertThrows(BusinessException.class,
                () -> uploader.saveAvatar(file("fake", "jpg", "<script>alert(1)</script>".getBytes()), 1L));
        assertEquals("文件内容不是有效的图片", ex.getMessage());
    }

    @Test
    void 超过5MB_拒绝() {
        byte[] big = new byte[(int) (5 * 1024 * 1024 + 1)];
        System.arraycopy(JPG_HEAD, 0, big, 0, 4);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> uploader.saveAvatar(file("big", "jpg", big), 1L));
        assertTrue(ex.getMessage().contains("5MB"));
    }

    @Test
    void 内容不足4字节_拒绝() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> uploader.saveAvatar(file("tiny", "jpg", new byte[]{1, 2, 3}), 1L));
        assertEquals("文件内容不是有效的图片", ex.getMessage());
    }

    @Test
    void 无扩展名_拒绝() {
        MockMultipartFile noExt = new MockMultipartFile("file", "noext",
                "application/octet-stream", PNG_HEAD);
        BusinessException ex = assertThrows(BusinessException.class,
                () -> uploader.saveAvatar(noExt, 1L));
        assertTrue(ex.getMessage().contains("格式"));
    }
}
