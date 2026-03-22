package com.example.oa_system_backend.common.utils;

import com.example.oa_system_backend.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Slf4j
public class ExcelUtils {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void exportExcel(HttpServletResponse response,
                                  List<?> dataList,
                                  List<String> headers,
                                  List<String> fieldNames,
                                  String fileName) throws IOException {

        if (dataList == null || dataList.isEmpty()) {
            throw new BusinessException("没有数据可导出");
        }

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");

        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = createHeaderStyle(workbook);

        for (int i = 0; i < headers.size(); i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers.get(i));
            cell.setCellStyle(headerStyle);
        }

        CellStyle dataStyle = createDataStyle(workbook);
        for (int i = 0; i < dataList.size(); i++) {
            Object data = dataList.get(i);
            Row row = sheet.createRow(i + 1);

            for (int j = 0; j < fieldNames.size(); j++) {
                String fieldName = fieldNames.get(j);
                Object value = getFieldValue(data, fieldName);

                Cell cell = row.createCell(j);
                setCellValue(cell, value, dataStyle);
            }
        }

        autoSizeColumns(sheet, headers.size());

        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
                .replaceAll("\\+", "%20");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment; filename*=utf-8''" + encodedFileName + ".xlsx");

        try (OutputStream outputStream = response.getOutputStream()) {
            workbook.write(outputStream);
            outputStream.flush();
        }

        workbook.close();

        log.info("Excel导出成功, 文件名: {}, 记录数: {}", fileName, dataList.size());
    }

    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);

        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        return style;
    }

    private static CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);

        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setWrapText(true);

        return style;
    }

    private static Object getFieldValue(Object obj, String fieldName) {
        try {
            String getterName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            java.lang.reflect.Method method = obj.getClass().getMethod(getterName);
            return method.invoke(obj);
        } catch (Exception e) {
            log.warn("获取字段值失败: {}.{}", obj.getClass().getSimpleName(), fieldName);
            return "";
        }
    }

    private static void setCellValue(Cell cell, Object value, CellStyle style) {
        cell.setCellStyle(style);

        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Date) {
            cell.setCellValue(DATE_FORMAT.format((Date) value));
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value ? "是" : "否");
        } else {
            cell.setCellValue(value.toString());
        }
    }

    private static void autoSizeColumns(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
            int width = sheet.getColumnWidth(i);
            if (width > 15000) {
                sheet.setColumnWidth(i, 15000);
            } else {
                sheet.setColumnWidth(i, width + 500);
            }
        }
    }
}