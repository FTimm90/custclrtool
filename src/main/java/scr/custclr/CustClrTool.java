package scr.custclr;

import java.nio.file.Paths;
import java.util.List;

import javax.swing.SwingUtilities;

import com.formdev.flatlaf.intellijthemes.FlatOneDarkIJTheme;

import scr.gui.mainWindow;
import scr.presentation.presentation;

public class CustClrTool {
    
    public static presentation newpres;
    public static mainWindow mainGUI;
    public static List<List<List<String[]>>> themes;
    public static void main(String[] args) {
        FlatOneDarkIJTheme.setup();
        SwingUtilities.invokeLater(() -> {
            mainGUI = new mainWindow();
        });
    }
    
    public static void readPresentation() {
        
        presentation.changeExtension(Paths.get(newpres.filePath), newpres.fileName, newpres.fileExtension, 1);
        newpres.zipPathString = newpres.filePath + presentation.osPathSymbol() + newpres.fileName + ".zip";
        themes = newpres.extractThemes(newpres.zipPathString);
        presentation.changeExtension(Paths.get(newpres.filePath), newpres.fileName, newpres.fileExtension, 2);
    }

    public static presentation createNewPresentation(String presentationPath, String presentationName,
            String presentationExtension) {
        
        newpres = new presentation();
        newpres.fileExtension = presentationExtension;
        newpres.fileName = presentationName;
        newpres.filePath = presentationPath;

        return newpres;
    }
}
