package scr.custclr;

import java.nio.file.Paths;
import java.util.List;

import javax.swing.SwingUtilities;

import com.formdev.flatlaf.intellijthemes.FlatOneDarkIJTheme;

import scr.gui.mainWindow;
import scr.presentation.presentation;

// TODO
// Finish implementing theme dataclass
// Read theme colors from presentation
// Read existing tables from presentation (if necessary)
// Make table UI adjustable (for each of the 6)
// Write table into file
// Add another table

public class CustClrTool {
    
    public static presentation newpres;
    public static mainWindow mainGUI;
    public static List<List<String>> themes;
    public static void main(String[] args) {
        FlatOneDarkIJTheme.setup();
        SwingUtilities.invokeLater(() -> {
            mainGUI = new mainWindow();
        });
    }
    
    public static void readPresentation() {
        
        presentation.changeExtension(Paths.get(newpres.getFilePath()), newpres.getFileName(), newpres.getFileExtension(), 1);
        newpres.setZipPathString(newpres.getFilePath() + presentation.osPathSymbol() + newpres.getFileName() + ".zip");
        themes = newpres.extractXMLData(newpres.getZipPathString());
        presentation.changeExtension(Paths.get(newpres.getFilePath()), newpres.getFileName(), newpres.getFileExtension(), 2);
    }

    public static presentation createNewPresentation(String presentationPath,
                                                    String presentationExtension,
                                                    String presentationName) {
        
        newpres = new presentation();
        newpres.setFileExtension(presentationExtension);        
        newpres.setFilePath(presentationPath);
        newpres.setFileName(presentationName);
        return newpres;
    }
}
