package com.arsframework.util;

import java.io.*;
import java.util.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.lang.reflect.Field;
import java.lang.reflect.Array;

import org.xml.sax.XMLReader;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

import com.arsframework.annotation.Min;
import com.arsframework.annotation.Nonnull;

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
     * 公式转换器
     */
    private static final ThreadLocal<FormulaEvaluator> evaluator = new ThreadLocal<>();

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
        @Nonnull
        public static Type parse(String name) {
            return Type.valueOf(name.toUpperCase());
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
     * 基于XML的Excel行实现
     */
    private static class XMLRow implements Row {
        /**
         * 行下标（从0开始）
         */
        private int index;

        /**
         * 单元格列下标列表（从小到大排序）
         */
        private TreeSet<Integer> columns = new TreeSet<>();

        /**
         * 单元格列下标/单元格映射表
         */
        private Map<Integer, Cell> cells = new HashMap<>();

        @Override
        public Cell createCell(int i) {
            Cell cell = this.createCell(i, CellType.BLANK);
            cell.setCellValue(Strings.EMPTY_STRING);
            return cell;
        }

        @Override
        public Cell createCell(@Min(0) int i, CellType cellType) {
            Cell cell = new XMLCell(this, i);
            cell.setCellType(cellType);
            this.cells.put(i, cell);
            this.columns.add(i);
            return cell;
        }

        @Override
        public void removeCell(Cell cell) {
            if (cell != null) {
                this.cells.remove(cell.getColumnIndex());
            }
        }

        @Override
        public void setRowNum(@Min(0) int i) {
            this.index = i;
        }

        @Override
        public int getRowNum() {
            return this.index;
        }

        @Override
        public Cell getCell(int i) {
            return this.getCell(i, MissingCellPolicy.RETURN_NULL_AND_BLANK);
        }

        @Override
        public Cell getCell(int i, MissingCellPolicy missingCellPolicy) {
            return this.cells.get(i);
        }

        @Override
        public short getFirstCellNum() {
            return this.columns.isEmpty() ? -1 : this.columns.first().shortValue();
        }

        @Override
        public short getLastCellNum() {
            return this.columns.isEmpty() ? -1 : (short) (this.columns.last().shortValue() + 1);
        }

        @Override
        public int getPhysicalNumberOfCells() {
            return this.columns.size();
        }

        @Override
        public void setHeight(short i) {
        }

        @Override
        public void shiftCellsRight(int i, int i1, int i2) {
        }

        @Override
        public void shiftCellsLeft(int i, int i1, int i2) {
        }

        @Override
        public void setZeroHeight(boolean b) {
        }

        @Override
        public boolean getZeroHeight() {
            return false;
        }

        @Override
        public void setHeightInPoints(float v) {
        }

        @Override
        public short getHeight() {
            return 0;
        }

        @Override
        public float getHeightInPoints() {
            return 0;
        }

        @Override
        public boolean isFormatted() {
            return false;
        }

        @Override
        public CellStyle getRowStyle() {
            return null;
        }

        @Override
        public void setRowStyle(CellStyle cellStyle) {
        }

        @Override
        public Iterator<Cell> cellIterator() {
            return new Iterator<Cell>() {
                private Iterator<Integer> columnIterator = columns.iterator();

                @Override
                public boolean hasNext() {
                    return columnIterator.hasNext();
                }

                @Override
                public Cell next() {
                    return cells.get(columnIterator.next());
                }
            };
        }

        @Override
        public Sheet getSheet() {
            return null;
        }

        @Override
        public int getOutlineLevel() {
            return 0;
        }

        @Override
        public Iterator<Cell> iterator() {
            return this.cellIterator();
        }

        @Override
        public String toString() {
            Integer last = -1;
            List<String> values = new LinkedList<>();
            for (Integer column : this.columns) {
                int differ = column - last;
                if (differ > 1) {
                    for (int i = 0, len = differ - 1; i < len; i++) {
                        values.add(null);
                    }
                }
                values.add(this.cells.get(column).toString());
                last = column;
            }
            return values.toString();
        }
    }

    /**
     * 基于XML的Excel单元格实现
     */
    private static class XMLCell implements Cell {
        private Row row;
        private int column;
        private Object value;
        private CellType type;

        public XMLCell(@Nonnull Row row, @Min(0) int column) {
            this.row = row;
            this.column = column;
        }

        @Override
        public int getColumnIndex() {
            return this.column;
        }

        @Override
        public int getRowIndex() {
            return this.row.getRowNum();
        }

        @Override
        public Sheet getSheet() {
            return null;
        }

        @Override
        public Row getRow() {
            return this.row;
        }

        @Override
        public void setCellType(CellType cellType) {
            this.type = cellType;
        }

        @Override
        public CellType getCellType() {
            return this.type;
        }

        @Override
        public CellType getCellTypeEnum() {
            return this.type;
        }

        @Override
        public CellType getCachedFormulaResultType() {
            return null;
        }

        @Override
        public CellType getCachedFormulaResultTypeEnum() {
            return null;
        }

        @Override
        public void setCellValue(double v) {
            this.value = v;
        }

        @Override
        public void setCellValue(Date date) {
            this.value = date;
        }

        @Override
        public void setCellValue(Calendar calendar) {
        }

        @Override
        public void setCellValue(RichTextString richTextString) {
        }

        @Override
        public void setCellValue(String s) {
            this.value = s;
        }

        @Override
        public void setCellFormula(String s) throws FormulaParseException {
        }

        @Override
        public String getCellFormula() {
            return null;
        }

        @Override
        public double getNumericCellValue() {
            return this.type == CellType.BLANK ? 0.0D : (double) this.value;
        }

        @Override
        public Date getDateCellValue() {
            return (Date) this.value;
        }

        @Override
        public RichTextString getRichStringCellValue() {
            return null;
        }

        @Override
        public String getStringCellValue() {
            return (String) this.value;
        }

        @Override
        public void setCellValue(boolean b) {
            this.value = b;
        }

        @Override
        public void setCellErrorValue(byte b) {
            this.value = b;
        }

        @Override
        public boolean getBooleanCellValue() {
            return this.type == CellType.BLANK ? false : (boolean) this.value;
        }

        @Override
        public byte getErrorCellValue() {
            return 0;
        }

        @Override
        public void setCellStyle(CellStyle cellStyle) {
        }

        @Override
        public CellStyle getCellStyle() {
            return null;
        }

        @Override
        public void setAsActiveCell() {
        }

        @Override
        public CellAddress getAddress() {
            return null;
        }

        @Override
        public void setCellComment(Comment comment) {
        }

        @Override
        public Comment getCellComment() {
            return null;
        }

        @Override
        public void removeCellComment() {
        }

        @Override
        public Hyperlink getHyperlink() {
            return null;
        }

        @Override
        public void setHyperlink(Hyperlink hyperlink) {
        }

        @Override
        public void removeHyperlink() {
        }

        @Override
        public CellRangeAddress getArrayFormulaRange() {
            return null;
        }

        @Override
        public boolean isPartOfArrayFormulaGroup() {
            return false;
        }

        @Override
        public String toString() {
            return this.value == null ? null : this.value.toString();
        }
    }

    /**
     * Excel2007数据读取处理器
     */
    public static class Excel2007Reader extends DefaultHandler {
        private Row row; // 当前行
        private int count; // 迭代数量
        private int index; // 开始行下标
        private int column; // 当前列下标
        private String value; // 当前单元格值
        private CellType type; // 当前单元格类型
        private Reader callback; // 行遍历回调接口
        private boolean datable; // 当前单元格是否为日期格式
        private boolean related; // 数据是否关联
        private XMLReader parser;
        private XSSFReader reader;
        private SharedStringsTable shared;

        public Excel2007Reader(OPCPackage pkg, Reader callback) {
            this(pkg, 0, callback);
        }

        @Nonnull
        public Excel2007Reader(OPCPackage pkg, @Min(0) int index, Reader callback) {
            this.index = index;
            this.callback = callback;
            this.value = Strings.EMPTY_STRING;
            try {
                this.reader = new XSSFReader(pkg);
                this.parser = this.buildXMLReader();
                this.shared = this.reader.getSharedStringsTable();
            } catch (IOException | SAXException | OpenXML4JException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * 重置数据读取处理器
         */
        protected void reset() {
            this.count = 0;
            this.column = 0;
            this.row = null;
            this.type = null;
            this.value = Strings.EMPTY_STRING;
            this.related = false;
            this.datable = false;
        }

        /**
         * 构建XML数据读取处理器
         *
         * @return XML数据读取处理器
         * @throws SAXException 构建异常
         */
        protected XMLReader buildXMLReader() throws SAXException {
            XMLReader reader = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
            reader.setContentHandler(this);
            return reader;
        }

        /**
         * Excel读操作
         *
         * @return 数据读取行
         */
        public int process() {
            this.reset();
            try {
                Iterator<InputStream> sheets = this.reader.getSheetsData();
                while (sheets.hasNext()) {
                    try (InputStream sheet = sheets.next()) {
                        this.parser.parse(new InputSource(sheet));
                    }
                }
            } catch (IOException | SAXException | InvalidFormatException e) {
                throw new RuntimeException(e);
            }
            return this.count;
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            this.value += new String(ch, start, length);
        }

        @Override
        public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
            if ("row".equals(name)) {
                this.row = null; // 清空当前行
                int i = Integer.parseInt(attributes.getValue("r")) - 1; // 当前行下标
                if (i >= this.index) {
                    this.row = new XMLRow();
                    this.row.setRowNum(i);
                }
            } else if (this.row != null && this.row.getRowNum() >= this.index) {
                this.value = Strings.EMPTY_STRING; // 清空过程数据
                if ("c".equals(name)) {
                    // 获取列下标并创建单元格
                    StringBuilder column = new StringBuilder();
                    for (char c : attributes.getValue("r").toCharArray()) {
                        if (c >= 'A' && c <= 'Z') {
                            column.append(c);
                        }
                    }
                    this.row.createCell((this.column = columnAdapter(column.toString())));

                    // 设置单元格数据类型
                    String t = attributes.getValue("t");
                    String s = attributes.getValue("s");
                    this.datable = "1".equals(s) || "2".equals(s);
                    if ((this.related = "s".equals(t)) || "inlineStr".equals(t)) {
                        this.type = CellType.STRING;
                    } else if ("b".equals(t)) {
                        this.type = CellType.BOOLEAN;
                    } else if ("e".equals(t)) {
                        this.type = CellType.ERROR;
                    } else if (this.datable || Strings.isEmpty(t)) {
                        this.type = CellType.NUMERIC;
                    }
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String name) throws SAXException {
            if (this.row != null && this.row.getRowNum() >= this.index) {
                if ("row".equals(name) && !isEmpty(this.row)) {
                    this.callback.read(this.row, ++this.count); // 一行解析结束，执行回调接口
                } else if (!this.value.isEmpty() && ("v".equals(name) || "t".equals(name))) {
                    // 设置当前单元格类型及值
                    Cell cell = this.row.getCell(this.column);
                    cell.setCellType(this.type == CellType.ERROR ? CellType.STRING : this.type);
                    if (this.related) { // 如果为字符串，则从共享关联表中取数据
                        cell.setCellValue(this.shared.getItemAt(Integer.parseInt(this.value)).getString());
                    } else if (this.type == CellType.BOOLEAN) {
                        cell.setCellValue(Integer.parseInt(this.value) > 0);
                    } else if (this.type == CellType.NUMERIC) {
                        double number = Double.parseDouble(this.value);
                        if (this.datable) {
                            cell.setCellValue(HSSFDateUtil.getJavaDate(number));
                        } else {
                            cell.setCellValue(number);
                        }
                    } else {
                        cell.setCellValue(this.value);
                    }
                }
            }
        }
    }

    /**
     * 将Excel列字母字符串转换成下标
     *
     * @param column 列字母字符串
     * @return 列下标
     */
    public static int columnAdapter(String column) {
        Asserts.letter(column);
        int basic = 1, number = 0;
        for (int i = column.toUpperCase().length() - 1; i >= 0; i--) {
            number += (Character.toUpperCase(column.charAt(i)) - 'A' + 1) * basic;
            basic *= 26;
            if (number > Integer.MAX_VALUE) {
                return -1;
            }
        }
        return number - 1;
    }

    /**
     * 将Excel列下标适配成字母形式
     *
     * @param column 列下标（从0开始）
     * @return 字母字符串
     */
    public static String columnAdapter(@Min(0) int column) {
        StringBuilder letter = new StringBuilder();
        do {
            int mod = column % 26;
            letter.append((char) (mod + 'A'));
            column = (column - mod) / 26;
        } while (column-- > 0);
        return letter.reverse().toString();
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
        Workbook workbook;
        if (type == Type.XLS) {
            workbook = new HSSFWorkbook(input);
        } else if (type == Type.XLSX) {
            workbook = new XSSFWorkbook(input);
        } else {
            throw new IllegalArgumentException("Not support excel type: " + type);
        }
        evaluator.set(workbook.getCreationHelper().createFormulaEvaluator());
        return workbook;
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
        CellType type = source.getCellType();
        if (type == CellType.BOOLEAN) {
            target.setCellValue(source.getBooleanCellValue());
        } else if (type == CellType.NUMERIC) {
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
        for (int i = source.getFirstCellNum(); i < source.getLastCellNum(); i++) {
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
        for (int r = source.getFirstRowNum(), rows = source.getLastRowNum(); r <= rows; r++) {
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
            for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
                Object value = getValue(row.getCell(i));
                if (value != null && (!(value instanceof CharSequence) || !Strings.isBlank((CharSequence) value))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 将单元格值转换成日期对象
     *
     * @param object 值对象
     * @return 日期对象
     */
    public static Date toDate(Object object) {
        return toDate(object, Dates.ALL_DATE_FORMATS);
    }

    /**
     * 将单元格值转换成日期对象
     *
     * @param object   值对象
     * @param patterns 日期格式数组
     * @return 日期对象
     */
    public static Date toDate(Object object, String... patterns) {
        return object == null ? null : object instanceof Date ? (Date) object : object instanceof Number ?
                HSSFDateUtil.getJavaDate(((Number) object).doubleValue()) : Objects.toDate(object, patterns);
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
        CellType type = cell.getCellType();
        if (type == CellType.BOOLEAN) {
            return cell.getBooleanCellValue();
        } else if (type == CellType.NUMERIC) {
            return cell instanceof XMLCell ? ((XMLCell) cell).value :
                    HSSFDateUtil.isCellDateFormatted(cell) ? cell.getDateCellValue() : cell.getNumericCellValue();
        } else if (type == CellType.FORMULA) {
            if (cell.getCachedFormulaResultType() == CellType.ERROR) {
                return FormulaError.forInt(cell.getErrorCellValue()).getString();
            }
            CellValue value = evaluator.get().evaluate(cell);
            if (value.getCellType() == CellType.NUMERIC) {
                return HSSFDateUtil.isCellDateFormatted(cell) ? HSSFDateUtil.getJavaDate(value.getNumberValue()) : value.getNumberValue();
            } else if (value.getCellType() == CellType.BOOLEAN) {
                return value.getBooleanValue();
            }
            String s = value.getStringValue();
            return s == null || (s = s.trim()).isEmpty() ? null : s;
        } else if (type == CellType.ERROR) {
            return FormulaError.forInt(cell.getErrorCellValue());
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
    public static Date getDate(Cell cell, String... patterns) {
        return toDate(getValue(cell), patterns);
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
                cell.getCellType() == CellType.NUMERIC ? cell.getNumericCellValue() : Objects.toDouble(getString(cell));
    }

    /**
     * 获取单元格Boolean值
     *
     * @param cell 单元格对象
     * @return true/false
     */
    public static Boolean getBoolean(Cell cell) {
        return cell == null ? null :
                cell.getCellType() == CellType.BOOLEAN ? cell.getBooleanCellValue() : Objects.toBoolean(getValue(cell));
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
        int columns = row.getLastCellNum();
        T[] values = (T[]) Array.newInstance(type, columns);
        if (columns > 0) {
            for (int i = row.getFirstCellNum(); i < columns; i++) {
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
            font.setBold(true);
            CellStyle style = workbook.createCellStyle();
            style.setFont(font);
            style.setAlignment(HorizontalAlignment.CENTER);
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
     * 通过解析XML的方式读取Excel2007数据
     *
     * @param pkg    文件包
     * @param reader Excel对象实例读取接口
     * @return 读取数量
     * @throws Exception 操作异常
     */
    public static int read(OPCPackage pkg, Reader reader) throws Exception {
        return read(pkg, 0, reader);
    }

    /**
     * 通过解析XML的方式读取Excel2007数据
     *
     * @param pkg    文件包
     * @param index  开始数据行下标（从0开始）
     * @param reader Excel对象实例读取接口
     * @return 读取数量
     * @throws Exception 操作异常
     */
    public static int read(OPCPackage pkg, int index, Reader reader) throws Exception {
        return new Excel2007Reader(pkg, index, reader).process();
    }

    /**
     * 根据Excel行获取对象实例
     *
     * @param <M>  数据类型
     * @param row  Excel行对象
     * @param type 对象类型
     * @return 对象实例
     */
    public static <M> M read(Row row, Class<M> type) {
        return read(row, type, (t, o) -> o == null ? null :
                Date.class.isAssignableFrom(t) ? toDate(o) : LocalDate.class.isAssignableFrom(t) ? Dates.adapter(toDate(o)).toLocalDate() :
                        LocalDateTime.class.isAssignableFrom(t) ? Dates.adapter(toDate(o)) : Objects.toObject(t, o));
    }

    /**
     * 根据Excel行获取对象实例
     *
     * @param <M>     数据类型
     * @param row     Excel行对象
     * @param type    对象类型
     * @param adapter 对象适配器
     * @return 对象实例
     */
    public static <M> M read(Row row, @Nonnull Class<M> type, @Nonnull Objects.Adapter adapter) {
        return isEmpty(row) ? null : Objects.initialize(type, getValues(row), adapter);
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
