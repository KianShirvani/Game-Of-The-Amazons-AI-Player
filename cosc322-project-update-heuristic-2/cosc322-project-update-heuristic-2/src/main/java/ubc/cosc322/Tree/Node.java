package ubc.cosc322.Tree;

import java.util.ArrayList;
import java.util.List;

class Node {
    int[][] move;
    int[][] board;
    boolean isWhite;
    Node parent;
    List<Node> children = new ArrayList<>();
    int wins = 0;
    int visits = 0;
    boolean fullyExpanded = false;

    Node(Node parent, int[][] move, int[][] board, boolean isWhite) {
        this.parent = parent;
        this.move = move;
        this.board = board;
        this.isWhite = isWhite;
    }
}