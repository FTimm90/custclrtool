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
        
        JTabbedPane windowTabs = new JTabbedPane();
        windowTabs.setBounds(10, 10, 1320, 870);
        windowTabs.addTab("Custom colors", custClrPanel);
        windowTabs.addTab("Table styles", tablePanel);
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

        String[] elementsArray = {"whole table", "banded rows", "banded columns", "first column", "last column", "text levels"};
        tableElements = newComboBox(elementsArray);
        tableElements.setBounds(30, 30, 240, 30);
        rightPanel.add(tableElements);

        settingsField testfield = new settingsField(30, 90);
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
            String filePath = CustClrTool.newpres.filePath;
            String fileName = CustClrTool.newpres.fileName;
            String zipPath = CustClrTool.newpres.zipPathString;

            presentation.changeExtension(Paths.get(filePath), fileName, CustClrTool.newpres.fileExtension, 1);
            presentation.writeZipOutput(zipPath, fileName, filePath);
        } catch (FileNotFoundException | ParserConfigurationException | SAXException | TransformerException ex) {
            eventLog.setText("An error occured while trying to write the colors to the theme.");
        }
        for (colorfield colorfield : colorfields) {
            colorfield.clearColorField();
            colorfield.deactivateEntry();
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
            String extension = presentation.getFileExtension(newPresentation.toString()).trim();
            if (!"pptx".equals(extension) && !"potx".equals(extension)) {
                eventLog.setText("Invalid file type! Please select .pptx or .potx file.");
            } else {
                CustClrTool.createNewPresentation(
                    presentation.getFilePath(presentationSelection.getSelectedFile().getAbsolutePath()),
                    presentation.getFilename(newPresentation.toString()),
                    presentation.getFileExtension(newPresentation.toString()));

                CustClrTool.readPresentation();
                drawDropDown();
                repaint();
                presentationNameLabel.setText(presentation.getFilename(newPresentation.toString()));
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
        int numberOfThemes = CustClrTool.newpres.allThemes.length;
        String[] themesNumbered = new String[numberOfThemes];

        for (int i = 0; i < numberOfThemes; i++) {
            themesNumbered[i] = (i + 1) + ". " + CustClrTool.newpres.allThemes[i];
        }

        themeSelection = newComboBox(themesNumbered);
        themeSelection.addActionListener(select -> {
            int selection = themeSelection.getSelectedIndex();
            activateAllColorfields();
            fillColorFields(CustClrTool.themes, selection);
            cacheButton.setEnabled(true);
            applyButton.setEnabled(true);
        });
        custClrPanel.add(themeSelection);
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
            colorfields[i - 1].colorName.setEditable(true);
            colorfields[i - 1].changeColor(Color.decode("#" + color));
            colorfields[i - 1].colorValue.setText(color);
            colorfields[i - 1].colorValue.setEditable(true);
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
