package com.github.dactiv.saas.config.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.dactiv.framework.commons.RestResult;
import com.github.dactiv.framework.commons.id.IdEntity;
import com.github.dactiv.framework.commons.page.Page;
import com.github.dactiv.framework.commons.page.PageRequest;
import com.github.dactiv.framework.commons.tree.TreeUtils;
import com.github.dactiv.framework.idempotent.annotation.Idempotent;
import com.github.dactiv.framework.mybatis.plus.MybatisPlusQueryGenerator;
import com.github.dactiv.framework.security.enumerate.ResourceType;
import com.github.dactiv.framework.security.plugin.Plugin;
import com.github.dactiv.saas.commons.enumeration.ResourceSourceEnum;
import com.github.dactiv.saas.config.domain.entity.dictionary.DataDictionaryEntity;
import com.github.dactiv.saas.config.domain.entity.dictionary.DictionaryTypeEntity;
import com.github.dactiv.saas.config.service.dictionary.DictionaryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * 数据字典管理控制器
 *
 * @author maurice
 */
@RestController
@RequestMapping("dictionary")
@Plugin(
        name = "数据字典管理",
        id = "dictionary",
        parent = "basic",
        icon = "icon-dictionary",
        type = ResourceType.Menu,
        sources = ResourceSourceEnum.CONSOLE_SOURCE_VALUE
)
public class DictionaryController {

    private final DictionaryService dictionaryService;

    private final MybatisPlusQueryGenerator<?> mybatisPlusQueryGenerator;

    public DictionaryController(DictionaryService dictionaryService,
                                MybatisPlusQueryGenerator<?> mybatisPlusQueryGenerator) {
        this.dictionaryService = dictionaryService;
        this.mybatisPlusQueryGenerator = mybatisPlusQueryGenerator;
    }

    // ----------------------------------------------- 数据字典管理 ----------------------------------------------- //

    /**
     * 获取所有数据字典
     *
     * @param request   http servlet request
     * @param mergeTree 是否合并树形，true 是，否则 false
     * @return 数据字典集合
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("findDataDictionary")
    public List<DataDictionaryEntity> findDataDictionary(HttpServletRequest request,
                                                         @RequestParam(required = false) boolean mergeTree) {

        List<DataDictionaryEntity> dataDictionaries = dictionaryService.getDataDictionaryService().find(
                mybatisPlusQueryGenerator.getQueryWrapperByHttpRequest(request)
        );

        if (mergeTree) {
            return TreeUtils.buildGenericTree(dataDictionaries);
        } else {
            return dataDictionaries;
        }
    }

    /**
     * 获取数据字典分页信息
     *
     * @param pageRequest 分页信息
     * @param request     http servlet request
     * @return 分页实体
     */
    @PostMapping("getDataDictionaryPage")
    @PreAuthorize("hasAuthority('perms[data_dictionary:page]')")
    @Plugin(name = "展示数据字典列表", audit = true)
    public Page<DataDictionaryEntity> getDataDictionaryPage(PageRequest pageRequest, HttpServletRequest request) {
        return dictionaryService.getDataDictionaryService().findPage(
                pageRequest,
                mybatisPlusQueryGenerator.getQueryWrapperByHttpRequest(request)
        );
    }

    /**
     * 判断数据字典唯一识别值是否唯一
     *
     * @param code 唯一识别值
     * @return true 是，否则 false
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("isDataDictionaryCodeUnique")
    public boolean isDataDictionaryCodeUnique(@RequestParam String code) {
        return Objects.isNull(dictionaryService.getDataDictionaryService().getByCode(code));
    }

    /**
     * 获取数据字典
     *
     * @param id 数据字典 ID
     * @return 数据字典实体
     */
    @GetMapping("getDataDictionary")
    @PreAuthorize("hasAuthority('perms[data_dictionary:get]')")
    @Plugin(name = "编辑数据字典", audit = true)
    public DataDictionaryEntity getDataDictionary(@RequestParam Integer id) {
        return dictionaryService.getDataDictionaryService().get(id);
    }

