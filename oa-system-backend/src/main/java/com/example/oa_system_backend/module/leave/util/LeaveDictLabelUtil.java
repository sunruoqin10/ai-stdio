package com.example.oa_system_backend.module.leave.util;

import com.example.oa_system_backend.module.dict.service.DictService;
import com.example.oa_system_backend.module.dict.vo.DictDataVO;
import com.example.oa_system_backend.module.leave.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class LeaveDictLabelUtil {

    private final DictService dictService;

    public void fillDictLabels(LeaveRequestVO vo) {
        if (vo == null) {
            return;
        }

        try {
            vo.setTypeLabel(getDictLabel("leave_type", vo.getType()));
            vo.setStatusLabel(getDictLabel("leave_status", vo.getStatus()));
        } catch (Exception e) {
            log.error("填充字典标签失败", e);
        }
    }

    public void fillDictLabels(LeaveDetailVO vo) {
        if (vo == null) {
            return;
        }

        try {
            vo.setTypeLabel(getDictLabel("leave_type", vo.getType()));
            vo.setStatusLabel(getDictLabel("leave_status", vo.getStatus()));

            if (vo.getApprovals() != null) {
                fillDictLabelsForApprovalList(vo.getApprovals());
            }
        } catch (Exception e) {
            log.error("填充字典标签失败", e);
        }
    }

    public void fillDictLabels(ApprovalRecordVO vo) {
        if (vo == null) {
            return;
        }

        try {
            vo.setStatusLabel(getDictLabel("approval_status", vo.getStatus()));
        } catch (Exception e) {
            log.error("填充字典标签失败", e);
        }
    }

    public void fillDictLabels(LeaveBalanceVO vo) {
        if (vo == null) {
            return;
        }

        try {
            vo.setWarningLevelLabel(getDictLabel("balance_warning_level", vo.getWarningLevel()));
        } catch (Exception e) {
            log.error("填充字典标签失败", e);
        }
    }

    public void fillDictLabels(HolidayVO vo) {
        if (vo == null) {
            return;
        }

        try {
            vo.setTypeLabel(getDictLabel("holiday_type", vo.getType()));
        } catch (Exception e) {
            log.error("填充字典标签失败", e);
        }
    }

    public void fillDictLabelsForList(List<LeaveRequestVO> voList) {
        if (voList == null || voList.isEmpty()) {
            return;
        }

        try {
            DictDataVO typeDict = dictService.getDictData("leave_type");
            DictDataVO statusDict = dictService.getDictData("leave_status");

            Map<String, String> typeLabelMap = typeDict.getItems().stream()
                    .collect(Collectors.toMap(
                            com.example.oa_system_backend.module.dict.vo.DictItemVO::getValue,
                            com.example.oa_system_backend.module.dict.vo.DictItemVO::getLabel
                    ));

            Map<String, String> statusLabelMap = statusDict.getItems().stream()
                    .collect(Collectors.toMap(
                            com.example.oa_system_backend.module.dict.vo.DictItemVO::getValue,
                            com.example.oa_system_backend.module.dict.vo.DictItemVO::getLabel
                    ));

            for (LeaveRequestVO vo : voList) {
                if (vo.getType() != null) {
                    vo.setTypeLabel(typeLabelMap.getOrDefault(vo.getType(), vo.getType()));
                }
                if (vo.getStatus() != null) {
                    vo.setStatusLabel(statusLabelMap.getOrDefault(vo.getStatus(), vo.getStatus()));
                }
            }
        } catch (Exception e) {
            log.error("批量填充字典标签失败", e);
        }
    }

    public void fillDictLabelsForApprovalList(List<ApprovalRecordVO> voList) {
        if (voList == null || voList.isEmpty()) {
            return;
        }

        try {
            DictDataVO statusDict = dictService.getDictData("approval_status");

            Map<String, String> statusLabelMap = statusDict.getItems().stream()
                    .collect(Collectors.toMap(
                            com.example.oa_system_backend.module.dict.vo.DictItemVO::getValue,
                            com.example.oa_system_backend.module.dict.vo.DictItemVO::getLabel
                    ));

            for (ApprovalRecordVO vo : voList) {
                if (vo.getStatus() != null) {
                    vo.setStatusLabel(statusLabelMap.getOrDefault(vo.getStatus(), vo.getStatus()));
                }
            }
        } catch (Exception e) {
            log.error("批量填充审批记录字典标签失败", e);
        }
    }

    private String getDictLabel(String dictTypeCode, String value) {
        if (value == null) {
            return "";
        }

        try {
            DictDataVO dictData = dictService.getDictData(dictTypeCode);
            if (dictData != null && dictData.getItems() != null) {
                return dictData.getItems().stream()
                        .filter(item -> value.equals(item.getValue()))
                        .findFirst()
                        .map(com.example.oa_system_backend.module.dict.vo.DictItemVO::getLabel)
                        .orElse(value);
            }
        } catch (Exception e) {
            log.error("获取字典标签失败: dictTypeCode={}, value={}", dictTypeCode, value, e);
        }

        return value;
    }
}
