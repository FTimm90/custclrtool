package scr.settingsField;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import scr.gui.mainWindow;

public class settingsField {

    private final int PANELWIDTH = 240;

    public JPanel widget;
    JPanel fontPanel;
    JPanel linePanel;
    JPanel colorPreview;
    private final ArrayList<JPanel> lineSidePanels = new ArrayList<>();

    private final HashMap<String, HashMap<String, JComboBox<XmlValue>>> allFields = new HashMap<>();
    
    public final HashMap<String, JComboBox<XmlValue>> left = new HashMap<>();
    public final HashMap<String, JComboBox<XmlValue>> right = new HashMap<>();
    public final HashMap<String, JComboBox<XmlValue>> top = new HashMap<>();
    public final HashMap<String, JComboBox<XmlValue>> bottom = new HashMap<>();
    public final HashMap<String, JComboBox<XmlValue>> insideH = new HashMap<>();
    public final HashMap<String, JComboBox<XmlValue>> insideV = new HashMap<>();
    public final HashMap<String, JComboBox<XmlValue>> fill = new HashMap<>();
    public final HashMap<String, JComboBox<XmlValue>> fontRef = new HashMap<>();

    public settingsField(int posX, int posY, boolean insideHCheck, boolean insideVCheck) {
        this.widget = settingsWidget(posX, posY, insideHCheck, insideVCheck);
    }

    private JPanel settingsWidget(int posX, int posY, boolean insideHCheck, boolean insideVCheck) {

        widget = mainWindow.newPanel(0, 0, 0, 0, 230, 700);
        widget.setBounds(posX, posY, PANELWIDTH, 750);

        allFields.put("left", left);
        allFields.put("right", right);
        allFields.put("top", top);
        allFields.put("bottom", bottom);
        if (insideHCheck) {
            allFields.put("insideH", insideH);
        }
        if (insideVCheck) {
            allFields.put("insideV", insideV);
        }

        // a:tcBdr
        setBordersWidget(insideHCheck, insideVCheck);
        lineSidePanels.get(0).setVisible(true);

        JPanel dividerLine = divider();
        widget.add(dividerLine);

        // a:solidFill
        JPanel cellColor = colorWidget(0, 0, "Cell color", fill);
        allFields.put("fill", fill);
        widget.add(cellColor);
        
        // a:fontRef
        JPanel fontSelection = fontWidget(0, 0, "Font:", fontRef);
        widget.add(fontSelection);
        
        JPanel fontColor = colorWidget(0, 0, "Text color:", fontRef);
        allFields.put("fontRef", fontRef);

        widget.add(fontColor);

        return widget;
    }

    private void lineSideSelected(JComboBox<XmlValue> lineSideSelection) {
        int selection = lineSideSelection.getSelectedIndex();
        for (int i = 0; i < lineSidePanels.size(); i++) {
            if (i == selection) {
                lineSidePanels.get(i).setVisible(true);
                System.out.println(lineSidePanels.get(i).getName() + " Selected.");
            } else {
                lineSidePanels.get(i).setVisible(false);
            }
        }
    }

    private void setBordersWidget(boolean insideHCheck, boolean insideVCheck) {
        
        // Placeholder for JComboBox that's created further down
        JPanel placeholder = new JPanel();
        widget.add(placeholder, 0);
        
        int sidescounter = 0;
        ArrayList<XmlValue> tempNames = new ArrayList<>();

        for (int i = 0; i < XmlValue.lineSides.length; i++) {

            if ((!insideHCheck && i == 4) || (!insideVCheck && i == 5)) {
                continue;
            }

            XmlValue side = XmlValue.lineSides[i];
            String sideName = side.getXmlValue();
            HashMap<String, JComboBox<XmlValue>> sideMap = allFields.get(sideName);
            JPanel border = lineWidget(0, 0, sideMap);

            border.setName(side + " Border");
            border.setVisible(false);
            lineSidePanels.add(border);
            widget.add(border);

            tempNames.add(side);
            sidescounter++;
        }

        XmlValue[] sides = new XmlValue[sidescounter];
        for (int j = 0; j < tempNames.size(); j++) {
            // System.out.println(tempNames.get(j));
            sides[j] = tempNames.get(j);
        }

        JComboBox<XmlValue> lineSideSelection = new JComboBox<>(sides);
        lineSideSelection.addActionListener(click -> {
            lineSideSelected(lineSideSelection);
        });
        
        // Replace the placeholder with the actual JComboBox
        widget.remove(placeholder);
        widget.add(lineSideSelection, 0); 
        widget.revalidate();
        widget.repaint();
    
    
    }

