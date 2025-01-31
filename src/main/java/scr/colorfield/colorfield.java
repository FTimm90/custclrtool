package scr.colorfield;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Locale;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import scr.gui.mainWindow;

public class colorfield implements FocusListener {    

    public boolean active = false;
    public JPanel widget;
    public JCheckBox activateColorField;
    public JTextField colorName;
    public JTextField colorValue;
            
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

        colorName = mainWindow.newTextField(textfieldsEditable, "Enter a name for the custom color if needed.",
                "Color name");
        colorName.addFocusListener(this);
        colorfield.add(colorName);

        JPanel colorPreview = mainWindow.newPanel(1, 1, 1, 1, 20, 20, colorVal);
        colorfield.add(colorPreview);

        colorValue = mainWindow.newTextField(textfieldsEditable, "Enter color HEX Value.", "Color value");
        colorValue.addFocusListener(this);
        colorfield.add(colorValue);

        return colorfield;
    }

    public static boolean isAlnum(String str) {
        return str.matches("^[a-zA-Z0-9 ]*$");
    }

    public static boolean validateHexColor(String hexColor) {
        try {
            Color.decode(hexColor.toUpperCase(Locale.ROOT));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    public boolean validateUserInput(String inputString, boolean hexValue) {
        inputString = inputString.trim();
        if (isAlnum(inputString)) {
            if (hexValue) {
                inputString = "#" + inputString;
                if (!validateHexColor(inputString)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void focusGained(FocusEvent e) {
        
        if (e.getSource() == colorName) {
            if ("Color name".equals(colorName.getText())) {
                colorName.setText(" ");
            }            
        } else if (e.getSource() == colorValue) {
            if ("Color value".equals(colorValue.getText())) {
                colorValue.setText(" ");
            }
        }        
    }

    @Override
    public void focusLost(FocusEvent e) {
        if (e.getSource() == colorName) {
            if (" ".equals(colorName.getText()) || colorName.getText() == null) {
                colorName.setText("Color name");
            } else {
                boolean valid = validateUserInput(colorName.getText(), false);
                if (!valid) {
                    colorName.setText("Invalid");
                }
            }
        } else if (e.getSource() == colorValue) {
            if (" ".equals(colorValue.getText()) || colorValue.getText() == null) {
                colorValue.setText("Color value");
            } else {
                boolean valid = validateUserInput(colorValue.getText(), true);
                if (!valid) {
                    colorValue.setText("Invalid");
                }
            }
        } 
    }
}
