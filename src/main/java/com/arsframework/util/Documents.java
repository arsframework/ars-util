package com.arsframework.util;

import java.io.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.awt.print.Paper;
import java.awt.print.PageFormat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.apache.fop.svg.PDFTranscoder;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.print.PrintTranscoder;
import org.xhtmlrenderer.render.PageBox;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.RenderingContext;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextOutputDevice;
import org.xhtmlrenderer.pdf.ITextReplacedElement;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.extend.ReplacedElementFactory;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.simple.extend.FormSubmissionListener;

import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.DocumentException;
import com.arsframework.annotation.Nonnull;

/**
 * 文档处理工具类
 *
 * @author yongqiang.wu
 * @version 2019-03-22 16:21
 */
public abstract class Documents {
    /**
     * 连接替换元素工厂对象
     */
    private static class ChainingReplacedElementFactory implements ReplacedElementFactory {
        private List<ReplacedElementFactory> replacedElementFactories = new ArrayList<>();

        public void addReplacedElementFactory(ReplacedElementFactory replacedElementFactory) {
            this.replacedElementFactories.add(0, replacedElementFactory);
        }

        @Override
        public ReplacedElement createReplacedElement(LayoutContext c, BlockBox box, UserAgentCallback uac, int cssWidth, int cssHeight) {
            for (ReplacedElementFactory replacedElementFactory : this.replacedElementFactories) {
                ReplacedElement element = replacedElementFactory.createReplacedElement(c, box, uac, cssWidth, cssHeight);
                if (element != null) {
                    return element;
                }
            }
            return null;
        }

        @Override
        public void reset() {
            for (ReplacedElementFactory replacedElementFactory : this.replacedElementFactories) {
                replacedElementFactory.reset();
            }
        }

        @Override
        public void remove(Element e) {
            for (ReplacedElementFactory replacedElementFactory : this.replacedElementFactories) {
                replacedElementFactory.remove(e);
            }
        }

        @Override
        public void setFormSubmissionListener(FormSubmissionListener listener) {
            for (ReplacedElementFactory replacedElementFactory : this.replacedElementFactories) {
                replacedElementFactory.setFormSubmissionListener(listener);
            }
        }

    }

    /**
     * SVG元素工厂对象
     */
    private static class SVGReplacedElementFactory implements ReplacedElementFactory {

        @Override
        public ReplacedElement createReplacedElement(LayoutContext c, BlockBox box, UserAgentCallback uac, int cssWidth, int cssHeight) {
            Element element = box.getElement();
            if ("svg".equals(element.getNodeName())) {
                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder;
                try {
                    documentBuilder = documentBuilderFactory.newDocumentBuilder();
                } catch (ParserConfigurationException e) {
                    throw new RuntimeException(e);
                }
                Document svgDocument = documentBuilder.newDocument();
                Element svgElement = (Element) svgDocument.importNode(element, true);
                svgDocument.appendChild(svgElement);
                return new SVGReplacedElement(svgDocument, cssWidth, cssHeight);
            }
            return null;
        }

        @Override
        public void reset() {

        }

        @Override
        public void remove(Element e) {

        }

        @Override
        public void setFormSubmissionListener(FormSubmissionListener listener) {

        }

    }

    /**
     * SVG元素对象
     */
    private static class SVGReplacedElement implements ITextReplacedElement {
        private Point location = new Point(0, 0);
        private Document svg;
        private int cssWidth;
        private int cssHeight;

        public SVGReplacedElement(Document svg, int cssWidth, int cssHeight) {
            this.cssWidth = cssWidth;
            this.cssHeight = cssHeight;
            this.svg = svg;
        }

        @Override
        public void detach(LayoutContext c) {
        }

        @Override
        public int getBaseline() {
            return 0;
        }

        @Override
        public int getIntrinsicWidth() {
            return cssWidth;
        }

        @Override
        public int getIntrinsicHeight() {
            return cssHeight;
        }

        @Override
        public boolean hasBaseline() {
            return false;
        }

        @Override
        public boolean isRequiresInteractivePaint() {
            return false;
        }

        @Override
        public Point getLocation() {
            return location;
        }

        @Override
        public void setLocation(int x, int y) {
            this.location.x = x;
            this.location.y = y;
        }

