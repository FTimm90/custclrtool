package scr.colorfield;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Locale;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

import scr.custclr.CustClrTool;
import scr.gui.mainWindow;

public class colorfield implements FocusListener {

    public JPanel widget;
    public JCheckBox activateColorField;
    public JTextField colorName;
    public JTextField colorValue;
    public JPanel colorPreview;

    public int posX;
    public int posY;

    public colorfield(int posX, int posY) {

        this.posX = posX;
        this.posY = posY;
        this.widget = colorFieldWidget(posX, posY);
    }

    private JPanel colorFieldWidget(int posX, int posY) {

        boolean textfieldsEditable = false;

        // Settings
        JPanel colorfield = mainWindow.newPanel(0, 0, 0, 0, 120, 140);
        colorfield.setBounds(posX, posY, 120, 110);

        // Items
        activateColorField = new JCheckBox();
        activateColorField.createToolTip();
        activateColorField.setToolTipText("Activate / deactivate this colorfield.");
        activateColorField.setEnabled(false);
        colorfield.add(activateColorField);

        colorName = mainWindow.newTextField(textfieldsEditable, "Enter a name for the custom color if needed.",
                "Color name");
        colorName.addFocusListener(this);
        colorfield.add(colorName);

        colorPreview = mainWindow.newPanel(1, 1, 1, 1, 20, 20);
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

    public static boolean validateUserInput(String inputString, boolean hexValue) {

        inputString = inputString.trim();
        if (isAlnum(inputString)) {
            if (hexValue) {
                inputString = "#" + inputString;
                return validateHexColor(inputString);
            }
            return true;
        }

        return false;
    }

    public void activateEntry() {

        colorName.setEnabled(true);
        colorValue.setEnabled(true);
        activateColorField.setEnabled(true);
    }

    public void changeColor(Color color) {
        colorPreview.setBackground(color);
    }

    public void clearColorField() {

        colorName.setText("Color name");
        colorValue.setText("Color value");
        activateColorField.setSelected(false);
        changeColor(Color.LIGHT_GRAY);
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
                    CustClrTool.mainGUI.eventLog.setText("Please enter a valid color name.");
                }
            }
        } else if (e.getSource() == colorValue) {
            if (" ".equals(colorValue.getText()) || colorValue.getText() == null) {
                colorValue.setText("Color value");
            } else {
                boolean valid = validateUserInput(colorValue.getText(), true);
                if (!valid) {
                    colorValue.setText("Invalid");
                    CustClrTool.mainGUI.eventLog.setText("Please enter a valid HEX value.");
                } else {
                    changeColor(Color.decode("#" + colorValue.getText().trim()));
                }
            }
        }
    }
}