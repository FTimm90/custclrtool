package scr.custclr;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.swing.SwingUtilities;

import scr.colorfield.colorfield;
import scr.gui.mainWindow;
import scr.presentation.presentation;

public class CustClrTool {
    
    public static presentation newpres;
    public static mainWindow mainGUI;
        public static void main(String[] args) {
    
            SwingUtilities.invokeLater(() -> {
                mainGUI = new mainWindow();
            });
    
            // // Need to update path to new extension
            // String source = changeToZip(filePath).toString();
            // List<List<List<String[]>>> themes = presentation.extractThemes(source);
            // // printThemesList(themes);
    
            // // TO-DO -> replace with user selection
            // List<String[]> hardCodedThemeSelection = themes.get(0).get(0);
            
            // try {
            //     presentation.writeZipOutput(source, hardCodedThemeSelection, testpres.fileName, testpres.filePath);
            // } catch (FileNotFoundException | ParserConfigurationException | SAXException | TransformerException e) {
            //     e.printStackTrace();
            // }
    
            // Initialize new colorfield
            colorfield colorField = new colorfield();
    
            // Mess with class variables
            //        colorField.printClassVariables();
            //        colorField.colorHex = "#FFFFFF";
            //        colorField.colorName = "CreamyGreeny";
            //        colorField.printClassVariables();
    
            // Initialize new array
            // colorfield[] colorfields = new colorfield[50];
    
            // Fill the new array with 50 color fields
            // String suffix = "Colorfield number: ";
            // for (int i = 0; i <= 49; i++) {
            //     colorfield newColorfield = new colorfield();
            //     newColorfield.colorName = suffix + (i + 1);
            //     colorfields[i] = newColorfield;
            // }
    
            // Print out content of the array
            //        for (colorfield colorfield : colorfields) {
            //            System.out.print(colorfield.colorName);
            //            System.out.printf(" is %s\n", colorfield.active);
            //        }
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
    
        private static Path openPresentation() {
            // Replace with actual open file dialog
            String sourcePath = System.getProperty("user.dir") + "/src/main/resources";
            String source = sourcePath + "/test.pptx";
            Path filePath = Paths.get(source);
            return filePath;
        }
    
        private static Path changeToZip(Path oldPath) {
            // Temporary method
            String oldPathCut = oldPath.toString().substring(0, oldPath.toString().lastIndexOf("."));
            Path newPath = Paths.get(oldPathCut + ".zip");
            return newPath;
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
