package scr.settingsField;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import scr.gui.mainWindow;

public class settingsField {

    public JPanel widget;
            
    public int posX;
    public int posY;

    private final int PANELWIDTH = 240;
    
    public settingsField(int posX, int posY) {

        this.posX = posX;
        this.posY = posY;
        this.widget = settingsWidget(posX, posY);
    }

    private JPanel settingsWidget(int posX, int posY) {

        widget = mainWindow.newPanel(1, 1, 1, 1, 120, 140);
        widget.setBounds(posX, posY, PANELWIDTH, 180);

        JPanel firstLine = lineWidget(0, 0, "Left Border:");
        widget.add(firstLine);        

        return widget;
    }

    private JPanel lineWidget(int posX, int posY, String lineText) {
        widget = mainWindow.newPanel(posX, posY, 0, 0, PANELWIDTH, 30);
        
        JLabel leftBorder = mainWindow.newLabel(0, 0, "");
        leftBorder.setText(lineText);
        leftBorder.setBounds(0, 0, 100, 30);
        widget.add(leftBorder);
        
        JComboBox<String> lineOne = widthSelection();
        widget.add(lineOne);
        
        return widget;
    }
    
    private JComboBox<String> widthSelection() {
        
        String[] lineWidths = { "0,25pt", "0,5pt", "0,75pt", "1pt", "1,5pt", "2,25pt", "3pt", "4pt", "6pt" };
        JComboBox<String> widthSelect = mainWindow.newComboBox(lineWidths);
        return widthSelect;
    }

}
