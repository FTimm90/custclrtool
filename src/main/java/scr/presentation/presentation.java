package scr.presentation;

import java.io.ByteArrayInputStream;
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
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
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
     * to extract information
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

    public static void writeZipOutput(String zipFilePath, List<String> themeSelection, String filename, String filePath)
            throws FileNotFoundException {
        
        // Bind the selected Theme in a constant
        final String THEME_PATH = "ppt" + osPathSymbol() + "theme" + osPathSymbol() + themeSelection.get(0);
        // Create a new zipfile to copy all the contents over to
        File zipNew = new File(filename + "_tmp.zip");
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
                zipWrite.putNextEntry(entry);

                try ( // Create input stream and a buffer to write to the new .zip
                        InputStream inputStream = zipFile.getInputStream(entry)) {
                    if (type.equals("FILE") && name.contains(themeSelection.get(0))) {
                        // maybe instead of picking the .xml file out of the the stream, we inject the already created .xml file here
                        // writeXML(themeSelection.get(0), inputStream);
                    }
                    
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = inputStream.read(buffer)) > 0) {
                        zipWrite.write(buffer, 0, len);
                    }
                }
                zipWrite.closeEntry();
            }
            zipWrite.close();
        } catch (IOException ex) {
            System.err.println(ex);
        }

        replaceOldFile(filename, filePath);
    }

    private static void writeXML(String selectedTheme, InputStream themeStream) throws IOException {
        // On hold, doesn't work the way I want
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        XMLStreamReader reader;
        
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            reader = XMLInputFactory.newInstance().createXMLStreamReader(themeStream);
            
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            byte[] data = new byte[1024];
            int read;
            
            while ((read = themeStream.read(data)) != -1 && !new String(data, 0, read).contains("END_CONDITION")) {
                buffer.write(data, 0, read);
            }
            
            Document xmldoc = docBuilder.parse(new ByteArrayInputStream(buffer.toByteArray()));
            Element element = xmldoc.getDocumentElement();

        } catch (ParserConfigurationException | SAXException | XMLStreamException | FactoryConfigurationError e) {
            e.printStackTrace();
            }
    }


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
