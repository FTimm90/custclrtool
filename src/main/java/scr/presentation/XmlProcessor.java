package scr.presentation;

@FunctionalInterface
public interface XmlProcessor {
    void process(java.io.InputStream inputStream, String destinationXML, java.util.zip.ZipOutputStream zipWrite) throws java.io.IOException, javax.xml.parsers.ParserConfigurationException, org.xml.sax.SAXException, javax.xml.transform.TransformerException;
}