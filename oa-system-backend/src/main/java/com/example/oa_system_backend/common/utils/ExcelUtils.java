package com.example.oa_system_backend.common.utils;

import com.example.oa_system_backend.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

    public static <T> List<T> importExcel(InputStream inputStream, Class<T> clazz) throws IOException {
        List<T> result = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new BusinessException("Excel文件为空");
            }

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new BusinessException("Excel文件没有表头");
            }

            int columnCount = headerRow.getLastCellNum();
            String[] headers = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                Cell cell = headerRow.getCell(i);
                headers[i] = getCellStringValue(cell);
            }

            Field[] fields = clazz.getDeclaredFields();
            Field[] sortedFields = new Field[columnCount];
            for (int i = 0; i < columnCount; i++) {
                String header = headers[i];
                for (Field field : fields) {
                    if (field.getName().equals(convertToFieldName(header))) {
                        sortedFields[i] = field;
                        break;
                    }
                }
            }

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }

                Constructor<?>[] constructors = clazz.getDeclaredConstructors();
                T obj = null;
                for (Constructor<?> constructor : constructors) {
                    if (constructor.getParameterCount() == 0) {
                        try {
                            constructor.setAccessible(true);
                            obj = (T) constructor.newInstance();
                            break;
                        } catch (Exception e) {
                            log.warn("使用构造函数创建实例失败，尝试使用 ReflectionFactory: {}", e.getMessage());
                        }
                    }
                }
                if (obj == null) {
                    try {
                        java.lang.reflect.Method reflectionFactoryMethod = Class.forName("sun.reflect.ReflectionFactory")
                                .getDeclaredMethod("getReflectionFactory");
                        Object reflectionFactory = reflectionFactoryMethod.invoke(null);
                        java.lang.reflect.Method newConstructorAccessorMethod = reflectionFactory.getClass()
                                .getDeclaredMethod("newConstructorAccessor", Constructor.class);
                        Object constructorAccessor = newConstructorAccessorMethod.invoke(reflectionFactory, constructors[0]);
                        java.lang.reflect.Method newInstanceMethod = constructorAccessor.getClass()
                                .getDeclaredMethod("newInstance", Object[].class);
                        obj = (T) newInstanceMethod.invoke(constructorAccessor, new Object[]{null});
                    } catch (Exception e) {
                        log.error("ReflectionFactory 创建实例失败: {}", e.getMessage());
                        throw new BusinessException("无法创建对象实例: " + clazz.getName());
                    }
                }
                boolean isEmptyRow = true;

                for (int colIndex = 0; colIndex < columnCount; colIndex++) {
                    Cell cell = row.getCell(colIndex);
                    String value = getCellStringValue(cell);

                    if (value != null && !value.trim().isEmpty()) {
                        isEmptyRow = false;
                    }

                    Field field = sortedFields[colIndex];
                    if (field != null && cell != null) {
                        setFieldValue(obj, field, value);
                    }
                }

                if (!isEmptyRow) {
                    result.add(obj);
                }
            }

            log.info("Excel导入成功, 记录数: {}", result.size());
        }

        return result;
    }

    private static String getCellStringValue(Cell cell) {
        if (cell == null) {
            return "";
        }

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toString();
                }
                double numericValue = cell.getNumericCellValue();
                if (numericValue == Math.floor(numericValue)) {
                    yield String.valueOf((long) numericValue);
                }
                yield String.valueOf(numericValue);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield cell.getStringCellValue();
                } catch (Exception e) {
                    yield String.valueOf(cell.getNumericCellValue());
                }
            }
            default -> "";
        };
    }

    private static String convertToFieldName(String header) {
        StringBuilder result = new StringBuilder();
        boolean nextUpperCase = false;

        for (char c : header.toCharArray()) {
            if (c == '_' || c == '-' || c == ' ' || c == '(' || c == ')' || c == '/' || c == '\\') {
                nextUpperCase = true;
            } else if (nextUpperCase) {
                result.append(Character.toUpperCase(c));
                nextUpperCase = false;
            } else {
                result.append(Character.toLowerCase(c));
            }
        }

        return result.toString();
    }

    private static void setFieldValue(Object obj, Field field, String value) {
        try {
            field.setAccessible(true);
            Class<?> fieldType = field.getType();

            if (value == null || value.trim().isEmpty()) {
                return;
            }

            if (fieldType == String.class) {
                field.set(obj, value);
            } else if (fieldType == Integer.class || fieldType == int.class) {
                field.set(obj, Integer.parseInt(value));
            } else if (fieldType == Long.class || fieldType == long.class) {
                field.set(obj, Long.parseLong(value));
            } else if (fieldType == Double.class || fieldType == double.class) {
                field.set(obj, Double.parseDouble(value));
            } else if (fieldType == Boolean.class || fieldType == boolean.class) {
                field.set(obj, Boolean.parseBoolean(value));
            } else if (fieldType == Short.class || fieldType == short.class) {
                field.set(obj, Short.parseShort(value));
            } else if (fieldType == Float.class || fieldType == float.class) {
                field.set(obj, Float.parseFloat(value));
            } else if (fieldType == Byte.class || fieldType == byte.class) {
                field.set(obj, Byte.parseByte(value));
            } else {
                field.set(obj, value);
            }
        } catch (Exception e) {
            log.warn("设置字段值失败: {}.{}, 值: {}", obj.getClass().getSimpleName(), field.getName(), value);
        }
    }
}