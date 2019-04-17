package com.arsframework.util;

import java.io.*;
import java.util.*;
import java.lang.reflect.Field;
import java.lang.reflect.Array;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

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
     * 默认Sheet容量大小
     */
    public static final int DEFAULT_SHEET_VOLUME = 50000;

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
     * Excel读接口
     */
    public interface Reader {
        /**
         * 读取Excel数据行并转换成对象实例
         *
         * @param row   数据行对象
         * @param count 当前记录数（从1开始）
         */
        void read(Row row, int count);
    }

    /**
     * Excel写接口
     *
     * @param <T> 数据模型
     */
    public interface Writer<T> {
        /**
         * 将对象实例写入到Excel数据行
         *
         * @param row    数据行对象
         * @param object 对象实例
         * @param count  当前记录数（从1开始）
         */
        void write(Row row, T object, int count);
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
     * 保存Excel数据到本地文件
     *
     * @param workbook Excel文件工作薄
     * @param file     文件对象
     * @throws IOException IO操作异常
     */
    @Nonnull
    public static void save(Workbook workbook, File file) throws IOException {
        try (OutputStream output = new FileOutputStream(file)) {
            workbook.write(output);
        } finally {
            workbook.close();
        }
    }

    /**
     * 拷贝单元格
     *
     * @param source 原始单元格
     * @param target 目标单元格
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
     * 拷贝行
     *
     * @param source 原始行
     * @param target 目标行
     */
    @Nonnull
    public static void copy(Row source, Row target) {
        for (int i = 0; i < source.getLastCellNum(); i++) {
            Cell cell = source.getCell(i);
            if (cell != null) {
                copy(cell, target.createCell(i));
            }
        }
    }

    /**
     * 拷贝表格
     *
     * @param source 原始表格
     * @param target 目标表格
     */
    @Nonnull
    public static void copy(Sheet source, Sheet target) {
        for (int r = 0, rows = source.getLastRowNum(); r <= rows; r++) {
            Row row = source.getRow(r);
            if (row != null) {
                copy(row, target.createRow(r));
            }
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
                Object value = getValue(row.getCell(i));
                if (value != null && (!(value instanceof CharSequence) || !Strings.isBlank((CharSequence) value))) {
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
        String value = cell.getStringCellValue();
        return value == null || (value = value.trim()).isEmpty() ? null : value;
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
        return cell == null ? null :
                HSSFDateUtil.isCellDateFormatted(cell) ? cell.getDateCellValue() : Objects.toDate(getValue(cell), patterns);
    }

    /**
     * 获取单元格文本值
     *
     * @param cell 单元格对象
     * @return 数据文本
     */
    public static String getString(Cell cell) {
        return Strings.toString(getValue(cell));
    }

    /**
     * 获取单元格数值
     *
     * @param cell 单元格对象
     * @return 数值
     */
    public static Double getNumber(Cell cell) {
        return cell == null ? null :
                cell.getCellType() == Cell.CELL_TYPE_NUMERIC ? cell.getNumericCellValue() : Objects.toDouble(getString(cell));
    }

    /**
     * 获取单元格Boolean值
     *
     * @param cell 单元格对象
     * @return true/false
     */
    public static Boolean getBoolean(Cell cell) {
        return cell == null ? null :
                cell.getCellType() == Cell.CELL_TYPE_BOOLEAN ? cell.getBooleanCellValue() : Objects.toBoolean(getValue(cell));
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
        int columns = row.getLastCellNum(); // 从1开始
        T[] values = (T[]) Array.newInstance(type, columns);
        if (columns > 0) {
            for (int i = 0; i < columns; i++) {
                values[i] = getValue(row.getCell(i), type);
            }
            for (; columns > 0 && values[columns - 1] == null; columns--) ; // 移除最右边连续为空的值
        }
        return columns == 0 ? values : Arrays.copyOf(values, columns);
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
            cell.setCellStyle(style);
            if (value instanceof int[]) {
                cell.setCellValue(Strings.join(Arrays.asList((int[]) value), ","));
            } else if (value instanceof char[]) {
                cell.setCellValue(Strings.join(Arrays.asList((char[]) value), ","));
            } else if (value instanceof byte[]) {
                cell.setCellValue(Strings.join(Arrays.asList((byte[]) value), ","));
            } else if (value instanceof short[]) {
                cell.setCellValue(Strings.join(Arrays.asList((short[]) value), ","));
            } else if (value instanceof long[]) {
                cell.setCellValue(Strings.join(Arrays.asList((long[]) value), ","));
            } else if (value instanceof float[]) {
                cell.setCellValue(Strings.join(Arrays.asList((float[]) value), ","));
            } else if (value instanceof double[]) {
                cell.setCellValue(Strings.join(Arrays.asList((double[]) value), ","));
            } else if (value instanceof boolean[]) {
                cell.setCellValue(Strings.join(Arrays.asList((boolean[]) value), ","));
            } else if (value instanceof Object[]) {
                cell.setCellValue(Strings.join(Arrays.asList((Object[]) value), ","));
            } else if (value instanceof Collection) {
                cell.setCellValue(Strings.join((Collection<?>) value, ","));
            } else {
                cell.setCellValue(Strings.toString(value));
            }
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
    public static void setValues(@Nonnull Row row, CellStyle style, @Nonnull Object... values) {
        for (int i = 0; i < values.length; i++) {
            setValue(row.createCell(i), style, values[i]);
        }
    }

    /**
     * 设置Excel文件标题
     *
     * @param sheet  Excel表格
     * @param titles 标题数组
     */
    public static void setTitles(Sheet sheet, String... titles) {
        setTitles(sheet, 0, titles);
    }

    /**
     * 设置Excel文件标题
     *
     * @param sheet  Excel表格
     * @param index  标题行下标（从0开始）
     * @param titles 标题数组
     */
    @Nonnull
    public static void setTitles(Sheet sheet, @Min(0) int index, String... titles) {
        if (titles.length > 0) {
            Workbook workbook = sheet.getWorkbook();
            Font font = workbook.createFont();
            font.setBoldweight(Font.BOLDWEIGHT_BOLD);
            CellStyle style = workbook.createCellStyle();
            style.setFont(font);
            style.setAlignment(CellStyle.ALIGN_CENTER);
            setValues(sheet.createRow(index), style, titles);
        }
    }

    /**
     * 统计Excel数据行数
     *
     * @param sheet Excel表格
     * @return 数量
     */
    public static int count(Sheet sheet) {
        return count(sheet, 0);
    }

    /**
     * 统计Excel数据行数
     *
     * @param workbook Excel文件工作薄
     * @return 数量
     */
    public static int count(Workbook workbook) {
        return count(workbook, 0);
    }

    /**
     * 统计Excel数据行数
     *
     * @param sheet Excel表格
     * @param index 开始数据行下标（从0开始）
     * @return 数量
     */
    @Nonnull
    public static int count(Sheet sheet, @Min(0) int index) {
        return sheet.getPhysicalNumberOfRows() - index;
    }

    /**
     * 统计Excel数据行数
     *
     * @param workbook Excel文件工作薄
     * @param index    开始数据行下标（从0开始）
     * @return 数量
     */
    @Nonnull
    public static int count(Workbook workbook, @Min(0) int index) {
        int count = 0;
        for (int i = 0, sheets = workbook.getNumberOfSheets(); i < sheets; i++) {
            count += count(workbook.getSheetAt(i), index);
        }
        return count;
    }

    /**
     * 根据Excel行获取对象实例
     *
     * @param <M>  数据类型
     * @param row  Excel行对象
     * @param type 对象类型
     * @return 对象实例
     */
    public static <M> M read(Row row, @Nonnull Class<M> type) {
        return isEmpty(row) ? null : Objects.initialize(type, getValues(row));
    }

    /**
     * 从Excel文件中获取对象实例
     *
     * @param <M>   数据类型
     * @param sheet Excel表格
     * @param type  对象类型
     * @return 对象实例列表
     */
    public static <M> List<M> read(Sheet sheet, Class<M> type) {
        return read(sheet, type, 0);
    }

    /**
     * 从Excel文件中获取对象实例
     *
     * @param <M>      数据类型
     * @param workbook Excel文件工作薄
     * @param type     对象类型
     * @return 对象实例列表
     */
    public static <M> List<M> read(Workbook workbook, Class<M> type) {
        return read(workbook, type, 0);
    }

    /**
     * 从Excel文件中获取对象实例
     *
     * @param <M>      数据类型
     * @param workbook Excel文件工作薄
     * @param type     对象类型
     * @param index    开始数据行下标（从0开始）
     * @return 对象实例列表
     */
    @Nonnull
    public static <M> List<M> read(Workbook workbook, Class<M> type, @Min(0) int index) {
        List<M> objects = new LinkedList<M>();
        for (int i = 0, sheets = workbook.getNumberOfSheets(); i < sheets; i++) {
            read(workbook.getSheetAt(i), type, index, objects);
        }
        return objects;
    }

    /**
     * 从Excel文件中获取对象实例
     *
     * @param <M>   数据类型
     * @param sheet Excel表格
     * @param type  对象类型
     * @param index 开始数据行下标（从0开始）
     * @return 对象实例列表
     */
    @Nonnull
    public static <M> List<M> read(Sheet sheet, Class<M> type, @Min(0) int index) {
        List<M> objects = new LinkedList<M>();
        read(sheet, type, index, objects);
        return objects;
    }

    /**
     * 从Excel文件中获取对象实例
     *
     * @param <M>       数据类型
     * @param sheet     Excel表格
     * @param type      对象类型
     * @param index     开始数据行下标（从0开始）
     * @param container 对象容器
     */
    @Nonnull
    private static <M> void read(Sheet sheet, Class<M> type, @Min(0) int index, List<M> container) {
        for (int r = index, rows = sheet.getLastRowNum(); r <= rows; r++) {
            M object = read(sheet.getRow(r), type);
            if (object != null) {
                container.add(object);
            }
        }
    }

    /**
     * 读Excel文件
     *
     * @param sheet  Excel表格
     * @param reader Excel对象实例读取接口
     * @return 读取数量
     */
    public static int read(Sheet sheet, Reader reader) {
        return read(sheet, 0, reader);
    }

    /**
     * 读Excel文件
     *
     * @param workbook Excel文件工作薄
     * @param reader   Excel对象实例读取接口
     * @return 读取数量
     */
    public static int read(Workbook workbook, Reader reader) {
        return read(workbook, 0, reader);
    }

    /**
     * 读Excel文件
     *
     * @param sheet  Excel表格
     * @param index  开始数据行下标（从0开始）
     * @param reader Excel对象实例读取接口
     * @return 读取数量
     */
    @Nonnull
    public static int read(Sheet sheet, @Min(0) int index, Reader reader) {
        int[] count = {0};
        read(sheet, index, count, reader);
        return count[0];
    }

    /**
     * 读Excel文件
     *
     * @param workbook Excel文件工作薄
     * @param index    开始数据行下标（从0开始）
     * @param reader   Excel对象实例读取接口
     * @return 读取数量
     */
    @Nonnull
    public static int read(Workbook workbook, @Min(0) int index, Reader reader) {
        int[] count = {0};
        for (int i = 0, sheets = workbook.getNumberOfSheets(); i < sheets; i++) {
            read(workbook.getSheetAt(i), index, count, reader);
        }
        return count[0];
    }

    /**
     * 读Excel文件
     *
     * @param sheet  Excel表格
     * @param index  开始数据行下标（从0开始）
     * @param count  当前记录数（从1开始）
     * @param reader Excel对象实例读取接口
     */
    private static void read(Sheet sheet, int index, int[] count, Reader reader) {
        for (int r = index, rows = sheet.getLastRowNum(); r <= rows; r++) {
            Row row = sheet.getRow(r);
            if (!isEmpty(row)) {
                reader.read(row, ++count[0]);
            }
        }
    }

    /**
     * 设置对象实例到Excel行
     *
     * @param row    Excel行对象
     * @param object 对象实例
     */
    public static void write(@Nonnull Row row, Object object) {
        if (object != null) {
            write(row, object, Objects.getFields(object.getClass()));
        }
    }

    /**
     * 设置对象实例到Excel行
     *
     * @param row    Excel行对象
     * @param object 对象实例
     * @param fields 对象字段数组
     */
    public static void write(@Nonnull Row row, Object object, @Nonnull Field... fields) {
        if (object != null) {
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
     * @param sheet   Excel表格
     * @param objects 对象实例列表
     * @return 设置数量
     */
    public static int write(Sheet sheet, List<?> objects) {
        return write(sheet, objects, 0);
    }

    /**
     * 将对象实例设置到Excel文件中
     *
     * @param workbook Excel文件工作薄
     * @param objects  对象实例列表
     * @return 设置数量
     */
    public static int write(Workbook workbook, List<?> objects) {
        return write(workbook, objects, 0);
    }

    /**
     * 将对象实例设置到Excel文件中
     *
     * @param sheet   Excel表格
     * @param objects 对象实例列表
     * @param index   开始数据行下标（从0开始）
     * @return 设置数量
     */
    @Nonnull
    public static int write(Sheet sheet, List<?> objects, @Min(0) int index) {
        if (objects.isEmpty()) {
            return 0;
        }
        int skip = index;
        Field[] fields = Objects.getFields(objects.get(0).getClass());
        for (Object object : objects) {
            if (object != null) {
                write(sheet.createRow(index++), object, fields);
            }
        }
        return index - skip;
    }

    /**
     * 将对象实例设置到Excel文件中
     *
     * @param workbook Excel文件工作薄
     * @param objects  对象实例列表
     * @param index    开始数据行下标（从0开始）
     * @return 设置数量
     */
    @Nonnull
    public static int write(Workbook workbook, List<?> objects, @Min(0) int index) {
        if (objects.isEmpty()) {
            return 0;
        }
        int c = 0, r = index;
        Sheet sheet = workbook.createSheet();
        Field[] fields = Objects.getFields(objects.get(0).getClass());
        for (Object object : objects) {
            if (object != null) {
                if (r > index && r % DEFAULT_SHEET_VOLUME == 0) {
                    r = index;
                    sheet = workbook.createSheet();
                }
                write(sheet.createRow(r++), object, fields);
                c++;
            }
        }
        return c;
    }

    /**
     * 读Excel文件
     *
     * @param <M>     数据类型
     * @param sheet   Excel表格
     * @param objects 对象实例列表
     * @param writer  Excel对象实例写入接口
     * @return 读取数量
     */
    public static <M> int write(Sheet sheet, List<M> objects, Writer<M> writer) {
        return write(sheet, objects, 0, writer);
    }

    /**
     * 读Excel文件
     *
     * @param <M>      数据类型
     * @param workbook Excel文件工作薄
     * @param objects  对象实例列表
     * @param writer   Excel对象实例写入接口
     * @return 读取数量
     */
    public static <M> int write(Workbook workbook, List<M> objects, Writer<M> writer) {
        return write(workbook, objects, 0, writer);
    }

    /**
     * 读Excel文件
     *
     * @param <M>     数据类型
     * @param sheet   Excel表格
     * @param objects 对象实例列表
     * @param index   开始数据行下标（从0开始）
     * @param writer  Excel对象实例写入接口
     * @return 读取数量
     */
    @Nonnull
    public static <M> int write(Sheet sheet, List<M> objects, @Min(0) int index, Writer<M> writer) {
        int count = 0;
        for (M object : objects) {
            if (object != null) {
                writer.write(sheet.createRow(index++), object, ++count);
            }
        }
        return count;
    }

    /**
     * 读Excel文件
     *
     * @param <M>      数据类型
     * @param workbook Excel文件工作薄
     * @param objects  对象实例列表
     * @param index    开始数据行下标（从0开始）
     * @param writer   Excel对象实例写入接口
     * @return 读取数量
     */
    @Nonnull
    public static <M> int write(Workbook workbook, List<M> objects, @Min(0) int index, Writer<M> writer) {
        if (objects.isEmpty()) {
            return 0;
        }
        int c = 0, r = index;
        Sheet sheet = workbook.createSheet();
        for (M object : objects) {
            if (object != null) {
                if (r > index && r % DEFAULT_SHEET_VOLUME == 0) {
                    r = index;
                    sheet = workbook.createSheet();
                }
                writer.write(sheet.createRow(r++), object, ++c);
            }
        }
        return c;
    }
}
