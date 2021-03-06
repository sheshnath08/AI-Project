package View;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import javax.swing.JPanel;

/**
 * Created by sheshnath on 4/10/2016.
 */


public class Board extends Observable implements Observer {
    private Vector[][] board;
    public int state[][];
    private Vector allPieces = new Vector();
    private int selectedRow = -1;
    private int selectedColumn = -1;
    private Piece selectedPiece = null;
    private int rows;
    private int columns;
    private int defaultSpeed = 10;
    private Board thisBoard;
    private JPanel display;
    protected boolean panelHasBeenResized = false;

    public Piece whitePiece[] = new RoundPiece[6];
    public Piece blackPiece[] = new RoundPiece[6];
    public Piece highlight[];

    /**
     * Creates a playing board with the given number of rows and columns.
     */
    public Board(int rows, int columns) {
        display = new DisplayPanel();
        this.rows = rows;
        this.columns = columns;
        thisBoard = this;
        // Each board location is a Vector, so that it can hold multiple pieces
        board = new Vector[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                board[i][j] = new Vector(1);
            }
        }
        display.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                selectedRow = yToRow(e.getY());
                selectedColumn = xToColumn(e.getX());
                setChanged();
                notifyObservers(new int[] { selectedRow, selectedColumn });
            }
        });        
        display.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent arg0) {
                System.out.println("Got a resize event: " + display.getVisibleRect());
                panelHasBeenResized  = true;
            }
        });
    }
    
    /**
     * Returns the JPanel on which this board is displayed.
     */
    public JPanel getJPanel() {
        return display;
    }

    /**
     * Returns the number of rows in this Board.
     */
    public int getRows() {
        return rows;
    }

    /**
     * Returns the number of columns in this Board.
     */
    public int getColumns() {
        return columns;
    }

    /**
     * Returns the piece at the given row and column in this Board, or
     */
    public Piece getPiece(int row, int column) {
        if (board[row][column].isEmpty()) {
            return null;
        }
        return (Piece) board[row][column].lastElement();
    }


    
    /**
     * Returns <code>true</code> if the given row and column on this board
     * contains no Pieces.
     */
    public boolean isEmpty(int state[][],int row, int column) {
        return state[row][column] == 0;
    }

    /**
     * Given x-y coordinates, finds and returns the topmost piece at that
     * location on this board, or null if there is no such piece.
     *
     */
    protected Piece findPiece(int x, int y) {
        return getPiece(yToRow(y), xToColumn(x));
    }



    /**
     * Given an x coordinate, determines which column it is in.
     *
     */
    public int xToColumn(int x) {
        return Math.min(columns - 1, (x * columns) / display.getWidth());
    }

    /**
     * Given a y coordinate, determines which row it is in..
     */
    public int yToRow(int y) {
        return Math.min(rows - 1, (y * rows) / display.getHeight());
    }

    /**
     * Returns the X coordinate of the left side of cells in the given column of
     * this Board.
     *
     */
    protected int columnToX(int columnNumber) {
        return (columnNumber * (display.getWidth() - 1)) / columns;
    }

    /**
     * Returns the Y coordinate of the top side of cells in the given column of
     * this Board.
     */
    protected int rowToY(int rowNumber) {
        return (rowNumber * (display.getHeight() - 1)) / rows;
    }

    /**
     * Places the given piece at the given location in this board.
     * It is possible to place more than one piece in a given board
     * location, in which case later pieces go "on top of" earlier
     * pieces.
   */
    public void place(Piece piece, int row, int column) {
        if (piece.getBoard() != null) {
            throw new IllegalArgumentException("Piece " + piece + " is already on a board");
        }
        board[row][column].add(piece);
        synchronized (allPieces) {
            allPieces.add(piece);
        }
        piece.placeHelper(this, row, column);
    }

    /**
     * Removes and returns the top piece at the given row and column on this Board.
     * 
    */
    public Piece remove(int row, int column) {
        Piece piece = getPiece(row, column);
        if (piece == null) {
            return null;
        } else {
            remove(piece);
            return piece;
        }
    }
    
    protected void changePositionOnBoard(Piece piece,
                                         int oldRow, int oldColumn,
                                         int newRow, int newColumn) {
        board[oldRow][oldColumn].remove(piece);
        board[newRow][newColumn].add(piece);
    }


    /*
    Methods to find row-sum, col-sum and diagonal sum
     */

    /*Returns row sum of pieces*/
    public int rowSum(int state[][],int selectedRow, int selectedColumn){

        int rowSum=0;
        for(int i = 0 ;i<rows;i++){
            rowSum = rowSum+ Math.abs(state[selectedRow][i]);
        }
        return rowSum;
    }

    /*Return columns sum of pieces*/
    public int colSum(int state[][],int selectedRow,int selectedColumn){
        int colSum=0;
        for(int i = 0 ;i<rows;i++){
            colSum = colSum+ Math.abs(state[i][selectedColumn]);
        }
        return colSum;
    }

    /*Retunrs sum of pieces that are present in upper diagonal*/
    public int dg1Sum(int state[][],int selectedRow,int selectedColumn){
        int dg1Sum = 0;
        dg1Sum = dg1Sum + Math.abs(state[selectedRow][selectedColumn]);
        for(int i=1;i<getRows();i++){
            if(selectedRow-i>=0 && selectedColumn - i >= 0) {
                dg1Sum = dg1Sum + Math.abs(state[selectedRow - i][selectedColumn - i]);
            }
            if(selectedRow+i<getRows() && selectedColumn +i < getRows()) {
                dg1Sum = dg1Sum + Math.abs(state[selectedRow + i][selectedColumn +i]);
            }
        }
        return dg1Sum;
    }

    /*retunrs sum of pieces that are present in lower diagonal*/
    public int dg2Sum(int state[][],int selectedRow,int selectedColumn){
        int dg2Sum = 0;
        dg2Sum = dg2Sum + Math.abs(state[selectedRow][selectedColumn]);
        for(int i=1;i<rows;i++){
            if(selectedRow-i>=0 && selectedColumn +i < getRows()) {
                dg2Sum = dg2Sum + Math.abs(state[selectedRow - i][selectedColumn + i]);
            }

            if(selectedRow+i<getRows() && selectedColumn - i >= 0) {
                dg2Sum = dg2Sum + Math.abs(state[selectedRow + i][selectedColumn -i]);
            }
        }
        return dg2Sum;
    }

    public boolean isValidMove(int state[][],int selectedRow, int selectedColumn, int row,int column){
        int colSum= colSum(state,selectedRow,selectedColumn);
        int rowSum= rowSum(state,selectedRow,selectedColumn);
        int dg1Sum = dg1Sum(state,selectedRow,selectedColumn);
        int dg2Sum = dg2Sum(state,selectedRow,selectedColumn);

        if(row<0 || row>=rows || column <0 || column>=columns || selectedRow <0 ||  selectedColumn<0){
            return false;
        }

        // checking if there is already friend piece in row,column
      if(!isEmpty(state,row,column) && state[selectedRow][selectedColumn] == state[row][column]
              && state[row][column]!=0){
            return false;
        }

        //checking if jumping over enemy piece

        if(row == selectedRow)//moving in same row
        {
            int n1 = selectedColumn+rowSum;
            int n2 = selectedColumn - rowSum;
            if(column == n2){
                for(int i=1;i<rowSum;i++){
                    if(!isEmpty(state,row,selectedColumn-i)){
                        if(state[selectedRow][selectedColumn] != state[row][selectedColumn-i]){
                            return false;
                        }
                    }
                }
                return true;
            }
            else if(column == n1){
                for(int i=1;i<rowSum;i++){
                    if(!isEmpty(state,row,selectedColumn+i)){
                        if(state[selectedRow][selectedColumn] != state[row][selectedColumn+i]){
                            return false;
                        }
                    }
                }
                return true;
            }
            else{
                return false;
            }
        }
        else if(column == selectedColumn)// moving in same column
        {
            if(row == selectedRow+colSum)
            {
                for(int i=1;i<colSum;i++){
                    if(!isEmpty(state,selectedRow+i,selectedColumn)){
                        if(state[selectedRow][selectedColumn] != state[selectedRow+i][selectedColumn]){
                            return false;
                        }
                    }
                }
                return true;
            }
            else if(row == selectedRow - colSum){
                for(int i=1;i<colSum;i++){
                    if(!isEmpty(state,selectedRow-i,selectedColumn)){
                        if(state[selectedRow][selectedColumn] != state[selectedRow-i][selectedColumn]){
                            return false;
                        }
                    }
                }
                return true;
            }
            else{
                return false;
            }
        }
        else // moving in diagonal
        {
            if(((row == selectedRow + dg1Sum) && (column == selectedColumn + dg1Sum))){
                for(int i=1;i<dg1Sum;i++){
                    if(!isEmpty(state, selectedRow+i,selectedColumn+i)){
                        if(state[selectedRow+i][selectedColumn+i] != state[selectedRow][selectedColumn]){
                            return false;
                        }
                    }
                }
                return true;
            }
            else if((row == selectedRow - dg1Sum) && (column == selectedColumn - dg1Sum)){
                for(int i=1;i<dg1Sum;i++){
                    if(!isEmpty(state, selectedRow-i,selectedColumn-i)){
                        if(state[selectedRow-i][selectedColumn-i] != state[selectedRow][selectedColumn]){
                            return false;
                        }
                    }
                }
                return true;
            }

            if((row == selectedRow + dg2Sum) && (column == selectedColumn - dg2Sum)){
                for(int i=1;i<dg2Sum;i++){
                    if(!isEmpty(state, selectedRow+i,selectedColumn-i)){
                        if(state[selectedRow][selectedColumn] != state[selectedRow+i][selectedColumn-i]){
                            return false;
                        }
                    }
                }
                return true;
            }
            else if((row == selectedRow - dg2Sum) && (column == selectedColumn +dg2Sum)){
                for(int i=1;i<dg2Sum;i++){
                    if(!isEmpty(state, selectedRow-i,selectedColumn+i)){
                        if(state[selectedRow][selectedColumn] != state[selectedRow-i][selectedColumn+i]){
                            return false;
                        }
                    }
                }
                return true;
            }
        }

        return false;
    }


    /*returns true if player with id wins*/
    public boolean isWinner(int state[][],int id){
        Set<int[]> connectedComponent = new HashSet<>();
        int r=0;
        int c = 0;
        outerloop:
        for(r=0;r<rows;r++){
            for(c = 0;c<rows;c++){
                if(state[r][c] == id){
                    break outerloop;
                }
            }
        }
        int pieceCount = getcount(state,id);
        //checking if there is olny one key left
        if(pieceCount ==1){
            return true;
        }
        try{
            findAllContiguous(state,r,c,connectedComponent,id);
        }
        catch (Exception e){
            System.out.print(state);
            return false;
        }
        return connectedComponent.size() == pieceCount;
    }

    /*Retunrs number of pieces a player with id has in state[][]*/
    private int getcount(int[][] state, int id) {
        int c =0;
        for(int i=0;i<rows;i++){
            for(int j = 0;j<rows;j++){
                if(state[i][j] == id){
                    c++;
                }
            }
        }
        return c;
    }

    /* mthod to find all the contiguous pieces*/
    void findAllContiguous(int state[][],int r,int c,Set<int[]> found, int id){

        int start[] = {r,c};
        if (!contains(found,start)) {
            found.add(start);
            for (Iterator<int[]> itr
                 = findAdjacent(state,r, c, id).iterator(); itr.hasNext();) {
                int[] next = itr.next();
                findAllContiguous(state,next[0], next[1], found,id);
            }
        }
    }

    /** Return true if SET contains PAIR. */
    static boolean contains(Set<int[]> set, int[] pair) {
        for (Iterator<int[]> itr = set.iterator(); itr.hasNext();) {
            int[] next = itr.next();
            if (next[0] == pair[0] && next[1] == pair[1]) {
                return true;
            }
        }
        return false;
    }

    /** Return true if the piece at _pieces[R][C] of THIS has
     * adjacent PIECE of the same side. */

    Set<int[]> findAdjacent(int s[][],int r, int c,int id) {
        int state[][] = new int[5][5];
        state = s.clone();
        Set<int[]> result = new HashSet<int[]>();
        if (r == 0 && c == 0) {
            if(state[0][1] == id){
                result.add(new int[]{0,1});
            }
            if(state[1][1] == id){
                result.add(new int[]{1,1});
            }
            if(state[1][0] == id){
                result.add(new int[]{1,0});
            }
        } else if (r == 0 && c == rows-1) {
            if(state[0][rows-2] == id){
                result.add(new int[]{0,rows-2});
            }
            if(state[1][rows-2] == id){
                result.add(new int[]{1,rows-2});
            }
            if(state[1][rows-1] == id){
                result.add(new int[]{1,rows-1});
            }
        } else if (r == rows-1 && c == 0) {
            if(state[rows-2][0] == id){
                result.add(new int[]{rows-2,0});
            }
            if(state[rows-1][1] == id){
                result.add(new int[]{rows-1,1});
            }
            if(state[rows-2][1] == id){
                result.add(new int[]{rows-2,1});
            }
        } else if (r == 4 && c == 4) {
            if(state[rows-1][rows-2] == id){
                result.add(new int[]{rows-1,rows-2});
            }
            if(state[rows-2][rows-2] == id){
                result.add(new int[]{rows-2,rows-2});
            }
            if(state[rows-2][rows-1] == id){
                result.add(new int[]{rows-2,rows-1});
            }
        } else if (r == 0) {
            if(state[0][c-1] == id){
                result.add(new int[]{0,c-1});
            }
            if(state[0][c+1] == id){
                result.add(new int[]{0,c+1});
            }
            if(state[1][c-1] == id){
                result.add(new int[]{1,c-1});
            }
            if(state[1][c+1] == id){
                result.add(new int[]{1,c+1});
            }
            if(state[1][c] == id){
                result.add(new int[]{1,c});
            }

        } else if (r == 4) {
            if(state[4][c-1] == id){
                result.add(new int[]{4,c-1});
            }
            if(state[4][c+1] == id){
                result.add(new int[]{4,c+1});
            }
            if(state[3][c-1] == id){
                result.add(new int[]{3,c-1});
            }
            if(state[3][c+1] == id){
                result.add(new int[]{3,c+1});
            }
            if(state[3][c] == id){
                result.add(new int[]{3,c});
            }

        } else if (c == 0) {
            if(state[r-1][0] == id){
                result.add(new int[]{r-1,0});
            }
            if(state[r+1][0] == id){
                result.add(new int[]{r+1,0});
            }
            if(state[r-1][1] == id){
                result.add(new int[]{r-1,1});
            }
            if(state[r+1][1] == id){
                result.add(new int[]{r+1,1});
            }
            if(state[r][1] == id){
                result.add(new int[]{r,1});
            }

        } else if (c == 4) {
            if(state[r-1][4] == id){
                result.add(new int[]{r-1,4});
            }
            if(state[r+1][4] == id){
                result.add(new int[]{r+1,4});
            }
            if(state[r-1][3] == id){
                result.add(new int[]{r-1,3});
            }
            if(state[r+1][3] == id){
                result.add(new int[]{r+1,3});
            }
            if(state[r][3] == id){
                result.add(new int[]{r,3});
            }
        } else {
            if(state[r-1][c-1] == id){
                result.add(new int[]{r-1,c-1});
            }
            if(state[r-1][c] == id){
                result.add(new int[]{r-1,c});
            }
            if(state[r-1][c+1] == id){
                result.add(new int[]{r-1,c+1});
            }
            if(state[r][c-1] == id){
                result.add(new int[]{r,c-1});
            }
            if(state[r][c+1] == id){
                result.add(new int[]{r,c+1});
            }
            if(state[r+1][c-1] == id){
                result.add(new int[]{r+1,c-1});
            }
            if(state[r+1][c] == id){
                result.add(new int[]{r+1,c});
            }
            if(state[r+1][c+1] == id){
                result.add(new int[]{r+1,c+1});
            }

        }
        return result;
    }


    /*This method returns arrayList with [row,column] of all the valid actions from row,column*/
    public ArrayList<int[]> getValidAction(int state[][],int row,int column){
        ArrayList<int[]> action = new ArrayList<int[]>();
        int rowSum = rowSum(state,row,column);
        int colSum = colSum(state,row,column);
        int dg1Sum = dg1Sum(state,row,column);
        int dg2Sum = dg2Sum(state,row,column);
        int move[] = new int[2]; // to store action's row,column
        //moving up
        if(isValidMove(state,row,column,row+colSum,column)){
            move[0] = row+colSum;
            move[1] = column;
        }
        else{
            move[0] = -1;
            move[1] = -1;
        }
        action.add(move);
        move = new int[2];
        //down
        if(isValidMove(state,row,column,row-colSum,column)){
            move[0] = row-colSum;
            move[1] = column;
        }
        else{
            move[0] = -1;
            move[1] = -1;
        }
        action.add(move);
        move = new int[2];
        //moving right
        if(isValidMove(state,row,column,row,column+rowSum)){
            move[0] = row;
            move[1] = column+rowSum;
        }
        else{
            move[0] = -1;
            move[1] = -1;
        }
        action.add(move);
        move = new int[2];
        // moving left
        if(isValidMove(state,row,column,row,column-rowSum)){
            move[0] = row;
            move[1] = column-rowSum;
        }
        else{
            move[0] = -1;
            move[1] = -1;
        }
        action.add(move);
        move = new int[2];
        //moving right up
        if(isValidMove(state,row,column,row+dg1Sum,column+dg1Sum)){
            move[0] = row+dg1Sum;
            move[1] = column+dg1Sum;
        }
        else{
            move[0] = -1;
            move[1] = -1;
        }
        action.add(move);
        move = new int[2];
        //left down
        if(isValidMove(state,row,column,row-dg1Sum,column-dg1Sum)){
            move[0] = row-dg1Sum;
            move[1] = column-dg1Sum;
        }
        else{
            move[0] = -1;
            move[1] = -1;
        }
        action.add(move);
        move = new int[2];
        //moving left up
        if(isValidMove(state,row,column,row-dg2Sum,column+dg2Sum)){
            move[0] = row-dg2Sum;
            move[1] = column+dg2Sum;
        }
        else{
            move[0] = -1;
            move[1] = -1;
        }
        action.add(move);
        move = new int[2];
        // moving left
        if(isValidMove(state,row,column,row+dg2Sum,column-dg2Sum)){
            move[0] = row+dg2Sum;
            move[1] = column-dg2Sum;
        }
        else{
            move[0] = -1;
            move[1] = -1;
        }
        action.add(move);

        return action;
    }

    /*function to update action of piece after every move*/
    public void updatePiecesActions(int state[][]){
        for(int i = 0; i< whitePiece.length; i++){
            if(whitePiece[i].getRow()<0){
                continue;
            }
            whitePiece[i].updateAction(state);
        }
        for(int i=0;i<blackPiece.length;i++){
            if(blackPiece[i].getRow()<0){
                continue;
            }
            blackPiece[i].updateAction(state);
        }
    }
    /**
     * Removes this piece from the board. Does nothing if the piece
     * is not, in fact, on the board.
     */
    public boolean remove(Piece piece) {
        if (piece == null || piece.getBoard() != this) {
            return false;
        }
        board[piece.getRow()][piece.getColumn()].remove(piece);
        synchronized (allPieces) {
            allPieces.remove(piece);
        }
        piece.removeHelper();
        return true;
    }
    
    /**
     * Ensures that the given piece will be drawn on top of any other pieces
     * in the same array location.
     */
    protected void moveToTop(Piece piece) {
        synchronized (allPieces) {
            allPieces.remove(piece);
            allPieces.add(piece);
        }
    }

    /**
     * Sets the default speed of movement for pieces on this board, in squares
     * per second. This value is used only for pieces that do not specify their
     * own speed.
     */
    public void setSpeed(int speed) {
        if (speed > 0)
            defaultSpeed = speed;
    }

    /**
     * Returns the default speed (in squares per second) of pieces on this
     * board.
     *
     * @return The default speed for pieces on this board.
     */
    public int getSpeed() {
        return defaultSpeed;
    }

    /**
     * Returns the current width, in pixels, of a single cell on this Board. The
     * value will change if this Board is resized.
     * @return 
     */
    protected int getCellWidth() {
        return display.getWidth() / columns;
    }

    /**
     * @return Returns the current height, in pixels, of a single cell on this
     *         Board. The value will change if this Board is resized.
     */
    protected int getCellHeight() {
        return display.getHeight() / rows;
    }

    /**
     * Determines whether the given row and column denote a legal position on
     * this Board.
     * 
     * @param row
     *        The given row number.
     * @param column
     *        The given column number.
     * @return <code>true</code> if the given row and column number represent
     *         a valid location on this board
     */

    public boolean isLegalPosition(int row, int column) {
        if (row < 0 || row >= rows)
            return false;
        if (column < 0 || column >= columns)
            return false;
        return true;
    }
    

    public void update(Observable changedPiece, Object rectangle) {
        Piece piece = (Piece)changedPiece;
        if (rectangle == null) {
            display.repaint();
        } else {
            Rectangle r = (Rectangle)rectangle;
            display.repaint(r.x, r.y, r.width, r.height);
        }
    }
    
    /**
     * Paints th1s board itself, not including the pieces.
     *  The Graphics context on which this board is painted.
     */
    public void paint(Graphics g) {
        int height = display.getHeight();
        int width = display.getWidth();
        int x, y;
        Color oldColor = g.getColor();
        Color backgroundColor = Color.gray;
        Color lineColor = new Color(132, 126, 255);

        // Fill background with solid color
        g.setColor(backgroundColor);
        g.fillRect(0, 0, display.getWidth(), display.getHeight());

        // Paint vertical lines
        g.setColor(lineColor);
        for (int i = 0; i <= columns; i++) {
            x = columnToX(i);
            g.drawLine(x, 0, x, height);
        }
        // Paint horizontal lines
        for (int i = 0; i <= rows; i++) {
            y = rowToY(i);
            g.drawLine(0, y, width, y);
        }
        // Mark selected square
        if (selectedRow >= 0) {
            g.setColor(Color.BLACK);
            g.drawRect(columnToX(selectedColumn), rowToY(selectedRow),
                       getCellWidth(), getCellHeight());
        }
        // Restore initial paint color
        g.setColor(oldColor);
    }


    /**
     * Displays the board contents (for debugging).
     */
    protected void dump() {
        System.out.println("----------- Board is " + rows + " rows, "
                           + columns + " columns.");
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                if (!board[i][j].isEmpty()) {
                    System.out.println("Board [" + i + "][" + j + "] contains:");
                    for (Iterator iter = board[i][j].iterator(); iter.hasNext();) {
                        Piece piece = (Piece) iter.next();
                        System.out.println("    " + piece.toString());
                    }
                }
            }
        }
        synchronized (allPieces) {
            System.out.println("Vector allPieces:");
            for (Iterator iter = allPieces.iterator(); iter.hasNext();) {
                Piece piece = (Piece) iter.next();
                System.out.println("    " + piece.toString());
            }
//            System.out.println("Selected piece = " + selectedPiece);
            System.out.println("----------- Pieces: ");
            for (Iterator iter = allPieces.iterator(); iter.hasNext();) {
                Piece piece = (Piece) iter.next();
                System.out.print(piece.toString());
            }
        }
    }
    
