package scr.settingsField;

public class XmlValue {
    private final String displayValue;
    private final String xmlValue;
    private final String tagName;

    public static final XmlValue[] lineSides = {
        new XmlValue("Left", "left", "a:left"),
        new XmlValue("Right", "right", "a:right"),
        new XmlValue("Top", "top", "a:top"),
        new XmlValue("Bottom", "bottom", "a:bottom"),
        new XmlValue("Inner Horizontal", "insideH", "a:insideH"),
        new XmlValue("Inner Vertical", "insideV", "a:insideV"),
    };
    
    public static final XmlValue[] tableElements = {
        new XmlValue("whole table", "wholeTbl", "a:wholeTbl"),
        new XmlValue("banded rows (uneven)", "band1H", "a:band1H"),
        new XmlValue("banded columns (uneven)", "band1V", "a:band1V"),
        // new XmlValue("banded rows (even)", "band2H"), Since these are not available for activating inside PP they are not needed
        // new XmlValue("banded columns (even)", "band2V"),
        new XmlValue("first column", "firstCol", "a:firstCol"),
        new XmlValue("last column", "lastCol", "a:lastCol"),
        new XmlValue("first row", "firstRow", "a:firstRow"),
        new XmlValue("last row", "lastRow", "a:lastRow"),
        // TODO look into definition of textlevels 
        // new XmlValue("text levels", "")
    };

    public static final XmlValue[] lineTypes = {
        new XmlValue("Single Line", "sng", "a:ln"),
        new XmlValue("Double Lines", "dbl", "a:ln"),
        new XmlValue("Thick Thin Double Lines", "thickThin", "a:ln"),
        new XmlValue("Thin Thick Double Lines", "thinThick", "a:ln"),
        new XmlValue("Thin Thick Thin Triple Lines", "tri", "a:ln")
    };
    
    public static final XmlValue[] lineWidths = {
        new XmlValue("0,00pt", "0", "a:ln"),
        new XmlValue("0,25pt", "3175", "a:ln"),
        new XmlValue("0,50pt", "6350", "a:ln"),
        new XmlValue("0,75pt", "9525", "a:ln"),
        new XmlValue("1,00pt", "12700", "a:ln"),
        new XmlValue("1,50pt", "28575", "a:ln"),
        new XmlValue("2,25pt", "38100", "a:ln"),
        new XmlValue("3,00pt", "57150", "a:ln"),
        new XmlValue("4,50pt", "76200", "a:ln"),
        new XmlValue("6,00pt", "104775", "a:ln")      
    };
        
    public static final XmlValue[] themeColors = {
        new XmlValue("Light 1", "lt1", "a:schemeClr"),
        new XmlValue("Dark 1", "dk1", "a:schemeClr"),
        new XmlValue("Light 2", "lt2", "a:schemeClr"),
        new XmlValue("Dark 2", "dk2", "a:schemeClr"),
        new XmlValue("accent 1", "accent1", "a:schemeClr"),
        new XmlValue("accent 2", "accent2", "a:schemeClr"),
        new XmlValue("accent 3", "accent3", "a:schemeClr"),
        new XmlValue("accent 4", "accent4", "a:schemeClr"),
        new XmlValue("accent 5", "accent5", "a:schemeClr"),
        new XmlValue("accent 6", "accent6", "a:schemeClr"),
        new XmlValue("Hyperlink", "hlink", "a:schemeClr"),
        new XmlValue("Followed Hyperlink", "folHlink", "a:schemeClr")
    };

    public static final XmlValue[] themeFonts = {
        new XmlValue("Body Text", "minor", "a:fontRef"),
        new XmlValue("Headline Text", "major", "a:fontRef"),
    };

    public static final XmlValue[] textStyle = {
        new XmlValue("Normal", "def", "a:tcTxStyle"),
        new XmlValue("Bold", "b", "a:tcTxStyle"),
        new XmlValue("Italic", "i", "a:tcTxStyle"),
    };

    public XmlValue(String displayValue, String xmlValue, String tagName) {
        this.displayValue = displayValue;
        this.xmlValue = xmlValue;
        this.tagName = tagName;
    }

    @Override
    public String toString() {
        return displayValue; 
    }

    public String getXmlValue() {
        return xmlValue;
    }

    public String getTagName() {
        return xmlValue;
    }
}
