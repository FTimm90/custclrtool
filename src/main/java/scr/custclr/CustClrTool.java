package scr.custclr;

import java.nio.file.Paths;
import java.util.List;

import javax.swing.SwingUtilities;

import scr.gui.mainWindow;
import scr.presentation.presentation;

public class CustClrTool {
    
    public static presentation newpres;
    public static mainWindow mainGUI;
        public static void main(String[] args) {
    
            SwingUtilities.invokeLater(() -> {
                mainGUI = new mainWindow();
            });

        }
        
        public static void readPresentation() {
            presentation.changeExtension(Paths.get(newpres.filePath), newpres.fileName, newpres.fileExtension, 1);
            String source = newpres.filePath + presentation.osPathSymbol() + newpres.fileName + ".zip";
            List<List<List<String[]>>> themes = presentation.extractThemes(source);
            printThemesList(themes);
            presentation.changeExtension(Paths.get(newpres.filePath), newpres.fileName, newpres.fileExtension, 2);
        }
        
        private static void printThemesList(List<List<List<String[]>>> themes) {
            // Mostly for debugging purposes
            for (List<List<String[]>> theme : themes) {
                System.out.println("Next Theme:");
                for (List<String[]> theme_content : theme) {
                    System.out.println("    └ theme content:");
                    for (String[] content : theme_content) {
                        System.out.println("        └ elements:");
                        for (int i = 0; i < content.length; i++) {
                            System.out.printf("             └ element %d: %s\n", i, content[i]);
                        }
                    }
                }
            }
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
