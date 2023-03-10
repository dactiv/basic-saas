package com.github.dactiv.saas.config.controller;

import com.github.dactiv.framework.commons.Casts;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.enumerate.ValueEnumUtils;
import com.github.dactiv.framework.commons.exception.ServiceException;
import com.github.dactiv.framework.commons.exception.SystemException;
import com.github.dactiv.framework.commons.minio.Bucket;
import com.github.dactiv.framework.commons.minio.FileObject;
import com.github.dactiv.framework.security.plugin.Plugin;
import com.github.dactiv.framework.spring.security.entity.SecurityUserDetails;
import com.github.dactiv.framework.spring.web.query.Property;
import com.github.dactiv.framework.spring.web.query.condition.support.SimpleConditionParser;
import com.github.dactiv.saas.commons.SecurityUserDetailsConstants;
import com.github.dactiv.saas.commons.enumeration.GenderEnum;
import com.github.dactiv.saas.config.config.AvatarConfig;
import com.github.dactiv.saas.config.domain.entity.AvatarHistoryEntity;
import com.github.dactiv.saas.config.enumerate.AttachmentTypeEnum;
import com.github.dactiv.saas.config.service.AttachmentService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

/**
 * 头像管理
 *
 * @author maurice.chen
 */
@Slf4j
@RestController
@RequestMapping("avatar")
public class AvatarController implements InitializingBean {

    private final AvatarConfig avatarConfig;

    private final AttachmentService attachmentService;

    public AvatarController(AvatarConfig avatarConfig, AttachmentService attachmentService) {
        this.avatarConfig = avatarConfig;
        this.attachmentService = attachmentService;
    }

    /**
     * 上传头像
     *
     * @param file 头像
     * @return reset 结果集
     * @throws Exception 上传错误时抛出
     */
    @PostMapping("upload/{type}/{targetId}")
    @PreAuthorize("isFullyAuthenticated()")
    @Plugin(name = "上传头像", parent = "avatar", audit = true)
    public RestResult<String> upload(@PathVariable("type") String type,
                                     @PathVariable("targetId") Integer targetId,
                                     @RequestParam MultipartFile file) throws Exception {

        AvatarHistoryEntity history = getAvatarHistory(targetId, type);

        String path = type + AntPathMatcher.DEFAULT_PATH_SEPARATOR + targetId + AntPathMatcher.DEFAULT_PATH_SEPARATOR;
        String historyFilename = path + file.getOriginalFilename();

        if (!history.getValues().contains(historyFilename)) {
            history.getValues().add(historyFilename);
        }

        if (history.getValues().size() > avatarConfig.getHistoryCount()) {
            history.getValues().remove(0);
        }

        history.setCurrentAvatarFilename(path + file.getOriginalFilename());
        FileObject historyFileObject = FileObject.of(history.getBucketName(), history.getHistoryFilename());
        attachmentService.getMinioTemplate().writeJsonValue(historyFileObject, history);
        attachmentService.getMinioTemplate().upload(
                FileObject.of(history.getBucketName(), historyFilename),
                file.getInputStream(),
                file.getSize(),
                file.getContentType()
        );

        String currentName = getCurrentAvatarFilename(targetId, type);
        attachmentService.getMinioTemplate().upload(
                FileObject.of(history.getBucketName(), currentName),
                file.getInputStream(),
                file.getSize(),
                file.getContentType()
        );

        return RestResult.of(
                "上传新头像完成",
                HttpStatus.OK.value(),
                RestResult.SUCCESS_EXECUTE_CODE,
                historyFilename
        );

    }

    /**
     * 获取历史头像
     *
     * @param type     头像类型参考
     * @param targetId 目标 id
     * @return 头像历史记录实体
     */
    @GetMapping("history/{type}/{targetId}")
    @PreAuthorize("isFullyAuthenticated()")
    public RestResult<AvatarHistoryEntity> history(@PathVariable("type") String type,
                                                   @PathVariable("targetId") Integer targetId) {
        return RestResult.ofSuccess(getAvatarHistory(targetId, type));
    }