//  -------------------------------------------------- inner class DisplayPanel
    
    class DisplayPanel extends JPanel {
        
        /**
         * Repaints this Board and everything on it.
         * 
         * @param g
         *        The Graphics context on which this board is painted.
         */
        public void update(Graphics g) {
            paint(g);
        }

        /**
         * Repaints this Board and everything on it.
         * 
         * @param g
         *        The Graphics context on which this board is painted.
         */
        public void paint(Graphics g) {
            // Paint the board
            thisBoard.paint(g);
            // Paint the pieces
            synchronized (allPieces) {
                for (Iterator iter = allPieces.iterator(); iter.hasNext();) {
                    Piece piece = (Piece)iter.next();
                    piece.paint(g, piece.getRectangle());
                }
            }
        }
    } // end inner class DisplayPanel

    /**
     * Returns the row number (counting from zero) of the square
     * most recently clicked on by the mouse.
     * 
     * @return The selected row number.
     */
    public int getSelectedRow() {
        return selectedRow;
    }
    
    /**
     * Returns the column number (counting from zero) of the square
     * most recently clicked on by the mouse.
     * 
     * @return The selected column number.
     */
    public int getSelectedColumn() {
        return selectedColumn;
    }



    /**
     * Mark the given Piece as the selected one; unmark any previously
     * selected Piece.
     * 
     * @param selectedPiece The Piece to be "selected."
     */
    public void setSelectedPiece(Piece selectedPiece) {
        this.selectedPiece = selectedPiece;
    }
    
    /**
     * Returns the currently selected Piece, or <code>null</code> if
     * no Piece has been selected on this Board.
     * 
     * @return The currently selected Piece.
     */
    public Piece getSelectedPiece() {
        return selectedPiece;
    }
    
    /**
     * Mark the given square as "selected."
     * 
     * @param row The row number of the square being selected.
     * @param column The column number of the square being selected.
     */
    public void setSelectedSquare(int row, int column) {
        selectedRow = row;
        selectedColumn = column;
        display.repaint(); // needed to erase old selection lines
    }
}