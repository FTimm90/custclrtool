package scr.settingsField;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import scr.custclr.CustClrTool;
import scr.gui.mainWindow;

public class settingsField {

    private final int PANELWIDTH = 240;

    public JPanel widget;
    JPanel fontPanel;
    JPanel linePanel;
    JPanel colorPreview;
    private final ArrayList<JPanel> lineSidePanels = new ArrayList<>();

    private final HashMap<String, HashMap<String, JComboBox<XmlValue>>> allFields = new HashMap<>();
    private final HashMap<String, JPanel> previewFields = new HashMap<>();

    public final HashMap<String, JComboBox<XmlValue>> left = new HashMap<>();
    public final HashMap<String, JComboBox<XmlValue>> right = new HashMap<>();
    public final HashMap<String, JComboBox<XmlValue>> top = new HashMap<>();
    public final HashMap<String, JComboBox<XmlValue>> bottom = new HashMap<>();
    public final HashMap<String, JComboBox<XmlValue>> insideH = new HashMap<>();
    public final HashMap<String, JComboBox<XmlValue>> insideV = new HashMap<>();
    public final HashMap<String, JComboBox<XmlValue>> fill = new HashMap<>();
    public final HashMap<String, JComboBox<XmlValue>> fontRef = new HashMap<>();

    public HashMap<String, HashMap<String, JComboBox<XmlValue>>> getAllFields() {
        return allFields;
    }

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
        JPanel cellColor = colorWidget(0, 0, "cell color", fill);
        allFields.put("fill", fill);
        widget.add(cellColor);

        return widget;
    }

    /**
     * @param part left, right, top, bottom, fill, insideV, insideH, frontRef
     * @param boxName XmlValue attribute name
     */
    public void setComboBox(String part, String boxName,  XmlValue newValue) {
        HashMap<String, JComboBox<XmlValue>> boxMap = allFields.get(part);
        JComboBox<XmlValue> boxSelect = boxMap.get(boxName);
        boxSelect.setSelectedItem(newValue);
    }

    public JPanel FontWidget() {
        JPanel parent = mainWindow.newPanel(0, 0, 0, 0, 230, 120);
        JPanel fontSelection = fontWidget(0, 0, "font", fontRef);
        parent.add(fontSelection);

        JPanel fontColor = colorWidget(0, 0, "text color", fontRef);
        allFields.put("fontRef", fontRef);
        parent.add(fontColor);
        return parent;
    }

    private void lineSideSelected(JComboBox<XmlValue> lineSideSelection) {
        int selection = lineSideSelection.getSelectedIndex();
        for (int i = 0; i < lineSidePanels.size(); i++) {
            lineSidePanels.get(i).setVisible(i == selection);
        }
    }

    private void setBordersWidget(boolean insideHCheck, boolean insideVCheck) {

        // Placeholder for JComboBox that's created further down
        JPanel placeholder = new JPanel();
        widget.add(placeholder, 0);

        int sidescounter = 0;
        ArrayList<XmlValue> tempNames = new ArrayList<>();

        for (int i = 0; i < XmlValue.LINESIDES.length; i++) {

            if ((!insideHCheck && i == 4) || (!insideVCheck && i == 5)) {
                continue;
            }

            XmlValue side = XmlValue.LINESIDES[i];
            String sideName = side.getAttributeValue();
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
        targetMap.put("line width", lineOne);
        linePanel.add(lineOne);

        JComboBox<XmlValue> lineStyle = lineStyleSelection();
        targetMap.put("line style", lineStyle);
        linePanel.add(lineStyle);

        JPanel lineColor = colorWidget(0, 0, "line color", targetMap);
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
        targetMap.put(lineText, fontSelectBox);
        fontPanel.add(fontSelectBox);

        JComboBox<XmlValue> textStyleBox = textStyleSelection();
        targetMap.put("text style", textStyleBox);
        fontPanel.add(textStyleBox);

        return fontPanel;
    }

    private JPanel colorWidget(int posX, int posY, String lineText, HashMap<String, JComboBox<XmlValue>> targetMap) {

        JPanel colorPanel = mainWindow.newPanel(posX, posY, 0, 0, PANELWIDTH, 30);

        JLabel colorLabel = mainWindow.newLabel(0, 0, "");
        colorLabel.setText(lineText);
        colorLabel.setBounds(0, 0, 100, 30);
        colorPanel.add(colorLabel);

        String previewID = UUID.randomUUID().toString();
        colorPreview = mainWindow.newPanel(1, 1, 1, 1, 15, 15);
        previewFields.put(previewID, colorPreview);

        JComboBox<XmlValue> colorSelectBox = colorSelection();
        colorSelectBox.addActionListener(select -> {
            XmlValue boxSelection = (XmlValue) colorSelectBox.getSelectedItem();
            HashMap<String, String> themeColors = CustClrTool.newpres.getSelectedThemeColors();
            assert boxSelection != null;
            String selectedColor = themeColors.get(boxSelection.getAttributeValue());
            JPanel previewPanel = previewFields.get(previewID);
            if (selectedColor == null) {
                // "No Color" selected
                previewPanel.setBackground(new Color(255, 255, 255));
            } else {
                previewPanel.setBackground(Color.decode("#" + selectedColor.substring(selectedColor.indexOf(":") + 1)));
            }
            colorPanel.repaint();
        });

        targetMap.put(lineText, colorSelectBox);
        colorPanel.add(colorSelectBox);
        colorPanel.add(colorPreview);

        return colorPanel;
    }

    private JPanel divider() {
        return mainWindow.newPanel(1, 0, 0, 0, PANELWIDTH, 10);
    }

    private JComboBox<XmlValue> lineWidthSelection() {
        return new JComboBox<>(XmlValue.LINEWIDTHS);
    }

    private JComboBox<XmlValue> lineStyleSelection() {
        return new JComboBox<>(XmlValue.LINETYPES);
    }

    private JComboBox<XmlValue> fontSelection() {
        return new JComboBox<>(XmlValue.THEMEFONTS);
    }

    private JComboBox<XmlValue> textStyleSelection() {
        return new JComboBox<>(XmlValue.TEXTSTYLE);
    }

    private JComboBox<XmlValue> colorSelection() {
        return new JComboBox<>(XmlValue.THEMECOLORS);
    }

    public void showSettingsField(boolean visible) {
        this.widget.setVisible(visible);
    }

    public static String[] getThemeColors() {
        String[] themeColorNames = new String[12];
        for (int i = 0; i < 12; i++) {
            themeColorNames[i] = XmlValue.THEMECOLORS[i].getAttributeValue();
        }
        return themeColorNames;
    }

    public HashMap<String, HashMap<String, JComboBox<XmlValue>>> getCollectedValues() {
        return allFields;
    }

    // TODO for debugging
    public void printAllValues() {
        for (String name : allFields.keySet()) {
            System.out.println("----------- " + name + " -----------");
            HashMap<String, JComboBox<XmlValue>> currentfield = allFields.get(name);
            for (String field : currentfield.keySet()) {
                JComboBox<XmlValue> comboBox = currentfield.get(field);
                XmlValue current = (XmlValue) comboBox.getSelectedItem();
                assert current != null;
                System.out.printf("%s: %s\n", field, current.getAttributeValue());
            }

        }
    }
}