    /**
     * 获取历史头像信息
     *
     * @param type     头像类型参考
     * @param targetId 目标 id
     * @return 用户头像历史记录实体
     */
    private AvatarHistoryEntity getAvatarHistory(Integer targetId, String type) {

        String objectName = type + AntPathMatcher.DEFAULT_PATH_SEPARATOR + targetId + AntPathMatcher.DEFAULT_PATH_SEPARATOR + avatarConfig.getHistoryFileToken();
        String avatarBucket = attachmentService.getAttachmentConfig().getBucketName(AttachmentTypeEnum.AVATAR.getValue());
        FileObject fileObject = FileObject.of(avatarBucket, objectName);
        AvatarHistoryEntity result = attachmentService.getMinioTemplate().readJsonValue(fileObject, AvatarHistoryEntity.class);

        if (Objects.isNull(result)) {
            result = new AvatarHistoryEntity();

            result.setId(targetId);
            result.setHistoryFilename(objectName);
            result.setBucketName(attachmentService.getAttachmentConfig().getBucketName(AttachmentTypeEnum.AVATAR.getValue()));
        }

        return result;
    }

    @GetMapping("get")
    public ResponseEntity<byte[]> get(@RequestParam("filename") String filename) throws Exception {

        InputStream is;

        try {

            String[] split = StringUtils.split(filename, AntPathMatcher.DEFAULT_PATH_SEPARATOR);
            if (split.length < 3) {
                throw new ServiceException("filename 格式错误");
            }

            int id = NumberUtils.toInt(split[1]);

            if (id <= 0) {
                is = new FileInputStream(avatarConfig.getDefaultPath() + StringUtils.replace(filename, AntPathMatcher.DEFAULT_PATH_SEPARATOR, SimpleConditionParser.DEFAULT_FIELD_CONDITION_SEPARATORS));
            } else {
                String bucket = attachmentService.getAttachmentConfig().getBucketName(AttachmentTypeEnum.AVATAR.getValue());
                is = attachmentService.getMinioTemplate().getObject(FileObject.of(bucket, filename));
            }

        } catch (Exception e) {
            is = new FileInputStream(avatarConfig.getDefaultPath() + avatarConfig.getDefaultAvatar());
        }

        return new ResponseEntity<>(IOUtils.toByteArray(is), new HttpHeaders(), HttpStatus.OK);
    }

    /**
     * 获取用户头像
     *
     * @return 头像 byte 数组
     *
     * @throws Exception 获取错误时抛出
     */
    @GetMapping("getPrincipal")
    public ResponseEntity<byte[]> getPrincipal(@CurrentSecurityContext SecurityContext securityContext) throws Exception {

        InputStream is = null;

        Authentication authentication = securityContext.getAuthentication();

        if (Objects.nonNull(authentication) && authentication.isAuthenticated()) {
            Object details = authentication.getDetails();
            if (SecurityUserDetails.class.isAssignableFrom(details.getClass())) {
                SecurityUserDetails userDetails = Casts.cast(details);
                Integer userId = Casts.cast(userDetails.getId());
                String current = getCurrentAvatarFilename(userDetails.getId(), userDetails.getType());
                String bucket = attachmentService.getAttachmentConfig().getBucketName(AttachmentTypeEnum.AVATAR.getValue());
                try {
                    is = attachmentService.getMinioTemplate().getObject(FileObject.of(bucket, current));
                } catch (Exception e) {
                    GenderEnum genderEnum = getPrincipalGender(userDetails.getMeta());
                    if (Objects.nonNull(genderEnum)) {
                        is = avatarConfig.getDefaultAvatarPath(genderEnum, userId);
                    }
                }
            }
        }

        if (Objects.isNull(is)) {
            is = new FileInputStream(avatarConfig.getDefaultPath() + avatarConfig.getDefaultAvatar());
        }

        return new ResponseEntity<>(IOUtils.toByteArray(is), new HttpHeaders(), HttpStatus.OK);
    }

