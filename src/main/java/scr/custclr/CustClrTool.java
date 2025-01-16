package scr.custclr;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import scr.colorfield.colorfield;
import scr.presentation.presentation;

public class CustClrTool {
    public static void main(String[] args) {
        // Define Presentation
        String sourcePath = System.getProperty("user.dir") + "/src/main/resources";
        String source = sourcePath + "/test.pptx";
        System.out.println(source);
        Path filePath = Paths.get(source);

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
        source = sourcePath + "/test.zip";
        filePath = Paths.get(source);
        List<List<String>> themes = presentation.extractThemes(source);
        System.out.printf("The found themes are: %s\n", themes);
        try {
            // remove hard coded theme selection
            presentation.writeZipOutput(source, themes.get(0), testpres.fileName, testpres.filePath);
        } catch (FileNotFoundException e) {
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
}
