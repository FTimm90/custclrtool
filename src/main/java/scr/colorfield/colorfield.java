package scr.colorfield;

import java.awt.Color;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import scr.gui.mainWindow;

public class colorfield {
    
    public String colorHex = "";
    public String colorName = "";
    public boolean active = false;
    public JPanel widget;

    private int posX;
    private int posY;

    public colorfield(int posX, int posY) {
        this.posX = posX;
        this.posY = posY;
        this.widget = colorFieldWidget(posX, posY);
    }

    public void printClassVariables() {
        /* Simply prints out the values of the colorfields */
        System.out.printf("The Color Hex Value is: %s\n", colorHex);
        System.out.printf("The Color Name is: %s\n", colorName);
        System.out.printf("Is the color field active? %s\n", active);
    }

    private static JPanel colorFieldWidget(int posX, int posY) {
        
        boolean textfieldsEditable = false;
        Color colorVal = new Color(20, 200, 20);

        // Settings
        JPanel colorfield = mainWindow.newPanel(0, 0, 0, 0, 120, 140, mainWindow.LIGHTER_BG);
        colorfield.setBounds(posX, posY, 120, 140);

        // Items
        JCheckBox activateColorField = new JCheckBox();
        activateColorField.createToolTip();
        activateColorField.setToolTipText("Activate / deactivate this colorfield.");
        colorfield.add(activateColorField);

        JTextField colorName = mainWindow.newTextField(textfieldsEditable, "Enter a name for the custom color if needed.", "Color name"); 
        colorfield.add(colorName);

        JPanel colorPreview = mainWindow.newPanel(1, 1, 1, 1, 20, 20, colorVal);
        colorfield.add(colorPreview);

        JTextField colorValue = mainWindow.newTextField(textfieldsEditable, "Enter color HEX Value.", "Color Value");
        colorfield.add(colorValue);
        
        return colorfield;
    }
}
