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
import javax.xml.transform.TransformerConfigurationException;
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

    private String[] allThemes;
    public String[] getAllThemes() {
        return allThemes;
    }
    private void setAllThemes(List<List<String>> collectedThemes) {
        allThemes = new String[collectedThemes.size()];
        for (int i = 0; i < collectedThemes.size(); i++) {
            String themeName = collectedThemes.get(i).get(1);
            allThemes[i] = themeName.substring(themeName.lastIndexOf(":") + 1);
        }
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

    final static String CUSTCLR_NODE = "custClrLst";
    final static String NAMESPACE = "http://schemas.openxmlformats.org/drawingml/2006/main";
    private List<List<String>> themeCollection;
    
// The following block handles file INPUT

    /**
     * Processes the contents of the .zip version of the presentation file 
     * to extract information about existing custom colors
     * @param zipFilePath   The path to the zipfile converted from the pptx.
     * @return              Returns a nested list with the extracted
     *                      theme Information.
     *                      per theme: [[Theme#, Theme Name],[Color Name, Color Value], ...]
     */
    public List<List<String>> extractXMLData(String zipFilePath) {

        themeCollection = new ArrayList<>();

        try (ZipFile zipFile = new ZipFile(zipFilePath)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                String type = entry.isDirectory() ? "DIR" : "FILE";

                if (type.equals("FILE") && name.contains("theme")) {
                    List<String> theme = extractThemeData(zipFile.getInputStream(entry), name);
                    themeCollection.add(theme);
                    extractThemeData(zipFile.getInputStream(entry), name);
                } else if (type.equals("FILE") && name.contains("tableStyles")) {
                    setTableStylesID(extractTableStylesID(zipFile.getInputStream(entry)));
                    setTableStylesXML(buildXMLDOM(zipFile.getInputStream(entry)));
                }
            }
            setAllThemes(themeCollection);
        } catch (IOException | XMLStreamException | SAXException | ParserConfigurationException ex) {
            System.err.println(ex);
            CustClrTool.mainGUI.eventLog.setText("Failed to extract themes from file.");
        }
        return themeCollection;
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

    /**
     * Extracts Data from the theme selection (Theme name & 
     * Potential existing custom colors) Split by ":"
     * @return  All information stored in a List of strings
     *          (0) --> Theme number (e.g. theme1)
     *          (1) --> Theme name
     *          (2:) -> Names and values of the custom colors
     */
    private static List<String> extractThemeData(InputStream inputStream, String themeNumber)
            throws XMLStreamException {

        List<String> theme = new ArrayList<>(); 

        themeNumber = themeNumber.substring(themeNumber.lastIndexOf('/') + 1, themeNumber.lastIndexOf('.'));

        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
        while (reader.hasNext()) {
            int eventType = reader.next();
            if (eventType == XMLStreamReader.START_ELEMENT) {
                switch (reader.getLocalName()) {
                    case "theme" -> {
                        String themeName = reader.getAttributeValue("", "name");
                        theme.add(String.format("theme number:%s", themeNumber));
                        theme.add(String.format("theme name:%s", themeName));
                    }
                    case CUSTCLR_NODE -> {
                        HashMap<String, String> findColorElements = new HashMap<>();
                        findColorElements.put("custClr", "name");
                        findColorElements.put("srgbClr", "val");
                        List<String> foundElements = gatherChildElements(reader, CUSTCLR_NODE, findColorElements);
                        for (int i = 0; i < foundElements.size(); i++) {
                            theme.add(foundElements.get(i));
                        }
                    }
                    case "themeFamily" -> {
                        String themeID = reader.getAttributeValue("", "id");
                        theme.add(String.format("themeID:%s", themeID));
                    }
                    default -> {
                    }
                }
            } else if (eventType == XMLStreamReader.END_ELEMENT && reader.getLocalName().equals("theme")) {
                break;
            }
        }
        return theme;
    }
    
    /**
     * @param reader Opened XML file.
     * @param parentElement Find child elements within the scope of this.
     * @param childKeyValue Tag : Value to be found.
     * @return A List of all the found child elements.
     * @throws XMLStreamException
     */
    private static List<String> gatherChildElements(XMLStreamReader reader, String parentElement,
            HashMap<String, String> childKeyValue) throws XMLStreamException {
        
        List<String> foundElements = new ArrayList<>();
        while (reader.hasNext()) {
            int eventType = reader.next();
            if (eventType == XMLStreamReader.END_ELEMENT && reader.getLocalName().equals(parentElement)) {
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
     * If the ID of the selected theme is equal to the ID in the tableStyles.xml
     * it is very likely, that potentially existing table styles are custom.
     * @param theme
     * @param tableStyleID
     */
    public static boolean validateID(List<String> theme, String tableStyleID) {
        String idExtract = theme.get(theme.size() - 1);
        String themeID = idExtract.substring(idExtract.lastIndexOf(":") + 1);
        System.out.printf("theme ID: %s, table styles ID: %s\n", themeID, tableStyleID);
        return themeID.equals(tableStyleID);
    }

// The following block handles file OUTPUT

    /**
     * Creates a copy of the .zip converted PowerPoint file 
     * and injects the modified .xml file
     * @param zipFilePath       Path of the .zip file
     * @param fileName          Base name of the file
     * @param filePath          Base path of the file
     * @param destinationXML    The XML file to be replaced
     * @param xmlProcessor      The method used to modify the found XML file
     */
    public static void writeZipOutput(String zipFilePath, String fileName, String filePath, String destinationXML, XmlProcessor xmlProcessor)
            throws FileNotFoundException, ParserConfigurationException, SAXException, TransformerException {

        final String ZIP_TMP = "_tmp.zip";
        
        File zipNew = new File(fileName + ZIP_TMP);
        ZipOutputStream zipWrite = new ZipOutputStream(new FileOutputStream(zipNew));

        try (ZipFile zipFile = new ZipFile(zipFilePath)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                String type = entry.isDirectory() ? "DIR" : "FILE";

                InputStream inputStream = zipFile.getInputStream(entry);

                if (type.equals("FILE") && name.contains(destinationXML)) {
                    xmlProcessor.process(inputStream, destinationXML, zipWrite);
                } else if (type.equals("FILE") && name.contains("tableStyles.xml")) {
                    writeZipEntry(CustClrTool.newpres.getTableStylesXML(), "ppt/tableStyles.xml", zipWrite);
                }
                else {
                    insertZipEntry(entry, zipWrite, inputStream);
                }
                
            }
            zipWrite.close();
        } catch (IOException ex) {
            System.err.println(ex);
            CustClrTool.mainGUI.eventLog.setText("Failed to create new Zipfile.");
        }
        replaceOldFile(fileName, filePath);
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
     * Takes the input stream from found theme to extract XML, 
     * load it into DOM object and further process it.
     * @param inputStream
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

        // Parse the input stream as an XML document
        Document document = builder.parse(inputStream);

        return document;
    }

    /**
     * Builds the XML structure for adding new custom colors and writes them into the XMl document
     * @param document  XML Document object.
     * @throws IOException 
     */
    private static void writeThemeXML(Document document, String themeSelection, ZipOutputStream zipWrite)
            throws ParserConfigurationException, TransformerException, IOException {

        Node extLstNode = findNode(document, "a:extLst");

        Element listElement = document.createElementNS(NAMESPACE, "a:custClrLst");
        List<String[]> userColors = mainWindow.fetchColors();
        for (int i = 0; i < userColors.size(); i++) {
            Element newElement = createCustClrElement(document, userColors.get(i)[0], userColors.get(i)[1]);
            listElement.appendChild(newElement);
        }

        extLstNode.getParentNode().insertBefore(listElement, extLstNode);
        String outputName = "ppt/theme/" + themeSelection + ".xml";
        writeZipEntry(document, outputName, zipWrite);
    }
    
    private static void writeZipEntry(Document document, String outputName, ZipOutputStream zipWrite)
            throws TransformerFactoryConfigurationError, TransformerConfigurationException, TransformerException,
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

public static Document flushTableStyles(Document tableStylesXML, int selection) {
        
        Element rootElement = tableStylesXML.getDocumentElement();
        NodeList styleList = tableStylesXML.getElementsByTagName("a:tblStyleLst");
        Node styleListNode = styleList.item(0);
        
        List<String> selectedTheme = CustClrTool.themes.get(selection);
        String IDValue = selectedTheme.get(selectedTheme.size() - 1);
        String ID = IDValue.substring((IDValue.lastIndexOf(":") + 1));

        if (styleListNode instanceof Element styleElement) { 
                        styleElement.setAttribute("def", ID);
        }
        // TODO removing doesn't work!!
        removeNode(rootElement, "tblStyle");
        return tableStylesXML;
    }

    /**
     * Remove node + all children
     * @param rootElement
     * @param nodeName
     */
    private static void removeNode(Element rootElement, String nodeName) {
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

        String fileextension = path.substring(path.lastIndexOf('.') + 1);

        return fileextension;
    }
    
    public static String extractFilename(String path) {
        return path.substring(path.lastIndexOf(osPathSymbol()) + 1, path.lastIndexOf('.'));
    }
    
    public static String extractFilePath(String path) {
        
        String filename = path.substring(0, path.lastIndexOf(osPathSymbol()));
        
        return filename;
    }

    public void clearPresentationObject() {
        filePath = "";
        fileName = "";
        fileExtension = "";
        zipPathString = "";
        allThemes = new String[0];
    }

    /**
     * @param filePath      the path of the file
     * @param fileName      the name of the file
     * @param fileExtension the original file extension
     * @param extension     the desired extension. 1 = .zip, anything else = original extension
     */
    public static void changeExtension(Path filePath, String fileName, String fileExtension, int extension) {

        Path oldFile;
        String newExtension;

        if (extension == 1) {
            oldFile = Paths.get(filePath.toString() + osPathSymbol() + fileName + "." + fileExtension);
            newExtension = ".zip";
        } else {
            oldFile = Paths.get(filePath.toString() + osPathSymbol() + fileName + ".zip");
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
    private static Node findNode(Node parent, String nodeName) {

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
    
    private static void replaceOldFile(String oldFile, String filePath) {
        
        String oldFilePath = filePath + osPathSymbol() + oldFile + ".zip";
        File oldZip = new File(oldFilePath);
        oldZip.delete();

        String newFilePath = filePath + osPathSymbol() + oldFile + ".pptx";
        File newZip = new File(oldFile + "_tmp.zip");
        newZip.renameTo(new File(newFilePath));
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
}