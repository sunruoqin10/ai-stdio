package com.example.oa_system_backend.module.employee.util;

import com.example.oa_system_backend.module.dict.service.DictService;
import com.example.oa_system_backend.module.dict.vo.DictDataVO;
import com.example.oa_system_backend.module.employee.vo.EmployeeDetailVO;
import com.example.oa_system_backend.module.employee.vo.EmployeeVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
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

            // 动态计算试用期状态
            String calculatedProbationStatus = calculateProbationStatus(vo.getProbationEndDate(), vo.getStatus());
            vo.setProbationStatus(calculatedProbationStatus);
            vo.setProbationStatusLabel(getDictLabel("probation_status", calculatedProbationStatus));

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

            // 动态计算试用期状态
            String calculatedProbationStatus = calculateProbationStatus(vo.getProbationEndDate(), vo.getStatus());
            vo.setProbationStatus(calculatedProbationStatus);
            vo.setProbationStatusLabel(getDictLabel("probation_status", calculatedProbationStatus));

            // 员工状态标签
            if (vo.getStatus() != null) {
                vo.setStatusLabel(getDictLabel("employee_status", vo.getStatus()));
            }
        } catch (Exception e) {
            log.error("填充字典标签失败", e);
        }
    }

    /**
     * 动态计算试用期状态
     * @param probationEndDate 试用期结束日期
     * @param employeeStatus 员工状态
     * @return 试用期状态
     */
    private String calculateProbationStatus(LocalDate probationEndDate, String employeeStatus) {
        // 如果员工已离职，返回已离职状态
        if ("resigned".equals(employeeStatus)) {
            return "resigned";
        }

        // 如果没有试用期结束日期，默认为试用期
        if (probationEndDate == null) {
            return "probation";
        }

        // 比较当前日期和试用期结束日期
        LocalDate today = LocalDate.now();
        if (today.isBefore(probationEndDate) || today.isEqual(probationEndDate)) {
            // 当前日期在试用期结束日期之前或当天，仍处于试用期
            return "probation";
        } else {
            // 当前日期已超过试用期结束日期，已转正
            return "regular";
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
