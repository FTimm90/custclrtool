package scr.presentation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

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

        String newExtension;
        if (extension == 1)
            newExtension = ".zip";
        else
            newExtension = "." + fileExtension;

        String newFileName = fileName + newExtension;
        try {
            Files.move(filePath, filePath.resolveSibling(newFileName));
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

    public static void writeZipOutput(String zipFilePath, List<String> themeSelection, String filename)
            throws FileNotFoundException {
        // "File" is outdated, preferrably use "Files" instead
        File zipNew = new File(filename + "_tmp.zip");
        ZipOutputStream zipWrite = new ZipOutputStream(new FileOutputStream(zipNew));

        try (ZipFile zipFile = new ZipFile(zipFilePath)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                String type = entry.isDirectory() ? "DIR" : "FILE";
                zipWrite.putNextEntry(entry);

                // Create input stream and a buffer to write to the new .zip
                InputStream inputStream = zipFile.getInputStream(entry);
                
                if (type.equals("FILE") && name.contains(themeSelection.get(0))) {
                    writeXML(themeSelection.get(0), inputStream);
                }
                
                byte[] buffer = new byte[1024];
                int len;
                while ((len = inputStream.read(buffer)) > 0) {
                    zipWrite.write(buffer, 0, len);
                }



                inputStream.close();
                zipWrite.closeEntry();
            }
            zipWrite.close();
        } catch (IOException ex) {
            System.err.println(ex);
        }

        replaceOldFile(filename);
    }

    private static void writeXML(String selectedTheme, InputStream themeStream) {
        File newXML = new File(selectedTheme + "_tmp.xml");

        XMLStreamReader reader;
        try {
            OutputStream outputStream = new FileOutputStream(newXML);
            reader = XMLInputFactory.newInstance().createXMLStreamReader(themeStream);
            XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(outputStream);
            // writer.writeDefaultNamespace("http://schemas.openxmlformats.org/drawingml/2006/main");
            
            while (reader.hasNext()) {
                int eventType = reader.next();
                writer.writeStartDocument();
                switch (eventType) {
                    case XMLStreamReader.START_ELEMENT -> {
                        writer.writeStartElement(reader.getPrefix(), reader.getLocalName(), reader.getNamespaceURI());
                        for (int i = 0; i < reader.getAttributeCount(); i++) {
                            writer.writeAttribute(reader.getAttributeLocalName(i), reader.getAttributeValue(i));
                        }
                        writer.writeDefaultNamespace(reader.getNamespaceURI());
                    }
                    case XMLStreamReader.END_ELEMENT -> writer.writeEndElement();
                    case XMLStreamReader.CHARACTERS -> writer.writeCharacters(reader.getText());
                    case XMLStreamReader.CDATA -> writer.writeCData(reader.getText());
                    case XMLStreamReader.SPACE -> writer.writeCharacters(reader.getText());
                    case XMLStreamReader.COMMENT -> writer.writeComment(reader.getText());
                }
                writer.writeEndDocument();
            }
        
            writer.flush();
            writer.close();
            reader.close();
        } catch (XMLStreamException | javax.xml.stream.FactoryConfigurationError | FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    private static void replaceOldFile(String oldFile) {
        String oldFilePath = oldFile + ".zip";
        File oldZip = new File(oldFilePath);
        oldZip.delete();

        String newFilePath = oldFile + ".pptx"; // -> no hardcoded file extension!!
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
