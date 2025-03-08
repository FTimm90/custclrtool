package scr.gui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import com.formdev.flatlaf.intellijthemes.FlatOneDarkIJTheme;

import scr.colorfield.colorfield;
import scr.custclr.CustClrTool;
import scr.presentation.presentation;
import scr.settingsField.settingsField;

public class mainWindow extends JFrame {
    
    final static Font BASE_FONT = new Font("roboto", Font.PLAIN, 11);

    public static String selectThemeFileName;    
    
    static JButton applyButton;
    static JButton cacheButton;
    JButton loadCacheButton;
    JButton chooseFileButton;
    static JComboBox<String> themeSelection;
    static JPanel custClrPanel;
    JPanel tablePanel;
    JPanel bottomPanel;
    JPanel rightPanel;
    JPanel centerPanel;
    JComboBox<String> tableElements;
    JLabel presentationNameLabel;
    public JLabel eventLog;
    static JTabbedPane windowTabs;
    settingsField testfield;

    static colorfield[] colorfields;
    static colorfield[] colorfieldCache;
                                
    public mainWindow() {
        JFrame window = new JFrame();

        // Settings
        try {
            UIManager.setLookAndFeel( new FlatOneDarkIJTheme() );
        } catch( Exception ex ) {
            System.err.println( "Failed to initialize LaF" );
        }
                    
        this.setTitle("CustClr Tool");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(1320, 900);
        this.setLayout(new BorderLayout());
        this.setVisible(true);

        // Items
        custClrPanel = newPanel(0, 0, 0, 0, 1000, 600);
        custClrPanel.setLayout(null);

        tablePanel = newPanel(0, 0, 0, 0, 1000, 600);
        tablePanel.setLayout(new BorderLayout());

        bottomPanel = newPanel(1, 0, 0, 0, 0, 30);
        bottomPanel.setLayout(null);
        this.add(bottomPanel, BorderLayout.SOUTH);
        
        eventLog = newLabel(10, 0, "The last action that happened.");
        bottomPanel.add(eventLog);        
        
        windowTabs = new JTabbedPane();
        windowTabs.setBounds(10, 10, 1320, 870);
        windowTabs.addTab("Custom colors", custClrPanel);
        windowTabs.addTab("Table styles", tablePanel);
        windowTabs.setEnabledAt(1, false);
        this.add(windowTabs);

        custClrPanelElements();
        tablePanelElements();
    }

    private void tablePanelElements() {
        rightPanel = newPanel(0, 1, 0, 0, 300, 0);
        rightPanel.setLayout(null);
        tablePanel.add(rightPanel, BorderLayout.EAST);

        centerPanel = newPanel(0,0, 0, 0, 0, 0);
        centerPanel.setLayout(null);
        tablePanel.add(centerPanel, BorderLayout.CENTER);

        JButton addTableButton = newButton(30, 30, "Add table", "Adds another empty custom table");
        addTableButton.addActionListener(click -> {
            testfield.getCollectedValues();
        });
        centerPanel.add(addTableButton);

        String[] elementsArray = {"whole table", "banded rows", "banded columns", "first column", "last column", "text levels"};
        tableElements = newComboBox(elementsArray);
        tableElements.setBounds(30, 30, 240, 30);
        rightPanel.add(tableElements);

        testfield = new settingsField(30, 90);
        rightPanel.add(testfield.widget);
    }

    private void custClrPanelElements() {
        chooseFileButton = newButton(30, 30, "Choose File", "Click to select a file from your computer.");
        chooseFileButton.addActionListener(click -> {
            eventFileSelection();
        });
        custClrPanel.add(chooseFileButton);

        presentationNameLabel = newLabel(150, 30, "Name of the currently loaded presentation file.");
        custClrPanel.add(presentationNameLabel);        

        buildColorFields();
        
        applyButton = newButton(30, 740, "Apply", "Write the custom colors into the file.");
        applyButton.addActionListener(click -> {
            eventApplyCustomColors();
            presentationNameLabel.setText("");
            cacheButton.setEnabled(false);
            eventLog.setText("Colors successfully modified.");
        });
        applyButton.setEnabled(false);
        custClrPanel.add(applyButton);

        cacheButton = newButton(1050, 740, "Cache colors", "Store the current custom colors to write into a different theme.");
        cacheButton.addActionListener(click -> {
            eventCacheColorFields();
            loadCacheButton.setEnabled(true);
            eventLog.setText("Current colors cached.");
        });
        cacheButton.setEnabled(false);
        custClrPanel.add(cacheButton);

        loadCacheButton = newButton(1175, 740, "Load cache", "Apply the stored custom colors to the current theme.");
        loadCacheButton.addActionListener(click -> {
            eventApplyCachedColors();
            eventLog.setText("Current colors replaced with cached colors.");
        });
        loadCacheButton.setEnabled(false);
        custClrPanel.add(loadCacheButton);
    }

