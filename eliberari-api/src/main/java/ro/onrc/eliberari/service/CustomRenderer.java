package ro.onrc.eliberari.service;

import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.PageDrawer;
import org.apache.pdfbox.rendering.PageDrawerParameters;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.contentstream.operator.text.ShowText;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

/**
 * PDFRenderer personalizat care forțează substituția fontului în timpul randării
 */
public class CustomRenderer extends PDFRenderer {
    private PDType0Font replacementFont;

    public CustomRenderer(PDDocument document) throws IOException {
        super(document);
        // Încărcăm Arial ca rezervă
        File fontFile = new File("C:/Windows/Fonts/arial.ttf");
        this.replacementFont = PDType0Font.load(document, fontFile);
    }

    @Override
    protected PageDrawer createPageDrawer(PageDrawerParameters parameters) throws IOException {
        return new CustomPageDrawer(parameters, replacementFont);
    }

    private static class CustomPageDrawer extends PageDrawer {
        private final PDType0Font fontDeRezerva;

        CustomPageDrawer(PageDrawerParameters parameters, PDType0Font fontDeRezerva) throws IOException {
            super(parameters);
            this.fontDeRezerva = fontDeRezerva;
        }

        @Override
        protected void showText(byte[] string) throws IOException {
            // Interceptăm fontul curent din starea grafică
            PDFont currentFont = getGraphicsState().getTextState().getFont();
            
            if (currentFont.getName().contains("Helvetica")) {
                // Forțăm temporar motorul să folosească Arial pentru acest fragment de text
                getGraphicsState().getTextState().setFont(fontDeRezerva);
                super.showText(string);
                // Revenim la fontul original pentru a nu corupe restul stării
                getGraphicsState().getTextState().setFont(currentFont);
            } else {
                super.showText(string);
            }
        }
    }
}
