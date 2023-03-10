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
 * ????????????
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
     * ????????????
     *
     * @param file ??????
     * @return reset ?????????
     * @throws Exception ?????????????????????
     */
    @PostMapping("upload/{type}/{targetId}")
    @PreAuthorize("isFullyAuthenticated()")
    @Plugin(name = "????????????", parent = "avatar", audit = true)
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
                "?????????????????????",
                HttpStatus.OK.value(),
                RestResult.SUCCESS_EXECUTE_CODE,
                historyFilename
        );

    }

    /**
     * ??????????????????
     *
     * @param type     ??????????????????
     * @param targetId ?????? id
     * @return ????????????????????????
     */
    @GetMapping("history/{type}/{targetId}")
    @PreAuthorize("isFullyAuthenticated()")
    public RestResult<AvatarHistoryEntity> history(@PathVariable("type") String type,
                                                   @PathVariable("targetId") Integer targetId) {
        return RestResult.ofSuccess(getAvatarHistory(targetId, type));
    }

    /**
     * ????????????????????????
     *
     * @param type     ??????????????????
     * @param targetId ?????? id
     * @return ??????????????????????????????
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
                throw new ServiceException("filename ????????????");
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
     * ??????????????????
     *
     * @return ?????? byte ??????
     *
     * @throws Exception ?????????????????????
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
     * ??????????????????
     *
     * @param type     ??????????????????
     * @param targetId ?????? id
     * @return rest ?????????
     * @throws Exception ???????????????????????????
     */
    @PostMapping("select/{type}/{targetId}")
    @PreAuthorize("isFullyAuthenticated()")
    public RestResult<?> select(@PathVariable("type") String type,
                                @PathVariable("targetId") Integer targetId,
                                @RequestParam String filename) throws Exception {

        AvatarHistoryEntity history = getAvatarHistory(targetId, type);

        if (!history.getValues().contains(filename)) {
            throw new SystemException("??????????????????????????????????????????");
        }

        String currentName = getCurrentAvatarFilename(targetId, type);
        attachmentService.getMinioTemplate().copyObject(
                FileObject.of(history.getBucketName(), filename),
                FileObject.of(history.getBucketName(), currentName)
        );

        history.setCurrentAvatarFilename(filename);

        attachmentService.getMinioTemplate().writeJsonValue(FileObject.of(history.getBucketName(), history.getHistoryFilename()), history);

        return RestResult.of("??????????????????");
    }

    /**
     * ??????????????????????????????
     *
     * @param targetId ??????
     * @param type   ??????????????????
     * @return ????????????????????????
     */
    private String getCurrentAvatarFilename(Object targetId, String type) {
        String token = avatarConfig.getCurrentUseFileToken();
        return type + AntPathMatcher.DEFAULT_PATH_SEPARATOR + targetId + AntPathMatcher.DEFAULT_PATH_SEPARATOR + token;
    }

    /**
     * ??????????????????
     *
     * @param type     ??????????????????
     * @param targetId ?????? id
     * @param filename ????????????????????????
     * @return rest ?????????
     * @throws Exception ????????????????????????
     */
    @PostMapping("delete/{type}/{targetId}")
    @PreAuthorize("isFullyAuthenticated()")
    public RestResult<?> delete(@PathVariable("type") String type,
                                @PathVariable("targetId") Integer targetId,
                                @RequestParam String filename) throws Exception {

        AvatarHistoryEntity history = getAvatarHistory(targetId, type);

        if (!history.getValues().contains(filename)) {
            throw new SystemException("??????????????????????????????????????????");
        }

        history.getValues().remove(filename);

        if (StringUtils.equals(filename, history.getCurrentAvatarFilename())) {
            history.setCurrentAvatarFilename("");
        }

        attachmentService.getMinioTemplate().writeJsonValue(FileObject.of(history.getBucketName(), history.getHistoryFilename()), history);

        attachmentService.getMinioTemplate().deleteObject(FileObject.of(history.getBucketName(), filename));

        return RestResult.of("????????????????????????");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        String avatarBucket = attachmentService.getAttachmentConfig().getBucketName(AttachmentTypeEnum.AVATAR.getValue());
        attachmentService.getMinioTemplate().makeBucketIfNotExists(Bucket.of(avatarBucket));
    }
}
