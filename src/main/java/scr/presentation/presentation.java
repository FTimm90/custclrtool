package scr.presentation;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import scr.custclr.CustClrTool;
import scr.gui.mainWindow;
import scr.settingsField.settingsField;

public class presentation {

    private String filePath = "";
    public String getFilePath() {
        return filePath;
    }
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    private String fileName = "";
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String name) {
        this.fileName = name;
    }

    private String fileExtension = "";
    public String getFileExtension() {
        return fileExtension;
    }
    public void setFileExtension(String extension) {
        this.fileExtension = extension;
    }

    private String zipPathString;
    public String getZipPathString() {
        return zipPathString;
    }

    public void setZipPathString(String zipPath) {
        this.zipPathString = zipPath;
    }

    /**
     * Contains all themes found in the presentation file.
     */
    private List<Themedata> themeDataList = new ArrayList<>();
    public List<Themedata> getThemeDataList() {
        return themeDataList;
    }

    private String tableStylesID = "";
    public String getTableStylesID() {
        return tableStylesID;
    }
    public void setTableStylesID(String ID) {
        tableStylesID = ID;
    }

    private Document tableStylesXML;
    public Document getTableStylesXML() {
        return tableStylesXML;
    }
    public void setTableStylesXML(Document tableStyles) {
        tableStylesXML = tableStyles;
    }

    /**
     * The actual slideMaster.xml file, used to set text levels.
     */
    private Document slideMaster;
    public Document getSlideMaster() { return slideMaster; }
    public void setSlideMaster(Document newSlideMaster) {
        slideMaster = newSlideMaster;
    }

    /**
     * Needs to be set to true once the text styles are copied and reset to false after saving.
     */
    public boolean textStylesCopied = false;

    final static String CUSTCLR_NODE = "custClrLst";
    final static String NAMESPACE = "http://schemas.openxmlformats.org/drawingml/2006/main";

