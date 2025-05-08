package scr.gui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
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
import javax.swing.event.ChangeEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.formdev.flatlaf.intellijthemes.FlatOneDarkIJTheme;

import scr.colorfield.colorfield;
import scr.custclr.CustClrTool;
import scr.presentation.presentation;
import scr.tableStyles.tableStyles;

public class mainWindow extends JFrame implements FocusListener {

    final static Font BASE_FONT = new Font("roboto", Font.PLAIN, 11);

    public static String selectThemeFileName;
    public static String selectThemeNumber;

    private boolean stylesHaveBeenExtracted = false;

    static JButton applyButton;
    static JButton cacheButton;
    JButton loadCacheButton;
    static JComboBox<String> themeSelection;
    /**
     * Selection (combo) box storing the names of all existing table styles.
     */
    JComboBox<String> tableSelection;
    static JPanel custClrPanel;
    JPanel tablePanel;
    JPanel bottomPanel;
    JPanel rightPanel;
    JPanel centerPanel;
    JTextField newTableName;
    tableVis tableVis;
    JLabel presentationNameLabel;
    JButton removeTableButton;
    JButton addTableButton;
    /**
     * At the very bottom of the GUI messages appear to fill the user in on what's happening.
     */
    public JLabel eventLog;
    static JTabbedPane windowTabs;
    /**
     * Stores all the table styles. These are use for constructing the output file.
     */
    List<tableStyles> tableObjects = new ArrayList<>();

    public tableStyles getTableObject(String tableName) {
        for (tableStyles tableObject : tableObjects) {
            if (tableObject.getTABLENAME().equals(tableName))
                return tableObject;
        }
        return null;
    }

    List<JComboBox<String>> tableElementSelection = new ArrayList<>();

    /**
     * Stores the settings panels in <Table Name, Settings> pairs.
     */
    HashMap<String, JPanel> settingsPanels = new HashMap<>();

    public JPanel getSettingsPanel(String panelName) {
        return settingsPanels.get(panelName);
    }

    JPanel currentSettingsPanel;
    /**
     * Upon first generation of the table selection box we need an array
     * containing something, even if it's initially empty.
     */
    String[] tableNames = new String[1];


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

        windowTabs.addChangeListener((ChangeEvent e) -> {
            eventSwitchTabs();
        });

