package scr.custclr;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

import scr.colorfield.colorfield;
import scr.gui.mainWindow;
import scr.presentation.presentation;

public class CustClrTool {
    public static void main(String[] args) {

        new mainWindow();
        
        // Define Presentation
        Path filePath = openPresentation();

        File presentationFile = new File(filePath.toString());

        if (presentationFile.isFile())
            System.out.println("File found.");
        else
            System.out.println("File not found.");

        String presentationExtension = presentation.getFileExtension(filePath.toString());
        String presentationName = presentation.getFilename(filePath.toString());
        String presentationPath = presentation.getFilePath(filePath.toString());

        // Initialize new presentation type
        presentation testpres = new presentation();
        testpres.fileExtension = presentationExtension;
        testpres.fileName = presentationName;
        testpres.filePath = presentationPath;

        // Check if it all worked
        System.out.printf("The path of the file is %s\n", testpres.filePath);
        System.out.printf("The name of the file is %s\n", testpres.fileName);

        // Change to .zip ...
        presentation.changeExtension(Paths.get(testpres.filePath), testpres.fileName, presentationExtension, 1);
        testpres.fileExtension = "zip";

        // Need to update path to new extension
        String source = changeToZip(filePath).toString();
        List<List<List<String[]>>> themes = presentation.extractThemes(source);
        // printThemesList(themes);

        // TO-DO -> replace with user selection
        List<String[]> hardCodedThemeSelection = themes.get(0).get(0);
        
        try {
            presentation.writeZipOutput(source, hardCodedThemeSelection, testpres.fileName, testpres.filePath);
        } catch (FileNotFoundException | ParserConfigurationException | SAXException | TransformerException e) {
            e.printStackTrace();
        }

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
}
