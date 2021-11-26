package com.sergeykuzmin.test;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;

public class XmlService {

    private  final String xslFile= "D:/test_mm/src/main/resources/scheme.xsl";
    private  final String oneXml = "1.xml";
    private  final String secondXml = "2.xml";

    private String db_url;

    private String db_username;

    private String db_password;

    private Integer N;

    private Connection connection;


    XmlService() {

    }

    void setConnection() {

        try {
            connection = DriverManager.getConnection(db_url, db_username, db_password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void createTable() throws SQLException {
        try(Statement statement = connection.createStatement()) {
            DatabaseMetaData databaseMetadata = connection.getMetaData();
            ResultSet resultSet = databaseMetadata.getTables(null, null, "test", null);
            if (resultSet.next()) {
                statement.executeUpdate("DELETE FROM TEST");
            } else{
                statement.executeUpdate("CREATE TABLE TEST (field INTEGER)");
            }
        }
    }


    void insertToDb() throws SQLException {
        String sql = "INSERT INTO TEST (field) values(?)";
        try(PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 1; i <= N; i++) {
                ps.setInt(1, i);
                ps.addBatch();
            }
            ps.executeBatch();
            System.out.println("Данные записаны в базу данных");
        }
    }


    String createXmlOne() throws ParserConfigurationException, TransformerException, IOException, SQLException {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db  = dbf.newDocumentBuilder();
        Document doc = db.newDocument();

        Element root = doc.createElement("entries");

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT field FROM TEST");
        while (resultSet.next()) {
            Element entry = doc.createElement("entry");
            Element field = doc.createElement("field");
            field.setTextContent(resultSet.getString(1));
            entry.appendChild(field);
            root.appendChild(entry);
        }

        resultSet.close();
        statement.close();

        doc.appendChild(root);
        String res = documentToString(doc);
        writeFile(res);
        return res;
    }


    private String documentToString(Node root) throws TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StringWriter writer = new StringWriter();

        transformer.transform(new DOMSource(root), new StreamResult(writer));
        return writer.getBuffer().toString();
    }

    private  void writeFile(String str) throws IOException {
        Path path = Paths.get(oneXml);
        try(BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(str);
        }
        System.out.println("Создан 1.xml");
    }

    void createXmlTwo(){

        TransformerFactory factory = TransformerFactory.newInstance();

        try {

            Transformer tf = factory.newTransformer(new StreamSource(xslFile));

            tf.transform(new StreamSource(oneXml), new StreamResult(
                    new FileOutputStream(secondXml)));

            System.out.println ("Преобразование 1.xml  к 2.xml произведенно успешно");
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
            System.out.println ("Не удалось получить экземпляр объекта преобразования");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println ("Исходный файл не найден");
        } catch (TransformerException e) {
            e.printStackTrace();
            System.out.println ("Не удалось преобразовать в целевой файл");
        }


    }


    long parseXml(Path path) throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db  = dbf.newDocumentBuilder();
        Document doc = db.parse(path.toFile());

        NodeList list = doc.getElementsByTagName("entry");
        long sum = 0;

        for(int i = 0; i < list.getLength(); i++) {
            String fieldValue = list.item(i).getAttributes().getNamedItem("field").getNodeValue();
            sum += Integer.parseInt(fieldValue);
        }
        return sum;
    }


    public Integer getN() {
        return N;
    }

    void setN(Integer n) {
        this.N = n;
    }

    public String getDb_url() {
        return db_url;
    }

    public void setDb_url(String db_url) {
        this.db_url = db_url;
    }

    public String getDb_username() {
        return db_username;
    }

    public void setDb_username(String db_username) {
        this.db_username = db_username;
    }

    public String getDb_password() {
        return db_password;
    }

    public void setDb_password(String db_password) {
        this.db_password = db_password;
    }

    Connection getConnection() {
        return connection;
    }

    public  String getXslFile() {
        return xslFile;
    }

    public  String getOneXml() {
        return oneXml;
    }

    public  String getSecondXml() {
        return secondXml;
    }
}
