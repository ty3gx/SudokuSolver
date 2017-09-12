
import java.util.*;
import java.io.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * This class represents a program that takes an input file specifying a valid 
 * Sudoku puzzle and displays the original board and one solution for the Sudoku
 * puzzle if at least one solution exists.
 *
 * Two example acceptable input files are puzzle1.txt and puzzle2.txt.
 * badpuzzle.txt is an example of a puzzle which has no solution.
 * 
 * @author Tianchang Yang
 * @version November 8th, 2016
 */
public class SudokuSolver {

    private int[][] myClue;
    private int[][] mySolution;
    /**
     * Symbol used to indicate a blank grid position
     */
    public static final int BLANK = 0;
    /**
     * Overall size of the grid
     */
    public static final int DIMENSION = 9;
    /**
     * Size of a sub region
     */
    public static final int REGION_DIM = 3;

    // For debugging purposes -- see solve() skeleton.
    private Scanner kbd;
    private static final boolean DEBUG = false;

    /**
     * Run the solver. If args.length >= 1, use args[0] as the name of a file
     * containing a puzzle, otherwise, allow the user to browse for a file.
     */
    public static void main(String[] args) {
        String filename = null;
        if (args.length < 1) {
            // file dialog
            //filename = args[0];
            JFileChooser fileChooser = new JFileChooser();
            try {
                File f = new File(new File(".").getCanonicalPath());
                fileChooser.setCurrentDirectory(f);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            int retValue = fileChooser.showOpenDialog(new JFrame());

            if (retValue == JFileChooser.APPROVE_OPTION) {
                File theFile = fileChooser.getSelectedFile();
                filename = theFile.getAbsolutePath();
            } else {
                System.out.println("No file selected: exiting.");
                System.exit(0);
            }
        } else {
            filename = args[0];
        }

        SudokuSolver s = new SudokuSolver(filename);
        if (DEBUG) {
            s.print();
        }

        if (s.solve(0, 0)) {
            // Pop up a window with the clue and the solution.
            s.display();
        } else {
            System.out.println("No solution is possible.");
            System.exit(0);
        }

    }

    /**
     * Create a solver given the name of a file containing a puzzle. We expect
     * the file to contain nine lines each containing nine digits separated by
     * whitespace. A digit from {1...9} represents a given value in the clue,
     * and the digit 0 indicates a position that is blank in the initial puzzle.
     */
    public SudokuSolver(String puzzleName) {
        myClue = new int[DIMENSION][DIMENSION];
        mySolution = new int[DIMENSION][DIMENSION];
        // Set up keyboard input if we need it for debugging.
        if (DEBUG) {
            kbd = new Scanner(System.in);
        }

        File pf = new File(puzzleName);
        Scanner s = null;
        try {
            s = new Scanner(pf);
        } catch (FileNotFoundException f) {
            System.out.println("Couldn't open file.");
            System.exit(1);
        }

        for (int i = 0; i < DIMENSION; i++) {
            for (int j = 0; j < DIMENSION; j++) {
                myClue[i][j] = s.nextInt();
            }
        }

        // Copy to solution
        for (int i = 0; i < DIMENSION; i++) {
            for (int j = 0; j < DIMENSION; j++) {
                mySolution[i][j] = myClue[i][j];
            }
        }
    }

    /**
     * Starting at a given grid position, generate values for all remaining grid
     * positions that do not violate the game constraints. Recurse through every
     * row in a column, and then move to the next column, and recurse through
     * all rows in the column, so on and so forth. The method can start from any
     * given position and fill the entire Sudouku board. Return true if a
     * solution was found starting from this position, false if not.
     *
     * @param row The row of the position to begin with
     * @param col The column of the position to begin with.
     *
     * @return true if a solution was found starting from this position, false
     * if not.
     */
    public boolean solve(int row, int col) {
        // This code will print the solution array and then wait for 
        // you to type "Enter" before proceeding. Helpful for debugging.
        // Set the DEBUG constant to true at the top of the class
        // declaration to turn this on.
        if (DEBUG) {
            System.out.println("solve(" + row + ", " + col + ")");
            print();
            kbd.nextLine();
        }

        //Base case of the recursion. If all cells are successfully filled,
        //return true.
        if (isFull()) {
            return true;
        }

        //When reaches the end of the board in terms of column, start from 
        //column zero again.
        if (col >= DIMENSION) {
            return solve(row, 0);
        }

        //When reaches the end of the board in terms of rows, recurse to the 
        //next column and row zero.
        if (row >= DIMENSION) {
            return solve(0, col + 1);
        }

        //Move to next position when current position is given in the clue.
        if (myClue[row][col] != BLANK) {
            return solve(row + 1, col);
        }

        //Try solution 1 to 9 for each position, if a number is validable for 
        //current position, move to the next position. 
        for (int i = 1; i <= DIMENSION; i++) {
            mySolution[row][col] = i;
            if (positionIsSafe(row, col)) {
                if (solve(row + 1, col)) {
                    return true;
                }
            }
            mySolution[row][col] = BLANK;
        }

        //If a solution cannot be found for current position, return false.
        return false;
    }

    /**
     * Iterate through the solution array to see if every index is filled.
     * Private helper method for solve method. Return true if every position in
     * the solution is filled with number other than 0, return false otherwise.
     *
     * @return true if every position in the solution is filled, return false
     * otherwise.
     */
    private boolean isFull() {

        //iterate through the solution array to see if every index is filled 
        //with items other than 0.
        for (int i = 0; i < DIMENSION; i++) {
            for (int j = 0; j < DIMENSION; j++) {
                if (mySolution[i][j] == BLANK) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Determine if a position meets the rules of a Sudouku. That is, each digit
     * from 1 to 9 appears only once in the row, column, and region of the given
     * position. Return true if such requirement is fulfilled, return false
     * otherwise.
     *
     * @param row The row of the position to be checked.
     * @param col The column of the position to be checked.
     * @return true if each digit from 1 to 9 appears only once in the row,
     * column, and region of the given position, return false otherwise.
     */
    private boolean positionIsSafe(int row, int col) {

        //call other private helper method to check if current row, current 
        //column, and current region all meet the requirement.
        if (rowIsSafe(row, col) && colIsSafe(row, col)
                && regionIsSafe(row, col)) {
            return true;
        }
        return false;
    }

    /**
     * Determine if each digit from 1 to 9 appears only once in the row of the
     * given position specified by the argument integers row and column. Return 
     * true if the row meets the requirement, return false otherwise.
     * 
     * @param The row of the position to be checked.
     * @param col The column of the position to be checked.
     * @return true if each digit from 1 to 9 appears only once in the row of 
     * the given position, return false otherwise.
     */
    private boolean rowIsSafe(int row, int col) {
        //create a 9-element array to store existing digits at matching indexes. 
        //(1 stores as 1 in index 0, 2 stores as 1 in index 1, and so on)
        int[] array = new int[DIMENSION];
        
        for (int i = 0; i < DIMENSION; i++) {
            if (mySolution[row][i] != BLANK
                    && i != col) {
                array[mySolution[row][i] - 1] = 1;
                //store existing digits as '1' to indicate it already exists 
                //at the matching index.
            }
        }
        
        //when the index of the digit at current position is marked as existed,
        //return false.
        if (array[mySolution[row][col] - 1] == 1) {
            return false;
        }
        return true;
    }

    /**
     * Determine if each digit from 1 to 9 appears only once in the column of 
     * the given position specified by the argument integers row and column. 
     * Return true if the column meets the requirement, return false otherwise.
     * 
     * @param The row of the position to be checked.
     * @param col The column of the position to be checked.
     * @return true if each digit from 1 to 9 appears only once in the column of 
     * the given position, return false otherwise.
     */
    private boolean colIsSafe(int row, int col) {
        int[] array = new int[DIMENSION];
        for (int i = 0; i < DIMENSION; i++) {
            if (mySolution[i][col] != BLANK
                    && i != row) {
                array[mySolution[i][col] - 1] = 1;
            }
        }
        if (array[mySolution[row][col] - 1] == 1) {
            return false;
        }
        return true;
    }

    /**
     * Determine if each digit from 1 to 9 appears only once in the region of 
     * the given position specified by the argument integers row and column. 
     * Return true if the region meets the requirement, return false otherwise.
     * 
     * @param The row of the position to be checked.
     * @param col The column of the position to be checked.
     * @return true if each digit from 1 to 9 appears only once in the region of 
     * the given position, return false otherwise.
     */
    private boolean regionIsSafe(int row, int col) {
        int[] array = new int[DIMENSION];
        
        //iterate through the region argument position is in, and store each 
        //existing digit in the matching index of the array.
        for (int i = (row / REGION_DIM) * REGION_DIM;
                i < (row / REGION_DIM) * REGION_DIM + REGION_DIM; i++) {
            for (int j = (col / REGION_DIM) * REGION_DIM;
                    j < (col / REGION_DIM) * REGION_DIM + REGION_DIM; j++) {
                if (mySolution[i][j] != BLANK) {
                    if (i != row && j != col) {
                        array[mySolution[i][j] - 1] = 1;
                    }
                }
            }
        }
        
        if (array[mySolution[row][col] - 1] == 1) {
            return false;
        }
        return true;
    }

    /**
     * Print a character-based representation of the solution array on standard
     * output.
     */
    public void print() {
        System.out.println("+---------+---------+---------+");
        for (int i = 0; i < DIMENSION; i++) {
            System.out.println("|         |         |         |");
            System.out.print("|");
            for (int j = 0; j < DIMENSION; j++) {
                System.out.print(" " + mySolution[i][j] + " ");
                if (j % REGION_DIM == (REGION_DIM - 1)) {
                    System.out.print("|");
                }
            }
            System.out.println();
            if (i % REGION_DIM == (REGION_DIM - 1)) {
                System.out.println("|         |         |         |");
                System.out.println("+---------+---------+---------+");
            }
        }
    }

    /**
     * Pop up a window containing a nice representation of the original puzzle
     * and out solution.
     */
    public void display() {
        JFrame f = new DisplayFrame();
        f.pack();
        f.setVisible(true);
    }

    /**
     * GUI display for the clue and solution arrays.
     */
    private class DisplayFrame extends JFrame implements ActionListener {

        private JPanel mainPanel;

        private DisplayFrame() {
            mainPanel = new JPanel();
            mainPanel.add(buildBoardPanel(myClue, "Clue"));
            mainPanel.add(buildBoardPanel(mySolution, "Solution"));
            add(mainPanel, BorderLayout.CENTER);

            JButton b = new JButton("Quit");
            b.addActionListener(this);
            add(b, BorderLayout.SOUTH);
        }

        private JPanel buildBoardPanel(int[][] contents, String label) {
            JPanel holder = new JPanel();
            JLabel l = new JLabel(label);
            BorderLayout b = new BorderLayout();
            holder.setLayout(b);
            holder.add(l, BorderLayout.NORTH);
            JPanel board = new JPanel();
            GridLayout g = new GridLayout(9, 9);
            g.setHgap(0);
            g.setVgap(0);
            board.setLayout(g);
            Color[] colorChoices = new Color[2];
            colorChoices[0] = Color.WHITE;
            colorChoices[1] = Color.lightGray;
            int colorIdx = 0;
            int rowStartColorIdx = 0;

            for (int i = 0; i < DIMENSION; i++) {
                if (i > 0 && i % REGION_DIM == 0) {
                    rowStartColorIdx = (rowStartColorIdx + 1) % 2;
                }
                colorIdx = rowStartColorIdx;
                for (int j = 0; j < DIMENSION; j++) {
                    if (j > 0 && j % REGION_DIM == 0) {
                        colorIdx = (colorIdx + 1) % 2;
                    }
                    JTextField t = new JTextField("" + contents[i][j]);
                    if (contents[i][j] == 0) {
                        t.setText("");
                    }
                    t.setPreferredSize(new Dimension(35, 35));
                    t.setEditable(false);
                    t.setHorizontalAlignment(JTextField.CENTER);
                    t.setBackground(colorChoices[colorIdx]);
                    board.add(t);
                }
            }
            holder.add(board, BorderLayout.CENTER);
            return holder;
        }

        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    }
}
