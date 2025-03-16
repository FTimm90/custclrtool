package scr.settingsField;

public class XmlValue {
    private String displayValue;
    private String xmlValue;

    public static final XmlValue[] lineSides = {
        new XmlValue("Left", "left"),
        new XmlValue("Right", "right"),
        new XmlValue("Top", "top"),
        new XmlValue("Bottom", "bottom"),
        new XmlValue("Inner Horizontal", "insideH"),
        new XmlValue("Inner Vertical", "insideV"),           
    };
    
    public static final XmlValue[] tableElements = {
        new XmlValue("whole table", "wholeTbl"),
        new XmlValue("banded rows", "band1H"),
        new XmlValue("banded columns", "band1V"),
        new XmlValue("first column", "firstCol"),
        new XmlValue("last column", "lastCol"),
        new XmlValue("first row", "firstRow"),
        new XmlValue("last row", "lastRow"),
        // TODO look into definition of textlevels 
        // new XmlValue("text levels", "")
    };

    // TODO implement line types
    public static final XmlValue[] lineTypes = {
        new XmlValue("Single Line", "sng"),
        new XmlValue("Double Lines", "dbl"),
        new XmlValue("Thick Thin Double Lines", "thickThin"),
        new XmlValue("Thin Thick Double Lines", "thinThick"),
        new XmlValue("Thin Thick Thin Triple Lines", "tri")
    };
    
    public static final XmlValue[] lineWidths = {
        new XmlValue("0,00pt", "0"),
        new XmlValue("0,25pt", "3175"),
        new XmlValue("0,50pt", "6350"),
        new XmlValue("0,75pt", "9525"),
        new XmlValue("1,00pt", "12700"),
        new XmlValue("1,50pt", "28575"),
        new XmlValue("2,25pt", "38100"),
        new XmlValue("3,00pt", "57150"),
        new XmlValue("4,50pt", "76200"),
        new XmlValue("6,00pt", "104775")      
    };
        
    public static final XmlValue[] themeColors = {
        new XmlValue("Light 1", "lt1"),
        new XmlValue("Dark 1", "dk1"),
        new XmlValue("Light 2", "lt2"),
        new XmlValue("Dark 2", "dk2"),
        new XmlValue("accent 1", "accent1"),
        new XmlValue("accent 2", "accent2"),
        new XmlValue("accent 3", "accent3"),
        new XmlValue("accent 4", "accent4"),
        new XmlValue("accent 5", "accent5"),
        new XmlValue("accent 6", "accent6"),
        new XmlValue("Hyperlink", "hlink"),
        new XmlValue("Followed Hyperlink", "folHlink")    
    };

    public static final XmlValue[] themeFonts = {
        new XmlValue("Body Text", "minor"),
        new XmlValue("Headline Text", "major"),
    };

    public XmlValue(String displayValue, String xmlValue) {
        this.displayValue = displayValue;
        this.xmlValue = xmlValue;
    }

    @Override
    public String toString() {
        return displayValue; 
    }

    public String getXmlValue() {
        return xmlValue;
    }
}