    private GenderEnum getPrincipalGender(Map<String, Object> meta) {
        if (!meta.containsKey(SecurityUserDetailsConstants.SECURITY_DETAILS_GENDER_KEY)) {
            return null;
        }

        Map<String, Object> map = Casts.cast(meta.get(SecurityUserDetailsConstants.SECURITY_DETAILS_GENDER_KEY));
        if (MapUtils.isEmpty(map)) {
            return null;
        }

        String valueString = map.get(Property.VALUE_FIELD).toString();
        Integer value = Casts.cast(valueString, Integer.class);
        return ValueEnumUtils.parse(value, GenderEnum.class);

    }

    /**
     * 选择历史头像
     *
     * @param type     头像类型参考
     * @param targetId 目标 id
     * @return rest 结果集
     * @throws Exception 拷贝文件错误时抛出
     */
    @PostMapping("select/{type}/{targetId}")
    @PreAuthorize("isFullyAuthenticated()")
    public RestResult<?> select(@PathVariable("type") String type,
                                @PathVariable("targetId") Integer targetId,
                                @RequestParam String filename) throws Exception {

        AvatarHistoryEntity history = getAvatarHistory(targetId, type);

        if (!history.getValues().contains(filename)) {
            throw new SystemException("图片不存在，可能已经被删除。");
        }

        String currentName = getCurrentAvatarFilename(targetId, type);
        attachmentService.getMinioTemplate().copyObject(
                FileObject.of(history.getBucketName(), filename),
                FileObject.of(history.getBucketName(), currentName)
        );

        history.setCurrentAvatarFilename(filename);

        attachmentService.getMinioTemplate().writeJsonValue(FileObject.of(history.getBucketName(), history.getHistoryFilename()), history);

        return RestResult.of("更换头像成功");
    }

    /**
     * 获取当前头像文件名称
     *
     * @param targetId 后缀
     * @param type   头像类型参考
     * @return 当前头像文件名称
     */
    private String getCurrentAvatarFilename(Object targetId, String type) {
        String token = avatarConfig.getCurrentUseFileToken();
        return type + AntPathMatcher.DEFAULT_PATH_SEPARATOR + targetId + AntPathMatcher.DEFAULT_PATH_SEPARATOR + token;
    }

    /**
     * 删除历史头像
     *
     * @param type     头像类型参考
     * @param targetId 目标 id
     * @param filename 要删除的文件名称
     * @return rest 结果集
     * @throws Exception 删除错误时候抛出
     */
    @PostMapping("delete/{type}/{targetId}")
    @PreAuthorize("isFullyAuthenticated()")
    public RestResult<?> delete(@PathVariable("type") String type,
                                @PathVariable("targetId") Integer targetId,
                                @RequestParam String filename) throws Exception {

        AvatarHistoryEntity history = getAvatarHistory(targetId, type);

        if (!history.getValues().contains(filename)) {
            throw new SystemException("图片不存在，可能已经被删除。");
        }

        history.getValues().remove(filename);

        if (StringUtils.equals(filename, history.getCurrentAvatarFilename())) {
            history.setCurrentAvatarFilename("");
        }

        attachmentService.getMinioTemplate().writeJsonValue(FileObject.of(history.getBucketName(), history.getHistoryFilename()), history);

        attachmentService.getMinioTemplate().deleteObject(FileObject.of(history.getBucketName(), filename));

        return RestResult.of("删除历史头像成功");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        String avatarBucket = attachmentService.getAttachmentConfig().getBucketName(AttachmentTypeEnum.AVATAR.getValue());
        attachmentService.getMinioTemplate().makeBucketIfNotExists(Bucket.of(avatarBucket));
    }
}
