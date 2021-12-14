package ru.dmv.lk.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;


@Component
@Scope("prototype") //при каждом new будет создан новый экземпляр класса
public class MyUtil {

    public  float round(float number, int scale) {
        int pow = 10;
        for (int i = 1; i < scale; i++)
            pow *= 10;
        float tmp = number * pow;
        return (float) (int) ((tmp - (int) tmp) >= 0.5f ? tmp + 1 : tmp) / pow;
    }

    //Дополнить пробелами слева до длины
    public String padLeftBySpaces(String text, int length) {
        return String.format("%" + length + "." + length + "s", text);
    }

    //Дополнить пробелами справа  до длины
    public String padRightBySpaces(String text, int length) {
        return String.format("%-" + length + "." + length + "s", text);
    }

    //Дополнить указанным символом слева до длины
    public String padLeftByChars(String text, int length, char padChar) {
        return StringUtils.leftPad(text, length, padChar);
    }

    //Дополнить указанным символом справа до длины
    public String padRightByChars(String text, int length, char padChar) {
        return StringUtils.rightPad(text, length, padChar);
    }

    public  String replaceSubstringInJsonFromVFP(String text){
       //В тексте Json, полученном из Vfp получаем кодировку некоторых символов. Раскодируем
        String[] keys = new String[]{"%CR%","%DOUBLEQUOTE%","%LF%","%TAB%","%SINGLEQUOTE%"};
        String[] values = new String[]{"; ","'",            ";",   " ",    "'"};
        return StringUtils.replaceEach( text, keys, values );
    }
    public  String getURLBase(HttpServletRequest request) throws MalformedURLException {

        URL requestURL = new URL(request.getRequestURL().toString());
        String port = requestURL.getPort() == -1 ? "" : ":" + requestURL.getPort();
        return requestURL.getProtocol() + "://" + requestURL.getHost() + port;

    }


   // загрузить файлы из списка
   public void saveUploadedFiles(List<MultipartFile> files , String uploaded_folder) throws Exception {
       Path path = Paths.get(uploaded_folder);
       if (!Files.exists(path))
           Files.createDirectories(path); //создать директорию, если ее нет
       // предполагаем, что в каталоге нет файлов
       for (MultipartFile file : files) {
           if (file.isEmpty())
               throw new Exception("Получили пустое имя файла");
           byte[]bytes = file.getBytes();

           // переименуем принимаемый файл (в любом случае)
           path = Paths.get(uploaded_folder+ File.separator+file.getOriginalFilename());
           Files.write(path, bytes);
       }

   }



    public XSSFWorkbook openXLSXFile(String fullPathFileName) throws Exception {
        InputStream in;
        XSSFWorkbook wb = null;
        //проверяем, что он вообще есть
        File file=new File(fullPathFileName);
        if (!file.exists()) throw new FileNotFoundException();
        while (wb==null){
            try {
                in = new FileInputStream(fullPathFileName);
                wb = new XSSFWorkbook(in);
            } catch (Exception e) {
                throw new Exception();
            }
        }
        return wb;
    }

    public String delMoreOneSpace(String y) {
        while (y.contains("  ")) {
            y = y.replace("  ", " ");
        }
        return y;
    }

    public void writeStringLineToFileWin1251(String fullPathFilename, String text){
        try{
            //FileWriter writer = new FileWriter(fullPathFilename, true); //Дописать в файл
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(fullPathFilename, true),"windows-1251");
            // запись всей строки
            writer.write(text+"\r\n");
            // запись по символам
            //writer.append('\n');
            writer.flush();
            writer.close();
        }
        catch(IOException ex){
            System.out.println(ex.getMessage());
        }
    }

    //Переобразовать String в utf8 без BOM
    public String convertStringToUtf8WithoutBOM(String text) {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        return new String(bytes, StandardCharsets.ISO_8859_1);
    }

    //Переобразовать String в utf8
    public String convertStringToUtf8(String text) {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    //Переобразовать String в 1251
    public String convertStringToAscii(String text) {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        return new String(bytes, StandardCharsets.US_ASCII);
    }


    // Если вам требуется сгенерировать строку необходимой длины со значением шестнадцатеричной системы, вы можете воспользоваться следующим кодом:
    public String generateRandomHexString(int length){
        Random r = new Random();
        StringBuilder sb = new StringBuilder();
        while(sb.length() < length){
            sb.append(Integer.toHexString(r.nextInt()));
        }
        return sb.toString().substring(0, length);
    }

    public void downloadFile(String fullPathFileName, HttpServletResponse response) throws Exception {
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;

        try {
            File file = new File(fullPathFileName);
            response.setContentType(mediaType.getType()); // Content-Type
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + file.getName()); // Content-Disposition
            response.setContentLength((int) file.length()); // Content-Length

            BufferedInputStream inStream = new BufferedInputStream(new FileInputStream(file));
            BufferedOutputStream outStream = new BufferedOutputStream(response.getOutputStream());

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }
            outStream.flush();
            inStream.close();
            outStream.close();
        }catch (Exception e) {throw new Exception("Ошибка при выгрузке файла. "+e.getMessage());}

    }

    public int toCents(double charge) {
        return 0;
    }
}