    private void buildColorFields() {

        int column = 30;
        int row = 150;

        colorfields = new colorfield[50];

        for (int i = 0; i < 50; i++) {
            colorfield colorWidget = new colorfield(column, row);
            colorfields[i] = colorWidget;
            custClrPanel.add(colorWidget.widget);
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
                column = 30;
                row += 115;
            } else {
                column += 125;
            }
        }
    }
    
// On-click events

    private void eventApplyCustomColors() {
        
        try {
            String filePath = CustClrTool.newpres.getFilePath();
            String fileName = CustClrTool.newpres.getFileName();
            String zipPath = CustClrTool.newpres.getZipPathString();

            presentation.changeExtension(Paths.get(filePath), fileName, CustClrTool.newpres.getFileExtension(), 1);
            presentation.writeZipOutput(zipPath, fileName, filePath, selectThemeFileName, (inputStream, destXML, zipWrite) -> presentation.processTheme(inputStream, destXML, zipWrite));
        } catch (FileNotFoundException | ParserConfigurationException | SAXException | TransformerException ex) {
            eventLog.setText("An error occured while trying to write the colors to the theme.");
        }
    }

    private void eventFileSelection() {

        JFileChooser presentationSelection = new JFileChooser();
        FileNameExtensionFilter presentationFilter = new FileNameExtensionFilter(
    "PowerPoint files (*.pptx, *.potx)", "pptx", "potx");
        presentationSelection.setFileFilter(presentationFilter);
        int response = presentationSelection.showOpenDialog(null);
        if (response == JFileChooser.APPROVE_OPTION) {
            if (CustClrTool.newpres != null) {
                for (colorfield colorfield : colorfields) {
                    colorfield.clearColorField();
                }
                CustClrTool.newpres.clearPresentationObject();
                custClrPanel.remove(themeSelection);
                custClrPanel.revalidate();
                custClrPanel.repaint();
                themeSelection = null;
            }
            File newPresentation = new File(presentationSelection.getSelectedFile().getAbsolutePath());
            String extension = presentation.extractFileExtension(newPresentation.toString()).trim();
            String path = presentation.extractFilePath(newPresentation.getAbsolutePath());
            String name = presentation.extractFilename(newPresentation.getAbsolutePath());
            if (!"pptx".equals(extension) && !"potx".equals(extension)) {
                eventLog.setText("Invalid file type! Please select .pptx or .potx file.");
            } else {
                CustClrTool.createNewPresentation(path, extension, name);
                CustClrTool.readPresentation();
                drawDropDown();
                repaint();
                presentationNameLabel.setText(name);
                eventLog.setText("File read.");
            }
        }
    }

    private static colorfield[] eventCacheColorFields() {
        
        colorfieldCache = new colorfield[50];
        for (int i = 0; i < colorfields.length; i++) {
            colorfield colorCacheWidget = new colorfield(colorfields[i].posX, colorfields[i].posY);
            if (colorfields[i].activateColorField.isSelected()) {
                colorCacheWidget.activateColorField.setSelected(true);
            }
            colorCacheWidget.colorName.setText(colorfields[i].colorName.getText());
            colorCacheWidget.colorValue.setText(colorfields[i].colorValue.getText());
            colorCacheWidget.colorPreview.setBackground(colorfields[i].colorPreview.getBackground());
            colorfieldCache[i] = colorCacheWidget;
        }

        return colorfieldCache;
    }
    
    private static void eventApplyCachedColors() {

        for (int i = 0; i < colorfields.length; i++) {
            if (colorfieldCache[i].activateColorField.isSelected()) {
                colorfields[i].activateColorField.setSelected(true);
            }
            colorfields[i].colorName.setText(colorfieldCache[i].colorName.getText());
            colorfields[i].colorValue.setText(colorfieldCache[i].colorValue.getText());
            colorfields[i].colorPreview.setBackground(colorfieldCache[i].colorPreview.getBackground());
        }
    }
    
