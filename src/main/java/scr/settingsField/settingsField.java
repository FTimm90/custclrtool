package scr.settingsField;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import scr.gui.mainWindow;

public class settingsField {

    public static String[] getThemeColors() {
        String[] themeColorNames = new String[12];
        for (int i = 0; i < 12; i++) {
            themeColorNames[i] = themeColors[i].getXmlValue();
        }
        return themeColorNames;
    }

    private final int PANELWIDTH = 240;
    private final String[] SIDES = { "Left", "Right", "Top", "Bottom" };
    
    private static final XmlValue[] lineWidths = {
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
        
    private static final XmlValue[] themeColors = {
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

    private static final XmlValue[] themeFonts = {
        new XmlValue("Body Text", "minor"),
        new XmlValue("Headline Text", "major"),
        };


    public JPanel widget;
    JPanel fontPanel;
    JPanel linePanel;
    JPanel colorPreview;
    JComboBox<XmlValue> fontSelectBox;
    JComboBox<XmlValue> colorSelectBox;
    private List<JComboBox<XmlValue>> collectedValues = new ArrayList<>();

    public List<String> getCollectedValues() {
    List<String> xmlValues = new ArrayList<>();
        for (JComboBox<XmlValue> value : collectedValues) {
            XmlValue selected = (XmlValue) value.getSelectedItem();
            System.out.printf("GUI Value: %s    XML Value: %s\n", value.getSelectedItem().toString(), selected.getXmlValue()).toString();
            xmlValues.add(selected.getXmlValue());
        }
        return xmlValues;
    }

    public settingsField(int posX, int posY) {

        this.widget = settingsWidget(posX, posY);
    }

    private JPanel settingsWidget(int posX, int posY) {

        widget = mainWindow.newPanel(0, 0, 0, 0, 120, 140);
        widget.setBounds(posX, posY, PANELWIDTH, 750);

        setBordersWidget();

        JPanel innerHorizontal = lineWidget(0, 0, "Inner Horizontal Border:");
        widget.add(innerHorizontal);

        JPanel innerVertical = lineWidget(0, 0, "Inner Vertical Border:");
        widget.add(innerVertical);

        JPanel dividerLine = divider();
        widget.add(dividerLine);

        JPanel cellColor = colorWidget(0, 0, "Cell color");
        widget.add(cellColor);
        
        JPanel fontSelection = fontWidget(0, 0, "Font:");
        widget.add(fontSelection);
        
        JPanel fontColor = colorWidget(0, 0, "Text color:");
        widget.add(fontColor);

        return widget;
    }

    private void setBordersWidget() {
        
        for (String SIDE : SIDES) {
            String borderName = String.format("%s Border:", SIDE);
            JPanel border = lineWidget(0, 0, borderName);
            widget.add(border);
        }
    }

    private JPanel lineWidget(int posX, int posY, String lineText) {

        linePanel = mainWindow.newPanel(posX, posY, 0, 0, PANELWIDTH, 70);

        JPanel dividerLine = divider();
        widget.add(dividerLine);

        JLabel leftBorder = mainWindow.newLabel(0, 0, "");
        leftBorder.setText(lineText);
        leftBorder.setBounds(0, 0, 100, 30);
        linePanel.add(leftBorder);

        JComboBox<XmlValue> lineOne = widthSelection();        

        linePanel.add(lineOne);

        JPanel lineColor = colorWidget(0, 0, "Line color:");
        linePanel.add(lineColor);
           
        return linePanel;
    }
    
    private JPanel fontWidget(int posX, int posY, String lineText) {

        fontPanel = mainWindow.newPanel(posX, posY, 0, 0, PANELWIDTH, 50);
        
        JPanel dividerLine = divider();
        fontPanel.add(dividerLine);

        JLabel fontLabel = mainWindow.newLabel(0, 0, "");
        fontLabel.setText(lineText);
        fontLabel.setBounds(0, 0, 100, 30);
        fontPanel.add(fontLabel);

        fontSelectBox = fontSelection();
        fontPanel.add(fontSelectBox);

        return fontPanel;
    }
    
    private JPanel colorWidget(int posX, int posY, String lineText) {

        JPanel colorPanel = mainWindow.newPanel(posX, posY, 0, 0, PANELWIDTH, 30);

        JLabel colorLabel = mainWindow.newLabel(0, 0, "");
        colorLabel.setText(lineText);
        colorLabel.setBounds(0, 0, 100, 30);
        colorPanel.add(colorLabel);

        colorSelectBox = colorSelection();
        colorPanel.add(colorSelectBox);

        colorPreview = mainWindow.newPanel(1, 1, 1, 1, 15, 15);
        colorPanel.add(colorPreview);

        return colorPanel;
    }
    
    private JPanel divider() {
        JPanel dividerLine = mainWindow.newPanel(1, 0, 0, 0, PANELWIDTH, 10);
        return dividerLine;
    }
    
    private JComboBox<XmlValue> widthSelection() {
        JComboBox<XmlValue> widthSelect = new JComboBox<>(lineWidths);
        collectedValues.add(widthSelect);
        return widthSelect;
    }
    
    private JComboBox<XmlValue> fontSelection() {
        JComboBox<XmlValue> fontSelect = new JComboBox<>(themeFonts);
        collectedValues.add(fontSelect);
        return fontSelect;
    }
    
    private JComboBox<XmlValue> colorSelection() {
        JComboBox<XmlValue> colorSelect = new JComboBox<>(themeColors);
        collectedValues.add(colorSelect);
        return colorSelect;
    }
}

class XmlValue {
    private String displayValue;
    private String xmlValue;

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