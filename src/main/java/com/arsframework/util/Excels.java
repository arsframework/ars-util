package com.arsframework.util;

import java.io.*;
import java.util.*;
import java.math.BigDecimal;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.udf.UDFFinder;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
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
     * Excel日期格式数组
     */
    @Deprecated
    public static final String[] DATE_FORMATS = {"yyyy-MM-dd", "yyyy/MM/dd", "yyyyMMdd"};

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
    }

    /**
     * Excel读对象接口
     *
     * @param <T> 数据模型
     */
    public interface Reader<T> {
        /**
         * 读取Excel数据行并转换成对象实体
         *
         * @param row   数据行对象
         * @param count 当前记录数（从1开始）
         * @return 对象实体
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
         * 将对象实体写入到Excel数据行
         *
         * @param entity 对象实体
         * @param row    数据行对象
         * @param count  当前记录数（从1开始）
         */
        void write(T entity, Row row, int count);
    }

    /**
     * Excel工作薄文件包装类
     */
    public static class WorkbookFileWrapper extends Nfile implements Workbook {
        protected final Workbook workbook;

        public WorkbookFileWrapper(String name, @Nonnull Workbook workbook) {
            super(name);
            this.workbook = workbook;
        }

        @Override
        public void write(OutputStream output) throws IOException {
            this.workbook.write(output);
        }

        @Override
        public int getActiveSheetIndex() {
            return this.workbook.getActiveSheetIndex();
        }

        @Override
        public void setActiveSheet(int i) {
            this.workbook.setActiveSheet(i);
        }

        @Override
        public int getFirstVisibleTab() {
            return this.workbook.getFirstVisibleTab();
        }

        @Override
        public void setFirstVisibleTab(int i) {
            this.workbook.setFirstVisibleTab(i);
        }

        @Override
        public void setSheetOrder(String s, int i) {
            this.workbook.setSheetOrder(s, i);
        }

        @Override
        public void setSelectedTab(int i) {
            this.workbook.setSelectedTab(i);
        }

        @Override
        public void setSheetName(int i, String s) {
            this.workbook.setSheetName(i, s);
        }

        @Override
        public String getSheetName(int i) {
            return this.workbook.getSheetName(i);
        }

        @Override
        public int getSheetIndex(String s) {
            return this.workbook.getSheetIndex(s);
        }

        @Override
        public int getSheetIndex(Sheet sheet) {
            return this.workbook.getSheetIndex(sheet);
        }

        @Override
        public Sheet createSheet() {
            return this.workbook.createSheet();
        }

        @Override
        public Sheet createSheet(String s) {
            return this.workbook.createSheet(s);
        }

        @Override
        public Sheet cloneSheet(int i) {
            return this.workbook.cloneSheet(i);
        }

        @Override
        public Iterator<Sheet> sheetIterator() {
            return this.workbook.sheetIterator();
        }

        @Override
        public int getNumberOfSheets() {
            return this.workbook.getNumberOfSheets();
        }

        @Override
        public Sheet getSheetAt(int i) {
            return this.workbook.getSheetAt(i);
        }

        @Override
        public Sheet getSheet(String s) {
            return this.workbook.getSheet(s);
        }

        @Override
        public void removeSheetAt(int i) {
            this.workbook.removeSheetAt(i);
        }

        @Override
        public void setRepeatingRowsAndColumns(int i, int i1, int i2, int i3, int i4) {
            this.workbook.setRepeatingRowsAndColumns(i, i1, i2, i3, i4);
        }

        @Override
        public Font createFont() {
            return this.workbook.createFont();
        }

        @Override
        public Font findFont(short i, short i1, short i2, String s, boolean b, boolean b1, short i3, byte b2) {
            return this.workbook.findFont(i, i1, i2, s, b, b1, i3, b2);
        }

        @Override
        public short getNumberOfFonts() {
            return this.workbook.getNumberOfFonts();
        }

        @Override
        public Font getFontAt(short i) {
            return this.workbook.getFontAt(i);
        }

        @Override
        public CellStyle createCellStyle() {
            return this.workbook.createCellStyle();
        }

        @Override
        public int getNumCellStyles() {
            return this.workbook.getNumCellStyles();
        }

        @Override
        public CellStyle getCellStyleAt(int i) {
            return this.workbook.getCellStyleAt(i);
        }

        @Override
        public void close() throws IOException {
            this.workbook.close();
        }

        @Override
        public int getNumberOfNames() {
            return this.workbook.getNumberOfNames();
        }

        @Override
        public Name getName(String s) {
            return this.workbook.getName(s);
        }

        @Override
        public Name getNameAt(int i) {
            return this.workbook.getNameAt(i);
        }

        @Override
        public Name createName() {
            return this.workbook.createName();
        }

        @Override
        public int getNameIndex(String s) {
            return this.workbook.getNameIndex(s);
        }

        @Override
        public void removeName(int i) {
            this.workbook.removeName(i);
        }

        @Override
        public void removeName(String s) {
            this.workbook.removeName(s);
        }

        @Override
        public int linkExternalWorkbook(String s, Workbook workbook) {
            return this.workbook.linkExternalWorkbook(s, workbook);
        }

        @Override
        public void setPrintArea(int i, String s) {
            this.workbook.setPrintArea(i, s);
        }

        @Override
        public void setPrintArea(int i, int i1, int i2, int i3, int i4) {
            this.workbook.setPrintArea(i, i1, i2, i3, i4);
        }

        @Override
        public String getPrintArea(int i) {
            return this.workbook.getPrintArea(i);
        }

        @Override
        public void removePrintArea(int i) {
            this.workbook.removePrintArea(i);
        }

        @Override
        public Row.MissingCellPolicy getMissingCellPolicy() {
            return this.workbook.getMissingCellPolicy();
        }

        @Override
        public void setMissingCellPolicy(Row.MissingCellPolicy missingCellPolicy) {
            this.workbook.setMissingCellPolicy(missingCellPolicy);
        }

        @Override
        public DataFormat createDataFormat() {
            return this.workbook.createDataFormat();
        }

        @Override
        public int addPicture(byte[] bytes, int i) {
            return this.workbook.addPicture(bytes, i);
        }

        @Override
        public List<? extends PictureData> getAllPictures() {
            return this.workbook.getAllPictures();
        }

        @Override
        public CreationHelper getCreationHelper() {
            return this.workbook.getCreationHelper();
        }

        @Override
        public boolean isHidden() {
            return this.workbook.isHidden();
        }

        @Override
        public void setHidden(boolean b) {
            this.workbook.setHidden(b);
        }

        @Override
        public boolean isSheetHidden(int i) {
            return this.workbook.isSheetHidden(i);
        }

        @Override
        public boolean isSheetVeryHidden(int i) {
            return this.workbook.isSheetVeryHidden(i);
        }

        @Override
        public void setSheetHidden(int i, boolean b) {
            this.workbook.setSheetHidden(i, b);
        }

        @Override
        public void setSheetHidden(int i, int i1) {
            this.workbook.setSheetHidden(i, i1);
        }

        @Override
        public void addToolPack(UDFFinder udfFinder) {
            this.workbook.addToolPack(udfFinder);
        }

        @Override
        public void setForceFormulaRecalculation(boolean b) {
            this.workbook.setForceFormulaRecalculation(b);
        }

        @Override
        public boolean getForceFormulaRecalculation() {
            return this.workbook.getForceFormulaRecalculation();
        }

        @Override
        public SpreadsheetVersion getSpreadsheetVersion() {
            return this.workbook.getSpreadsheetVersion();
        }

        @Override
        public Iterator<Sheet> iterator() {
            return this.workbook.iterator();
        }
    }

    /**
     * 获取Excel文件工作薄文件形式
     *
     * @param workbook Excel文件工作薄
     * @param name     文件名称
     * @return Excel文件对象
     */
    @Deprecated
    public static Nfile getNfile(Workbook workbook, String name) {
        return workbook2file(workbook, name);
    }

    /**
     * 获取Excel文件工作薄
     *
     * @param file Excel文件对象
     * @return Excel文件工作薄
     * @throws IOException IO操作异常
     */
    @Deprecated
    public static Workbook getWorkbook(File file) throws IOException {
        return file2workbook(file);
    }

    /**
     * 获取Excel文件工作薄
     *
     * @param file Excel文件对象
     * @return Excel文件工作薄
     * @throws IOException IO操作异常
     */
    @Deprecated
    public static Workbook getWorkbook(Nfile file) throws IOException {
        return file2workbook(file);
    }

    /**
     * 构建Excel工作薄
     *
     * @return Excel工作薄
     */
    public static Workbook buildWorkbook() {
        return buildWorkbook(Type.XLS);
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
            return new XSSFWorkbook();
        }
        throw new IllegalArgumentException("Not support excel type: " + type);
    }

    /**
     * 构建Excel工作薄
     *
     * @param name Excel文件名称
     * @return Excel工作薄
     */
    @Nonempty
    public static WorkbookFileWrapper buildWorkbook(String name) {
        return workbook2file(buildWorkbook(Objects.toEnum(Type.class, Files.getSuffix(name))), name);
    }

    /**
     * 将文件转换成Excel工作薄
     *
     * @param file 文件对象
     * @return Excel工作薄
     * @throws IOException IO操作异常
     */
    @Nonnull
    public static Workbook file2workbook(File file) throws IOException {
        try (InputStream is = new FileInputStream(file)) {
            return stream2workbook(is, Objects.toEnum(Type.class, Files.getSuffix(file.getName())));
        }
    }

    /**
     * 将文件转换成Excel工作薄
     *
     * @param file 文件对象
     * @return Excel工作薄
     * @throws IOException IO操作异常
     */
    @Nonnull
    public static Workbook file2workbook(Nfile file) throws IOException {
        try (InputStream is = file.getInputStream()) {
            return stream2workbook(is, Objects.toEnum(Type.class, Files.getSuffix(file.getName())));
        }
    }

    /**
     * 将数据流转换成Excel工作薄
     *
     * @param input 数据输入流
     * @param type  文件类型
     * @return Excel工作薄
     * @throws IOException IO操作异常
     */
    @Nonnull
    public static Workbook stream2workbook(InputStream input, Type type) throws IOException {
        if (type == Type.XLS) {
            return new HSSFWorkbook(input);
        } else if (type == Type.XLSX) {
            return new XSSFWorkbook(input);
        }
        throw new IllegalArgumentException("Not support excel type: " + type);
    }

    /**
     * 将Excel工作薄转换成文件
     *
     * @param workbook Excel工作薄
     * @param name     文件名称
     * @return Excel工作薄文件包装对象
     */
    public static WorkbookFileWrapper workbook2file(Workbook workbook, String name) {
        return new WorkbookFileWrapper(name, workbook);
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
     * 将Excel数据写入文件
     *
     * @param workbook Excel文件工作薄
     * @param file     文件对象
     * @throws IOException IO操作异常
     */
    @Nonnull
    public static void write(Workbook workbook, Nfile file) throws IOException {
        try (OutputStream output = file.getOutputStream()) {
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
        if (!Objects.isEmpty(value)) {
            if (value instanceof Object[]) {
                value = Strings.join((Object[]) value, ",");
            } else if (value instanceof Collection) {
                value = Strings.join((Collection<?>) value, ",");
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
        for (int i = 0; i < values.length; i++) {
            setValue(row.createCell(i), values[i]);
        }
    }

    /**
     * 设置Excel文件标题
     *
     * @param row    Excel数据行对象
     * @param titles 标题数组
     */
    @Nonnull
    public static void setTitles(Row row, String... titles) {
        if (titles.length > 0) {
            Workbook workbook = row.getSheet().getWorkbook();
            Font font = workbook.createFont();
            font.setBoldweight(Font.BOLDWEIGHT_BOLD);
            CellStyle style = workbook.createCellStyle();
            style.setFont(font);
            style.setAlignment(CellStyle.ALIGN_CENTER);
            setTitles(row, style, titles);
        }
    }

    /**
     * 设置Excel文件标题
     *
     * @param row    Excel数据行对象
     * @param style  单元格样式
     * @param titles 标题数组
     */
    @Nonnull
    public static void setTitles(Row row, CellStyle style, String... titles) {
        if (titles.length > 0) {
            for (int c = 0; c < titles.length; c++) {
                Cell cell = row.createCell(c);
                cell.setCellStyle(style);
                cell.setCellValue(titles[c]);
            }
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
     * 从Excel文件中获取对象实体
     *
     * @param <M>    数据类型
     * @param sheet  Excel sheet
     * @param reader Excel对象实体读取接口
     * @return 对象实体列表
     */
    public static <M> List<M> getObjects(Sheet sheet, Reader<M> reader) {
        return getObjects(sheet, 0, reader);
    }

    /**
     * 从Excel文件中获取对象实体
     *
     * @param <M>      数据类型
     * @param workbook Excel文件工作薄
     * @param reader   Excel对象实体读取接口
     * @return 对象实体列表
     */
    public static <M> List<M> getObjects(Workbook workbook, Reader<M> reader) {
        return getObjects(workbook, 0, reader);
    }

    /**
     * 从Excel文件中获取对象实体
     *
     * @param <M>    数据类型
     * @param sheet  Excel sheet
     * @param index  开始数据行下标（从0开始）
     * @param reader Excel对象实体读取接口
     * @return 对象实体列表
     */
    @Nonnull
    public static <M> List<M> getObjects(Sheet sheet, @Min(0) int index, Reader<M> reader) {
        return getObjects(sheet, index, new int[]{0}, reader);
    }

    /**
     * 从Excel文件中获取对象实体
     *
     * @param <M>      数据类型
     * @param workbook Excel文件工作薄
     * @param index    开始数据行下标（从0开始）
     * @param reader   Excel对象实体读取接口
     * @return 对象实体列表
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
     * 从Excel文件中获取对象实体
     *
     * @param <M>    数据类型
     * @param sheet  Excel sheet
     * @param index  开始数据行下标（从0开始）
     * @param count  当前记录数（从1开始）
     * @param reader Excel对象实体读取接口
     * @return 对象实体列表
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
     * 将对象实体设置到Excel文件中
     *
     * @param <M>     数据类型
     * @param sheet   Excel sheet
     * @param objects 对象实体列表
     * @param writer  Excel对象实体写入接口
     * @return 设置数量
     */
    public static <M> int setObjects(Sheet sheet, List<M> objects, Writer<M> writer) {
        return setObjects(sheet, 0, objects, writer);
    }

    /**
     * 将对象实体设置到Excel文件中
     *
     * @param <M>      数据类型
     * @param workbook Excel文件工作薄
     * @param objects  对象实体列表
     * @param writer   Excel对象实体写入接口
     * @return 设置数量
     */
    public static <M> int setObjects(Workbook workbook, List<M> objects, Writer<M> writer) {
        return setObjects(workbook, 0, objects, writer);
    }

    /**
     * 将对象实体设置到Excel文件中
     *
     * @param <M>     数据类型
     * @param sheet   Excel sheet
     * @param index   开始数据行下标（从0开始）
     * @param objects 对象实体列表
     * @param writer  Excel对象实体写入接口
     * @return 设置数量
     */
    @Nonnull
    public static <M> int setObjects(Sheet sheet, @Min(0) int index, List<M> objects, Writer<M> writer) {
        int count = 0;
        for (int i = 0; i < objects.size(); i++) {
            M object = objects.get(i);
            if (object != null) {
                writer.write(object, sheet.createRow(index++), ++count);
            }
        }
        return count;
    }

    /**
     * 将对象实体设置到Excel文件中
     *
     * @param <M>      数据类型
     * @param workbook Excel文件工作薄
     * @param index    开始数据行下标（从0开始）
     * @param objects  对象实体列表
     * @param writer   Excel对象实体写入接口
     * @return 设置数量
     */
    @Nonnull
    public static <M> int setObjects(Workbook workbook, @Min(0) int index, List<M> objects, Writer<M> writer) {
        int count = 0;
        int r = index;
        Sheet sheet = null;
        for (int i = 0; i < objects.size(); i++) {
            if (i % 50000 == 0) {
                r = index;
                sheet = workbook.createSheet();
            }
            M object = objects.get(i);
            if (object != null) {
                writer.write(object, sheet.createRow(r++), ++count);
            }
        }
        return count;
    }

    /**
     * Excel文件迭代
     *
     * @param sheet  Excel sheet
     * @param reader Excel对象实体读取接口
     * @return 读取数量
     */
    public static int iteration(Sheet sheet, Reader<?> reader) {
        return iteration(sheet, 0, reader);
    }

    /**
     * Excel文件迭代
     *
     * @param workbook Excel文件工作薄
     * @param reader   Excel对象实体读取接口
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
     * @param reader Excel对象实体读取接口
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
     * @param reader   Excel对象实体读取接口
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
     * @param reader Excel对象实体读取接口
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
