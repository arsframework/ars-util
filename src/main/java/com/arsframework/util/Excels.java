package com.arsframework.util;

import java.io.*;
import java.util.Map;
import java.util.List;
import java.util.Date;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedList;
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

import com.arsframework.annotation.Min;
import com.arsframework.annotation.Nonnull;

/**
 * Excel处理工具类
 *
 * @author yongqiang.wu
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
     * Excel格式枚举
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
     * Excel读写器实现
     */
    public static class ReadWriter implements Reader {
        private int row; // 当前行下标
        private Sheet sheet; // 当前Excel表格
        protected final int index; // 写入数据开始行下标
        protected final Workbook workbook; // 写入数据Excel工作薄
        protected final Writer<Row> writer; // 数据行写入接口

        @Nonnull
        public ReadWriter(Workbook workbook) {
            this(workbook, 0);
        }

        @Nonnull
        public ReadWriter(Workbook workbook, int index) {
            this(workbook, index, (target, source, count) -> copy(source, target));
        }

        @Nonnull
        public ReadWriter(Workbook workbook, Writer<Row> writer) {
            this(workbook, 0, writer);
        }

        @Nonnull
        public ReadWriter(Workbook workbook, @Min(0) int index, Writer<Row> writer) {
            this.index = index;
            this.writer = writer;
            this.workbook = workbook;
        }

        @Override
        public void read(Row row, int count) {
            if (this.sheet == null || (this.row > this.index && this.row % DEFAULT_SHEET_VOLUME == 0)) {
                this.row = index;
                this.sheet = this.workbook.createSheet();
            }
            this.writer.write(this.sheet.createRow(this.row++), row, count);
        }
    }

    /**
     * 基于XML的Excel行实现
     */
    public static class XMLRow implements Row {
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
            throw new UnsupportedOperationException();
        }

        @Override
        public void shiftCellsRight(int i, int i1, int i2) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void shiftCellsLeft(int i, int i1, int i2) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setZeroHeight(boolean b) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean getZeroHeight() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setHeightInPoints(float v) {
            throw new UnsupportedOperationException();
        }

        @Override
        public short getHeight() {
            throw new UnsupportedOperationException();
        }

        @Override
        public float getHeightInPoints() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isFormatted() {
            throw new UnsupportedOperationException();
        }

        @Override
        public CellStyle getRowStyle() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setRowStyle(CellStyle cellStyle) {
            throw new UnsupportedOperationException();
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
            throw new UnsupportedOperationException();
        }

        @Override
        public int getOutlineLevel() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Iterator<Cell> iterator() {
            return this.cellIterator();
        }

        @Override
        public String toString() {
            Integer last = -1;
            List<Cell> cells = new LinkedList<>();
            for (Integer column : this.columns) {
                int differ = column - last;
                if (differ > 1) {
                    for (int i = 0, len = differ - 1; i < len; i++) {
                        cells.add(null);
                    }
                }
                cells.add(this.cells.get(column));
                last = column;
            }
            return cells.toString();
        }
    }

    /**
     * 基于XML的Excel单元格实现
     */
    public static class XMLCell implements Cell {
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
            throw new UnsupportedOperationException();
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
            throw new UnsupportedOperationException();
        }

        @Override
        public CellType getCachedFormulaResultTypeEnum() {
            throw new UnsupportedOperationException();
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
            throw new UnsupportedOperationException();
        }

        @Override
        public void setCellValue(RichTextString richTextString) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setCellValue(String s) {
            this.value = s;
        }

        @Override
        public void setCellFormula(String s) throws FormulaParseException {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getCellFormula() {
            throw new UnsupportedOperationException();
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
            throw new UnsupportedOperationException();
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
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean getBooleanCellValue() {
            return this.type == CellType.BLANK ? false : (boolean) this.value;
        }

        @Override
        public byte getErrorCellValue() {
            throw new UnsupportedOperationException();
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
            throw new UnsupportedOperationException();
        }

        @Override
        public CellAddress getAddress() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setCellComment(Comment comment) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Comment getCellComment() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void removeCellComment() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Hyperlink getHyperlink() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setHyperlink(Hyperlink hyperlink) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void removeHyperlink() {
            throw new UnsupportedOperationException();
        }

        @Override
        public CellRangeAddress getArrayFormulaRange() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isPartOfArrayFormulaGroup() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString() {
            return this.value == null ? null : this.value.toString();
        }
    }

    /**
     * Excel2007数据读取处理器抽象实现
     */
    public static abstract class AbstractExcel2007Reader extends DefaultHandler implements Reader {
        private Row row; // 当前行
        private int count; // 迭代数量
        private int index; // 开始行下标
        private int column; // 当前列下标
        private String value; // 当前单元格值
        protected boolean datable; // 值是否为日期
        protected boolean related; // 数据是否关联
        protected OPCPackage pkg; // 文件包
        protected SharedStringsTable shared; // 共享字符串表

        public AbstractExcel2007Reader(OPCPackage pkg) {
            this(pkg, 0);
        }

        @Nonnull
        public AbstractExcel2007Reader(OPCPackage pkg, @Min(0) int index) {
            this.pkg = pkg;
            this.index = index;
            this.value = Strings.EMPTY_STRING;
        }

        /**
         * 构建行对象，解析行开始标签时调用
         *
         * @return 行对象实例
         */
        protected Row buildRow() {
            return new XMLRow();
        }

        /**
         * 构建单元格，解析单元格开始标签时调用
         *
         * @param row    行对象
         * @param column 列下标（从0开始）
         * @return 单元格对象
         */
        @Nonnull
        protected Cell buildCell(Row row, @Min(0) int column) {
            return row.createCell(column);
        }

        /**
         * 构建XML数据读取处理器
         *
         * @return XML数据读取处理器
         * @throws SAXException 构建异常
         */
        protected XMLReader buildXMLReader() throws SAXException {
            return XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
        }

        /**
         * 根据单元格标签属性解析单元格地址，解析单元格开始标签时调用
         *
         * @param attributes 标签属性对象
         * @return 单元格地址
         */
        @Nonnull
        protected CellAddress analyseCellAddress(Attributes attributes) {
            return addressAdapter(attributes.getValue("r"));
        }

        /**
         * 初始化单元格类型，解析单元格开始标签时调用
         *
         * @param cell       单元格对象
         * @param attributes 标签属性对象
         */
        @Nonnull
        protected void initializeCellType(Cell cell, Attributes attributes) {
            String t = attributes.getValue("t");
            String s = attributes.getValue("s");
            this.datable = "1".equals(s) || "2".equals(s);
            if ((this.related = "s".equals(t)) || "inlineStr".equals(t)) {
                cell.setCellType(CellType.STRING);
            } else if ("b".equals(t)) {
                cell.setCellType(CellType.BOOLEAN);
            } else if (this.datable || Strings.isEmpty(t)) {
                cell.setCellType(CellType.NUMERIC);
            } else {
                cell.setCellType(CellType.STRING);
            }
        }

        /**
         * 初始化单元格值，解析单元格值结束标签时调用
         *
         * @param cell  单元格对象
         * @param value 原始值
         */
        @Nonnull
        protected void initializeCellValue(Cell cell, String value) {
            if (this.related) { // 如果为字符串，则从共享关联表中取数据
                cell.setCellValue(this.shared.getItemAt(Integer.parseInt(value)).getString());
            } else if (cell.getCellType() == CellType.BOOLEAN) {
                cell.setCellValue(Integer.parseInt(value) > 0);
            } else if (cell.getCellType() == CellType.NUMERIC) {
                double number = Double.parseDouble(value);
                if (this.datable) {
                    cell.setCellValue(HSSFDateUtil.getJavaDate(number));
                } else {
                    cell.setCellValue(number);
                }
            } else {
                cell.setCellValue(value);
            }
        }

        /**
         * Excel读操作
         *
         * @return 数据行总数
         */
        public int process() {
            try {
                XMLReader parser = this.buildXMLReader();
                parser.setContentHandler(this);
                XSSFReader reader = new XSSFReader(this.pkg);
                this.shared = reader.getSharedStringsTable();
                Iterator<InputStream> sheets = reader.getSheetsData();
                while (sheets.hasNext()) {
                    try (InputStream sheet = sheets.next()) {
                        parser.parse(new InputSource(sheet));
                    }
                }
            } catch (IOException | SAXException | OpenXML4JException e) {
                throw new RuntimeException(e);
            }
            return this.count;
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (this.value.isEmpty()) {
                this.value = new String(ch, start, length);
            } else {
                this.value += new String(ch, start, length);
            }
        }

        @Override
        public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
            if ("row".equals(name)) { // 行开始标签
                this.row = null; // 清空当前行
                int number = Integer.parseInt(attributes.getValue("r")) - 1; // 当前行下标
                if (number >= this.index) {
                    this.row = this.buildRow();
                    this.row.setRowNum(number);
                }
            } else if (this.row != null && this.row.getRowNum() >= this.index) {
                this.value = Strings.EMPTY_STRING; // 清空过程数据
                if ("c".equals(name)) { // 单元格开始标签
                    // 解析单元格所在列下标
                    this.column = this.analyseCellAddress(attributes).getColumn();
                    // 构建单元格并初始化单元格值类型
                    this.initializeCellType(this.buildCell(this.row, this.column), attributes);
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String name) throws SAXException {
            if (this.row != null && this.row.getRowNum() >= this.index) {
                if ("row".equals(name) && !isEmpty(this.row)) { // 行解析完成
                    this.read(this.row, ++this.count);
                } else if (!this.value.isEmpty() && ("v".equals(name) || "t".equals(name))) { // 值解析完成
                    this.initializeCellValue(this.row.getCell(this.column), this.value);
                }
            }
        }
    }

    /**
     * Excel2007数据行记录器
     */
    public static class Excel2007Counter extends AbstractExcel2007Reader {
        public Excel2007Counter(OPCPackage pkg) {
            super(pkg);
        }

        public Excel2007Counter(OPCPackage pkg, int index) {
            super(pkg, index);
        }

        @Override
        protected void initializeCellType(Cell cell, Attributes attributes) {
            cell.setCellType(CellType.NUMERIC);
        }

        @Override
        protected void initializeCellValue(Cell cell, String value) {
            CellType type = cell.getCellType();
            if (!(type == CellType.BLANK || type == CellType.STRING)
                    || (type == CellType.STRING && !value.isEmpty() && !value.trim().isEmpty())) {
                cell.setCellValue(1); // 设置1表示此单元格值不为空
            }
        }

        @Override
        public void read(Row row, int count) {
        }
    }

    /**
     * 将Excel列字母字符串转换成下标
     *
     * @param column 列字母字符串
     * @return 列下标
     */
    @Nonnull
    public static int columnAdapter(String column) {
        if (!Strings.isLetter(column)) {
            throw new IllegalArgumentException("Invalid cell column: " + column);
        }
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
     * 将单元格对象转换成单元格地址字符串形式
     *
     * @param cell 单元格对象
     * @return 单元格地址字符串
     */
    @Nonnull
    public static String addressAdapter(Cell cell) {
        return addressAdapter(cell.getRowIndex(), cell.getColumnIndex());
    }

    /**
     * 将单元格地址对象转换成字符串形式
     *
     * @param address 单元格地址对象
     * @return 单元格地址字符串
     */
    @Nonnull
    public static String addressAdapter(CellAddress address) {
        return addressAdapter(address.getRow(), address.getColumn());
    }

    /**
     * 将单元格地址对象转换成字符串形式
     *
     * @param row    单元格行下标
     * @param column 单元格列下标
     * @return 单元格地址字符串
     */
    public static String addressAdapter(@Min(0) int row, @Min(0) int column) {
        return columnAdapter(column).concat(String.valueOf(row + 1));
    }

    /**
     * 将单元格地址字符串转换成地址对象
     *
     * @param address 单元格地址字符串
     * @return 单元格地址对象
     */
    @Nonnull
    public static CellAddress addressAdapter(String address) {
        for (int i = 0; i < address.length(); i++) {
            char c = address.charAt(i);
            if (c >= '1' && c <= '9') {
                return new CellAddress(Integer.parseInt(address.substring(i)) - 1, columnAdapter(address.substring(0, i)));
            }
        }
        throw new IllegalArgumentException("Invalid cell address: " + address);
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
     * @param path 文件路径
     * @return Excel工作薄
     * @throws IOException IO操作异常
     */
    @Nonnull
    public static Workbook buildWorkbook(String path) throws IOException {
        try (InputStream is = new FileInputStream(path)) {
            return buildWorkbook(is, Type.parse(Files.getSuffix(path)));
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
     * 构建默认读写器
     *
     * @param workbook Excel工作薄
     * @return Excel读接口
     */
    public static Reader buildReadWriter(Workbook workbook) {
        return new ReadWriter(workbook);
    }

    /**
     * 构建默认读写器
     *
     * @param workbook Excel工作薄
     * @param index    写文件开始行下标
     * @return Excel读接口
     */
    public static Reader buildReadWriter(Workbook workbook, int index) {
        return new ReadWriter(workbook, index);
    }

    /**
     * 构建指定读写器
     *
     * @param workbook Excel工作薄
     * @param writer   Excel写接口
     * @return Excel读接口
     */
    public static Reader buildReadWriter(Workbook workbook, Writer<Row> writer) {
        return new ReadWriter(workbook, writer);
    }

    /**
     * 构建指定读写器
     *
     * @param workbook Excel工作薄
     * @param index    写文件开始行下标
     * @param writer   Excel写接口
     * @return Excel读接口
     */
    public static Reader buildReadWriter(Workbook workbook, int index, Writer<Row> writer) {
        return new ReadWriter(workbook, index, writer);
    }

    /**
     * 保存Excel数据到本地文件
     *
     * @param workbook Excel工作薄
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
        setValue(target, source.getCellStyle(), getValue(source));
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
     * 获取Excel单元格值
     *
     * @param cell Excel单元格对象
     * @return 值
     */
    public static Object getValue(Cell cell) {
        if (cell == null) {
            return null;
        } else if (cell instanceof XMLCell) {
            Object value = ((XMLCell) cell).value;
            return value instanceof String ? Strings.trim((String) value) : value;
        }
        CellType type = cell.getCellType();
        if (type == CellType.BLANK) {
            return null;
        } else if (type == CellType.BOOLEAN) {
            return cell.getBooleanCellValue();
        } else if (type == CellType.NUMERIC) {
            return HSSFDateUtil.isCellDateFormatted(cell) ? cell.getDateCellValue() : cell.getNumericCellValue();
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
            return Strings.trim(value.getStringValue());
        } else if (type == CellType.ERROR) {
            return FormulaError.forInt(cell.getErrorCellValue());
        }
        return Strings.trim(cell.getStringCellValue());
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
        return Objects.toDate(getValue(cell), patterns);
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
        return Objects.toDouble(getValue(cell));
    }

    /**
     * 获取单元格Boolean值
     *
     * @param cell 单元格对象
     * @return true/false
     */
    public static Boolean getBoolean(Cell cell) {
        return Objects.toBoolean(getValue(cell));
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
            if (value instanceof Number) {
                cell.setCellValue(((Number) value).doubleValue());
            } else if (value instanceof Boolean) {
                cell.setCellValue((Boolean) value);
            } else if (value instanceof int[]) {
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
                cell.setCellValue(Strings.join((Object[]) value, ","));
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
        if (values.length > 0) {
            for (int i = 0; i < values.length; i++) {
                setValue(row.createCell(i), style, values[i]);
            }
        }
    }

    /**
     * 设置Excel公式
     *
     * @param row      Excel行
     * @param formulas 公式数组
     */
    @Nonnull
    public static void setFormulas(Row row, String... formulas) {
        setFormulas(row, null, formulas);
    }

    /**
     * 设置Excel公式
     *
     * @param row      Excel行
     * @param style    单元格样式
     * @param formulas 公式数组
     */
    public static void setFormulas(@Nonnull Row row, CellStyle style, @Nonnull String... formulas) {
        if (formulas.length > 0) {
            for (int i = 0; i < formulas.length; i++) {
                String formula = formulas[i];
                if (!Strings.isBlank(formula)) {
                    Cell cell = row.createCell(i);
                    cell.setCellStyle(style);
                    cell.setCellFormula(formula);
                }
            }
        }
    }

    /**
     * 设置Excel过滤器
     *
     * @param row Excel行
     */
    @Nonnull
    public static void setFilters(Row row) {
        row.getSheet().setAutoFilter(
                new CellRangeAddress(row.getRowNum(), row.getRowNum(), row.getFirstCellNum(), row.getLastCellNum() - 1));
    }

    /**
     * 构建标题样式
     *
     * @param workbook Excel工作薄
     * @return 样式对象
     */
    @Nonnull
    public static CellStyle buildTitleStyle(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    /**
     * 设置Excel标题
     *
     * @param row    Excel行
     * @param titles 标题数组
     */
    @Nonnull
    public static void setTitles(Row row, String... titles) {
        if (titles.length > 0) {
            setValues(row, buildTitleStyle(row.getSheet().getWorkbook()), titles);
        }
    }

    /**
     * Excel单元格树
     */
    public static class Tree {
        /**
         * 单元格数量
         */
        public final int count;

        /**
         * 树宽度
         */
        public final int width;

        /**
         * 树高度
         */
        public final int height;

        /**
         * 行跨度
         */
        public final int rowspan;

        /**
         * 单元格值
         */
        public final Object value;

        /**
         * 单元格样式
         */
        public final CellStyle style;

        /**
         * 单元格子树数组
         */
        public final Tree[] children;

        public Tree(Object value, CellStyle style, @Min(1) int rowspan, @Nonnull Tree... children) {
            this.value = value;
            this.style = style;
            this.rowspan = rowspan;
            this.children = children;
            this.count = Arrays.stream(children).mapToInt(t -> t.count).sum() + 1;
            this.width = children.length == 0 ? 1 : Arrays.stream(children).mapToInt(t -> t.width).sum();
            this.height = children.length == 0 ? 1 : Arrays.stream(children).mapToInt(t -> t.height + 1).max().getAsInt();
        }

        @Override
        public String toString() {
            return this.value == null ? null : this.value.toString();
        }
    }

    /**
     * 构建单元格树
     *
     * @param value    单元格值
     * @param children 单元格子树数组
     * @return 单元格树对象
     */
    public static Tree T(Object value, Tree... children) {
        return T(value, null, 1, children);
    }

    /**
     * 转换标题
     *
     * @param value    单元格值
     * @param rowspan  行跨度
     * @param children 单元格子树数组
     * @return 单元格树对象
     */
    public static Tree T(Object value, int rowspan, Tree... children) {
        return T(value, null, rowspan, children);
    }

    /**
     * 转换标题
     *
     * @param value    单元格值
     * @param style    单元格样式
     * @param children 单元格子树数组
     * @return 单元格树对象
     */
    public static Tree T(Object value, CellStyle style, Tree... children) {
        return T(value, style, 1, children);
    }

    /**
     * 转换标题
     *
     * @param value    单元格值
     * @param style    单元格样式
     * @param rowspan  行跨度
     * @param children 单元格子树数组
     * @return 单元格树对象
     */
    public static Tree T(Object value, CellStyle style, int rowspan, Tree... children) {
        return new Tree(value, style, rowspan, children);
    }

    /**
     * 设置单元格树
     *
     * @param sheet Excel表
     * @param trees 单元格树数组
     */
    public static void setTrees(Sheet sheet, Tree... trees) {
        setTrees(sheet, 0, trees);
    }

    /**
     * 设置单元格树
     *
     * @param sheet Excel表
     * @param index 开始行下标（从0开始）
     * @param trees 单元格树数组
     */
    public static void setTrees(Sheet sheet, int index, Tree... trees) {
        setTrees(sheet, index, 0, new HashMap<>(), trees);
    }

    /**
     * 设置单元格树
     *
     * @param sheet  Excel表
     * @param index  开始行下标（从0开始）
     * @param column 开始列下标（从0开始）
     * @param cache  行对象缓存
     * @param trees  单元格树数组
     */
    @Nonnull
    private static void setTrees(Sheet sheet, @Min(0) int index, @Min(0) int column, Map<Integer, Row> cache, Tree... trees) {
        if (trees.length > 0) {
            Row row = cache.get(index);
            if (row == null) {
                row = sheet.createRow(index);
                cache.put(index, row);
            }
            for (Tree tree : trees) {
                setValue(row.createCell(column), tree.style, tree.value); // 设置单元格值
                if (tree.width > 1 || tree.rowspan > 1) { // 合并单元格
                    sheet.addMergedRegion(new CellRangeAddress(index, index + tree.rowspan - 1, column, column + tree.width - 1));
                }
                if (tree.children.length > 0) { // 遍历单元格子树
                    setTrees(sheet, index + tree.rowspan, column, cache, tree.children);
                }
                column += tree.width;
            }
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
     * @param workbook Excel工作薄
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
     * @param workbook Excel工作薄
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
     * 统计Excel数据行数
     *
     * @param pkg 文件包
     * @return 数量
     */
    public static int count(OPCPackage pkg) {
        return count(pkg, 0);
    }

    /**
     * 统计Excel数据行数
     *
     * @param pkg   文件包
     * @param index 开始数据行下标（从0开始）
     * @return 数量
     */
    @Nonnull
    public static int count(OPCPackage pkg, @Min(0) int index) {
        return new Excel2007Counter(pkg, index).process();
    }

    /**
     * 通过解析XML的方式读取Excel2007数据
     *
     * @param pkg    文件包
     * @param reader Excel读接口
     * @return 读取数量
     */
    public static int read(OPCPackage pkg, Reader reader) {
        return read(pkg, 0, reader);
    }

    /**
     * 通过解析XML的方式读取Excel2007数据
     *
     * @param pkg    文件包
     * @param index  开始数据行下标（从0开始）
     * @param reader Excel读接口
     * @return 读取数量
     */
    public static int read(OPCPackage pkg, int index, Reader reader) {
        return new AbstractExcel2007Reader(pkg, index) {
            @Override
            public void read(Row row, int count) {
                reader.read(row, count);
            }
        }.process();
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
        return isEmpty(row) ? null : Objects.initialize(type, getValues(row));
    }

    /**
     * 从Excel中获取对象实例
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
     * 从Excel中获取对象实例
     *
     * @param <M>      数据类型
     * @param workbook Excel工作薄
     * @param type     对象类型
     * @return 对象实例列表
     */
    public static <M> List<M> read(Workbook workbook, Class<M> type) {
        return read(workbook, type, 0);
    }

    /**
     * 从Excel中获取对象实例
     *
     * @param <M>      数据类型
     * @param workbook Excel工作薄
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
     * 从Excel中获取对象实例
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
     * 从Excel中获取对象实例
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
     * 读Excel
     *
     * @param sheet  Excel表格
     * @param reader Excel读接口
     * @return 读取数量
     */
    public static int read(Sheet sheet, Reader reader) {
        return read(sheet, 0, reader);
    }

    /**
     * 读Excel
     *
     * @param workbook Excel工作薄
     * @param reader   Excel读接口
     * @return 读取数量
     */
    public static int read(Workbook workbook, Reader reader) {
        return read(workbook, 0, reader);
    }

    /**
     * 读Excel
     *
     * @param sheet  Excel表格
     * @param index  开始数据行下标（从0开始）
     * @param reader Excel读接口
     * @return 读取数量
     */
    @Nonnull
    public static int read(Sheet sheet, @Min(0) int index, Reader reader) {
        int[] count = {0};
        read(sheet, index, count, reader);
        return count[0];
    }

    /**
     * 读Excel
     *
     * @param workbook Excel工作薄
     * @param index    开始数据行下标（从0开始）
     * @param reader   Excel读接口
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
     * 读Excel
     *
     * @param sheet  Excel表格
     * @param index  开始数据行下标（从0开始）
     * @param count  当前记录数（从1开始）
     * @param reader Excel读接口
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
            Objects.foreach(object.getClass(), (field, i) -> setValue(row.createCell(i), Objects.getValue(object, field)));
        }
    }

    /**
     * 将对象实例设置到Excel中
     *
     * @param sheet   Excel表格
     * @param objects 对象实例列表
     */
    public static void write(Sheet sheet, List<?> objects) {
        write(sheet, objects, 0);
    }

    /**
     * 将对象实例设置到Excel中
     *
     * @param workbook Excel工作薄
     * @param objects  对象实例列表
     */
    public static void write(Workbook workbook, List<?> objects) {
        write(workbook, objects, 0);
    }

    /**
     * 将对象实例设置到Excel中
     *
     * @param sheet   Excel表格
     * @param objects 对象实例列表
     * @param index   开始数据行下标（从0开始）
     */
    @Nonnull
    public static void write(Sheet sheet, List<?> objects, @Min(0) int index) {
        write(sheet, objects, index, (row, object, count) -> write(row, object));
    }

    /**
     * 将对象实例设置到Excel中
     *
     * @param workbook Excel工作薄
     * @param objects  对象实例列表
     * @param index    开始数据行下标（从0开始）
     */
    @Nonnull
    public static void write(Workbook workbook, List<?> objects, @Min(0) int index) {
        write(workbook, objects, index, (row, object, count) -> write(row, object));
    }

    /**
     * 读Excel
     *
     * @param <M>     数据类型
     * @param sheet   Excel表格
     * @param objects 对象实例列表
     * @param writer  Excel对象实例写入接口
     */
    public static <M> void write(Sheet sheet, List<M> objects, Writer<M> writer) {
        write(sheet, objects, 0, writer);
    }

    /**
     * 读Excel
     *
     * @param <M>      数据类型
     * @param workbook Excel工作薄
     * @param objects  对象实例列表
     * @param writer   Excel对象实例写入接口
     */
    public static <M> void write(Workbook workbook, List<M> objects, Writer<M> writer) {
        write(workbook, objects, 0, writer);
    }

    /**
     * 读Excel
     *
     * @param <M>     数据类型
     * @param sheet   Excel表格
     * @param objects 对象实例列表
     * @param index   开始数据行下标（从0开始）
     * @param writer  Excel对象实例写入接口
     */
    @Nonnull
    public static <M> void write(Sheet sheet, List<M> objects, @Min(0) int index, Writer<M> writer) {
        if (!objects.isEmpty()) {
            int c = 0;
            for (M object : objects) {
                writer.write(sheet.createRow(index++), object, ++c);
            }
        }
    }

    /**
     * 读Excel
     *
     * @param <M>      数据类型
     * @param workbook Excel工作薄
     * @param objects  对象实例列表
     * @param index    开始数据行下标（从0开始）
     * @param writer   Excel对象实例写入接口
     */
    @Nonnull
    public static <M> void write(Workbook workbook, List<M> objects, @Min(0) int index, Writer<M> writer) {
        if (!objects.isEmpty()) {
            int c = 0, r = index;
            Sheet sheet = workbook.createSheet();
            for (M object : objects) {
                if (r > index && r % DEFAULT_SHEET_VOLUME == 0) {
                    r = index;
                    sheet = workbook.createSheet();
                }
                writer.write(sheet.createRow(r++), object, ++c);
            }
        }
    }
}
