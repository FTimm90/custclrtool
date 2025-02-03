package scr.gui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import scr.colorfield.colorfield;
import scr.custclr.CustClrTool;
import scr.presentation.presentation;

public class mainWindow extends JFrame implements ActionListener {
    
    public final static Color BACKGROUND = new Color(24, 24, 24);
    public final static Color LIGHTER_BG = new Color(31, 31, 31);
    final static Color SCR_GREEN = new Color(80, 170, 100);
    final static Color TEXT_GRAY = new Color(163, 163, 163);
    final static Color ENTRY_BG = new Color(20, 20, 20);
    final static Color BORDER = new Color(36, 36, 36);

    final static Font BASE_FONT = new Font("roboto", Font.PLAIN, 12);

    public static String selectThemeFileName;
    
        JButton chooseFileButton;
        JButton applyButton;
        static JComboBox themeSelection;        
        static JPanel centerPanel;
    
        static colorfield[] colorfields;
                
        public mainWindow() {
            JFrame window = new JFrame();
    
            // Settings
            // try {
            //     UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            // } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
            //         | UnsupportedLookAndFeelException e) {
            //     e.printStackTrace();
            // }
                        
            this.setTitle("CustClr Tool");
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            this.setSize(1320, 980);
            this.setLayout(new BorderLayout());
            this.setVisible(true);
            // this.setResizable(false);
    
            // Formatting
            String IconPath = System.getProperty("user.dir") + "/src/main/resources/scr_icon.png";
            ImageIcon icon = new ImageIcon(IconPath);
            this.setIconImage(icon.getImage());
            this.getContentPane().setBackground(BACKGROUND);
    
            JPanel bottomPanel = newPanel(1, 0, 0, 0, 0, 30, LIGHTER_BG);
            this.add(bottomPanel, BorderLayout.SOUTH);
    
            centerPanel = newPanel(0, 0, 0, 0, 20, 20, BACKGROUND);
            centerPanel.setLayout(null);
            this.add(centerPanel, BorderLayout.CENTER);
    
            chooseFileButton = newButton(30, 30, "Choose File", "Click to select a file from your computer.");
            chooseFileButton.addActionListener(this);
            centerPanel.add(chooseFileButton);
    
            int row = 30;
            int column = 120;
    
            colorfields = new colorfield[50];
            
            for (int i = 0; i < 50; i++) {
                colorfield colorWidget = new colorfield(row, column);
                colorfields[i] = colorWidget;
                centerPanel.add(colorWidget.widget);
                colorWidget.activateColorField.addActionListener((ActionEvent check) -> {
                    JCheckBox cb = (JCheckBox) check.getSource();
                    if (cb.isSelected()) {
                        colorWidget.colorName.setEditable(true);
                        colorWidget.colorValue.setEditable(true);
                        System.out.println("The colorfield is ACTIVE!");
                    } else {
                        colorWidget.colorName.setEditable(false);
                        colorWidget.colorValue.setEditable(false);
                        System.out.println("The colorfield is INACTIVE!");
                    }
                });
    
                if ((i + 1) % 10 == 0) {
                    row = 30;
                    column += 145;
                } else {
                    row += 125;
                }
            }
            
            applyButton = newButton(30, 850, "Apply", "Write the custom colors into the file.");
            applyButton.addActionListener(this);
            centerPanel.add(applyButton);
    
        }
            
        private static Border createBorder(int top, int left, int bottom, int right) {
            
            Border generalBorder = BorderFactory.createMatteBorder(top, left, bottom, right, BORDER);
            
            return generalBorder;
        }
            
        public static JPanel newPanel(int borderTop,
                                        int borderLeft,
                                        int borderBottom,
                                        int borderRight,
                                        int sizeW,
                                        int sizeH,
                                        Color bgColor) {
    
            JPanel panel = new JPanel();
    
            // Settings
            panel.setPreferredSize(new Dimension(sizeW, sizeH));
    
            // Formatting
            panel.setBackground(bgColor);
            panel.setBorder(createBorder(borderTop, borderLeft, borderBottom, borderRight));        
    
            return panel;
        }
                
        private static JButton newButton(int posX, int posY, String text, String toolTip) {
    
            JButton swingButton = new JButton();
    
            // Settings
            swingButton.setBounds(posX, posY, 100, 30);
            swingButton.createToolTip();
            swingButton.setToolTipText(toolTip);
            swingButton.setRolloverEnabled(isDefaultLookAndFeelDecorated());
            swingButton.setText(text);
            swingButton.setFocusable(false);
    
            // Formatting
            swingButton.setFont(BASE_FONT);
            swingButton.setBorder(createBorder(1, 1, 1, 1));
            swingButton.setBackground(BACKGROUND);
            swingButton.setForeground(Color.white);
    
            return swingButton;
        }
            
