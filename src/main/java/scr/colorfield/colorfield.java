package scr.colorfield;

public class colorfield {
    
    public String colorHex = "";
    public String colorName = "";
    public boolean active = false;

    public void printClassVariables() {
        /* Simply prints out the values of the colorfields */
        System.out.printf("The Color Hex Value is: %s\n", colorHex);
        System.out.printf("The Color Name is: %s\n", colorName);
        System.out.printf("Is the color field active? %s\n", active);
    }
}