        @Override
        public void paint(RenderingContext renderingContext, ITextOutputDevice outputDevice, BlockBox blockBox) {
            PdfContentByte cb = outputDevice.getWriter().getDirectContent();
            float width = this.cssWidth / outputDevice.getDotsPerPoint();
            float height = this.cssHeight / outputDevice.getDotsPerPoint();

            PdfTemplate template = cb.createTemplate(width, height);
            Graphics2D g2d = new PdfGraphics2D(cb, width, height);
            PrintTranscoder prm = new PrintTranscoder();
            TranscoderInput ti = new TranscoderInput(this.svg);
            prm.transcode(ti, null);
            PageFormat pg = new PageFormat();
            Paper pp = new Paper();
            pp.setSize(width, height);
            pp.setImageableArea(0, 0, width, height);
            pg.setPaper(pp);
            prm.print(g2d, pg, 0);
            g2d.dispose();

            PageBox page = renderingContext.getPage();
            float x = blockBox.getAbsX() + page.getMarginBorderPadding(renderingContext, CalculatedStyle.LEFT);
            float y = (page.getBottom() - (blockBox.getAbsY() + this.cssHeight))
                    + page.getMarginBorderPadding(renderingContext, CalculatedStyle.BOTTOM);
            x /= outputDevice.getDotsPerPoint();
            y /= outputDevice.getDotsPerPoint();

            cb.addTemplate(template, x, y);
        }

    }

    /**
     * 将SVG数据转换成PDF文件
     *
     * @param svg    SVG字符串
     * @param output PDF文件输出流
     * @throws TranscoderException 转换异常
     */
    @Nonnull
    public static void svg2pdf(String svg, OutputStream output) throws TranscoderException {
        svg2pdf(new StringReader(svg), new OutputStreamWriter(output));
    }

    /**
     * 将SVG输入流转换并写入到PDF输出流
     *
     * @param reader SVG输入流
     * @param writer PDF输出流
     * @throws TranscoderException 转换异常
     */
    @Nonnull
    public static void svg2pdf(Reader reader, Writer writer) throws TranscoderException {
        Transcoder transcoder = new PDFTranscoder();
        transcoder.transcode(new TranscoderInput(reader), new TranscoderOutput(writer));
    }

    /**
     * 将SVG数据转换成PNG文件
     *
     * @param svg    SVG字符串
     * @param output PNG文件输出流
     * @throws TranscoderException 转换异常
     */
    @Nonnull
    public static void svg2png(String svg, OutputStream output) throws TranscoderException {
        svg2png(new StringReader(svg), new OutputStreamWriter(output));
    }

    /**
     * 将SVG输入流转换并写入到PNG输出流
     *
     * @param reader SVG输入流
     * @param writer PNG输出流
     * @throws TranscoderException 转换异常
     */
    @Nonnull
    public static void svg2png(Reader reader, Writer writer) throws TranscoderException {
        Transcoder transcoder = new PNGTranscoder();
        transcoder.transcode(new TranscoderInput(reader), new TranscoderOutput(writer));
    }

    /**
     * 将SVG数据转换成JPEG文件
     *
     * @param svg    SVG字符串
     * @param output JPEG文件输出流
     * @throws TranscoderException 转换异常
     */
    @Nonnull
    public static void svg2jpeg(String svg, OutputStream output) throws TranscoderException {
        svg2jpeg(new StringReader(svg), new OutputStreamWriter(output));
    }

    /**
     * 将SVG输入流转换并写入到JPEG输出流
     *
     * @param reader SVG字符流
     * @param writer JPEG输出流
     * @throws TranscoderException 转换异常
     */
    @Nonnull
    public static void svg2jpeg(Reader reader, Writer writer) throws TranscoderException {
        Transcoder transcoder = new JPEGTranscoder();
        transcoder.transcode(new TranscoderInput(reader), new TranscoderOutput(writer));
    }

    /**
     * 将文本格式转换成PDF格式
     *
     * @param renderer 文本渲染器
     * @param output   PDF文件输出流
     * @param fonts    样式文件路径数据
     * @throws DocumentException 文档操作异常
     * @throws IOException       IO操作异常
     */
    @Nonnull
    public static void text2pdf(ITextRenderer renderer, OutputStream output, String... fonts) throws DocumentException, IOException {
        ChainingReplacedElementFactory chainingReplacedElementFactory = new ChainingReplacedElementFactory();
        chainingReplacedElementFactory.addReplacedElementFactory(new SVGReplacedElementFactory());
        renderer.getSharedContext().setReplacedElementFactory(chainingReplacedElementFactory);
        if (fonts.length > 0) {
            ITextFontResolver fontResolver = renderer.getFontResolver();
            for (String font : fonts) {
                fontResolver.addFont(Strings.toRealPath(font), BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
            }
        }
        renderer.layout();
        renderer.createPDF(output);
    }
}
