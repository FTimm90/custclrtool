package scr.gui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;

import scr.custclr.CustClrTool;
import scr.presentation.presentation;

public class mainWindow extends JFrame implements ActionListener {
    
    final static Color BACKGROUND = new Color(24, 24, 24);
    final static Color LIGHTER_BG = new Color(31, 31, 31);
    final static Color SCR_GREEN = new Color(80, 170, 100);
    final static Color TEXT_GRAY = new Color(163, 163, 163);
    final static Color ENTRY_BG = new Color(20, 20, 20);
    final static Color BORDER = new Color(36, 36, 36);

    final static Font BASE_FONT = new Font("roboto", Font.PLAIN, 12);

    JButton chooseFileButton;
    static JComboBox themeSelection;
        
    static JPanel centerPanel;
        
    public mainWindow() {
        JFrame window = new JFrame();

        // Settings
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                | UnsupportedLookAndFeelException e) {
            // TODO Auto-generated catch block                e.printStackTrace();
        }            
        this.setTitle("CustClr Tool");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(1080, 720);
        this.setLayout(new BorderLayout());
        this.setVisible(true);
        // this.setResizable(false);

        // Formatting
        String IconPath = System.getProperty("user.dir") + "/src/main/resources/scr_icon.png";
        ImageIcon icon = new ImageIcon(IconPath);
        this.setIconImage(icon.getImage());
        this.getContentPane().setBackground(BACKGROUND);

        // Items
        JPanel leftPanel = newPanel(0, 0, 0, 0, 250, 0, LIGHTER_BG);
        this.add(leftPanel, BorderLayout.WEST);

        JPanel bottomPanel = newPanel(1, 0, 0, 0, 0, 30, LIGHTER_BG);
        this.add(bottomPanel, BorderLayout.SOUTH);

        centerPanel = newPanel(0, 0, 0, 0, 20, 20, BACKGROUND);
        centerPanel.setLayout(null);
        this.add(centerPanel, BorderLayout.CENTER);

        chooseFileButton = newButton(30, 30, "Choose File", "Click to select a file from your computer.");
        chooseFileButton.addActionListener(this);
        centerPanel.add(chooseFileButton);

    }
    
    private static Border createBorder(int top, int left, int bottom, int right) {
        
        Border generalBorder = BorderFactory.createMatteBorder(top, left, bottom, right, TEXT_GRAY);
        
        return generalBorder;
    }
    
    private static JPanel newPanel(int borderTop,
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

                System.out.printf("Presentation path: %s\n", CustClrTool.newpres.filePath);
                System.out.printf("Presentation name: %s\n", CustClrTool.newpres.fileName);
                System.out.printf("Presentation extension: %s\n", CustClrTool.newpres.fileExtension);

                CustClrTool.readPresentation();
                drawDropDown();
                repaint();
            }
        }
        if (click.getSource() == themeSelection) {
            int selection = themeSelection.getSelectedIndex();
            System.out.println(selection);
        }
    }
}
