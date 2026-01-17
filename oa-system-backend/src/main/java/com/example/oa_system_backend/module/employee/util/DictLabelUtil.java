package com.example.oa_system_backend.module.employee.util;

import com.example.oa_system_backend.module.dict.service.DictService;
import com.example.oa_system_backend.module.dict.vo.DictDataVO;
import com.example.oa_system_backend.module.employee.vo.EmployeeDetailVO;
import com.example.oa_system_backend.module.employee.vo.EmployeeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 字典标签工具类
 * 用于将字典值转换为显示标签
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DictLabelUtil {

    private final DictService dictService;

    /**
     * 为EmployeeVO填充字典标签
     */
    public void fillDictLabels(EmployeeVO vo) {
        if (vo == null) {
            return;
        }

        try {
            // 性别标签
            if (vo.getGender() != null) {
                vo.setGenderLabel(getDictLabel("gender", vo.getGender()));
            }

            // 职位标签
            if (vo.getPosition() != null) {
                vo.setPositionLabel(getDictLabel("position_type", vo.getPosition()));
            }

            // 职级标签
            if (vo.getLevel() != null) {
                vo.setLevelLabel(getDictLabel("employee_level", vo.getLevel()));
            }

            // 试用期状态标签
            if (vo.getProbationStatus() != null) {
                vo.setProbationStatusLabel(getDictLabel("probation_status", vo.getProbationStatus()));
            }

            // 员工状态标签
            if (vo.getStatus() != null) {
                vo.setStatusLabel(getDictLabel("employee_status", vo.getStatus()));
            }
        } catch (Exception e) {
            log.error("填充字典标签失败", e);
        }
    }

    /**
     * 为EmployeeDetailVO填充字典标签
     */
    public void fillDictLabels(EmployeeDetailVO vo) {
        if (vo == null) {
            return;
        }

        try {
            // 性别标签
            if (vo.getGender() != null) {
                vo.setGenderLabel(getDictLabel("gender", vo.getGender()));
            }

            // 职位标签
            if (vo.getPosition() != null) {
                vo.setPositionLabel(getDictLabel("position_type", vo.getPosition()));
            }

            // 职级标签
            if (vo.getLevel() != null) {
                vo.setLevelLabel(getDictLabel("employee_level", vo.getLevel()));
            }

            // 试用期状态标签
            if (vo.getProbationStatus() != null) {
                vo.setProbationStatusLabel(getDictLabel("probation_status", vo.getProbationStatus()));
            }

            // 员工状态标签
            if (vo.getStatus() != null) {
                vo.setStatusLabel(getDictLabel("employee_status", vo.getStatus()));
            }
        } catch (Exception e) {
            log.error("填充字典标签失败", e);
        }
    }

    /**
     * 为EmployeeVO列表填充字典标签
     */
    public void fillDictLabelsForList(List<EmployeeVO> voList) {
        if (voList == null || voList.isEmpty()) {
            return;
        }

        voList.forEach(this::fillDictLabels);
    }

    /**
     * 根据字典类型编码和值获取标签
     */
    private String getDictLabel(String dictTypeCode, String value) {
        try {
            DictDataVO dictData = dictService.getDictData(dictTypeCode);
            if (dictData != null && dictData.getItems() != null) {
                return dictData.getItems().stream()
                        .filter(item -> value.equals(item.getValue()))
                        .findFirst()
                        .map(item -> item.getLabel())
                        .orElse(value);
            }
        } catch (Exception e) {
            log.warn("获取字典标签失败: dictTypeCode={}, value={}", dictTypeCode, value, e);
        }
        return value;
    }
}
