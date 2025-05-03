package ubc.cosc322.Tree;

import ubc.cosc322.Heuristic.MinDistanceHeuristic;
import java.util.*;

public class MonteCarloTree {
    private final Random random = new Random();
    private final int simulations = 10000;
    private final double explorationConstant = Math.sqrt(5);
    private final Map<Long, Integer> transpositionTable = new HashMap<>();

    public int[][] search(boolean isWhite, int[][] board) {
        Node root = new Node(null, null, board, isWhite);

        // Check if the opponent has any valid moves
        if (hasNoValidMoves(!isWhite, board)) {
            System.out.println((isWhite ? "White" : "Black") + " wins, opponent has no valid moves.");
            return board;  // No need to make a move, as the opponent has no valid moves left
        }

        // Check if we have any valid moves
        if (hasNoValidMoves(isWhite, board)) {
            System.out.println((isWhite ? "White" : "Black") + " loses, no more valid moves.");
            return board;  // No need to make a move
        }

        for (int i = 0; i < simulations; i++) {
            Node node = select(root);
            if (!node.fullyExpanded) {
                node = expand(node);
            }
            int result = simulate(node);
            backpropagation(node, result);
        }
        return bestMove(root);
    }

    private Node select(Node node) {
        while (!node.children.isEmpty()) {
            node = bestUCTChild(node);
        }
        return node;
    }

    private Node bestUCTChild(Node node) {
        return Collections.max(node.children, Comparator.comparingDouble(this::uctValue));
    }

    private double uctValue(Node node) {
        if (node.visits == 0) return Double.MAX_VALUE;

        double adaptiveC = explorationConstant / (1 + Math.log(1 + node.parent.visits));  // Reduce exploration as depth increases

        return (double) node.wins / node.visits + adaptiveC * Math.sqrt(Math.log(node.parent.visits) / node.visits);
    }


    public boolean hasNoValidMoves(boolean isWhite, int[][] board) {
        List<int[][]> possibleMoves = generatePossibleMoves(isWhite, board);
        return possibleMoves.isEmpty();
    }

    private Node expand(Node node) {
        List<int[][]> possibleMoves = generatePossibleMoves(node.isWhite, node.board);

        if (possibleMoves.isEmpty()) {
            node.fullyExpanded = true;
            return node;
        }

        // Filter unexplored moves
        Set<String> exploredMoves = new HashSet<>();
        for (Node child : node.children) {
            exploredMoves.add(Arrays.deepToString(child.move));
        }

        List<int[][]> unexploredMoves = new ArrayList<>();
        for (int[][] move : possibleMoves) {
            if (!exploredMoves.contains(Arrays.deepToString(move))) {
                unexploredMoves.add(move);
            }
        }

        if (unexploredMoves.isEmpty()) {
            node.fullyExpanded = true;
            return node;
        }

        int[][] selectedMove = unexploredMoves.get(random.nextInt(unexploredMoves.size()));
        int[][] newBoard = applyMove(node.board, selectedMove);
        Node child = new Node(node, selectedMove, newBoard, !node.isWhite);
        node.children.add(child);

        return child;
    }

    private int simulate(Node node) {
        long hash = hashBoard(node.board);

        // If we already evaluated this board, return the stored value
        if (transpositionTable.containsKey(hash)) {
            return transpositionTable.get(hash);
        }

        int value = MinDistanceHeuristic.evaluateBoard(node.board, node.isWhite);
        transpositionTable.put(hash, value);  // Store result

        return value;
    }

    private void backpropagation(Node node, int result) {
        while (node != null) {
            node.visits++;
            node.wins += (double) result / node.visits; // Normalize heuristic value
            node = node.parent;
        }
    }

    private long hashBoard(int[][] board) {
        long hash = 0;
        long prime = 31;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                hash = hash * prime + board[i][j];
            }
        }
        return hash;
    }

    private int[][] bestMove(Node root) {
        return Collections.max(root.children, Comparator.comparingDouble(n -> (double) n.wins / n.visits)).move;
    }

    public List<int[][]> generatePossibleMoves(boolean isWhite, int[][] board) {
        List<int[][]> moves = new ArrayList<>();
        List<int[]> queens = getQueens(isWhite, board);

        // Sort queens by urgency: lowest mobility first
        queens.sort(Comparator.comparingInt(q -> MinDistanceHeuristic.evaluateQueenUrgency(q, board)));

        for (int[] queenOld : queens) {
            List<int[]> validQueenMoves = getValidQueenMoves(queenOld, board);

            for (int[] queenNew : validQueenMoves) {
                List<int[]> validArrows = getValidQueenMoves(queenNew, board);
                // A queen is always allowed to shoot back to its old position
                moves.add(new int[][]{queenOld, queenNew, queenOld});
                for (int[] arrow : validArrows) {
                    moves.add(new int[][]{queenOld, queenNew, arrow});
                }
            }
        }

        moves.sort(Comparator.comparingInt((int[][] move) ->
                MinDistanceHeuristic.evaluateArrowDensity(board, move[1][0], move[1][1])
        ).reversed());

        return moves;
    }

    public int[][] applyMove(int[][] board, int[][] move) {
        int[][] newBoard = deepCopyBoard(board);
        int oldX = move[0][0], oldY = move[0][1];
        int newX = move[1][0], newY = move[1][1];
        int arrowX = move[2][0], arrowY = move[2][1];

        int queenType = newBoard[oldY][oldX];

        newBoard[oldY][oldX] = 0;
        newBoard[newY][newX] = queenType;
        newBoard[arrowY][arrowX] = 3;

        return newBoard;
    }

    public List<int[]> getQueens(boolean isWhite, int[][] board) {
        List<int[]> queens = new ArrayList<>();
        int queenType = isWhite ? 2 : 1;

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (board[i][j] == queenType) {
                    queens.add(new int[]{i, j});
                }
            }
        }
        return queens;
    }

    public List<int[]> getValidQueenMoves(int[] from, int[][] board) {
        List<int[]> validMoves = new ArrayList<>();
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {-1, 1}, {1, -1}, {1, 1}};

        for (int[] dir : directions) {
            int x = from[1] + dir[0];
            int y = from[0] + dir[1];

            while (isWithinBounds(x, y) && board[y][x] == 0) {
                validMoves.add(new int[]{y, x});
                x += dir[0];
                y += dir[1];
            }
        }
        return validMoves;
    }

    private boolean isWithinBounds(int x, int y) {
        return x >= 0 && x < 10 && y >= 0 && y < 10;
    }

    private int[][] deepCopyBoard(int[][] board) {
        int[][] copy = new int[10][10];
        for (int i = 0; i < 10; i++) {
            System.arraycopy(board[i], 0, copy[i], 0, 10);
        }
        return copy;
    }
}
