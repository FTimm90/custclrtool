import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void writeZipOutput(String zipFilePath, List<String> themeSelection, String filename, String filePath)
        throws FileNotFoundException, IOException {

    // Bind the selected Theme in a constant
    final String THEME_PATH = "ppt" + osPathSymbol() + "theme" + osPathSymbol() + themeSelection.get(0);
    // Create a new zipfile to copy all the contents over to
    File zipNew = new File(filename + "_tmp.zip");

    try (ZipFile zipFile = new ZipFile(zipFilePath)) {
        // This enum holds the references to the contents of the zipfile
        Enumeration<? extends ZipEntry> entries = zipFile.entries();

        // Iterate over the entries of the zipfile
        while (entries.hasMoreElements()) {
            // Gather information about the current entry
            ZipEntry entry = entries.nextElement();
            String name = entry.getName();

            // Check if the entry matches the desired theme and file path (if provided)
            if (name.contains(THEME_PATH) && (filePath == null || name.equals(filePath))) {
                // Extract the entry content
                byte[] content = extractEntryContent(zipFile, entry);

                // Modify the content here (if necessary)

                // Write the modified content to the new zip file
                writeEntryToZip(zipNew, entry, content);
            }
        }
    } catch (IOException ex) {
        System.err.println(ex);
    }

    replaceOldFile(filename, filePath);
}

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


// Helper method to extract entry content
private static byte[] extractEntryContent(ZipFile zipFile, ZipEntry entry) throws IOException {
    try (InputStream inputStream = zipFile.getInputStream(entry)) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int len;
        while ((len = inputStream.read(data)) > 0) {
            buffer.write(data, 0, len);
        }
        return buffer.toByteArray();
    }
}

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


// Helper method to write entry to zip file
private static void writeEntryToZip(File zipFile, ZipEntry entry, byte[] content) throws IOException {
    try (ZipOutputStream zipWrite = new ZipOutputStream(new FileOutputStream(zipFile))) {
        zipWrite.putNextEntry(entry);
        zipWrite.write(content, 0, content.length);
        zipWrite.closeEntry();
    }
}