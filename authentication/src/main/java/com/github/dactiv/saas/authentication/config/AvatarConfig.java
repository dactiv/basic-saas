package com.github.dactiv.saas.authentication.config;

import com.github.dactiv.saas.commons.enumeration.GenderEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

@Data
@Component
@NoArgsConstructor
@ConfigurationProperties("dactiv.saas.authentication.app.avatar")
public class AvatarConfig {

    /**
     * 默认头像图片
     */
    public static final String DEFAULT_AVATAR = "default.png";

    /**
     * 历史文件 token
     */
    private String historyFileToken = "avatar_history_{0}.json";

    /**
     * 当前使用的头像名称
     */
    private String currentUseFileToken = "current_{0}";

    /**
     * 保留的历史头像记录总数
     */
    private Integer historyCount = 5;

    /**
     * 默认头像位置
     */
    private String defaultPath = "./avatar/";

    /**
     * 默认头像
     */
    private String defaultAvatar = DEFAULT_AVATAR;

    /**
     * 获取默认头像路径
     *
     * @param genderEnum 性别
     * @param userId     用户主键 id
     * @return 头像路径
     */
    public InputStream getDefaultAvatarPath(GenderEnum genderEnum, Integer userId) throws IOException {
        String folder = defaultPath + genderEnum.toString().toLowerCase();
        String[] fileList = Objects.requireNonNull(new File(folder).list());
        int index = userId % fileList.length;
        return new FileInputStream(folder + File.separator + fileList[index]);
    }
}
