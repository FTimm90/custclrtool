package scr.tableStyles;

import java.io.InputStream;
import java.util.HashMap;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import scr.presentation.presentation;
import scr.settingsField.XmlValue;
import scr.settingsField.settingsField;

public class tableStyles {
    
    private static Document XMLtemplate;
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

            // Setting condition to skip specific parts of the UI if not needed
            boolean insideH = true;
            boolean insideV = true;
            if (element.getXmlValue().equals("band1H") ||
                element.getXmlValue().equals("firstRow") ||
                element.getXmlValue().equals("lastRow")) {
                // System.out.println(element.toString() + " : NO Inside Horizontal");
                insideH = false;
            }
            
            if (element.getXmlValue().equals("band1V") ||
                element.getXmlValue().equals("firstCol") ||
                element.getXmlValue().equals("lastCol")) {
                // System.out.println(element.toString() + " : NO Inside Vertical");
                insideV = false;
            }
            
            elementsArray[counter] = element.toString();
            settingsField settingsfield = new settingsField(30, 90, insideH, insideV);
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
    public static void writeTableStyles(tableStyles tableObject, String themeID) {
        // TODO WORK IN PROGRESS
        
        // First: write the style ID and the style Name into the template
        Node templateRoot = writeTableNameID(tableObject, themeID);
        
        // Iterate over the 7 elements a table style consists of
        for (XmlValue element : XmlValue.tableElements) {
            
            // Pick the correct node from the XML template e.g.: a:wholeTbl
            Node currentElementNode = presentation.findNode(templateRoot,
                    "a:" + element.getXmlValue());
            System.out.println("-------------- " + element + " --------------");
            
            // Pick the current settingsfield from the settingsfields hashmap
            settingsField currentFields = tableObject.settingsFields.get(element.toString());
            HashMap<String, HashMap<String, JComboBox<XmlValue>>> allElements = currentFields.getCollectedValues();
            
            // Iterate over the individual settings for the elements (sides etc.)
            for (String part : allElements.keySet()) {
                HashMap<String, JComboBox<XmlValue>> currentElement = allElements.get(part);
                
                // Find the corresponding node within the template
                Node ElementNode = presentation.findNode(currentElementNode, "a:" + part);

                if (ElementNode != null) {
                    System.out.println("Found " + ElementNode.getNodeName() + " in the template.");
                } else {
                    System.out.println("Could not find " + "a:" + part + " in the template.");
                }
                
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

    private static Node writeTableNameID(tableStyles tableObject, String themeID) {
        Node templateRoot = XMLtemplate.getDocumentElement();
        Element rootElement = (Element) templateRoot;
        rootElement.setAttribute("styleId", themeID);
        rootElement.setAttribute("styleName", tableObject.getTableName());
        return templateRoot;
    }
}
