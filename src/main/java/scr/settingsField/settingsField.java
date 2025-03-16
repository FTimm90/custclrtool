package scr.settingsField;
import java.util.HashMap;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import scr.gui.mainWindow;

public class settingsField {

    public static String[] getThemeColors() {
        String[] themeColorNames = new String[12];
        for (int i = 0; i < 12; i++) {
            themeColorNames[i] = XmlValue.themeColors[i].getXmlValue();
        }
        return themeColorNames;
    }

    private final int PANELWIDTH = 240;

    public JPanel widget;
    JPanel fontPanel;
    JPanel linePanel;
    JPanel colorPreview;
    JComboBox<XmlValue> fontSelectBox;
    JComboBox<XmlValue> colorSelectBox;
    private HashMap<String, JComboBox<XmlValue>> allFields = new HashMap<>();

    public HashMap<String, JComboBox<XmlValue>> getCollectedValues() {
        // for (String name : allFields.keySet()) {
        //     System.out.printf("%s: %s\n", name, allFields.get(name).getSelectedItem());
        // }
        return allFields;
    }

    public settingsField(int posX, int posY) {
        this.widget = settingsWidget(posX, posY);
    }

    private JPanel settingsWidget(int posX, int posY) {

        widget = mainWindow.newPanel(0, 0, 0, 0, 120, 140);
        widget.setBounds(posX, posY, PANELWIDTH, 750);

        // a:tcBdr
        setBordersWidget();

        JPanel dividerLine = divider();
        widget.add(dividerLine);

        // a:solidFill
        JPanel cellColor = colorWidget(0, 0, "Cell color", "solidFill");
        widget.add(cellColor);
        
        // a:fontRef
        JPanel fontSelection = fontWidget(0, 0, "Font:", "fontRef");
        widget.add(fontSelection);
        
        JPanel fontColor = colorWidget(0, 0, "Text color:", "font color");
        widget.add(fontColor);

        return widget;
    }

    private void setBordersWidget() {
        
        for (XmlValue side : XmlValue.lineSides) {
            String borderName = String.format("%s Border:", side);
            JPanel border = lineWidget(0, 0, borderName, side.toString());
            widget.add(border);            
        }
    }

    private JPanel lineWidget(int posX, int posY, String lineText, String name) {

        linePanel = mainWindow.newPanel(posX, posY, 0, 0, PANELWIDTH, 70);

        JPanel dividerLine = divider();
        widget.add(dividerLine);

        JLabel leftBorder = mainWindow.newLabel(0, 0, "");
        leftBorder.setText(lineText);
        leftBorder.setBounds(0, 0, 100, 30);
        linePanel.add(leftBorder);

        JComboBox<XmlValue> lineOne = widthSelection(name + " width");        

        linePanel.add(lineOne);

        JPanel lineColor = colorWidget(0, 0, "Line color:", name + " color");
        linePanel.add(lineColor);
           
        return linePanel;
    }
    
    private JPanel fontWidget(int posX, int posY, String lineText, String name) {

        fontPanel = mainWindow.newPanel(posX, posY, 0, 0, PANELWIDTH, 50);
        
        JPanel dividerLine = divider();
        fontPanel.add(dividerLine);

        JLabel fontLabel = mainWindow.newLabel(0, 0, "");
        fontLabel.setText(lineText);
        fontLabel.setBounds(0, 0, 100, 30);
        fontPanel.add(fontLabel);

        fontSelectBox = fontSelection(name);
        fontPanel.add(fontSelectBox);

        return fontPanel;
    }
    
    private JPanel colorWidget(int posX, int posY, String lineText, String name) {

        JPanel colorPanel = mainWindow.newPanel(posX, posY, 0, 0, PANELWIDTH, 30);

        JLabel colorLabel = mainWindow.newLabel(0, 0, "");
        colorLabel.setText(lineText);
        colorLabel.setBounds(0, 0, 100, 30);
        colorPanel.add(colorLabel);

        colorSelectBox = colorSelection(name);
        colorPanel.add(colorSelectBox);

        colorPreview = mainWindow.newPanel(1, 1, 1, 1, 15, 15);
        colorPanel.add(colorPreview);

        return colorPanel;
    }
    
    private JPanel divider() {
        JPanel dividerLine = mainWindow.newPanel(1, 0, 0, 0, PANELWIDTH, 10);
        return dividerLine;
    }
    
    private JComboBox<XmlValue> widthSelection(String name) {
        JComboBox<XmlValue> widthSelect = new JComboBox<>(XmlValue.lineWidths);
        allFields.put(name, widthSelect);
        return widthSelect;
    }
    
    private JComboBox<XmlValue> fontSelection(String name) {
        JComboBox<XmlValue> fontSelect = new JComboBox<>(XmlValue.themeFonts);
        allFields.put(name, fontSelect);
        return fontSelect;
    }
    
    private JComboBox<XmlValue> colorSelection(String name) {
        JComboBox<XmlValue> colorSelect = new JComboBox<>(XmlValue.themeColors);
        allFields.put(name, colorSelect);
        return colorSelect;
    }

    public void showSettingsField(boolean selection) {
        this.widget.setVisible(selection);
    }
}