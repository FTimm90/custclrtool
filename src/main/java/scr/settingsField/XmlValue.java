package scr.settingsField;

public class XmlValue {
    private final String displayValue;
    private final String attributeValue;
    private final String tagName;
    private final String attributeName;

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
        new XmlValue("Single Line", "sng", "a:ln", "cmpd"),
        new XmlValue("Double Lines", "dbl", "a:ln", "cmpd"),
        new XmlValue("Thick Thin Double Lines", "thickThin", "a:ln", "cmpd"),
        new XmlValue("Thin Thick Double Lines", "thinThick", "a:ln", "cmpd"),
        new XmlValue("Thin Thick Thin Triple Lines", "tri", "a:ln", "cmpd")
    };
    
    public static final XmlValue[] lineWidths = {
        new XmlValue("0,00pt", "0", "a:ln", "w"),
        new XmlValue("0,25pt", "3175", "a:ln", "w"),
        new XmlValue("0,50pt", "6350", "a:ln", "w"),
        new XmlValue("0,75pt", "9525", "a:ln", "w"),
        new XmlValue("1,00pt", "12700", "a:ln", "w"),
        new XmlValue("1,50pt", "28575", "a:ln", "w"),
        new XmlValue("2,25pt", "38100", "a:ln", "w"),
        new XmlValue("3,00pt", "57150", "a:ln", "w"),
        new XmlValue("4,50pt", "76200", "a:ln", "w"),
        new XmlValue("6,00pt", "104775", "a:ln", "w")
    };
        
    public static final XmlValue[] themeColors = {
        // TODO Add option for "no color".
        new XmlValue("Light 1", "lt1", "a:schemeClr", "val"),
        new XmlValue("Dark 1", "dk1", "a:schemeClr", "val"),
        new XmlValue("Light 2", "lt2", "a:schemeClr", "val"),
        new XmlValue("Dark 2", "dk2", "a:schemeClr", "val"),
        new XmlValue("accent 1", "accent1", "a:schemeClr", "val"),
        new XmlValue("accent 2", "accent2", "a:schemeClr", "val"),
        new XmlValue("accent 3", "accent3", "a:schemeClr", "val"),
        new XmlValue("accent 4", "accent4", "a:schemeClr", "val"),
        new XmlValue("accent 5", "accent5", "a:schemeClr", "val"),
        new XmlValue("accent 6", "accent6", "a:schemeClr", "val"),
        new XmlValue("Hyperlink", "hlink", "a:schemeClr", "val"),
        new XmlValue("Followed Hyperlink", "folHlink", "a:schemeClr", "val")
    };

    public static final XmlValue[] themeFonts = {
        new XmlValue("Body Text", "minor", "a:fontRef", "idx"),
        new XmlValue("Headline Text", "major", "a:fontRef", "idx"),
    };

    public static final XmlValue[] textStyle = {
        new XmlValue("Normal", "on", "a:tcTxStyle", "def"),
        new XmlValue("Bold", "on", "a:tcTxStyle", "b"),
        new XmlValue("Italic", "on", "a:tcTxStyle", "i"),
    };

    public XmlValue(String displayValue, String xmlValue, String tagName) {
        // Not all XmlValue arrays need a tagName, so we make it optional by overloading the constructor.
        this(displayValue, xmlValue, tagName, null); 
    }
    
    public XmlValue(String displayValue, String attributeValue, String tagName, String attributeName) {
        this.displayValue = displayValue;
        this.attributeValue = attributeValue;
        this.tagName = tagName;
        this.attributeName = attributeName;
    }

    @Override
    public String toString() {
        return displayValue; 
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    public String getTagName() {
        return tagName;
    }

    public String getAttributeName() {
        return attributeName;
    }
}
