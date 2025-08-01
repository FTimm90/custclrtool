package scr.tableStyles;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

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

    private Document XMLtemplate;
    private final String TABLENAME;
    public static String[] elementsArray = new String[XmlValue.TABLEELEMENTS.length];
    public HashMap<String, settingsField> settingsFields = new HashMap<>();

    public final static String NAMESPACE = "http://schemas.openxmlformats.org/drawingml/2006/main";

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

            settingsfield.widget.add(settingsfield.FontWidget());

            settingsFields.put(element.toString(), settingsfield);
            settingsElements.add(settingsfield.widget);

            counter++;
        }
        return settingsElements;
    }

    /**
     * Writes the finished table object into the XML template DOM
     */
    public static Node fillTableTemplate(tableStyles tableObject) {

        Node templateRoot = writeTableNameID(tableObject);
        for (XmlValue element : XmlValue.TABLEELEMENTS) {
            Node templateElementNode = presentation.findNode(templateRoot, "a:" + element.getAttributeValue());

            settingsField currentFields = tableObject.settingsFields.get(element.toString());
            HashMap<String, HashMap<String, JComboBox<XmlValue>>> allElements = currentFields.getCollectedValues();

            for (String part : allElements.keySet()) {
                HashMap<String, JComboBox<XmlValue>> currentElement = allElements.get(part);
                assert templateElementNode != null;
                Node elementNode = presentation.findNode(templateElementNode, "a:" + part);

                for (JComboBox<XmlValue> currentBox : currentElement.values()) {
                    XmlValue boxElement = (XmlValue) currentBox.getSelectedItem();

                    assert boxElement != null;
                    String attributeValue = boxElement.getAttributeValue();
                    String attributeName = boxElement.getAttributeName();
                    String tagName = boxElement.getTagName();

                    Node foundNode;
                    if (tagName.equals("a:schemeClr")
                            && templateElementNode.getNodeName().equals("a:wholeTbl")
                            && Objects.requireNonNull(elementNode).getNodeName().equals("a:fill")) {
                        foundNode = presentation.findNode(elementNode, tagName);
                    } else {
                        foundNode = findNode(templateElementNode, elementNode, tagName);
                    }

                    Element templateNode = (Element) foundNode;
                    assert templateNode != null;

                    // Handle case if the color is set to "No Color".
                    if (boxElement.getAttributeValue().equals("none") && boxElement.getTagName().equals("a:schemeClr")) {
                        // Remove all the color fill nodes
                        Node solidFill = templateNode.getParentNode();
                        Node fill = solidFill.getParentNode();
                        presentation.removeAllChildNodes(fill);
                        // Append noFill node
                        Document tableDoc = tableObject.XMLtemplate;
                        Element emptyColor = tableDoc.createElementNS(NAMESPACE, "a:noFill");
                        fill.appendChild(emptyColor);
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
        try {
            for (XmlValue tableElement : XmlValue.TABLEELEMENTS) {
                Node elementNode = presentation.findNode(tableStyleNode, tableElement.getTagName());
                settingsField elementField = currentTable.getSettingsField(tableElement.toString());
                HashMap<String, HashMap<String, JComboBox<XmlValue>>> parts = elementField.getAllFields();

                for (String part : parts.keySet()) {
                    // Part: left, right, top, bottom, fill, insideV, insideH
                    assert elementNode != null;
                    Node partNode = presentation.findNode(elementNode, "a:" + part);
                    HashMap<String, JComboBox<XmlValue>> element = parts.get(part);

                    for (String boxName : element.keySet()) {
                        // Box names: cell color, line style, line width, line color, text style, text color, font
                        JComboBox<XmlValue> comboBox = element.get(boxName);
                        XmlValue comboBoxSelection = (XmlValue) comboBox.getSelectedItem();
                        assert comboBoxSelection != null;
                        Node valueNode = findNode(elementNode, partNode, comboBoxSelection.getTagName());
                        XmlValue attributeAsXmlValue;

                        if (comboBoxSelection.getTagName().equals("a:schemeClr") && valueNode == null) {
                            // If the tag name is schemeClr, but no node could be found it means that there is no fill
                            attributeAsXmlValue = XmlValue.findValue("No Color");
                        } else {
                            Element attribute = (Element) valueNode;
                            String foundAttribute;

                            if (boxName.equals("text style")) {
                                // For "text style" we cannot use the AttributeValue,
                                // since that's always "on".
                                Node tctxstylenode = presentation.findNode(elementNode, "a:tcTxStyle");
                                Element tctxstyleElement = (Element) tctxstylenode;
                                NamedNodeMap attributes = Objects.requireNonNull(tctxstyleElement).getAttributes();
                                Node selectedAttribute = attributes.item(0);

                                if (selectedAttribute == null) {
                                    // If there is no attribute (no special text formatting) just go on.
                                    continue;
                                }

                                foundAttribute = selectedAttribute.getNodeName();

                            } else {
                                foundAttribute = attribute.getAttribute(comboBoxSelection.getAttributeName());
                            }
                            attributeAsXmlValue = XmlValue.findValue(foundAttribute);
                        }
                        elementField.setComboBox(part, boxName, attributeAsXmlValue);
                    }
                }
            }
        } catch (NullPointerException e) {
            System.err.println("A NullPointerException occurred: " + e.getMessage());
            CustClrTool.mainGUI.updateEventLog("Error:" + currentTable.getTABLENAME());
        }
    }

    /**
     * Special version of the findNode method to find the <a:fontRef> node in the correct place.
     * @param currentTablePart the node containing the special case
     * @param fontRefNode the node that should be searched in
     * @param searchTag the tag that is being searched for
     */
    private static Node findNode(Node currentTablePart, Node fontRefNode, String searchTag) {

        if (isFontNode(fontRefNode, searchTag)) {
            return handleTextStyleCase(currentTablePart, searchTag);
        }
        return presentation.findNode(fontRefNode, searchTag);
    }

    private static boolean isFontNode(Node fontRefNode, String searchTag) {

        return fontRefNode.getNodeName().equals("a:fontRef")
                && (searchTag.equals("a:schemeClr")
                || searchTag.equals("a:fontRef")
                || searchTag.equals("a:tcTxStyle"));
    }

    private static Node handleTextStyleCase(Node currentTablePart, String searchTag) {

        Node tcTxStyleNode = presentation.findNode(currentTablePart, "a:tcTxStyle");
        if (tcTxStyleNode == null) {
            System.err.println("Could not find a:tcTxStyle in " + currentTablePart.getNodeName());
            return null;
        }

        return switch (searchTag) {
            case "a:schemeClr" -> presentation.findNode(tcTxStyleNode, "a:schemeClr");
            case "a:fontRef" -> presentation.findNode(tcTxStyleNode, "a:fontRef");
            case "a:tcTxStyle" -> tcTxStyleNode;
            default -> null;
        };
    }

    private static Node writeTableNameID(tableStyles tableObject) {

        Element templateRoot = tableObject.XMLtemplate.getDocumentElement();
        templateRoot.setAttribute("styleId", generateUniqueID());
        templateRoot.setAttribute("styleName", tableObject.getTABLENAME());
        return templateRoot;
    }

    private static String generateUniqueID() {
        UUID uuid = UUID.randomUUID();

        // Get the two 64-bit long values
        long mostSignificantBits = uuid.getMostSignificantBits();
        long leastSignificantBits = uuid.getLeastSignificantBits();

        // Extract the parts according to the UUID structure (8-4-4-4-12)
        // Most significant bits: AAAAAAAA-BBBB-CCCC
        long part1 = (mostSignificantBits >> 32) & 0xFFFFFFFFL; // First 8 hex digits
        long part2 = (mostSignificantBits >> 16) & 0xFFFFL;     // Next 4 hex digits
        long part3 = mostSignificantBits & 0xFFFFL;             // Next 4 hex digits

        // Least significant bits: DDDD-EEEEEEEEEEEE
        long part4 = (leastSignificantBits >> 48) & 0xFFFFL;    // Next 4 hex digits
        long part5 = leastSignificantBits & 0xFFFFFFFFFFFFL;  // Last 12 hex digits

        return String.format("{%08X-%04X-%04X-%04X-%012X}",
                part1, part2, part3, part4, part5);
    }
}