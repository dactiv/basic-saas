package com.github.dactiv.saas.config.service.captcha.picture;

import com.github.dactiv.saas.config.domain.meta.captcha.PictureMeta;

import java.io.OutputStream;

/**
 * 图片验证码生成器
 *
 * @author maurice
 */
public interface PictureCaptchaGenerator {

    /**
     * 生成验证码
     *
     * @param entity       图片验证码描述实体
     * @param outputStream 返回的图片留
     * @return 验证码
     * @throws Exception 生成错误时抛出
     */
    String generateCaptcha(PictureMeta entity, OutputStream outputStream) throws Exception;

}