        private static JComboBox comboBox(String[] themes) {
            
            JComboBox newCB = new JComboBox(themes);
            
            newCB.setBounds(30, 90, 200, 30);
    
            return newCB;
        }
                    
        private static void drawDropDown() {
            
            int numberOfThemes = CustClrTool.newpres.allThemes.length;
            String[] themesNumbered = new String[numberOfThemes];
            
            for (int i = 0; i < numberOfThemes; i++) {
                themesNumbered[i] = (i + 1) + ". " + CustClrTool.newpres.allThemes[i];
            }
    
            themeSelection = comboBox(themesNumbered);
            themeSelection.addActionListener(CustClrTool.mainGUI);
            centerPanel.add(themeSelection);
        }
        
        public static JTextField newTextField(boolean editable, String tooltip, String previewText) {
    
            JTextField textfield = new JTextField();
    
            // Settings
            textfield.setPreferredSize(new Dimension(100, 30));
            textfield.setText(previewText);
            textfield.setEditable(editable);
            textfield.setEnabled(false);
            textfield.setFont(BASE_FONT);
            textfield.createToolTip();
            textfield.setToolTipText(tooltip);
    
            // Formatting
            textfield.setBackground(ENTRY_BG);
            textfield.setCaretColor(Color.white);
    
            return textfield;
        }
            
        public static void fillColorFields(List<List<List<String[]>>> themes, int themeSelection) {
            List<List<String[]>> getTheme = themes.get(themeSelection);
            List<String[]> selectedTheme = getTheme.get(0);
            selectThemeFileName = selectedTheme.get(0)[0];
        // i = 1 because (0) is theme name and number
        for (int i = 1; i < selectedTheme.size(); i++) {
            String name = selectedTheme.get(i)[0];
            String color = selectedTheme.get(i)[1];
            colorfields[i - 1].activateColorField.setEnabled(true);
            colorfields[i - 1].activateColorField.setSelected(true);
            colorfields[i - 1].colorName.setText(name);
            colorfields[i - 1].changeColor(Color.decode("#" + color));
            colorfields[i - 1].colorValue.setText(color);            
        }
    }
    
    public static List<String[]> fetchColors() {
        List<String[]> fetchedColors = new ArrayList<>();
        for (int i = 0; i < colorfields.length; i++) {
            if (colorfields[i].activateColorField.isSelected()) {
                String[] addcolor = new String[2];
                addcolor[0] = colorfields[i].colorName.getText();
                addcolor[1] = colorfields[i].colorValue.getText();
                fetchedColors.add(addcolor);
            }
        }
        return fetchedColors;
    }

    public static void printAllColors() {
        // DEBUGGING
        for (int i = 0; i < colorfields.length; i++) {

            String checkBoxValue;
            if (colorfields[i].activateColorField.isSelected()) {
                checkBoxValue = "checked!";
            } else {
                checkBoxValue = "not checked!";
            }

            System.out.printf("Colorfield Nr.%d\n", i);
            System.out.printf("Color Name: %s\n", colorfields[i].colorName.getText());
            System.out.printf("Color Value: %s\n", colorfields[i].colorValue.getText());
            System.out.printf("Color Checkbox: %s\n", checkBoxValue);
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent click) {
        if (click.getSource() == chooseFileButton) {
            JFileChooser presentationSelection = new JFileChooser();
            int response = presentationSelection.showOpenDialog(null);
            if (response == JFileChooser.APPROVE_OPTION) {
                File newPresentation = new File(presentationSelection.getSelectedFile().getAbsolutePath());

                CustClrTool.createNewPresentation(
                        presentation.getFilePath(presentationSelection.getSelectedFile().getAbsolutePath()),
                        presentation.getFilename(newPresentation.toString()),
                        presentation.getFileExtension(newPresentation.toString()));
                
                CustClrTool.readPresentation();
                drawDropDown();
                repaint();
            }
        }
        else if (click.getSource() == themeSelection) {
            int selection = themeSelection.getSelectedIndex();
            for (colorfield colorfield : colorfields) {
                colorfield.activateEntry(false);
            }
            fillColorFields(CustClrTool.themes, selection);
            System.out.println(selection);
        }
        else if (click.getSource() == applyButton) {
            try {
                String filePath = CustClrTool.newpres.filePath;
                String fileName = CustClrTool.newpres.fileName;
                String zipPath = CustClrTool.newpres.zipPathString;
                
                presentation.changeExtension(Paths.get(filePath), fileName, CustClrTool.newpres.fileExtension, 1);
                presentation.writeZipOutput(zipPath, fileName, filePath);
            } catch (FileNotFoundException | ParserConfigurationException | SAXException | TransformerException ex) {
            }
        }
    }
}
