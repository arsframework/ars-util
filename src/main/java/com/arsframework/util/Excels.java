package com.arsframework.util;

import java.io.*;
import java.util.*;
import java.math.BigDecimal;
import java.lang.reflect.Field;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.arsframework.annotation.Min;
import com.arsframework.annotation.Nonnull;
import com.arsframework.annotation.Nonempty;

/**
 * Excel处理工具类
 *
 * @author yongqiang.wu
 * @version 2019-03-22 09:38
 */
public abstract class Excels {
    /**
     * Excel文件格式枚举
     */
    public enum Type {
        /**
         * xls格式
         */
        XLS,

        /**
         * xlsx格式
         */
        XLSX;

        /**
         * 根据类型名称转换类型枚举
         *
         * @param name 类型名称
         * @return 类型枚举
         */
        public static Type parse(String name) {
            return name == null || name.isEmpty() ? null : Type.valueOf(name.toUpperCase());
        }
    }

    /**
     * Excel读对象接口
     *
     * @param <T> 数据模型
     */
    public interface Reader<T> {
        /**
         * 读取Excel数据行并转换成对象实例
         *
         * @param row   数据行对象
         * @param count 当前记录数（从1开始）
         * @return 对象实例
         */
        T read(Row row, int count);
    }

    /**
     * Excel写对象接口
     *
     * @param <T> 数据模型
     */
    public interface Writer<T> {
        /**
         * 将对象实例写入到Excel数据行
         *
         * @param entity 对象实例
         * @param row    数据行对象
         * @param count  当前记录数（从1开始）
         */
        void write(T entity, Row row, int count);
    }

    /**
     * 构建Excel工作薄
     *
     * @return Excel工作薄
     */
    public static Workbook buildWorkbook() {
        return buildWorkbook(Type.XLSX);
    }

    /**
     * 构建Excel工作薄
     *
     * @param type Excel类型
     * @return Excel工作薄
     */
    @Nonnull
    public static Workbook buildWorkbook(Type type) {
        if (type == Type.XLS) {
            return new HSSFWorkbook();
        } else if (type == Type.XLSX) {
            return new SXSSFWorkbook();
        }
        throw new IllegalArgumentException("Not support excel type: " + type);
    }

    /**
     * 构建Excel工作薄
     *
     * @param file 文件对象
     * @return Excel工作薄
     * @throws IOException IO操作异常
     */
    @Nonnull
    public static Workbook buildWorkbook(File file) throws IOException {
        try (InputStream is = new FileInputStream(file)) {
            return buildWorkbook(is, Type.parse(Files.getSuffix(file.getName())));
        }
    }

    /**
     * 构建Excel工作薄
     *
     * @param input 数据输入流
     * @return Excel工作薄
     * @throws IOException IO操作异常
     */
    @Nonnull
    public static Workbook buildWorkbook(InputStream input) throws IOException {
        return buildWorkbook(input, Type.XLSX);
    }

    /**
     * 构建Excel工作薄
     *
     * @param input 数据输入流
     * @param type  文件类型
     * @return Excel工作薄
     * @throws IOException IO操作异常
     */
    @Nonnull
    public static Workbook buildWorkbook(InputStream input, Type type) throws IOException {
        if (type == Type.XLS) {
            return new HSSFWorkbook(input);
        } else if (type == Type.XLSX) {
            return new XSSFWorkbook(input);
        }
        throw new IllegalArgumentException("Not support excel type: " + type);
    }

    /**
     * 将Excel数据写入文件
     *
     * @param workbook Excel文件工作薄
     * @param file     文件对象
     * @throws IOException IO操作异常
     */
    @Nonnull
    public static void write(Workbook workbook, File file) throws IOException {
        try (OutputStream output = new FileOutputStream(file)) {
            workbook.write(output);
        } finally {
            workbook.close();
        }
    }