    /**
     * 保存数据字典
     *
     * @param entity 数据字典实体
     */
    @PostMapping("saveDataDictionary")
    @PreAuthorize("hasAuthority('perms[data_dictionary:save]') and isFullyAuthenticated()")
    @Plugin(name = "添加或保存数据字典", audit = true)
    @Idempotent(key = "config:data-dictionary:save:[#securityContext.authentication.details.id]")
    public RestResult<Integer> saveDataDictionary(@Valid @RequestBody DataDictionaryEntity entity,
                                                  @CurrentSecurityContext SecurityContext securityContext) {
        dictionaryService.saveDataDictionary(entity);
        return RestResult.ofSuccess("保存成功", entity.getId());
    }

    /**
     * 删除数据字典
     *
     * @param ids 主键值集合
     */
    @PostMapping("deleteDataDictionary")
    @PreAuthorize("hasAuthority('perms[data_dictionary:delete]') and isFullyAuthenticated()")
    @Plugin(name = "删除数据字典实体", audit = true)
    @Idempotent(key = "config:data-dictionary:delete:[#securityContext.authentication.details.id]")
    public RestResult<?> deleteDataDictionary(@RequestParam List<Integer> ids,
                                              @CurrentSecurityContext SecurityContext securityContext) {
        dictionaryService.deleteDataDictionary(ids);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }

    // ----------------------------------------------- 字典类型管理 ----------------------------------------------- //

    /**
     * 获取所有字典类型
     *
     * @param request   http servlet request
     * @param mergeTree 是否合并树形，true 是，否则 false
     * @return 字典类型集合
     */
    @PostMapping("findDictionaryType")
    @Plugin(name = "首页展示")
    @PreAuthorize("hasAuthority('perms[dictionary_type:find]')")
    public List<DictionaryTypeEntity> findDictionaryType(HttpServletRequest request,
                                                         @RequestParam(required = false) boolean mergeTree) {

        QueryWrapper<DictionaryTypeEntity> query = mybatisPlusQueryGenerator.getQueryWrapperByHttpRequest(request);
        query.orderByDesc(IdEntity.ID_FIELD_NAME);

        List<DictionaryTypeEntity> dictionaryTypes = dictionaryService.getDictionaryTypeService().find(query);

        if (mergeTree) {
            return TreeUtils.buildGenericTree(dictionaryTypes);
        } else {
            return dictionaryTypes;
        }
    }

    /**
     * 获取字典类型实体
     *
     * @param id 主键 ID
     * @return 字典类型实体
     */
    @GetMapping("getDictionaryType")
    @Plugin(name = "编辑字典类型")
    @PreAuthorize("hasAuthority('perms[dictionary_type:get]')")
    public DictionaryTypeEntity getDictionaryType(@RequestParam Integer id) {
        return dictionaryService.getDictionaryTypeService().get(id);
    }

    /**
     * 判断字典类型唯一识别值是否唯一
     *
     * @param code 唯一识别值
     * @return true 是，否则 false
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("isDictionaryTypeCodeUnique")
    public boolean isDictionaryTypeCodeUnique(@RequestParam String code) {

        return Objects.isNull(dictionaryService.getDictionaryTypeService().getByCode(code));
    }

    /**
     * 保存数据字典类型
     *
     * @param entity 数据字典类型实体
     */
    @PostMapping("saveDictionaryType")
    @PreAuthorize("hasAuthority('perms[dictionary_type:save]') and isFullyAuthenticated()")
    @Plugin(name = "添加或保存字典类型", audit = true)
    @Idempotent(key = "config:dictionary-type:save:[#securityContext.authentication.details.id]")
    public RestResult<Integer> saveDictionaryType(@Valid @RequestBody DictionaryTypeEntity entity,
                                                  @CurrentSecurityContext SecurityContext securityContext) {
        dictionaryService.saveDictionaryType(entity);
        return RestResult.ofSuccess("保存成功", entity.getId());
    }

    /**
     * 删除字典类型类型
     *
     * @param ids 主键值集合
     */
    @PostMapping("deleteDictionaryType")
    @Plugin(name = "删除字典类型", audit = true)
    @PreAuthorize("hasAuthority('perms[dictionary_type:delete]') and isFullyAuthenticated()")
    @Idempotent(key = "config:dictionary-type:delete:[#securityContext.authentication.details.id]")
    public RestResult<?> deleteDictionaryType(@RequestParam List<Integer> ids,
                                              @CurrentSecurityContext SecurityContext securityContext) {
        dictionaryService.deleteDictionaryType(ids);
        return RestResult.of("删除" + ids.size() + "条记录成功");
    }


}
