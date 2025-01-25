package scr.gui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.Border;

public class mainWindow extends JFrame {
    
    final static Color BACKGROUND = new Color(24, 24, 24);
    final static Color LIGHTER_BG = new Color(31, 31, 31);
    final static Color SCR_GREEN = new Color(80, 170, 100);
    final static Color TEXT_GRAY = new Color(163, 163, 163);
    final static Color ENTRY_BG = new Color(20, 20, 20);
    final static Color BORDER = new Color(36, 36, 36);

    final static Font BASE_FONT = new Font("roboto", Font.PLAIN, 12);

    public mainWindow() {
        JFrame window = new JFrame();
        this.setTitle("CustClr Tool");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(1080, 720);
        this.setLayout(new BorderLayout());
        this.setVisible(true);
        // this.setResizable(false);

        String IconPath = System.getProperty("user.dir") + "/src/main/resources/scr_icon.png";
        ImageIcon icon = new ImageIcon(IconPath);

        this.setIconImage(icon.getImage());
        this.getContentPane().setBackground(BACKGROUND);
        
        
        JPanel leftPanel = newPanel(0, 0, 0, 0, 250, 0);
        this.add(leftPanel, BorderLayout.WEST);

        JPanel bottomPanel = newPanel(1, 0, 0, 0, 0, 30);
        this.add(bottomPanel, BorderLayout.SOUTH);

        JPanel centerPanel = newPanel(0, 0, 0, 0, 0, 0);
        centerPanel.setLayout(new BorderLayout(20, 20));
        
        JPanel testPanel = new JPanel();      
        testPanel.setBackground(Color.cyan);
        
        centerPanel.add(testPanel, BorderLayout.WEST);
        
        this.add(centerPanel, BorderLayout.CENTER);

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
                                    int sizeH) {
        
        JPanel panel = new JPanel();
        
        panel.setBackground(BORDER);
        panel.setBorder(createBorder(borderTop, borderLeft, borderBottom, borderRight));
        panel.setPreferredSize(new Dimension(sizeW, sizeH));
        panel.getBaselineResizeBehavior();

        return panel;
    }
}
