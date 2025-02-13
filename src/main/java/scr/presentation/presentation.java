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

    public String filePath = "";
    public String fileName = "";
    public String fileExtension = "";
    public String zipPathString;

    public String[] allThemes;
    public static List<List<List<String[]>>> foundThemes;
        
    final static String CUSTCLR_NODE = "custClrLst";
    final static String NAMESPACE = "http://schemas.openxmlformats.org/drawingml/2006/main";

    public static String getFileExtension(String path) {
        
        String fileextension = path.substring(path.lastIndexOf('.') + 1);
        
        return fileextension;
    }

    public static String getFilename(String path) {
        
        String filename = path.substring(path.lastIndexOf(osPathSymbol()) + 1, path.lastIndexOf('.'));
        
        return filename;
    }

    public static String getFilePath(String path) {
        
        String filename = path.substring(0, path.lastIndexOf(osPathSymbol()));
        
        return filename;
    }

    public void clearPresentationObject() {
        filePath = "";
        fileName = "";
        fileExtension = "";
        zipPathString = "";
        allThemes = new String[0];
        foundThemes = new ArrayList<>();
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
        
        if (extension == 1){
            oldFile = Paths.get(filePath.toString() + osPathSymbol() + fileName + "." + fileExtension);
            newExtension = ".zip";
        }  
        else {
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
     * Processes the contents of the .zip version of the presentation file 
     * to extract information about existing custom colors
     * @param zipFilePath   The path to the zipfile converted from the pptx.
     * @return              Returns a nested list with the extracted
     *                      theme Information.
     *                      per theme: [[Theme#, Theme Name],[Color Name, Color Value], ...]
     */
    public List<List<List<String[]>>> extractThemes(String zipFilePath) {

        foundThemes = new ArrayList<>();

        try (ZipFile zipFile = new ZipFile(zipFilePath)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                String type = entry.isDirectory() ? "DIR" : "FILE";

                if (type.equals("FILE") && name.contains("theme")) {
                    List<List<String[]>> themeData = extractThemeData(zipFile.getInputStream(entry), name);
                    foundThemes.add(themeData);
                }
            }

            populateAllThemesList();

        } catch (IOException | XMLStreamException ex) {
            System.err.println(ex);
            CustClrTool.mainGUI.eventLog.setText("Failed to extract themes from file.");
        }

        return foundThemes;
    }

    private void populateAllThemesList() {
        
        allThemes = new String[foundThemes.size()];
        for (int i = 0; i < foundThemes.size(); i++) {
                allThemes[i] = foundThemes.get(i).get(0).get(0)[1];
            }
    }   

    /**
     * Extracts Data from the theme selection (Theme name & 
     * Potential existing custom colors)
     * @return  All information stored in a List of strings
     *          (0) --> Theme number (e.g. theme1)
     *          (1) --> Theme name
     *          (2:) -> Names and values of the custom colors
     */
    private static List<List<String[]>> extractThemeData(InputStream inputStream, String themeNumber)
            throws XMLStreamException {
        
        List<String[]> theme = new ArrayList<>(); // Stores all information for a single theme
        List<List<String[]>> themeData = new ArrayList<>(); // Stores all collected themes together
        String[] numberAndName = new String[2]; // Stores theme number and name in theme[0]
        String themeName;

        numberAndName[0] = (themeNumber.substring(themeNumber.lastIndexOf('/')+1,themeNumber.lastIndexOf('.')));

        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
        while (reader.hasNext()) {
            int eventType = reader.next();
            if (eventType == XMLStreamReader.START_ELEMENT) {
                if (reader.getLocalName().equals("theme")) {
                    themeName = reader.getAttributeValue("", "name");
                    numberAndName[1] = themeName;
                    theme.add(numberAndName);
                } else if (reader.getLocalName().equals(CUSTCLR_NODE)) {
                    processColorList(reader, theme);
                }
            } else if (eventType == XMLStreamReader.END_ELEMENT && reader.getLocalName().equals("theme")) {
                themeData.add(theme);
            }
        }

        return themeData;
    }

    /**
     * Reads Custom colors from the input .zip and adds them directly 
     * to the theme list.
     * @param reader    Input XML Data
     * @param theme     Theme list to store the colors
    */
    private static void processColorList(XMLStreamReader reader, List<String[]> theme) throws XMLStreamException {
    
        String[] clrSet = new String[2]; // [0] = Name of the color, [1] = HEX Value of the color
        
        while (reader.hasNext()) {
            int eventType = reader.next();

            if (eventType == XMLStreamReader.END_ELEMENT && reader.getLocalName().equals(CUSTCLR_NODE)) {
                break;

            } else if (eventType == XMLStreamReader.END_ELEMENT && reader.getLocalName().equals("custClr")) {
                theme.add(clrSet.clone());

            } else if (eventType == XMLStreamReader.START_ELEMENT && reader.getLocalName().equals("custClr")) {
                String colorName = reader.getAttributeValue("", "name");
                if (colorName != null) {
                    clrSet[0] = colorName;
                } else {
                    clrSet[0] = " ";
                }

            } else if (eventType == XMLStreamReader.START_ELEMENT && reader.getLocalName().equals("srgbClr")) {
                String colorValue = reader.getAttributeValue("", "val");
                clrSet[1] = colorValue;
            }
        }
    }

    /**
     * Creates a copy of the .zip converted PowerPoint file 
     * and injects the modified .xml file
     * @param zipFilePath       Path of the .zip file
     * @param themeSelection    User selected theme file to modify
     * @param fileName          Base name of the file
     * @param filePath          Base path of the file
     */
    public static void writeZipOutput(String zipFilePath, String fileName, String filePath)
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

                if (type.equals("FILE") && name.contains(mainWindow.selectThemeFileName)) {
                    processTheme(inputStream, mainWindow.selectThemeFileName, zipWrite);
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
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();

        // Parse the input stream as an XML document
        Document document = builder.parse(inputStream);
        Element rootElement = document.getDocumentElement();
        NodeList childNodes = rootElement.getChildNodes();
        
        // Remove Custom Color Node
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (childNode.getNodeName().equals("a:" + CUSTCLR_NODE)) {
                Node parentNode = childNode.getParentNode();
                if (parentNode != null) {
                    parentNode.removeChild(childNode);
                }
                break;
            }
        }

        writeIntoTheme(document, themeSelection, zipWrite);
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

    /**
     * Builds the XML structure for adding new custom colors and writes them into the XMl document
     * @param document  XML Document object.
     * @throws IOException 
     */
    private static void writeIntoTheme(Document document, String themeSelection, ZipOutputStream zipWrite) throws ParserConfigurationException, TransformerException, IOException {

        Node extLstNode = findNode(document, "a:extLst");
        
        Element listElement = document.createElementNS(NAMESPACE, "a:custClrLst");
        List<String[]> userColors = mainWindow.fetchColors();
        for (int i = 0; i < userColors.size(); i++) {
            Element newElement = createCustClrElement(document, userColors.get(i)[0], userColors.get(i)[1]);
            listElement.appendChild(newElement);
        }
        
        extLstNode.getParentNode().insertBefore(listElement, extLstNode);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(); 
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(outputStream); 
        transformer.transform(source, result);

        String outputName = "ppt/theme/" + themeSelection + ".xml";
        ZipEntry zipEntry = new ZipEntry(outputName);
        zipEntry.setSize(outputStream.size());
        zipEntry.setTime(System.currentTimeMillis()); 

        zipWrite.putNextEntry(zipEntry);
        zipWrite.write(outputStream.toByteArray());
        zipWrite.closeEntry();
    }

    /**
     * Create a single new custom color to insert into theme.xml. 
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

    /**
     * Helper function to Replace the old .zip file with the new, modified one.
     * @param oldFile   The path of the old file
     * @param filePath  Path of the new file
     */
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
