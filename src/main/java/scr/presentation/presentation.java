package scr.presentation;

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
import javax.xml.transform.OutputKeys;
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

public class presentation {

    public String filePath = "";
    public String fileName = "";
    public String fileExtension = "";
    
    public void pickFile() {
        System.out.println("File selection not yet implemented.");
    }

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

    /**
     * Changes the extension of the specified file.
     * @param filePath      the path of the file
     * @param fileName      the name of the file
     * @param fileExtension the original file extension
     * @param extension     the desired extension. 1 = .zip, anything else = original extension
     */
    public static void changeExtension(Path filePath, String fileName, String fileExtension, int extension) {

        Path oldFile = Paths.get(filePath.toString() + osPathSymbol() + fileName + "." + fileExtension);
        System.out.printf("the old file path is: %s\n", oldFile.toString());
        String newExtension;
        if (extension == 1)
            newExtension = ".zip";
        else
            newExtension = "." + fileExtension;

        String newFileName = fileName + newExtension;
        try {
            Files.move(oldFile, oldFile.resolveSibling(newFileName));
            System.out.printf("pptx file converted to %s\n", newExtension);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
     
    /**
     * Processes the contents of the .zip version of the presentation file 
     * to extract information about existing custom colors
     * @param zipFilePath   The path to the zipfile converted from the pptx.
     * @return              Returns a nested list with the extracted information
     *                      the first string is the name of the theme, after that
     *                      come potential existing custom color names and values.
     *                      Colors sometimas have a name, but always a HEX value
     */
    public static List<List<String>> extractThemes(String zipFilePath) {
        List<List<String>> foundThemes = new ArrayList<>();      

        try (ZipFile zipFile = new ZipFile(zipFilePath)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                String type = entry.isDirectory() ? "DIR" : "FILE";

                if (type.equals("FILE") && name.contains("theme")) {
                    List<String> themeData = extractThemeData(zipFile.getInputStream(entry), name);
                    foundThemes.add(themeData);
                }
            }
        } catch (IOException | XMLStreamException ex) {
            System.err.println(ex);
        }

        return foundThemes;
    }

    /**
     * Extracts Data from the theme selection (Theme name & 
     * Potential existing custom colors)
     * @return  All information stored in a List of strings
     *          (0) --> Theme number (e.g. theme1)
     *          (1) --> Theme name
     *          (2:) -> Names and values of the custom colors
     */
    private static List<String> extractThemeData(InputStream inputStream, String themeNumber) throws XMLStreamException {
        List<String> themeData = new ArrayList<>();
        String themeName = "";
        themeData.add(themeNumber.substring(themeNumber.lastIndexOf('/') + 1, themeNumber.lastIndexOf('.')));

        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
        while (reader.hasNext()) {
            int eventType = reader.next();
            if (eventType == XMLStreamReader.START_ELEMENT) {
                if (reader.getLocalName().equals("theme")) {
                    themeName = reader.getAttributeValue("", "name");
                    themeData.add(themeName);
                } else if (reader.getLocalName().equals("custClrLst")) {
                    processColorList(reader, themeData);
                }
            }
        }
        return themeData;
    }

    private static void processColorList(XMLStreamReader reader, List<String> themeData) throws XMLStreamException {
        while (reader.hasNext()) {
            int eventType = reader.next();
            if (eventType == XMLStreamReader.END_ELEMENT && reader.getLocalName().equals("custClrLst")) {
                break;
            }
            if (eventType == XMLStreamReader.START_ELEMENT) {
                if (reader.getLocalName().equals("custClr")) {
                    String colorName = reader.getAttributeValue("", "name");
                    if (colorName != null) {
                        themeData.add(colorName);
                    }
                } else if (reader.getLocalName().equals("srgbClr")) {
                    String colorValue = reader.getAttributeValue("", "val");
                    themeData.add(colorValue);
                }
            }
        }
    }

    /**
     * Creates a copy of the .zip converted PowerPoint file 
     * and injects the modified .xml file
     * @param zipFilePath       Path of the .zip file
     * @param themeSelection    User selected theme file to modify
     * @param filename          Base name of the file
     * @param filePath          Base path of the file
     */
    public static void writeZipOutput(String zipFilePath, List<String> themeSelection, String filename, String filePath)
            throws FileNotFoundException, ParserConfigurationException, SAXException {

        final String ZIP_TMP = "_tmp.zip";
        
        // Create a new zipfile to copy all the contents over to
        File zipNew = new File(filename + ZIP_TMP);
        // Create an output stream for copying everything over
        ZipOutputStream zipWrite = new ZipOutputStream(new FileOutputStream(zipNew));

        // Now open the zipfile
        try (ZipFile zipFile = new ZipFile(zipFilePath)) {
            // This enum holds the references to the contents of the zipfile
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            // Iterate over the entries of the zipfile
            while (entries.hasMoreElements()) {
                // Gather information about the current entry
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                String type = entry.isDirectory() ? "DIR" : "FILE";
                // Put the current zip entry into the new file

                InputStream inputStream = zipFile.getInputStream(entry);

                if (type.equals("FILE") && name.contains(themeSelection.get(0))) {
                    processTheme(inputStream);
                }
                else {
                    insertZipEntry(entry, zipWrite, inputStream);
                }
                
            }

            zipWrite.close();
        } catch (IOException ex) {
            System.err.println(ex);
        }

        replaceOldFile(filename, filePath);
    }
    
    /**
     * Helper method to write .zip entries from InputStream (old file) into
     * ZipOutPutStream (new file)
     * @param entry         Zip entry from old file
     * @param zipWrite      The zip file that is going to be written into
     * @param inputStream   The zip entry as InputStream
     */
    public static void insertZipEntry(ZipEntry entry, ZipOutputStream zipWrite, InputStream inputStream) throws IOException {
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
    public static void processTheme(InputStream inputStream) throws IOException, ParserConfigurationException, SAXException {
        final String CUSTCLR_NODE = "a:custClrLst";
        
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
            if (childNode.getNodeName().equals(CUSTCLR_NODE)) {
                Node parentNode = childNode.getParentNode();
                if (parentNode != null) {
                    parentNode.removeChild(childNode);
                }
                break;
            }
        }
        // -> TO-DO: Insert newly written XML
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
    return null; // Node not found
    }

    /**
     * Buils the XML structure for adding new custom colors and writes them into the XMl document
     * @param document  XML Document object.
     */
    private static void writeIntoTheme(Document document) throws ParserConfigurationException, TransformerException {
        // Create a new Document
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        // Create the parent element (example)
        Element parentElement = document.createElementNS("http://schemas.openxmlformats.org/drawingml/2006/main",
                "a:custClrLst");

        // Create new Custom colors
        Element newElement = createCustClrElement(document, "TEST COLOR", "FFFFFF");

        // Insert the new custom colors to the DOM
        Node extLstNode = findNode(parentElement, "a:extLst"); 
        if (extLstNode != null) {
            parentElement.insertBefore(newElement, extLstNode); 
        }

        // Print the resulting XML
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(new DOMSource(document), new StreamResult(System.out));
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
        // This needs to adjusted to be fed with the actual data (list?)
        
        // Create the two elements that a custom color consists of
        Element custClrElement = document.createElementNS("http://schemas.openxmlformats.org/drawingml/2006/main", "a:custClr"); 
        Element srgbClrElement = document.createElementNS("http://schemas.openxmlformats.org/drawingml/2006/main", "a:srgbClr");
        // Set the values for the custom color elements
        custClrElement.setAttribute("name", name);
        srgbClrElement.setAttribute("val", value);
        // Append the srgbClr Element to the custClr Element 
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

        String newFilePath = filePath + osPathSymbol() + oldFile + ".pptx"; // better use the "change file extension" method
        File newZip = new File(oldFile + "_tmp.zip");
        newZip.renameTo(new File(newFilePath));
    }

    private static char osPathSymbol() {
        char pathDivider;

        String currentOS = System.getProperty("os.name");
        if (currentOS.startsWith("Windows"))
            pathDivider = '\\';
        else
            pathDivider = '/';

        return pathDivider;
    }
}
