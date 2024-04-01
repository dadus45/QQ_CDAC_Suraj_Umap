package com.app;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class PdfTranslator {

    public static void main(String[] args) throws IOException {
        String pdfFilePath = "https://drive.google.com/file/d/1HtPXlM1vLD1HeRfQSW1THPodouFiBj39/view?u"; // Update with the path to your PDF file
        System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", pdfFilePath);
        List<Integer> rowNumbers = List.of(4, 7, 8);

        String marathiText = extractTextFromRows(pdfFilePath, rowNumbers);
        String translatedText = translateText(marathiText, "mr", "en");

        saveToCSV(marathiText, translatedText);
        System.out.println("Translation saved to translated_text.csv");
    }

    private static String extractTextFromRows(String pdfFilePath, List<Integer> rowNumbers) throws IOException {
        StringBuilder text = new StringBuilder();
        try (PDDocument document = PDDocument.load(new File(pdfFilePath))) {
            for (int rowNumber : rowNumbers) {
                PDFTextStripper stripper = new PDFTextStripper();
                stripper.setStartPage(rowNumber);
                stripper.setEndPage(rowNumber);
                text.append(stripper.getText(document)).append("\n");
            }
        }
        return text.toString().trim();
    }

    private static String translateText(String text, String sourceLang, String targetLang) {
        Translate translate = TranslateOptions.getDefaultInstance().getService();
        Translation translation = translate.translate(text, Translate.TranslateOption.sourceLanguage(sourceLang),
                Translate.TranslateOption.targetLanguage(targetLang));
        return translation.getTranslatedText();
    }

    private static void saveToCSV(String marathiText, String translatedText) throws IOException {
        List<String> marathiLines = List.of(marathiText.split("\n"));
        List<String> englishLines = List.of(translatedText.split("\n"));

        List<List<String>> rows = new ArrayList<>();
        for (int i = 0; i < marathiLines.size(); i++) {
            List<String> row = new ArrayList<>();
            row.add(marathiLines.get(i));
            row.add(englishLines.get(i));
            rows.add(row);
        }

        try (java.io.PrintWriter writer = new java.io.PrintWriter(new File("translated_text.csv"))) {
            writer.write("Marathi,English\n");
            for (List<String> row : rows) {
                writer.write(String.join(",", row) + "\n");
            }
        }
    }
}
