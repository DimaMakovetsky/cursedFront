package com.example.tets2;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.*;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PDFFiller {
    private static Logger logger=Logger.getLogger(Tets2Application.class.getName());

    public static String printDocument(Map<String, String> toPrint, int count, int type) {
        logger.logp(Level.INFO, "PDFFiller", "printDocument", "Начало создания файла");
        String sourceFilePath="";
        switch (type) {//выбор шаблона на основе данных пользователя
            case 1:
                sourceFilePath ="src/main/resources/files/doverennostCorrected.pdf";
                break;
            case 2:
                sourceFilePath ="src/main/resources/files/dosilFizCorrected.pdf";
                break;
            case 3:
                sourceFilePath ="src/main/resources/files/dosilYurCorrected.pdf";
                break;
            default:
                break;
        }
        try {
            logger.logp(Level.INFO, "PDFFiller", "printDocument", "Загрузка шаблона");
            PDDocument doc = PDDocument.load(new File(sourceFilePath));//считывание файла шаблона
            logger.logp(Level.INFO, "PDFFiller", "printDocument", "Считывание формы");

            PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();//считывание формы
            PDResources dr = acroForm.getDefaultResources();

            PDFont font = PDType0Font.load(doc, new FileInputStream("c:/windows/fonts/arial.ttf"), false);
            //загрузка шрифта
            COSName fontName = dr.add(font);
            Iterator<PDField> it = acroForm.getFieldIterator();
            while (it.hasNext())//настройка полей новым шрифтом
            {
                PDField field = it.next();
                if (field instanceof PDTextField)
                {
                    PDTextField textField = (PDTextField) field;
                    String da = textField.getDefaultAppearance();
                    Pattern pattern = Pattern.compile("/(\\w+)\\s.*");
                    Matcher matcher = pattern.matcher(da);
                    if (!matcher.find()||matcher.groupCount()<2) {
                    }
                    String oldFontName = matcher.group(1);
                    da = da.replaceFirst(oldFontName, fontName.getName());
                    textField.setDefaultAppearance(da);
                }
            }
            logger.logp(Level.INFO, "PDFFiller", "printDocument", "Заполнение полей");
            List fields = acroForm.getFields();//получение полей
            for (Object o : fields) {
                PDTextField field = (PDTextField) o;
                String value = toPrint.get(field.getPartialName());//получение данных из словаря
                if (value != null) {
                    if (field.getPartialName().equals("name")||field.getPartialName().equals("address")||field.getPartialName().equals("oldAddress")||field.getPartialName().equals("newAddress"))
                    {
                        field.setMultiline(true);
                    }
                    field.setValue(value);//установка значения поля
                }
            }
            acroForm.flatten();//закрытие редактирования
            logger.logp(Level.INFO, "PDFFiller", "printDocument", "Создание файла");
            String fileName=String.format("%d_%d.pdf",System.currentTimeMillis(),count);//создание названия файла
            doc.save(String.format("src/main/resources/resultFiles/%s",fileName));//создание файла
            logger.logp(Level.INFO, "PDFFiller", "printDocument", "Закрытие файла");

            doc.close();//закрытие файла
            return fileName;
        }
        catch (IOException ex) {
            logger.logp(Level.INFO, "PDFFiller", "printDocument", "Ошибка при создании файла");
            ErrorLog log=new ErrorLog(0,500,ex.getMessage());
            SQLConnector.AddError(log);
            SQLConnector.errorCount++;
        }
        return null;
    }
}

