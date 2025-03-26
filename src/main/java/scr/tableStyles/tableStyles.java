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

    public void writeTableStyles(HashMap<String, settingsField> settingsFields) {
        // TODO WORK IN PROGRESS
        for (XmlValue element : XmlValue.tableElements) {
            Node currentElementNode = presentation.findNode(XMLtemplate.getDocumentElement(),
                    "a:" + element.getXmlValue());

            // Pick the correct part from the hashmap
            settingsField currentFields = settingsFields.get(element.toString());
            HashMap<String, JComboBox<XmlValue>> allValues = currentFields.getCollectedValues();

            for (String name : settingsFields.keySet()) {
                if (name.contains("inner vertical")) {
                    
                } else if (name.contains("inner horizontal")) {
                    
                } else if (name.contains("right")) {
                    
                } else if (name.contains("left")) {
                    
                } else if (name.contains("top")) {
                    
                } else if (name.contains("bottom")) {
                    
                } else if (name.contains("font")) {
                    
                } else if (name.contains("solid")) {
                    
                }
            }

            // System.out.println("current element: " + element);
            // System.out.println("found " + currentElementNode.getNodeName());
        }
    }
}
