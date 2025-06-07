package scr.gui;

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JPanel;

public class tableVis {

    private final int CELLHEIGHT = 40;
    private final int CELLWIDTH = 110;
    private final int BORDERWITH = 1;

    private final int COLUMNS = 8;
    private final int ROWS = 8;

    private final Color HIGHLIGHTCOLOR = new Color(69, 76, 86);
    private final Color BACKGROUNDCOLOR = new Color(33, 37, 43);


    private JPanel cell;
    private final JPanel[][] TABLE = new JPanel[COLUMNS][ROWS];
    public JPanel tableFrame;

    public tableVis(int posX, int posY) {
        this.tableFrame = table(posX, posY);
    }

    private JPanel table(int posX, int posY) {

        int tableHeight = CELLHEIGHT * ROWS;
        int tableWidth = CELLWIDTH * COLUMNS;

        tableFrame = mainWindow.newPanel(0, 0, 0, 0, tableWidth, tableHeight);
        tableFrame.setBounds(posX, posY, tableWidth, tableHeight);
        tableFrame.setLayout(new GridLayout(ROWS, COLUMNS));

        for (int i = 0; i < COLUMNS; i++) {
            for (int j = 0; j < ROWS; j++) {
                JPanel newCell = cell();
                tableFrame.add(newCell);
                TABLE[i][j] = newCell;
            }
        }

        return tableFrame;
    }

    private JPanel cell() {

        cell = mainWindow.newPanel(BORDERWITH, BORDERWITH, BORDERWITH, BORDERWITH, CELLWIDTH, CELLHEIGHT);
        return cell;
    }

    private void setColorWholeTbl(Color color) {
        for (int i = 0; i < COLUMNS; i++) {
            for (int j = 0; j < ROWS; j++) {
                TABLE[i][j].setBackground(color);
            }
        }
    }

    private void highlightBandedRows() {
        for (int i = 0; i < COLUMNS; i++) {
            for (int j = 0; j < ROWS; j++) {
                if (i % 2 == 0) {
                    TABLE[i][j].setBackground(HIGHLIGHTCOLOR);
                }
            }
        }

    }

    private void highlightBandedColumns() {
        for (int i = 0; i < COLUMNS; i++) {
            for (int j = 0; j < ROWS; j++) {
                if (j % 2 == 0) {
                    TABLE[i][j].setBackground(HIGHLIGHTCOLOR);
                }
            }
        }
    }

    private void highlightFirstColumn() {
        for (int i = 0; i < COLUMNS; i++) {
            TABLE[i][0].setBackground(HIGHLIGHTCOLOR);
        }
    }

    private void highlightLastColumn() {
        for (int i = 0; i < COLUMNS; i++) {
            TABLE[i][ROWS - 1].setBackground(HIGHLIGHTCOLOR);
        }
    }

    private void highlightFirstRow() {
        for (int i = 0; i < ROWS; i++) {
            TABLE[0][i].setBackground(HIGHLIGHTCOLOR);
        }
    }

    private void highlightLastRow() {
        for (int i = 0; i < ROWS; i++) {
            TABLE[COLUMNS - 1][i].setBackground(HIGHLIGHTCOLOR);
        }
    }

    public void highlightElement(String tableElement) {

        setColorWholeTbl(BACKGROUNDCOLOR);

        switch (tableElement) {
            case "whole table" -> {
                setColorWholeTbl(HIGHLIGHTCOLOR);
            }
            case "banded rows (uneven)" -> {
                highlightBandedRows();
            }
            case "banded columns (uneven)" -> {
                highlightBandedColumns();
            }
            case "first column" -> {
                highlightFirstColumn();
            }
            case "last column" -> {
                highlightLastColumn();
            }
            case "first row" -> {
                highlightFirstRow();
            }
            case "last row" -> {
                highlightLastRow();
            }
        }
    }
}