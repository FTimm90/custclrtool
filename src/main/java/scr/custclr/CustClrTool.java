package scr.custclr;

import javax.swing.SwingUtilities;

import com.formdev.flatlaf.intellijthemes.FlatOneDarkIJTheme;

import scr.gui.mainWindow;
import scr.presentation.presentation;

// TODO
// italic/bold as buttons?
// remove parts completely?

public class CustClrTool {

    public static presentation newpres;
    public static mainWindow mainGUI;

    public static void main(String[] args) {
        FlatOneDarkIJTheme.setup();
        SwingUtilities.invokeLater(() -> {
            mainGUI = new mainWindow();
        });
    }

    public static void readPresentation() {

        presentation.changeExtension(newpres, 1);
        newpres.setZipPathString(newpres.getFilePath() + presentation.osPathSymbol() + newpres.getFileName() + ".zip");
        newpres.extractXMLData(newpres.getZipPathString());
        presentation.changeExtension(newpres, 2);
    }

    public static void createNewPresentation(String presentationPath,
                                             String presentationExtension,
                                             String presentationName) {

        newpres = new presentation();
        newpres.setFileExtension(presentationExtension);
        newpres.setFilePath(presentationPath);
        newpres.setFileName(presentationName);
    }
}