    /**
     * 拷贝单元格对象数据
     *
     * @param source 原始单元格对象
     * @param target 目标单元格对象
     */
    @Nonnull
    public static void copy(Cell source, Cell target) {
        target.setCellStyle(source.getCellStyle());
        int type = source.getCellType();
        if (type == Cell.CELL_TYPE_BOOLEAN) {
            target.setCellValue(source.getBooleanCellValue());
        } else if (type == Cell.CELL_TYPE_NUMERIC) {
            if (HSSFDateUtil.isCellDateFormatted(source)) {
                target.setCellValue(source.getDateCellValue());
            } else {
                target.setCellValue(source.getNumericCellValue());
            }
        } else {
            target.setCellValue(source.getStringCellValue());
        }
    }

    /**
     * 拷贝行对象数据
     *
     * @param source 原始行对象
     * @param target 目标行对象
     */
    @Nonnull
    public static void copy(Row source, Row target) {
        for (int i = 0; i < source.getLastCellNum(); i++) {
            copy(source.getCell(i), target.createCell(i));
        }
    }

    /**
     * 判断Excel数据行是否为空
     *
     * @param row Excel数据行对象
     * @return true/false
     */
    public static boolean isEmpty(Row row) {
        if (row != null) {
            for (int i = 0; i < row.getLastCellNum(); i++) {
                if (!Strings.isBlank(getString(row.getCell(i)))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 获取Excel单元格值
     *
     * @param cell Excel单元格对象
     * @return 值
     */
    public static Object getValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        int type = cell.getCellType();
        if (type == Cell.CELL_TYPE_BOOLEAN) {
            return cell.getBooleanCellValue();
        } else if (type == Cell.CELL_TYPE_NUMERIC) {
            return HSSFDateUtil.isCellDateFormatted(cell) ? cell.getDateCellValue() : cell.getNumericCellValue();
        }
        String value = Strings.clean(cell.getStringCellValue()).trim();
        return value.isEmpty() ? null : value;
    }

    /**
     * 获取Excel单元格值
     *
     * @param <T>  数据类型
     * @param cell Excel单元格对象
     * @param type 值类型
     * @return 值
     */
    public static <T> T getValue(Cell cell, Class<T> type) {
        return (T) Objects.toObject(type, getValue(cell));
    }

    /**
     * 获取Excel一行单元格的值，如果所有值都为空则返回空数组
     *
     * @param row Excel行对象
     * @return 值数组
     */
    public static Object[] getValues(Row row) {
        return getValues(row, Object.class);
    }

    /**
     * 获取Excel一行单元格的值，如果所有值都为空则返回空数组
     *
     * @param <T>  数据类型
     * @param row  Excel行对象
     * @param type 数据类型
     * @return 值数组
     */
    public static <T> T[] getValues(Row row, @Nonnull Class<T> type) {
        if (row == null) {
            return null;
        }
        boolean empty = true;
        int columns = row.getLastCellNum(); // 从1开始
        T[] values = Objects.buildArray(type, columns);
        for (int i = 0; i < columns; i++) {
            if ((values[i] = getValue(row.getCell(i), type)) != null && empty) {
                empty = false;
            }
        }
        if (empty) {
            return Objects.buildArray(type, 0);
        }
        for (int i = columns - 1; i >= 0; i--) {
            if (values[i] == null) {
                columns--;
                continue;
            }
            break;
        }
        return columns == values.length ? values : Arrays.copyOf(values, columns);
    }

    /**
     * 获取单元格日期值
     *
     * @param cell 单元格对象
     * @return 日期对象
     */
    public static Date getDate(Cell cell) {
        return getDate(cell, Dates.ALL_DATE_FORMATS);
    }

    /**
     * 获取单元格日期值
     *
     * @param cell     单元格对象
     * @param patterns 日期格式数组
     * @return 日期对象
     */
    public static Date getDate(Cell cell, @Nonempty String... patterns) {
        if (cell == null) {
            return null;
        } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            return cell.getDateCellValue();
        }
        String value = getString(cell);
        return value == null ? null : Dates.parse(value, patterns);
    }

    /**
     * 获取单元格文本值
     *
     * @param cell 单元格对象
     * @return 数据文本
     */
    public static String getString(Cell cell) {
        Object value = getValue(cell);
        if (value != null && cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            String string = new BigDecimal((Double) value).toString();
            return string.endsWith(".0") ? string.substring(0, string.length() - 2) : string;
        }
        return Strings.toString(value);
    }

    /**
     * 获取单元格数值
     *
     * @param cell 单元格对象
     * @return 数值
     */
    public static Double getNumber(Cell cell) {
        if (cell == null) {
            return null;
        } else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            return cell.getNumericCellValue();
        }
        String value = getString(cell);
        return value == null ? null : Double.parseDouble(value);
    }

    /**
     * 获取单元格Boolean值
     *
     * @param cell 单元格对象
     * @return true/false
     */
    public static Boolean getBoolean(Cell cell) {
        if (cell == null) {
            return null;
        } else if (cell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
            return cell.getBooleanCellValue();
        }
        String value = getString(cell);
        return value == null ? null : Boolean.parseBoolean(value);
    }

    /**
     * 设置Excel单元格值
     *
     * @param cell  Excel单元格对象
     * @param value 值
     */
    public static void setValue(@Nonnull Cell cell, Object value) {
        setValue(cell, null, value);
    }

    /**
     * 设置Excel单元格值
     *
     * @param cell  Excel单元格对象
     * @param style 单元格样式
     * @param value 值
     */
    public static void setValue(@Nonnull Cell cell, CellStyle style, Object value) {
        if (!Objects.isEmpty(value)) {
            if (value instanceof Object[]) {
                value = Strings.join((Object[]) value, ",");
            } else if (value instanceof Collection) {
                value = Strings.join((Collection<?>) value, ",");
            }
            if (style != null) {
                cell.setCellStyle(style);
            }
            cell.setCellValue(Strings.toString(value));
        }
    }

    /**
     * 设置Excel单元格值
     *
     * @param row    Excel行对象
     * @param values 单元格值数组
     */
    @Nonnull
    public static void setValues(Row row, Object... values) {
        setValues(row, null, values);
    }

    /**
     * 设置Excel单元格值
     *
     * @param row    Excel行对象
     * @param style  单元格样式
     * @param values 单元格值数组
     */
    @Nonnull
    public static void setValues(Row row, CellStyle style, Object... values) {
        for (int i = 0; i < values.length; i++) {
            setValue(row.createCell(i), style, values[i]);
        }
    }

    /**
     * 获取Excel数据总行数
     *
     * @param sheet Excel sheet
     * @return 总行数
     */
    public static int getCount(Sheet sheet) {
        return getCount(sheet, 0);
    }

    /**
     * 获取Excel数据总行数
     *
     * @param workbook Excel文件工作薄
     * @return 总行数
     */
    public static int getCount(Workbook workbook) {
        return getCount(workbook, 0);
    }

    /**
     * 获取Excel数据总行数
     *
     * @param sheet Excel sheet
     * @param index 开始数据行下标（从0开始）
     * @return 总行数
     */
    @Nonnull
    public static int getCount(Sheet sheet, @Min(0) int index) {
        return sheet.getPhysicalNumberOfRows() - index;
    }

    /**
     * 获取Excel文件总行数
     *
     * @param workbook Excel文件工作薄
     * @param index    开始数据行下标（从0开始）
     * @return 总行数
     */
    @Nonnull
    public static int getCount(Workbook workbook, @Min(0) int index) {
        int count = 0;
        for (int i = 0, sheets = workbook.getNumberOfSheets(); i < sheets; i++) {
            count += getCount(workbook.getSheetAt(i), index);
        }
        return count;
    }

    /**
     * 根据Excel行获取对象实例
     *
     * @param row  Excel行对象
     * @param type 目标对象
     * @param <M>  目标对象类型
     * @return 对象实例
     */
    public static <M> M getObject(Row row, @Nonnull Class<M> type) {
        if (row == null) {
            return null;
        }
        M object = Objects.initialize(type);
        Field[] fields = Objects.getFields(type);
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true);
            try {
                field.set(object, getValue(row.getCell(i), field.getType()));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return object;
    }

    /**
     * 从Excel文件中获取对象实例
     *
     * @param <M>    数据类型
     * @param sheet  Excel sheet
     * @param reader Excel对象实例读取接口
     * @return 对象实例列表
     */
    public static <M> List<M> getObjects(Sheet sheet, Reader<M> reader) {
        return getObjects(sheet, 0, reader);
    }

    /**
     * 从Excel文件中获取对象实例
     *
     * @param <M>      数据类型
     * @param workbook Excel文件工作薄
     * @param reader   Excel对象实例读取接口
     * @return 对象实例列表
     */
    public static <M> List<M> getObjects(Workbook workbook, Reader<M> reader) {
        return getObjects(workbook, 0, reader);
    }

    /**
     * 从Excel文件中获取对象实例
     *
     * @param <M>    数据类型
     * @param sheet  Excel sheet
     * @param index  开始数据行下标（从0开始）
     * @param reader Excel对象实例读取接口
     * @return 对象实例列表
     */
    @Nonnull
    public static <M> List<M> getObjects(Sheet sheet, @Min(0) int index, Reader<M> reader) {
        return getObjects(sheet, index, new int[]{0}, reader);
    }

    /**
     * 从Excel文件中获取对象实例
     *
     * @param <M>      数据类型
     * @param workbook Excel文件工作薄
     * @param index    开始数据行下标（从0开始）
     * @param reader   Excel对象实例读取接口
     * @return 对象实例列表
     */
    @Nonnull
    public static <M> List<M> getObjects(Workbook workbook, @Min(0) int index, Reader<M> reader) {
        int[] count = {0};
        List<M> objects = new LinkedList<M>();
        for (int i = 0, sheets = workbook.getNumberOfSheets(); i < sheets; i++) {
            objects.addAll(getObjects(workbook.getSheetAt(i), index, count, reader));
        }
        return objects;
    }

    /**
     * 从Excel文件中获取对象实例
     *
     * @param <M>    数据类型
     * @param sheet  Excel sheet
     * @param index  开始数据行下标（从0开始）
     * @param count  当前记录数（从1开始）
     * @param reader Excel对象实例读取接口
     * @return 对象实例列表
     */
    @Nonempty
    private static <M> List<M> getObjects(Sheet sheet, @Min(0) int index, int[] count, Reader<M> reader) {
        List<M> objects = new LinkedList<M>();
        for (int r = index, rows = sheet.getLastRowNum(); r <= rows; r++) {
            Row row = sheet.getRow(r);
            if (!isEmpty(row)) {
                M object = reader.read(row, ++count[0]);
                if (object != null) {
                    objects.add(object);
                }
            }
        }
        return objects;
    }

    /**
     * 设置对象实例到Excel行
     *
     * @param row    Excel行对象
     * @param object 对象实例
     */
    public static void setObject(@Nonnull Row row, Object object) {
        if (object != null) {
            Field[] fields = Objects.getFields(object.getClass());
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                field.setAccessible(true);
                try {
                    setValue(row.createCell(i), field.get(object));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * 将对象实例设置到Excel文件中
     *
     * @param <M>     数据类型
     * @param sheet   Excel sheet
     * @param objects 对象实例列表
     * @param writer  Excel对象实例写入接口
     * @return 设置数量
     */
    public static <M> int setObjects(Sheet sheet, List<M> objects, Writer<M> writer) {
        return setObjects(sheet, 0, objects, writer);
    }

    /**
     * 将对象实例设置到Excel文件中
     *
     * @param <M>      数据类型
     * @param workbook Excel文件工作薄
     * @param objects  对象实例列表
     * @param writer   Excel对象实例写入接口
     * @return 设置数量
     */
    public static <M> int setObjects(Workbook workbook, List<M> objects, Writer<M> writer) {
        return setObjects(workbook, 0, objects, writer);
    }

    /**
     * 将对象实例设置到Excel文件中
     *
     * @param <M>     数据类型
     * @param sheet   Excel sheet
     * @param index   开始数据行下标（从0开始）
     * @param objects 对象实例列表
     * @param writer  Excel对象实例写入接口
     * @return 设置数量
     */
    @Nonnull
    public static <M> int setObjects(Sheet sheet, @Min(0) int index, List<M> objects, Writer<M> writer) {
        int count = 0;
        for (M object : objects) {
            if (object != null) {
                writer.write(object, sheet.createRow(index++), ++count);
            }
        }
        return count;
    }

    /**
     * 将对象实例设置到Excel文件中
     *
     * @param <M>      数据类型
     * @param workbook Excel文件工作薄
     * @param index    开始数据行下标（从0开始）
     * @param objects  对象实例列表
     * @param writer   Excel对象实例写入接口
     * @return 设置数量
     */
    @Nonnull
    public static <M> int setObjects(Workbook workbook, @Min(0) int index, List<M> objects, Writer<M> writer) {
        Sheet sheet = null;
        int i = 0, count = 0, r = index;
        for (M object : objects) {
            if (i++ % 50000 == 0) {
                r = index;
                sheet = workbook.createSheet();
            }
            if (object != null) {
                writer.write(object, sheet.createRow(r++), ++count);
            }
        }
        return count;
    }

    /**
     * 设置Excel文件标题
     *
     * @param sheet  Excel sheet
     * @param titles 标题数组
     */
    @Nonnull
    public static void setTitles(Sheet sheet, String... titles) {
        if (titles.length > 0) {
            Workbook workbook = sheet.getWorkbook();
            Font font = workbook.createFont();
            font.setBoldweight(Font.BOLDWEIGHT_BOLD);
            CellStyle style = workbook.createCellStyle();
            style.setFont(font);
            style.setAlignment(CellStyle.ALIGN_CENTER);
            setValues(sheet.createRow(0), style, titles);
        }
    }

    /**
     * Excel文件迭代
     *
     * @param sheet  Excel sheet
     * @param reader Excel对象实例读取接口
     * @return 读取数量
     */
    public static int iteration(Sheet sheet, Reader<?> reader) {
        return iteration(sheet, 0, reader);
    }

    /**
     * Excel文件迭代
     *
     * @param workbook Excel文件工作薄
     * @param reader   Excel对象实例读取接口
     * @return 读取数量
     */
    public static int iteration(Workbook workbook, Reader<?> reader) {
        return iteration(workbook, 0, reader);
    }

    /**
     * Excel文件迭代
     *
     * @param sheet  Excel sheet
     * @param index  开始数据行下标（从0开始）
     * @param reader Excel对象实例读取接口
     * @return 读取数量
     */
    @Nonnull
    public static int iteration(Sheet sheet, @Min(0) int index, Reader<?> reader) {
        int[] count = {0};
        iteration(sheet, index, count, reader);
        return count[0];
    }

    /**
     * Excel文件迭代
     *
     * @param workbook Excel文件工作薄
     * @param index    开始数据行下标（从0开始）
     * @param reader   Excel对象实例读取接口
     * @return 读取数量
     */
    @Nonnull
    public static int iteration(Workbook workbook, @Min(0) int index, Reader<?> reader) {
        int[] count = {0};
        for (int i = 0, sheets = workbook.getNumberOfSheets(); i < sheets; i++) {
            iteration(workbook.getSheetAt(i), index, count, reader);
        }
        return count[0];
    }

    /**
     * Excel文件迭代
     *
     * @param sheet  Excel sheet
     * @param index  开始数据行下标（从0开始）
     * @param count  当前记录数（从1开始）
     * @param reader Excel对象实例读取接口
     */
    @Nonnull
    private static void iteration(Sheet sheet, @Min(0) int index, int[] count, Reader<?> reader) {
        for (int r = index, rows = sheet.getLastRowNum(); r <= rows; r++) {
            Row row = sheet.getRow(r);
            if (!isEmpty(row)) {
                reader.read(row, ++count[0]);
            }
        }
    }

}
