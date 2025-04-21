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

import scr.custclr.CustClrTool;
import scr.presentation.presentation;
import scr.settingsField.XmlValue;
import scr.settingsField.settingsField;

public class tableStyles {
    
    private static Document XMLtemplate;
    private String tableName;
    public static String[] elementsArray = new String[XmlValue.tableElements.length];
    public HashMap<String, settingsField> settingsFields = new HashMap<>();

    public settingsField getSettingsField(String name) {
        return settingsFields.get(name);
    }

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
                insideH = false;
            }
            if (element.getAttributeValue().equals("band1V") ||
                    element.getAttributeValue().equals("firstCol") ||
                    element.getAttributeValue().equals("lastCol")) {
                insideV = false;
            }

            elementsArray[counter] = element.toString();
            settingsField settingsfield = new settingsField(30, 90, insideH, insideV);
            settingsfield.showSettingsField(false);

            if (element.getAttributeValue().equals("wholeTbl")) {
                settingsfield.widget.add(settingsfield.FontWidget());
                // Generally we don't want the settingsfields to be visible,
                // only wholeTbl should be visible on creation so that 
                // the panel is not empty.
                settingsfield.showSettingsField(true);

            }

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
        for (XmlValue element : XmlValue.tableElements) {
            Node templateElementNode = presentation.findNode(templateRoot, "a:" + element.getAttributeValue());
            settingsField currentFields = tableObject.settingsFields.get(element.toString());
            HashMap<String, HashMap<String, JComboBox<XmlValue>>> allElements = currentFields.getCollectedValues();

            for (String part : allElements.keySet()) {
                HashMap<String, JComboBox<XmlValue>> currentElement = allElements.get(part);
                Node elementNode = presentation.findNode(templateElementNode, "a:" + part);

                for (JComboBox<XmlValue> currentBox : currentElement.values()) {
                    XmlValue boxElement = (XmlValue) currentBox.getSelectedItem();
                    String attributeValue = boxElement.getAttributeValue();
                    String attributeName = boxElement.getAttributeName();
                    String tagName = boxElement.getTagName();

                    Node foundNode = findNode(templateElementNode, elementNode, tagName);

                    Element templateNode = (Element) foundNode;
                    templateNode.setAttribute(attributeName, attributeValue);
                }
            }
        }
        return templateRoot;
    }

    public static void extractExistingTableStyles(Document tableStylesFile) {
        
        String namespaceURI = "http://schemas.openxmlformats.org/drawingml/2006/main";
        String localName = "tblStyle";

        NodeList tableStyleNodes = tableStylesFile.getElementsByTagNameNS(namespaceURI, localName);

        for (int i = 0; i < tableStyleNodes.getLength(); i++) {
            // Iterate over all existing tableStyles
            Node tableStyleNode = tableStyleNodes.item(i);
            Element nodeElement = (Element) tableStyleNode;
            String styleName = nodeElement.getAttribute("styleName");
            CustClrTool.mainGUI.eventCreateNewTable(styleName);
            tableStyles currentTable = CustClrTool.mainGUI.getTableObject(styleName);
            if (currentTable == null) {
                System.err.println("Table object not found.");
            } else {
                fillTableObject(tableStyleNode, currentTable);
            }
        }
    }

    /**
     * Change the components of the table styles GUI elements to match the input table Style.
     * @param tableStyleNode
     * @param currentTable
     */
    private static void fillTableObject(Node tableStyleNode, tableStyles currentTable) {
        // TODO -> This works so far. We just need to find the right moment for this to take place AND the correct settingsfields to show.
        // TODO ALSO Check if fonts shenanigans work!
        for (XmlValue tableElement : XmlValue.tableElements) {
            // Iterate over all table elements (whole table, ...)
            // Get the Element from the tableStyleNode(loaded table style) and the currentTable
            Node elementNode = presentation.findNode(tableStyleNode, "a:" + tableElement.getAttributeValue());
            if (elementNode == null) {
                continue;
            }
            settingsField elementField = currentTable.getSettingsField(tableElement.toString());
            // Get the parts of the settingsfield (comboboxes) & iterate over them
            HashMap<String, HashMap<String, JComboBox<XmlValue>>> parts = elementField.getAllFields();
            for (String part : parts.keySet()) {
                // Part = left, right, top ...
                Node partNode = presentation.findNode(elementNode, "a:" + part);
                if (partNode == null) {
                    continue;
                }
                HashMap<String, JComboBox<XmlValue>> element = parts.get(part);
                for (String boxName : element.keySet()) {
                    // Iterating over the individual Comboboxes in the Hashmap
                    JComboBox<XmlValue> comboBox = element.get(boxName);
                    XmlValue comboBoxSelection = (XmlValue) comboBox.getSelectedItem();
                    Node valueNode = presentation.findNode(partNode, comboBoxSelection.getTagName());
                    if (valueNode == null) {
                        continue;
                    }
                    Element attribute = (Element) valueNode;
                    String foundAttribute = attribute.getAttribute(comboBoxSelection.getAttributeName());
                    XmlValue attributeAsXmlValue = XmlValue.findValue(foundAttribute);
                    comboBox.setSelectedItem(attributeAsXmlValue);
                } 
            }
        }
    }

    private static Node findNode(Node templateElementNode, Node elementNode, String tagName) {
        
        if (isSpecialCase(templateElementNode, elementNode, tagName)) {
            return handleSpecialCase(templateElementNode, tagName);
        }
        return presentation.findNode(elementNode, tagName);
    }

    private static boolean isSpecialCase(Node templateElementNode, Node elementNode, String tagName) {
        
        return templateElementNode.getNodeName().equals("a:wholeTbl")
                && elementNode.getNodeName().equals("a:fontRef")
                && (tagName.equals("a:schemeClr")
                || tagName.equals("a:fontRef")
                || tagName.equals("a:tcTxStyle"));
    }

    private static Node handleSpecialCase(Node templateElementNode, String tagName) {
        
        Node tcTxStyleNode = presentation.findNode(templateElementNode, "a:tcTxStyle");
        return switch (tagName) {
            case "a:schemeClr" -> presentation.findNode(tcTxStyleNode, "a:schemeClr");
            case "a:fontRef" -> presentation.findNode(tcTxStyleNode, "a:fontRef");
            case "a:tcTxStyle" -> tcTxStyleNode;
            default -> null;
        };
    }

    private static Node writeTableNameID(tableStyles tableObject, String themeID) {
        
        Node templateRoot = XMLtemplate.getDocumentElement();
        Element rootElement = (Element) templateRoot;
        rootElement.setAttribute("styleId", themeID);
        rootElement.setAttribute("styleName", tableObject.getTableName());
        return templateRoot;
    }

    // TODO For debugging
    public static void printXml(Node node, String indent) {
        
        if (node == null) return;

        // Print the current node with indentation
        System.out.println(indent + "<" + node.getNodeName());

        // Print attributes if any
        printAttributes(node, indent);

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

    private static void printAttributes(Node node, String indent) {
        
        NamedNodeMap attributes = node.getAttributes();
        if (attributes != null) {
            for (int i = 0; i < attributes.getLength(); i++) {
                Node attribute = attributes.item(i);
                System.out.println(indent + "  " + attribute.getNodeName() + "=\"" + attribute.getNodeValue() + "\"");
            }
        }
    }
}