    private JPanel lineWidget(int posX, int posY, HashMap<String, JComboBox<XmlValue>> targetMap) {

        linePanel = mainWindow.newPanel(posX, posY, 0, 0, PANELWIDTH, 110);

        JLabel leftBorder = mainWindow.newLabel(0, 0, "");
        leftBorder.setText("Line width: ");
        leftBorder.setBounds(0, 0, 100, 30);
        linePanel.add(leftBorder);

        JComboBox<XmlValue> lineOne = lineWidthSelection();
        targetMap.put("w", lineOne);
        linePanel.add(lineOne);

        JComboBox<XmlValue> lineStyle = lineStyleSelection();
        targetMap.put("cmpd", lineStyle);
        linePanel.add(lineStyle);

        JPanel lineColor = colorWidget(0, 0, "Line color:", targetMap);
        linePanel.add(lineColor);
           
        return linePanel;
    }
    
    private JPanel fontWidget(int posX, int posY, String lineText, HashMap<String, JComboBox<XmlValue>> targetMap) {

        fontPanel = mainWindow.newPanel(posX, posY, 0, 0, PANELWIDTH, 50);
        
        JPanel dividerLine = divider();
        fontPanel.add(dividerLine);

        JLabel fontLabel = mainWindow.newLabel(0, 0, "");
        fontLabel.setText(lineText);
        fontLabel.setBounds(0, 0, 100, 30);
        fontPanel.add(fontLabel);

        JComboBox<XmlValue> fontSelectBox = fontSelection();
        targetMap.put("idx", fontSelectBox);
        fontPanel.add(fontSelectBox);

        JComboBox<XmlValue> textStyleBox = textStyleSelection();
        targetMap.put("tcTxStyle", textStyleBox);
        fontPanel.add(textStyleBox);

        return fontPanel;
    }
    
    private JPanel colorWidget(int posX, int posY, String lineText, HashMap<String, JComboBox<XmlValue>> targetMap) {

        JPanel colorPanel = mainWindow.newPanel(posX, posY, 0, 0, PANELWIDTH, 30);

        JLabel colorLabel = mainWindow.newLabel(0, 0, "");
        colorLabel.setText(lineText);
        colorLabel.setBounds(0, 0, 100, 30);
        colorPanel.add(colorLabel);

        JComboBox<XmlValue> colorSelectBox = colorSelection();
        targetMap.put("val", colorSelectBox);
        colorPanel.add(colorSelectBox);

        colorPreview = mainWindow.newPanel(1, 1, 1, 1, 15, 15);
        colorPanel.add(colorPreview);

        return colorPanel;
    }
    
    private JPanel divider() {
        JPanel dividerLine = mainWindow.newPanel(1, 0, 0, 0, PANELWIDTH, 10);
        return dividerLine;
    }
    
    private JComboBox<XmlValue> lineWidthSelection() {
        JComboBox<XmlValue> widthSelect = new JComboBox<>(XmlValue.lineWidths);
        return widthSelect;
    }

    private JComboBox<XmlValue> lineStyleSelection() {
        JComboBox<XmlValue> styleSelect = new JComboBox<>(XmlValue.lineTypes);
        return styleSelect;
    }
    
    private JComboBox<XmlValue> fontSelection() {
        JComboBox<XmlValue> fontSelect = new JComboBox<>(XmlValue.themeFonts);
        return fontSelect;
    }

    private JComboBox<XmlValue> textStyleSelection() {
        JComboBox<XmlValue> textStyle = new JComboBox<>(XmlValue.textStyle);
        return textStyle;
    }
    
    private JComboBox<XmlValue> colorSelection() {
        JComboBox<XmlValue> colorSelect = new JComboBox<>(XmlValue.themeColors);
        return colorSelect;
    }

    public void showSettingsField(boolean selection) {
        this.widget.setVisible(selection);
    }

    public static String[] getThemeColors() {
        String[] themeColorNames = new String[12];
        for (int i = 0; i < 12; i++) {
            themeColorNames[i] = XmlValue.themeColors[i].getXmlValue();
        }
        return themeColorNames;
    }

    public HashMap<String, HashMap<String, JComboBox<XmlValue>>> getCollectedValues() {
        // printAllValues();
        return allFields;
    }

    public void printAllValues() {
        for (String name : allFields.keySet()) {
            System.out.println("----------- " + name + " -----------");
            HashMap<String, JComboBox<XmlValue>> currentfield = allFields.get(name);
            for (String field : currentfield.keySet()) {
                JComboBox<XmlValue> comboBox = currentfield.get(field);
                XmlValue current = (XmlValue) comboBox.getSelectedItem();
                System.out.printf("%s: %s\n", field, current.getXmlValue());
            }

        }
    }
}