package ubc.cosc322.Test;

import org.junit.Before;
import org.junit.Test;
import ubc.cosc322.Tree.MonteCarloTree;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class TestMonteCarloTree {

    private MonteCarloTree monteCarloTree;
    private int[][] board;

    @Before
    public void setUp() {
        monteCarloTree = new MonteCarloTree();
        board = new int[10][10];
        // Initialize the board with a sample setup
        // 1 represents black queen, 2 represents white queen, 3 represents arrow, 0 represents empty
        board[9][3] = 1;
        board[9][6] = 1;
        board[6][0] = 1;
        board[6][9] = 1;

        board[3][0] = 2;
        board[3][9] = 2;
        board[0][3] = 2;
        board[0][6] = 2;
    }

    @Test
    public void testGetWhiteQueens() {
        List<int[]> queens = monteCarloTree.getQueens(true, board);
        assertEquals(4, queens.size());
    }

    @Test
    public void testGetBlackQueens() {
        List<int[]> queens = monteCarloTree.getQueens(false, board);
        assertEquals(4, queens.size());
    }
    
    @Test
    public void testGetQueenMovesWhenQueensAreSurrounded() {
        // Surround all queens with arrows
        for(int i = 0; i < 10; i++){
            for(int j = 0; j < 10; j++){
                if(board[i][j] == 0){
                    board[i][j] = 3;
                }
            }
        }

        List<int[]> queens = monteCarloTree.getQueens(true, board);
        for(int[] queen: queens){
            List<int[]> validQueenMoves = monteCarloTree.getValidQueenMoves(queen, board);
            assertTrue(validQueenMoves.isEmpty());
        }
    }

    @Test
    public void testGeneratePossibleMovesWhenQueensAreSurrounded(){
        // Surround all queens with arrows
        for(int i = 0; i < 10; i++){
            for(int j = 0; j < 10; j++){
                if(board[i][j] == 0){
                    board[i][j] = 3;
                }
            }
        }

        List<int[][]> moves = monteCarloTree.generatePossibleMoves(true, board);
        assertTrue(moves.isEmpty());
    }

    @Test
    public void testGeneratePossibleMovesWhenQueensAreNotSurrounded(){
        List<int[][]> moves = monteCarloTree.generatePossibleMoves(true, board);
        assertFalse(moves.isEmpty());
    }

    @Test
    public void testGeneratePossibleMovesWhenWhiteQueenCanOnlyMoveDiagonally(){
        List<int[]> queens = monteCarloTree.getQueens(true, board);
        int[] queen = queens.get(0); // Bottom-left white queen;
        board[0][2] = 3;
        board[1][2] = 3;
        board[1][3] = 3;
        board[0][4] = 3;

        List<int[]> validQueenMoves = monteCarloTree.getValidQueenMoves(queen, board);
        assertEquals(5, validQueenMoves.size());
        assertEquals(1, validQueenMoves.get(0)[0]); // y-value
        assertEquals(4, validQueenMoves.get(0)[1]); // x-value
    }

    @Test
    public void testGeneratePossibleMovesWhenWhiteQueenIsCompletelySurrounded(){
        List<int[]> queens = monteCarloTree.getQueens(true, board);
        int[] queen = queens.get(0); // Bottom-left white queen;
        board[0][2] = 3;
        board[1][2] = 3;
        board[1][3] = 3;
        board[0][4] = 3;
        board[1][4] = 3;

        List<int[]> validQueenMoves = monteCarloTree.getValidQueenMoves(queen, board);
        assertTrue(validQueenMoves.isEmpty());
    }

    @Test
    public void testGeneratePossibleMovesWhenBlackQueenCanOnlyMoveHorizontallyByOneTile(){
        List<int[]> queens = monteCarloTree.getQueens(false, board);
        int[] queen = queens.get(2); // Top left black queen
        assertEquals(9, queen[0]);
        assertEquals(3, queen[1]);

        board[9][2] = 3;
        board[9][5] = 3;
        board[8][2] = 3;
        board[8][3] = 3;
        board[8][4] = 3;

        List<int[]> validQueenMoves = monteCarloTree.getValidQueenMoves(queen, board);
        assertEquals(1, validQueenMoves.size());

        // Test whether it detects that its only valid move is to go right by 1 tile
        assertEquals(9, validQueenMoves.get(0)[0]);
        assertEquals(4, validQueenMoves.get(0)[1]);
    }

    @Test
    public void testGeneratePossibleMovesWhenBlackQueenIsCompletelySurrounded(){
        List<int[]> queens = monteCarloTree.getQueens(false, board);
        int[] queen = queens.get(2); // Top left black queen
        assertEquals(9, queen[0]);
        assertEquals(3, queen[1]);

        board[9][2] = 3;
        board[9][5] = 3;
        board[8][2] = 3;
        board[8][3] = 3;
        board[8][4] = 3;
        board[9][4] = 3;

        List<int[]> validQueenMoves = monteCarloTree.getValidQueenMoves(queen, board);
        assertTrue(validQueenMoves.isEmpty());
    }
}