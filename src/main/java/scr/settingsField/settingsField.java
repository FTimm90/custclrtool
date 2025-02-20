package scr.settingsField;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import scr.gui.mainWindow;

public class settingsField {

    public JPanel widget;
    JPanel fontPanel;
    JPanel linePanel;
    JPanel colorPreview;
    JComboBox<String> fontSelectBox;
    JComboBox<String> colorSelectBox;
            
    public int posX;
    public int posY;

    public JComboBox<String>[] lineSizes = new JComboBox[6];
    int lineSizeCounter = 0;

    private final int PANELWIDTH = 240;
    private final String[] SIDES = { "Left", "Right", "Top", "Bottom" };
    
    public settingsField(int posX, int posY) {

        this.posX = posX;
        this.posY = posY;
        this.widget = settingsWidget(posX, posY);
    }

    private JPanel settingsWidget(int posX, int posY) {

        widget = mainWindow.newPanel(1, 1, 1, 1, 120, 140);
        widget.setBounds(posX, posY, PANELWIDTH, 700);

        setBordersWidget();

        JPanel innerHorizontal = lineWidget(0, 0, "Inner Horizontal Border:");
        widget.add(innerHorizontal);

        JPanel innerVertical = lineWidget(0, 0, "Inner Vertical Border:");
        widget.add(innerVertical);

        JPanel fontSelection = fontWidget(0, 0, "Font:");
        widget.add(fontSelection);

        JPanel fontColor = colorWidget(0, 0, "Text color:");
        widget.add(fontColor);
        
        JPanel cellColor = colorWidget(0, 0, "Cell color");
        widget.add(cellColor);

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

        JComboBox<String> lineOne = widthSelection();        
        lineSizes[lineSizeCounter] = lineOne;
        lineSizeCounter += 1;

        linePanel.add(lineOne);

        JPanel lineColor = colorWidget(0, 0, "Line color:");
        linePanel.add(lineColor);
           
        return linePanel;
    }
    
    private JPanel fontWidget(int posX, int posY, String lineText) {

        fontPanel = mainWindow.newPanel(posX, posY, 0, 0, PANELWIDTH, 30);

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

    
    private JComboBox<String> widthSelection() {

        String[] lineWidths = { "0,25pt", "0,5pt", "0,75pt", "1pt", "1,5pt", "2,25pt", "3pt", "4pt", "6pt" };
        JComboBox<String> widthSelect = mainWindow.newComboBox(lineWidths);
        return widthSelect;
    }
    
    private JComboBox<String> fontSelection() {

        String[] fonts = { "Body text", "Headline text" };
        JComboBox<String> fontSelect = mainWindow.newComboBox(fonts);
        return fontSelect;
    }
    
    private JComboBox<String> colorSelection() {
        
        String[] themeColors = { "Light 1", "Dark 1", "Light 2", "Dark 2", "Accent 1", "Accent 2", "Accent 3", "Accent 4", "Accent 5", "Accent 6", "Link", "Followed link" };
        JComboBox<String> fontSelect = mainWindow.newComboBox(themeColors);
        return fontSelect;
    }
}