        custClrPanelElements();
        tablePanelElements();
    }

    /**
     * Contains all UI elements for the table style tab.
     */
    private void tablePanelElements() {

        rightPanel = newPanel(0, 1, 0, 0, 300, 0);
        rightPanel.setLayout(null);
        tablePanel.add(rightPanel, BorderLayout.EAST);

        centerPanel = newPanel(0, 0, 0, 0, 0, 0);
        centerPanel.setLayout(null);
        tablePanel.add(centerPanel, BorderLayout.CENTER);

        addTableButton = newButton(30, 30, "Add table", "Adds another empty custom table");
        addTableButton.addActionListener(click -> {
            eventCreateNewTable(newTableName.getText());
            newTableName.setText("");
            tableSelection.requestFocusInWindow();
        });

        addTableButton.setEnabled(false);
        centerPanel.add(addTableButton);

        removeTableButton = newButton(850, 30, "Remove table", "Remove current table style");
        removeTableButton.addActionListener(click -> {
            eventRemoveCurrentTable();
        });
        removeTableButton.setEnabled(false);
        centerPanel.add(removeTableButton);

        newTableName = mainWindow.newTextField(true, "Enter name for a new table style.", "Table style name");
        newTableName.setBounds(200, 30, 300, 30);
        newTableName.setEnabled(true);
        newTableName.addFocusListener(this);
        centerPanel.add(newTableName);

        tableVis = new tableVis(30, 200);
        centerPanel.add(tableVis.tableFrame);

        JButton saveTableButton = newButton(30, 740, "Save tables", "Save all table styles into opened file.");
        saveTableButton.addActionListener(click -> {
            eventSaveTableStyles(CustClrTool.newpres);
        });
        centerPanel.add(saveTableButton);

        tableSelection = new JComboBox<>(tableNames);
        tableSelection.setBounds(30, 90, 200, 30);
        tableSelection.setEnabled(false);
        tableSelection.addActionListener(e -> {
            eventSwitchSelectedTable((String) tableSelection.getSelectedItem());
            setTableElementsBoxVisibility((String) tableSelection.getSelectedItem());
        });
        centerPanel.add(tableSelection);
    }

    private void drawTableSettingsCombobox(tableStyles tableObject) {

        String name = tableObject.getTABLENAME() + "Box";
        JComboBox<String> tableElements = newComboBox(tableStyles.elementsArray);
        tableElements.setBounds(30, 30, 240, 30);
        tableElements.setName(name);
        tableElements.addActionListener(select -> {
            setTableSettingsVisibility(tableObject, tableElements);
        });
        tableElementSelection.add(tableElements);
        rightPanel.add(tableElements);
    }

    /**
     * Show only the combobox (tableElements selection) for a specific table.
     */
    private void setTableElementsBoxVisibility(String tableName) {

        String boxName = tableName + "Box";
        for (int i = 0; i < tableElementSelection.size(); i++) {
            if (tableElementSelection.get(i).getName().equals(boxName)) {
                tableElementSelection.get(i).setVisible(true);
            } else {
                tableElementSelection.get(i).setVisible(false);
            }
        }
    }

    /**
     * Only show a specific settings element (tableElements) (whole table, banded rows, ...)
     */
    private void setTableSettingsVisibility(tableStyles tableObject, JComboBox<String> tableElements) {

        String selection = tableElements.getSelectedItem().toString();
        for (String settingsElement : tableObject.settingsFields.keySet()) {
            if (settingsElement.equals(selection)) {
                tableObject.settingsFields.get(settingsElement).showSettingsField(true);
            } else {
                tableObject.settingsFields.get(settingsElement).showSettingsField(false);
            }
        }
        tableVis.highlightElement(selection);
    }

    private JComboBox<String> getTableElementsBox(String boxName) {
        for (int i = 0; i < tableElementSelection.size(); i++) {
            String elementBoxName = tableElementSelection.get(i).getName();
            if (elementBoxName.equals(boxName)) {
                return tableElementSelection.get(i);
            }
        }
        return null;
    }

    // TODO for Debugging
    private static void printTableValues(tableStyles tableObject) {

        for (String currentField : tableObject.settingsFields.keySet()) {
            System.out.println("______________ " + currentField + " ______________");
            tableObject.settingsFields.get(currentField).printAllValues();
            System.out.println("");
        }
    }

    /**
     * Contains all UI elements for the Custom color tab.
     */
    private void custClrPanelElements() {

        JButton chooseFileButton = newButton(30, 30, "Choose File", "Click to select a file from your computer.");
        chooseFileButton.addActionListener(click -> {
            eventFileSelection();
        });
        custClrPanel.add(chooseFileButton);

        presentationNameLabel = newLabel(150, 30, "Name of the currently loaded presentation file.");
        custClrPanel.add(presentationNameLabel);

        buildColorFields();

        applyButton = newButton(30, 740, "Apply", "Write the custom colors into the file.");
        applyButton.addActionListener(click -> {
            eventApplyAllChanges(true, false);
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

    /**
     * Builds the 10x5 table of colorfields on the UI.
     */
    private void buildColorFields() {

        int columnDistance = 30;
        int rowDistance = 150;

        colorfields = new colorfield[50];

        for (int i = 0; i < 50; i++) {
            colorfield colorWidget = new colorfield(columnDistance, rowDistance);
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
                columnDistance = 30;
                rowDistance += 115;
            } else {
                columnDistance += 125;
            }
        }
    }

// On-click events

    /**
     * If the user wants to switch to the "tablestyles tab"
     * the table styles ID is matched with the theme ID.
     */
    private void eventSwitchTabs() {

        int selectedIndex = windowTabs.getSelectedIndex();

        if (selectedIndex == 0) {
//            System.out.println("Custom colors tab selected");
        } else if (selectedIndex == 1) {
//            System.out.println("Table styles tab selected");
            // Check if theme ID matches table styles ID. If not, match it.
            int selection = themeSelection.getSelectedIndex();
            if (!presentation.validateID(CustClrTool.newpres.getThemeID(selection),
                    CustClrTool.newpres.getTableStylesID())) {
                CustClrTool.newpres.setTableStylesXML(presentation.matchIDs(CustClrTool.newpres, selection));
                eventLog.setText("IDs matched.");
                if (tableStyles.hasExistingStyles(CustClrTool.newpres.getTableStylesXML())) {
                    // If there are table styles, but the IDs do not match it's best to remove them.
                    Document tableStylesDoc = CustClrTool.newpres.getTableStylesXML();
                    Element tableStyleNodes = tableStylesDoc.getDocumentElement();

                    // Flush <a:tblStyleLst>
                    NodeList childNodes = tableStyleNodes.getChildNodes();
                    while (childNodes.getLength() > 0) {
                        tableStyleNodes.removeChild(childNodes.item(0));
                    }

                    eventLog.setText("Existing table styles removed.");
                }
            }

            if (tableStyles.hasExistingStyles(CustClrTool.newpres.getTableStylesXML())
                && !stylesHaveBeenExtracted) {
                // Extract existing table styles if present.
                tableStyles.extractExistingTableStyles(CustClrTool.newpres.getTableStylesXML());
                tableStyles firstTable = tableObjects.get(0);
                setTableSettingsVisibility(firstTable, getTableElementsBox(firstTable.getTABLENAME() + "Box"));
                tableSelection.setSelectedIndex(0);
                eventLog.setText("Existing table styles read.");
                stylesHaveBeenExtracted = true;
            }
        }
    }

    private void eventRemoveCurrentTable() {

        String currentPanelName = (String) tableSelection.getSelectedItem();
        JPanel currentPanel = settingsPanels.get(currentPanelName);

        tableSelection.remove(currentPanel);
        for (tableStyles style : tableObjects) {
            if (style.getTABLENAME().equals(currentPanelName)) {
                tableObjects.remove(style);
                break;
            }
        }
        rightPanel.remove(currentPanel);
        updateTableSelectionBox();

        if (!tableObjects.isEmpty()) {
            // If a table is removed and there are still entries left,
            // just select the first one to make sure a valid entry is selected.
            String firstObject = tableObjects.get(0).getTABLENAME();
            tableSelection.setSelectedItem(firstObject);
            eventSwitchSelectedTable((String) tableSelection.getSelectedItem());
            setTableElementsBoxVisibility((String) tableSelection.getSelectedItem());
        } else {
            removeTableButton.setEnabled(false);
        }
        eventLog.setText("Removed table " + currentPanelName + ".");
    }

    /**
     * Takes all table style objects, appends them to the table styles XML
     * and saves the changes into the file.
     * @param presentation
     */
    private void eventSaveTableStyles(presentation presentation) {

        String themeID = CustClrTool.newpres.getThemeID(getSelectedTheme());

        for (tableStyles table : tableObjects) {
            Node filledTemplate = tableStyles.fillTableTemplate(table, themeID);
            org.w3c.dom.Document tableStylesFile = presentation.getTableStylesXML();
            appendTemplate(tableStylesFile, filledTemplate);
            presentation.setTableStylesXML(tableStylesFile);
            eventApplyAllChanges(false, true);
        }
    }

    /**
     * Runs through all of the filled template and appends it to the tableStyles.xml file
     * @param tableStylesFile
     * @param filledTemplate
     */
    private void appendTemplate(Document tableStylesFile, Node filledTemplate) {

        Node styleListNode = scr.presentation.presentation.findNode(tableStylesFile, "a:tblStyleLst");
        Node importedTemplate = tableStylesFile.importNode(filledTemplate, true);
        styleListNode.appendChild(importedTemplate);
    }

    public void eventCreateNewTable(String tableName) {

        // We only want the button to be active if the textfield actually has been changed
        addTableButton.setEnabled(false);

        // create new table object & fill settings
        tableStyles newTable = new tableStyles(tableName);
        JPanel newTableSettings = newTable.createSettingsFields();
        newTableSettings.setName(tableName);
        settingsPanels.put(tableName, newTableSettings);
        tableObjects.add(newTable);
        rightPanel.add(newTableSettings);
        newTableSettings.setVisible(false);

        // replace current settings field with newly created one
        currentSettingsPanel = newTableSettings;

        rightPanel.revalidate();
        rightPanel.repaint();

        tableSelection.setEnabled(true);
        updateTableSelectionBox();
        drawTableSettingsCombobox(newTable);
        // makes sense to have the newly created table selected
        tableSelection.setSelectedItem(newTableName.getText());

        removeTableButton.setEnabled(true);

        eventLog.setText("Created new table " + newTableName.getText() + ".");
    }

    /**
     * Updates the content of the table selection combobox if a new one is added.
     */
    private void updateTableSelectionBox() {

        tableNames = new String[0];
        int tableCount = tableObjects.size();
        tableNames = new String[tableCount];
        for (int i = 0; i < tableCount; i++) {
            tableNames[i] = tableObjects.get(i).getTABLENAME();
        }
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(tableNames);
        tableSelection.setModel(model);
    }

    /**
     * Show selected table, hide all others.
     */
    public void eventSwitchSelectedTable(String selectedTable) {

        for (tableStyles table : tableObjects) {
            JPanel settingsPanel = table.settingsElements;
            if (table.getTABLENAME().equals(selectedTable)) {
                if (settingsPanel.getParent() == null) {
                    rightPanel.add(settingsPanel);
                }
                table.settingsElements.setVisible(true);
            } else {
                table.settingsElements.setVisible(false);
            }
        }
    }

    private void eventApplyAllChanges(boolean customColors, boolean tableStyles) {

        try {
            presentation.changeExtension(CustClrTool.newpres, 1);
            presentation.writeZipOutput(CustClrTool.newpres, selectThemeNumber, (inputStream, destXML, zipWrite) -> presentation.processTheme(inputStream, destXML, zipWrite), customColors, tableStyles);
        } catch (FileNotFoundException | ParserConfigurationException | SAXException | TransformerException ex) {
            eventLog.setText("An error occured while trying to write changes to the file.");
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
                presentation.clearPresentationObject(CustClrTool.newpres);
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

    /**
     * Save current color fields in memory to apply to a different theme or file.
     */
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

    /**
     * Apply colors cached to the current theme or file.
     */
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
        return BorderFactory.createMatteBorder(top, left, bottom, right, Color.LIGHT_GRAY);
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

    public int getSelectedTheme() {
        return themeSelection.getSelectedIndex();
    }

    private static void drawDropDown() {

        int numberOfThemes = CustClrTool.newpres.getThemeDataList().size();
        String[] themesNumbered = new String[numberOfThemes];

        for (int i = 0; i < numberOfThemes; i++) {
            String currentThemeName = CustClrTool.newpres.getThemeNames(i);
            themesNumbered[i] = (i + 1) + ". " + currentThemeName;
        }

        themeSelection = newComboBox(themesNumbered);
        themeSelection.addActionListener(select -> {
            int selection = themeSelection.getSelectedIndex();
            activateAllColorfields();
            fillColorFields(CustClrTool.newpres, selection);
            windowTabs.setEnabledAt(1, true);
            cacheButton.setEnabled(true);
            applyButton.setEnabled(true);
        });
        custClrPanel.add(themeSelection);
    }

    /**
     * Fills the UI Colorfields with the colors from selected theme.
     * @param currentPresentation
     * @param themeSelection
     */
    public static void fillColorFields(presentation currentPresentation, int themeSelection) {

        // Storing this in class variable
        selectThemeFileName = currentPresentation.getThemeNames(themeSelection);
        selectThemeNumber = currentPresentation.getThemeNumber(themeSelection);

        List<String> selectThemeCustClr = currentPresentation.getThemeCustClr(themeSelection);

        int fieldIndex = 0;
        for (int i = 0; i < selectThemeCustClr.size() - 1; i++) {
            int getIndex = (selectThemeCustClr.get(i).lastIndexOf(":")) + 1;
            if (selectThemeCustClr.get(i).contains("custClr")) {
                String name = selectThemeCustClr.get(i).substring(getIndex);
                colorfields[fieldIndex].colorName.setText(name);
            } else if (selectThemeCustClr.get(i).contains("srgbClr")) {
                String color = selectThemeCustClr.get(i).substring(getIndex);
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

    /**
     * Fetches all custom colors from the colorfields that are marked as active.
     */
    public static List<String[]> fetchColors() {

        List<String[]> fetchedColors = new ArrayList<>();
        for (colorfield colorfield : colorfields) {
            if (colorfield.activateColorField.isSelected()) {
                String[] addcolor = new String[2];
                addcolor[0] = colorfield.colorName.getText().trim();
                addcolor[1] = colorfield.colorValue.getText().trim().toUpperCase();
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

    @Override
    public void focusGained(FocusEvent e) {

        if (e.getSource() == newTableName) {
            if ("Enter name for a new table style.".equals(newTableName.getText()) || newTableName.getText() == null) {
                newTableName.setText("");
            }
            addTableButton.setEnabled(true);
        }
    }

    @Override
    public void focusLost(FocusEvent e) {

        if (e.getSource() == newTableName) {
            // System.out.println("New Table name field focus lost.");
        }
    }

}