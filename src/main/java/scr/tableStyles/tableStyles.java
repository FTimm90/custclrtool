package scr.tableStyles;

import java.io.InputStream;
import java.util.HashMap;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import scr.presentation.presentation;
import scr.settingsField.XmlValue;
import scr.settingsField.settingsField;

public class tableStyles {
    
    Document XMLtemplate;
    private String tableName;
    public static String[] elementsArray = new String[XmlValue.tableElements.length];
    public HashMap<String, settingsField> settingsFields = new HashMap<>();
    public JPanel settingsElements;

    public tableStyles(String name) {

        this.tableName = name;
        InputStream inputStream = tableStyles.class.getResourceAsStream("/TableStyleTemplate.xml");

        if (inputStream == null) {
            System.err.println("Resource not found!");
        } else {
            // Use the InputStream to parse the XML
            // ... (your XML parsing code using inputStream) ...
            try {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                XMLtemplate = dBuilder.parse(inputStream);
                XMLtemplate.getDocumentElement().normalize();
                // System.out.println("Root element: " + XMLtemplate.getDocumentElement().getNodeName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getTableName() {
        return this.tableName;
    }

    public JPanel createSettingsFields() {
        settingsElements = new JPanel();
        settingsElements.setBounds(30, 120, 230, 750);
        int counter = 0;
        for (XmlValue element : XmlValue.tableElements) {
            elementsArray[counter] = element.toString();
            settingsField settingsfield = new settingsField(30, 90);
            settingsfield.showSettingsField(false);
            settingsFields.put(element.toString(), settingsfield);
            settingsElements.add(settingsfield.widget);
            counter++;
        }
        return settingsElements;
    }

    /**
     * Writes the finished table object into the XML template DOM
     */
    public void writeTableStyles(tableStyles tableObject) {
        // TODO WORK IN PROGRESS
        
        // Iterate over the 7 elements a table style consists of
        for (XmlValue element : XmlValue.tableElements) {
            // Pick the correct node from the XML template e.g.: a:wholeTbl
            Node currentElementNode = presentation.findNode(XMLtemplate.getDocumentElement(),
                    "a:" + element.getXmlValue());
            // Pick the current settingsfield from the settingsfields hashmap
            settingsField currentFields = tableObject.settingsFields.get(element.toString());
            HashMap<String, HashMap<String, JComboBox<XmlValue>>> allElements = currentFields.getCollectedValues();
            // Iterate over the individual settings for the elements (sides etc.)
            for (String part : allElements.keySet()) {
                HashMap<String, JComboBox<XmlValue>> currentElement = allElements.get(part);
                // Find the corresponding node within the template
                Node ElementNode = presentation.findNode(currentElementNode, part);
                
                switch (part) {
                    case "top" -> {
                        System.out.println("found top");
                    }
                    case "bottom" -> {
                        System.out.println("found bottom");
                    }
                    case "left" -> {
                        System.out.println("found left");
                    }
                    case "right" -> {
                        System.out.println("found right");
                    }
                    case "insideV" -> {
                        System.out.println("found insideV");
                    }
                    case "insideH" -> {
                        System.out.println("found insideH");
                    }
                    case "fill" -> {
                        System.out.println("found fill");
                    }
                    case "fontref" -> {
                        System.out.println("found fontref");
                    }
                }
            }
        }
    }
}