// UI Elements

    private static Border newBorder(int top, int left, int bottom, int right) {
        
        Border generalBorder = BorderFactory.createMatteBorder(top, left, bottom, right, Color.LIGHT_GRAY);
        
        return generalBorder;
    }
                    
    public static JPanel newPanel(int borderTop,
                                    int borderLeft,
                                    int borderBottom,
                                    int borderRight,
                                    int sizeW,
                                    int sizeH) {

        JPanel panel = new JPanel();

        // Settings
        panel.setPreferredSize(new Dimension(sizeW, sizeH));

        // Formatting
        panel.setBorder(newBorder(borderTop, borderLeft, borderBottom, borderRight));        

        return panel;
    }
                        
    private static JButton newButton(int posX, int posY, String text, String toolTip) {

        JButton button = new JButton();

        // Settings
        button.setBounds(posX, posY, 100, 30);
        button.createToolTip();
        button.setToolTipText(toolTip);
        button.setRolloverEnabled(isDefaultLookAndFeelDecorated());
        button.setText(text);
        button.setFocusable(false);

        // Formatting
        button.setFont(BASE_FONT);
        button.setBorder(newBorder(1, 1, 1, 1));

        return button;
    }
                    
    public static JComboBox<String> newComboBox(String[] themes) {

        JComboBox<String> comboBox = new JComboBox<>(themes);

        comboBox.setBounds(30, 90, 245, 30);

        return comboBox;
    }

    public static JTextField newTextField(boolean editable, String tooltip, String previewText) {

        JTextField textfield = new JTextField();

        // Settings
        textfield.setPreferredSize(new Dimension(100, 20));
        textfield.setText(previewText);
        textfield.setEditable(editable);
        textfield.setEnabled(false);
        textfield.createToolTip();
        textfield.setToolTipText(tooltip);

        // Formatting
        textfield.setFont(BASE_FONT);

        return textfield;
    }

    public static JLabel newLabel(int posX, int posY, String tooltip) {

        JLabel label = new JLabel();

        // Settings
        label.setBounds(posX, posY, 500, 30);
        label.setText("");
        label.createToolTip();
        label.setToolTipText(tooltip);
        label.setFocusable(false);

        // Formattig
        label.setFont(BASE_FONT);

        return label;
    }

// Helper Methods
    private static void drawDropDown() {
        int numberOfThemes = CustClrTool.newpres.getAllThemes().length;
        String[] themesNumbered = new String[numberOfThemes];

        for (int i = 0; i < numberOfThemes; i++) {
            themesNumbered[i] = (i + 1) + ". " + CustClrTool.newpres.getAllThemes()[i];
        }

        themeSelection = newComboBox(themesNumbered);
        themeSelection.addActionListener(select -> {
            int selection = themeSelection.getSelectedIndex();
            activateAllColorfields();
            fillColorFields(CustClrTool.themes, selection);
            // If the selected theme ID and the ID in the tableStyles.xml are identical
            // it's very likely, that the contained table styles already are custom ones.
            if (presentation.validateID(CustClrTool.themes.get(selection), CustClrTool.newpres.getTableStylesID())){
                System.out.println("ID VALID! Theme ID and Table styles ID are the same.");
            } else {
                System.out.println("ID INVALID! Theme ID and Table styles ID are not the same.");
                CustClrTool.newpres.setTableStylesXML(presentation.flushTableStyles(CustClrTool.newpres.getTableStylesXML(), selection));
            }
            windowTabs.setEnabledAt(1, true);
            cacheButton.setEnabled(true);
            applyButton.setEnabled(true);
        });
        custClrPanel.add(themeSelection);
    }
        
    public static void fillColorFields(List<List<String>> themes, int themeSelection) {
        
        List<String> selectedTheme = themes.get(themeSelection);
        
        String themeName = selectedTheme.get(1);
        int index = (themeName.lastIndexOf(":")) + 1;
        selectThemeFileName = themeName.substring(index);
        
        int fieldIndex = 0;
        // i = 2 because (0-1) is theme name and number
        for (int i = 2; i < selectedTheme.size() - 1; i++) {
            int getIndex = (selectedTheme.get(i).lastIndexOf(":")) + 1;
            if (selectedTheme.get(i).contains("custClr")) {
                String name = selectedTheme.get(i).substring(getIndex);
                colorfields[fieldIndex].colorName.setText(name);
            } else if (selectedTheme.get(i).contains("srgbClr")) {
                String color = selectedTheme.get(i).substring(getIndex);
                colorfields[fieldIndex].activateColorField.setEnabled(true);
                colorfields[fieldIndex].activateColorField.setSelected(true);
                colorfields[fieldIndex].colorName.setEditable(true);
                colorfields[fieldIndex].changeColor(Color.decode("#" + color));
                colorfields[fieldIndex].colorValue.setText(color);
                colorfields[fieldIndex].colorValue.setEditable(true);
                fieldIndex++;
            }
            
        }
    }
    
    public static List<String[]> fetchColors() {
        
        List<String[]> fetchedColors = new ArrayList<>();
        for (colorfield colorfield : colorfields) {
            if (colorfield.activateColorField.isSelected()) {
                String[] addcolor = new String[2];
                addcolor[0] = colorfield.colorName.getText().trim();
                addcolor[1] = colorfield.colorValue.getText().trim();
                fetchedColors.add(addcolor);
            }
        }

        return fetchedColors;
    }

    private static void activateAllColorfields() {
        
        for (colorfield colorfield : colorfields) {
            colorfield.activateEntry();
            colorfield.clearColorField();
        }
    }
}
