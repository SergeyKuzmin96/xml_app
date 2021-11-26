package com.sergeykuzmin.test;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;

public class Application {

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        XmlService xmlService;
        long sum = 0;
        try {
            xmlService = new XmlService();
            xmlService.setDb_url("jdbc:postgresql://localhost:5432/testdb");
            xmlService.setDb_username("postgres");
            xmlService.setDb_password("1234");
            xmlService.setConnection();
            xmlService.createTable();
            xmlService.setN(1000000);


            xmlService.insertToDb();
            xmlService.createXmlOne();
            xmlService.createXmlTwo();
            Path path = Paths.get(xmlService.getSecondXml());
            sum = xmlService.parseXml(path);
            if (xmlService.getConnection() != null)
                xmlService.getConnection().close();
        } catch (IOException | SAXException | SQLException | ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        long result = (endTime - startTime) / 60000;

        System.out.println("Сумма значений = " + sum + ", Время выполнение операций " + result + " minutes");
    }
}
