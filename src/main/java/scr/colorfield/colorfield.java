package scr.colorfield;

import java.awt.Color;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import scr.gui.mainWindow;

public class colorfield {    

    public static boolean active = false;
    public JPanel widget;
    public static JCheckBox activateColorField;
    public static JTextField colorName;
    public static JTextField colorValue;
            
    private int posX;
    private int posY;
    
    public colorfield(int posX, int posY) {
        this.posX = posX;
        this.posY = posY;
        this.widget = colorFieldWidget(posX, posY);        
    }

    public String[] getColorProperties() {
        
        String[] colorProperties = new String[]{
            colorName.getText(),
            colorValue.getText()
        };
        
        return colorProperties;
    }

    private JPanel colorFieldWidget(int posX, int posY) {
        
        boolean textfieldsEditable = false;
        Color colorVal = new Color(20, 200, 20);

        // Settings
        JPanel colorfield = mainWindow.newPanel(0, 0, 0, 0, 120, 140, mainWindow.LIGHTER_BG);
        colorfield.setBounds(posX, posY, 120, 140);        

        // Items
        activateColorField = new JCheckBox();
        // activateColorField.setEnabled(false); // this needs to be deactivated for testing purposes
        activateColorField.createToolTip();
        activateColorField.setToolTipText("Activate / deactivate this colorfield.");
        colorfield.add(activateColorField);
        
        colorName = mainWindow.newTextField(textfieldsEditable, "Enter a name for the custom color if needed.", "Color name"); 
        colorfield.add(colorName);

        JPanel colorPreview = mainWindow.newPanel(1, 1, 1, 1, 20, 20, colorVal);
        colorfield.add(colorPreview);

        colorValue = mainWindow.newTextField(textfieldsEditable, "Enter color HEX Value.", "Color Value");
        colorfield.add(colorValue);
        
        return colorfield;
    }
}