// The following block handles file INPUT

    /**
     * Processes the contents of the .zip version of the presentation file
     * to extract information about existing custom colors
     * @param zipFilePath   The path to the zipfile converted from the pptx.
     */
    public void extractXMLData(String zipFilePath) {

        try (ZipFile zipFile = new ZipFile(zipFilePath)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                String type = entry.isDirectory() ? "DIR" : "FILE";

                // Extract the three xml files needed for modification
                if (type.equals("FILE") && name.contains("theme")) {
                    Themedata newTheme = new Themedata();
                    extractThemeData(zipFile.getInputStream(entry), name, newTheme);
                    themeDataList.add(newTheme);

                } else if (type.equals("FILE") && name.contains("tableStyles")) {
                    setTableStylesID(extractTableStylesID(zipFile.getInputStream(entry)));
                    setTableStylesXML(buildXMLDOM(zipFile.getInputStream(entry)));
                } else if (type.equals("FILE") && name.equals("ppt/slideMasters/slideMaster1.xml")) {
                    setSlideMaster(buildXMLDOM(zipFile.getInputStream(entry)));
                }
            }
        } catch (IOException | XMLStreamException | SAXException | ParserConfigurationException ex) {
            ex.printStackTrace();
            CustClrTool.mainGUI.eventLog.setText("Failed to extract themes from file.");
        }
    }

    private static String extractTableStylesID(InputStream inputStream) throws XMLStreamException {
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
        while (reader.hasNext()) {
            int eventType = reader.next();
            if (eventType == XMLStreamReader.START_ELEMENT && reader.getLocalName().equals("tblStyleLst")) {
                return reader.getAttributeValue("", "def");
            }
        }
        return "";
    }

    private static void extractThemeData(InputStream inputStream, String themeNumber, Themedata newTheme)
            throws XMLStreamException {

        themeNumber = themeNumber.substring(themeNumber.lastIndexOf('/') + 1, themeNumber.lastIndexOf('.'));

        newTheme.themeNumber = themeNumber;

        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
        while (reader.hasNext()) {
            int eventType = reader.next();
            if (eventType == XMLStreamReader.START_ELEMENT) {
                switch (reader.getLocalName()) {
                    case "theme" -> newTheme.themeName = reader.getAttributeValue("", "name");
                    case "clrScheme" -> {
                        try {
                            gatherThemeColors(reader, newTheme.themeColors);
                        } catch (IndexOutOfBoundsException e) {
                            CustClrTool.mainGUI.eventLog.setText("Error reading theme colors from " + newTheme.themeName + ", please make sure they are defined properly.");
                        }
                    }
                    case CUSTCLR_NODE -> {
                        HashMap<String, String> findColorElements = new HashMap<>();
                        findColorElements.put("custClr", "name");
                        findColorElements.put("srgbClr", "val");
                        List<String> foundElements = gatherChildElements(reader, findColorElements);
                        newTheme.customColors.addAll(foundElements);
                    }
                    case "themeFamily" -> newTheme.themeID = reader.getAttributeValue("", "id");
                    default -> {
                    }
                }
            } else if (eventType == XMLStreamReader.END_ELEMENT && reader.getLocalName().equals("theme")) {
                break;
            }
        }
    }

    /**
     * @param reader        Opened XML file.
     * @param childKeyValue Tag : Value to be found.
     * @return A List of all the found child elements.
     */
    private static List<String> gatherChildElements(XMLStreamReader reader,
                                                    HashMap<String, String> childKeyValue) throws XMLStreamException {

        List<String> foundElements = new ArrayList<>();
        while (reader.hasNext()) {
            int eventType = reader.next();
            if (eventType == XMLStreamReader.END_ELEMENT && reader.getLocalName().equals(presentation.CUSTCLR_NODE)) {
                return foundElements;
            } else {
                for (String i : childKeyValue.keySet()) {
                    if (eventType == XMLStreamReader.START_ELEMENT && reader.getLocalName().equals(i)) {
                        String name = reader.getAttributeValue("", childKeyValue.get(i));
                        if (name == null) {
                            name = "";
                        }
                        String foundElement = String.format("%s:%s", i, name);
                        foundElements.add(foundElement);
                    }
                }
            }
        }
        return foundElements;
    }

    /**
     * @param reader        Opened XML file.
     * @param themeMap      Theme color HashMap to be filled.
     */
    private static void gatherThemeColors(XMLStreamReader reader, HashMap<String, String> themeMap) throws XMLStreamException {

        while (reader.hasNext()) {
            int eventType = reader.next();
            if (eventType == XMLStreamReader.END_ELEMENT && reader.getLocalName().equals("clrScheme")) {
                return;
            } else {
                String currentElement = reader.getLocalName();
                if (themeMap.containsKey(currentElement) && eventType == XMLStreamReader.START_ELEMENT) {
                    while (reader.hasNext()) {
                        eventType = reader.next();
                        if (eventType == XMLStreamReader.START_ELEMENT && reader.getLocalName().equals("srgbClr")) {
                            String val = reader.getAttributeValue(null, "val");
                            themeMap.put(currentElement, val);
                            break;
                        }
                        if (eventType == XMLStreamReader.START_ELEMENT && reader.getLocalName().equals("sysClr")) {
                            String val = reader.getAttributeValue(null, "lastClr");
                            themeMap.put(currentElement, val);
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * If the ID of the selected theme is equal to the ID in the tableStyles.xml
     * it is very likely, that potentially existing table styles are custom.
     */
    public static boolean validateID(String themeID, String tableStyleID) {
        return themeID.equals(tableStyleID);
    }

// The following block handles file OUTPUT

    /**
     * Creates a copy of the .zip converted PowerPoint file
     * and injects the modified .xml file
     * @param presentation      the current presentation object
     * @param destinationXML    The XML file to be replaced
     * @param xmlProcessor      The method used to modify the found XML file
     */
    public static void writeZipOutput(presentation presentation, String destinationXML, XmlProcessor xmlProcessor, boolean customColors, boolean tableStyles)
            throws FileNotFoundException, ParserConfigurationException, SAXException, TransformerException {

        final String ZIP_TMP = "_tmp.zip";

        String filePath = presentation.getFilePath();
        String fileName = presentation.getFileName();
        String zipFilePath = presentation.getZipPathString();
        String presentationExtension = "." + presentation.getFileExtension();

        File zipNew = new File(fileName + ZIP_TMP);
        ZipOutputStream zipWrite = new ZipOutputStream(new FileOutputStream(zipNew));

        try (ZipFile zipFile = new ZipFile(zipFilePath)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                String type = entry.isDirectory() ? "DIR" : "FILE";

                InputStream inputStream = zipFile.getInputStream(entry);

                if (customColors && type.equals("FILE") && name.contains(destinationXML)) {
                    xmlProcessor.process(inputStream, destinationXML, zipWrite);
                } else if (tableStyles && type.equals("FILE") && name.contains("tableStyles.xml")) {
                    writeZipEntry(CustClrTool.newpres.getTableStylesXML(), "ppt/tableStyles.xml", zipWrite);
                } else if (tableStyles && type.equals("FILE") && name.equals("ppt/slideMasters/slideMaster1.xml") && !presentation.textStylesCopied) {
                    // Only match the master/table text levels when saving the file.
                    Document adjustedTextLevels = setTextLevels(CustClrTool.newpres.getSlideMaster());
                    CustClrTool.newpres.setSlideMaster(adjustedTextLevels);
                    writeZipEntry(CustClrTool.newpres.getSlideMaster(), "ppt/slideMasters/slideMaster1.xml", zipWrite);
                    presentation.textStylesCopied = true;
                }
                else {
                    insertZipEntry(entry, zipWrite, inputStream);
                }
            }
            zipWrite.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            CustClrTool.mainGUI.eventLog.setText("Failed to create new Zipfile.");
        }
        replaceOldFile(fileName, filePath, presentationExtension);
    }

    /**
     * Helper method to write .zip entries from InputStream (old file) into
     * ZipOutPutStream (new file)
     * @param entry         Zip entry from old file
     * @param zipWrite      The zip file that is going to be written into
     * @param inputStream   The zip entry as InputStream
     */
    public static void insertZipEntry(ZipEntry entry, ZipOutputStream zipWrite, InputStream inputStream)
            throws IOException {

        zipWrite.putNextEntry(entry);
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) > 0) {
            zipWrite.write(buffer, 0, len);
        }
        zipWrite.closeEntry();
    }

    /**
     * Takes the inputStream, builds an XMLDOM from it, removes any existing customcolors and inserts the new ones.
     */
    public static void processTheme(InputStream inputStream, String themeSelection, ZipOutputStream zipWrite) throws IOException, ParserConfigurationException, SAXException, TransformerException {

        Document document = buildXMLDOM(inputStream);
        Element rootElement = document.getDocumentElement();

        removeNode(rootElement, CUSTCLR_NODE);

        writeThemeXML(document, themeSelection, zipWrite);
    }

    private static Document buildXMLDOM(InputStream inputStream)
            throws SAXException, IOException, ParserConfigurationException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();

        return builder.parse(inputStream);
    }

    /**
     * Builds the XML structure for adding new custom colors and writes them into the XMl document
     * @param document  XML Document object.
     */
    private static void writeThemeXML(Document document, String themeSelection, ZipOutputStream zipWrite)
            throws TransformerException, IOException {

        Node extLstNode = findNode(document, "a:extLst");

        Element listElement = document.createElementNS(NAMESPACE, "a:custClrLst");
        List<String[]> userColors = mainWindow.fetchColors();
        for (String[] userColor : userColors) {
            Element newElement = createCustClrElement(document, userColor[0], userColor[1]);
            listElement.appendChild(newElement);
        }

        assert extLstNode != null;
        extLstNode.getParentNode().insertBefore(listElement, extLstNode);
        String outputName = "ppt/theme/" + themeSelection + ".xml";
        writeZipEntry(document, outputName, zipWrite);
    }

    private static void writeZipEntry(Document document, String outputName, ZipOutputStream zipWrite)
            throws TransformerFactoryConfigurationError, TransformerException,
            IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(outputStream);
        transformer.transform(source, result);

        ZipEntry zipEntry = new ZipEntry(outputName);
        zipEntry.setSize(outputStream.size());
        zipEntry.setTime(System.currentTimeMillis());

        zipWrite.putNextEntry(zipEntry);
        zipWrite.write(outputStream.toByteArray());
        zipWrite.closeEntry();
    }

    /**
     * Create a single new custom color node to insert into theme.xml.
     * Needs a <a:custClrLst> parent element.
     * @param document  XML DOM document theme.xml to add the custom colors to
     * @param name      Name of the custom color
     * @param value     HEX Value of the custom color
     * @return          Custom color as XML Element
     */
    private static Element createCustClrElement(Document document, String name, String value) {

        Element custClrElement = document.createElementNS(NAMESPACE, "a:custClr");
        Element srgbClrElement = document.createElementNS(NAMESPACE, "a:srgbClr");
        custClrElement.setAttribute("name", name);
        srgbClrElement.setAttribute("val", value);
        custClrElement.appendChild(srgbClrElement);

        return custClrElement;
    }

// Following are helper methods

    private static Document setTextLevels(Document slideMaster) {

        Element baseNode = slideMaster.getDocumentElement();
        Node txStyles = findNode(baseNode, "p:txStyles");
        if (txStyles == null) {
            throw new IllegalStateException("txStyles node not found");
        }

        // Pick the text levels from the master.
        Node bodyStyle = findNode(txStyles, "p:bodyStyle");
        if (bodyStyle == null) {
            throw new IllegalStateException("bodyStyle node not found");
        }

        // Remove existing text styles.
        Node otherStyles = findNode(txStyles, "p:otherStyle");
        if (otherStyles == null) {
            throw new IllegalStateException("otherStyle node not found");
        }

        removeAllChildNodes(otherStyles);

        // Append cloned text levels to otherStyles
        for (int j = 0; j < bodyStyle.getChildNodes().getLength(); j++) {
            Node clonedNode = bodyStyle.getChildNodes().item(j).cloneNode(true);
            otherStyles.appendChild(clonedNode);
        }

        return slideMaster;
    }

    public static void removeAllChildNodes(Node ParentNode) {
        NodeList childNodes = ParentNode.getChildNodes();
        // Collect nodes to be removed
        List<Node> nodesToRemove = new ArrayList<>();
        for (int i = 0; i < childNodes.getLength(); i++) {
            nodesToRemove.add(childNodes.item(i));
        }

        // Remove collected nodes
        for (Node node : nodesToRemove) {
            ParentNode.removeChild(node);
        }
    }

    /**
     * Match the tableStylesXML ID to theme ID.
     * If there are any pre-existing table styles in the file they get matched
     * to the ID of the selected theme.
     * @param presentation  the presentation object that should have its tableStyles ID matched to theme ID
     * @param selection     selected theme for comparison
     */
    public static Document matchIDs(presentation presentation, int selection) {

        Document tableStylesXML = presentation.getTableStylesXML();
        NodeList styleList = tableStylesXML.getElementsByTagName("a:tblStyleLst");
        Node styleListNode = styleList.item(0);

        String ID = presentation.getThemeID(selection);

        if (styleListNode instanceof Element styleElement) {
            styleElement.setAttribute("def", ID);
        }
        return tableStylesXML;
    }

    /**
     * Remove a specific node + all children
     */
    public static void removeNode(Element rootElement, String nodeName) {
        NodeList rootNode = rootElement.getChildNodes();
        for (int i = 0; i < rootNode.getLength(); i++) {
            Node childNode = rootNode.item(i);
            if (childNode.getNodeName().equals("a:" + nodeName)) {
                Node parentNode = childNode.getParentNode();
                if (parentNode != null) {
                    parentNode.removeChild(childNode);
                }
                break;
            }
        }
    }

    public static String extractFileExtension(String path) {
        return path.substring(path.lastIndexOf('.') + 1);
    }

    public static String extractFilename(String path) {
        return path.substring(path.lastIndexOf(osPathSymbol()) + 1, path.lastIndexOf('.'));
    }

    public static String extractFilePath(String path) {
        return path.substring(0, path.lastIndexOf(osPathSymbol()));
    }

    static public void clearPresentationObject(presentation presentation) {
        presentation.setFilePath("");
        presentation.setFileName("");
        presentation.setFileExtension("");
        presentation.setZipPathString("");
        presentation.themeDataList = new ArrayList<>();
    }

    /**
     * @param extension     the desired extension. 1 = .zip, anything else = original extension
     */
    public static void changeExtension(presentation presentation, int extension) {

        String filePath = presentation.getFilePath();
        String fileName = presentation.getFileName();
        String fileExtension = presentation.getFileExtension();

        Path oldFile;
        String newExtension;

        if (extension == 1) {
            oldFile = Paths.get(filePath + osPathSymbol() + fileName + "." + fileExtension);
            newExtension = ".zip";
        } else {
            oldFile = Paths.get(filePath + osPathSymbol() + fileName + ".zip");
            newExtension = "." + fileExtension;
        }

        String newFileName = fileName + newExtension;
        try {
            Files.move(oldFile, oldFile.resolveSibling(newFileName));
            String logMessage = String.format("File converted to %s\n", newExtension);
            CustClrTool.mainGUI.eventLog.setText(logMessage);
        } catch (IOException e) {
            CustClrTool.mainGUI.eventLog.setText("Failed to convert file.");
        }

    }

    /**
     * Helper method to find a specific child node based on a parent node
     * and its name as a string.
     * @param parent    Parent node
     * @param nodeName  Name of the node that should be found
     * @return          The node itself, or null if not found
     */
    public static Node findNode(Node parent, String nodeName) {

        NodeList childNodes = parent.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            if (child.getNodeName().equals(nodeName)) {
                return child;
            } else if (child.hasChildNodes()) {
                Node foundNode = findNode(child, nodeName);
                if (foundNode != null) {
                    return foundNode;
                }
            }
        }
        return null;
    }

    private static void replaceOldFile(String oldFile, String filePath, String extension) {

        String oldFilePath = filePath + osPathSymbol() + oldFile + ".zip";
        File oldZip = new File(oldFilePath);
        boolean deleteZip = oldZip.delete();
        if (deleteZip) {
            CustClrTool.mainGUI.updateEventLog("Zip file removed.");
        }

        String newFilePath = filePath + osPathSymbol() + oldFile + extension;
        File newZip = new File(oldFile + "_tmp.zip");
        boolean renamedZip = newZip.renameTo(new File(newFilePath));
        if (renamedZip) {
            CustClrTool.mainGUI.updateEventLog("Zip file successfully renamed.");
        }
    }

    public static char osPathSymbol() {

        char pathDivider;
        String currentOS = System.getProperty("os.name");
        if (currentOS.startsWith("Windows"))
            pathDivider = '\\';
        else
            pathDivider = '/';

        return pathDivider;
    }

    public String getThemeNames(int number) {
        return getThemeDataList().get(number).themeName;
    }

    public List<String> getThemeCustClr(int number) {
        return getThemeDataList().get(number).customColors;
    }

    public String getThemeID(int number) {
        return getThemeDataList().get(number).themeID;
    }

    public String getThemeNumber(int number) {
        return getThemeDataList().get(number).themeNumber;
    }

    public HashMap<String, String> getSelectedThemeColors() {
        int selectedTheme = CustClrTool.mainGUI.getSelectedTheme();
        return CustClrTool.newpres.getThemeDataList().get(selectedTheme).themeColors;
    }

    public static class Themedata {

        public String themeName = "";
        public String themeNumber = "";
        public String themeID = "";
        public HashMap<String, String> themeColors = new HashMap<>();
        public List<String> customColors = new ArrayList<>();

        Themedata() {
            String[] currentColorName = settingsField.getThemeColors();
            for (int i = 0; i < 12; i++) {
                themeColors.put(currentColorName[i], "");
            }
        }

    }
}