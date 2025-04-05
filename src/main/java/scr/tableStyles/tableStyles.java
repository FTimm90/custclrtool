package scr.tableStyles;

import java.io.InputStream;
import java.util.HashMap;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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

            if (element.getAttributeValue().equals("band1H") ||
                element.getAttributeValue().equals("firstRow") ||
                element.getAttributeValue().equals("lastRow")) {
                // System.out.println(element.toString() + " : NO Inside Horizontal");
                insideH = false;
            }
            if (element.getAttributeValue().equals("band1V") ||
                element.getAttributeValue().equals("firstCol") ||
                element.getAttributeValue().equals("lastCol")) {
                // System.out.println(element.toString() + " : NO Inside Vertical");
                insideV = false;
            }
            
            elementsArray[counter] = element.toString();
            settingsField settingsfield = new settingsField(30, 90, insideH, insideV);

            if (element.getAttributeValue().equals("wholeTbl")) {
                settingsfield.widget.add(settingsfield.FontWidget());
            }

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
    public static Node fillTableTemplate(tableStyles tableObject, String themeID) {
        Node templateRoot = writeTableNameID(tableObject, themeID);
        // printXml(templateRoot, "   ");
        for (XmlValue element : XmlValue.tableElements) {
            Node currentElementNode = presentation.findNode(templateRoot,
                    "a:" + element.getAttributeValue());
            // System.out.println("-------------- " + element + " --------------");
            settingsField currentFields = tableObject.settingsFields.get(element.toString());
            HashMap<String, HashMap<String, JComboBox<XmlValue>>> allElements = currentFields.getCollectedValues();

            for (String part : allElements.keySet()) {
                // System.out.println("+++++++++ " + part + " +++++++++");
                HashMap<String, JComboBox<XmlValue>> currentElement = allElements.get(part);
                // Find the corresponding node within the template (e.g. a:top)
                Node ElementNode = presentation.findNode(currentElementNode, "a:" + part);

                for (JComboBox<XmlValue> currentBox : currentElement.values()) {
                    // Get the content of the box and split it into its elements.
                    XmlValue boxElement = (XmlValue) currentBox.getSelectedItem();
                    String attributeValue = boxElement.getAttributeValue();
                    String attributeName = boxElement.getAttributeName();
                    String tagName = boxElement.getTagName();

                    Element templateNode;
                    if (currentElementNode.getNodeName().equals("a:wholeTbl")) {
                        templateNode = (Element) currentElementNode;
                    } else {
                        templateNode = (Element) presentation.findNode(ElementNode, tagName);
                    }
                    templateNode.setAttribute(attributeName, attributeValue);
                }
            }
        }
        return templateRoot;
    }

    private static Node writeTableNameID(tableStyles tableObject, String themeID) {
        Node templateRoot = XMLtemplate.getDocumentElement();
        Element rootElement = (Element) templateRoot;
        rootElement.setAttribute("styleId", themeID);
        rootElement.setAttribute("styleName", tableObject.getTableName());
        return templateRoot;
    }

    public static void printXml(Node node, String indent) {
        if (node == null) return;

        // Print the current node with indentation
        System.out.println(indent + "<" + node.getNodeName());

        // Print attributes if any
        NamedNodeMap attributes = node.getAttributes();
        if (attributes != null) {
            for (int i = 0; i < attributes.getLength(); i++) {
                Node attribute = attributes.item(i);
                System.out.println(indent + "  " + attribute.getNodeName() + "=\"" + attribute.getNodeValue() + "\"");
            }
        }

        // Add closing bracket for empty elements
        if (node.getChildNodes().getLength() == 0) {
            System.out.println(indent + "/>");
        } else {
            System.out.println(indent + ">");
        }

        // Recursively print child nodes with increased indentation
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                printXml(childNode, indent + "  ");
            } else if (childNode.getNodeType() == Node.TEXT_NODE && childNode.getNodeValue().trim().length() > 0) {
                System.out.println(indent + "  " + childNode.getNodeValue().trim());
            }
        }

        // Print closing tag for non-empty elements
        if (node.getChildNodes().getLength() > 0) {
            System.out.println(indent + "</" + node.getNodeName() + ">");
        }
    }
}
