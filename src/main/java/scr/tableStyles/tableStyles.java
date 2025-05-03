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
    private final String TABLENAME;
    public static String[] elementsArray = new String[XmlValue.TABLEELEMENTS.length];
    public HashMap<String, settingsField> settingsFields = new HashMap<>();

    final private static String NAMESPACE = "http://schemas.openxmlformats.org/drawingml/2006/main";

    public settingsField getSettingsField(String name) {
        return settingsFields.get(name);
    }

    public JPanel settingsElements;

    public tableStyles(String name) {

        this.TABLENAME = name;
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

    public String getTABLENAME() {
        return this.TABLENAME;
    }

    public JPanel createSettingsFields() {

        settingsElements = new JPanel();
        settingsElements.setBounds(30, 120, 230, 750);
        int counter = 0;
        for (XmlValue element : XmlValue.TABLEELEMENTS) {

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
        for (XmlValue element : XmlValue.TABLEELEMENTS) {
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

                    // Handle case if the color is set to "No Color".
                    if (boxElement.getAttributeValue().equals("none") && boxElement.getTagName().equals("a:schemeClr")) {
                        templateNode.removeAttribute(attributeName);
                    } else {
                        templateNode.setAttribute(attributeName, attributeValue);
                    }
                }
            }
        }
        return templateRoot;
    }

    public static boolean hasExistingStyles(Document tableStylesFile) {

        String localName = "tblStyle";
        NodeList tableStyleNodes = tableStylesFile.getElementsByTagNameNS(NAMESPACE, localName);

        return tableStyleNodes.getLength() > 0;
    }

    public static void extractExistingTableStyles(Document tableStylesFile) {

        String localName = "tblStyle";
        NodeList tableStyleNodes = tableStylesFile.getElementsByTagNameNS(NAMESPACE, localName);

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
     * @param tableStyleNode <a:tblStyle> node from the tableStyles.xml of the loaded PowerPoint file.
     * @param currentTable table style object from the GUI.
     */
    private static void fillTableObject(Node tableStyleNode, tableStyles currentTable) {

        for (XmlValue tableElement : XmlValue.TABLEELEMENTS) {
            Node elementNode = presentation.findNode(tableStyleNode, tableElement.getTagName());
            settingsField elementField = currentTable.getSettingsField(tableElement.toString());
            HashMap<String, HashMap<String, JComboBox<XmlValue>>> parts = elementField.getAllFields();

            for (String part : parts.keySet()) {
                // Part = left, right, top, bottom, fill, insideV, insideH
                Node partNode = presentation.findNode(elementNode, "a:" + part);
                HashMap<String, JComboBox<XmlValue>> element = parts.get(part);

                for (String boxName : element.keySet()) {
                    // Box names: cell color, line style, line width, line color, text style, text color, font
                    JComboBox<XmlValue> comboBox = element.get(boxName);
                    XmlValue comboBoxSelection = (XmlValue) comboBox.getSelectedItem();
                    Node valueNode = findNode(elementNode, partNode, comboBoxSelection.getTagName());

                    XmlValue attributeAsXmlValue;
                    if (valueNode.getNodeName().equals("a:schemeClr") && !valueNode.hasAttributes()) {
                        attributeAsXmlValue = XmlValue.findValue("No Color");
                    } else {
                        Element attribute = (Element) valueNode;
                        String foundAttribute;

                        if (boxName.equals("text style")) {
                            // For "text style" we cannot use the AttributeValue,
                            // since that's always "on".
                            foundAttribute = comboBoxSelection.toString();
                        } else {
                            foundAttribute = attribute.getAttribute(comboBoxSelection.getAttributeName());
                        }
                        attributeAsXmlValue = XmlValue.findValue(foundAttribute);
                    }

                    elementField.setComboBox(part, boxName, attributeAsXmlValue);
                }
            }
        }
    }

    /**
     * Special version of the findNode method to handle some special cases
     * @param currentTablePart the node containing the special case
     * @param fontRefNode the node that should be searched
     * @param searchTag the tag that is beeing searched for
     * @return
     */
    private static Node findNode(Node currentTablePart, Node fontRefNode, String searchTag) {

        if (isSpecialCase(currentTablePart, fontRefNode, searchTag)) {
            return handleSpecialCase(currentTablePart, searchTag);
        }
        return presentation.findNode(fontRefNode, searchTag);
    }

    private static boolean isSpecialCase(Node currentTablePart, Node fontRefNode, String searchTag) {

        return currentTablePart.getNodeName().equals("a:wholeTbl")
                && fontRefNode.getNodeName().equals("a:fontRef")
                && (searchTag.equals("a:schemeClr")
                || searchTag.equals("a:fontRef")
                || searchTag.equals("a:tcTxStyle"));
    }

    private static Node handleSpecialCase(Node currentTablePart, String searchTag) {

        Node tcTxStyleNode = presentation.findNode(currentTablePart, "a:tcTxStyle");
        return switch (searchTag) {
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
        rootElement.setAttribute("styleName", tableObject.getTABLENAME());
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