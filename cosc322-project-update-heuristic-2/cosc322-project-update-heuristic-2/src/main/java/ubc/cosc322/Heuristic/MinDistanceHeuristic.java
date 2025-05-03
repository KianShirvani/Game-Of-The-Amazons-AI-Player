package ubc.cosc322.Heuristic;

import java.util.*;

public class MinDistanceHeuristic {

    public static int evaluateBoard(int[][] board, boolean isWhite) {
        int whiteScore = 0;
        int blackScore = 0;

        int[][] distanceToWhite = new int[10][10];
        int[][] distanceToBlack = new int[10][10];
        for (int i = 0; i < 10; i++) {
            Arrays.fill(distanceToWhite[i], Integer.MAX_VALUE);
            Arrays.fill(distanceToBlack[i], Integer.MAX_VALUE);
        }

        Queue<int[]> queue = new LinkedList<>();

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (board[i][j] == 2) {
                    queue.add(new int[]{i, j, 0});
                    distanceToWhite[i][j] = 0;
                }
            }
        }
        bfs(queue, board, distanceToWhite);

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (board[i][j] == 1) {
                    queue.add(new int[]{i, j, 0});
                    distanceToBlack[i][j] = 0;
                }
            }
        }
        bfs(queue, board, distanceToBlack);

        int arrowCount = 0;
        for (int[] row : board)
            for (int cell : row)
                if (cell == 3) arrowCount++;

        String phase;
        if (arrowCount < 15) phase = "opening";
        else if (arrowCount < 45) phase = "mid";
        else phase = "end";

        int centerControlBonus = 0;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if ((board[i][j] == 2 && isWhite) || (board[i][j] == 1 && !isWhite)) {
                    if (i >= 3 && i <= 6 && j >= 3 && j <= 6) {
                        centerControlBonus += 3;
                    }
                }
            }
        }

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (board[i][j] == 0) {
                    int whiteDist = distanceToWhite[i][j];
                    int blackDist = distanceToBlack[i][j];

                    if (whiteDist < blackDist) whiteScore += (phase.equals("end") ? 2 : 1);
                    else if (blackDist < whiteDist) blackScore += (phase.equals("end") ? 2 : 1);
                }
            }
        }

        whiteScore += evaluateQueenMobility(board, 2, phase);
        blackScore += evaluateQueenMobility(board, 1, phase);

        if (phase.equals("mid") || phase.equals("end")) {
            whiteScore += evaluateQueenTrapping(board, 1); // Trap black
            blackScore += evaluateQueenTrapping(board, 2); // Trap white
        }

        // Avoid nearly trapped situations (1-3 mobility range)
        whiteScore += evaluateImminentTraps(board, 2);
        blackScore += evaluateImminentTraps(board, 1);

        //Calculate queen spacing
        whiteScore += evaluateQueenSpacing(board, 2);
        blackScore += evaluateQueenSpacing(board, 1);

        if (!phase.equals("end")) {
            whiteScore += evaluateQueenSpacing(board, 2);
            blackScore += evaluateQueenSpacing(board, 1);
        }


        int finalScore = isWhite ? whiteScore - blackScore : blackScore - whiteScore;
        if (phase.equals("opening")) finalScore += centerControlBonus;

        return finalScore;
    }

    private static void bfs(Queue<int[]> queue, int[][] board, int[][] distances) {
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {-1, 1}, {1, -1}, {1, 1}};

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int x = current[1], y = current[0], dist = current[2];

            for (int[] dir : directions) {
                int newX = x + dir[0];
                int newY = y + dir[1];

                while (isWithinBounds(newX, newY) && board[newY][newX] == 0) {
                    if (distances[newY][newX] > dist + 1) {
                        distances[newY][newX] = dist + 1;
                        queue.add(new int[]{newY, newX, dist + 1});
                    }
                    newX += dir[0];
                    newY += dir[1];
                }
            }
        }
    }

    private static int evaluateImminentTraps(int[][] board, int queenType) {
        int dangerPenalty = 0;
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {-1, 1}, {1, -1}, {1, 1}};

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (board[i][j] == queenType) {
                    int mobility = 0;
                    for (int[] dir : directions) {
                        int x = j + dir[0];
                        int y = i + dir[1];
                        while (isWithinBounds(x, y) && board[y][x] == 0) {
                            mobility++;
                            x += dir[0];
                            y += dir[1];
                        }
                    }
                    if (mobility <= 3 && mobility > 0) {
                        dangerPenalty += (4 - mobility) * 10; // closer to trapped → bigger penalty
                    }
                }
            }
        }
        return -dangerPenalty;
    }

    public static int evaluateArrowDensity(int[][] board, int row, int col) {
        int arrowCount = 0;
        int[][] directions = {
                {-1, 0}, {1, 0}, {0, -1}, {0, 1},
                {-1, -1}, {-1, 1}, {1, -1}, {1, 1}
        };

        for (int[] dir : directions) {
            int x = col + dir[0];
            int y = row + dir[1];

            if (isWithinBounds(x, y) && board[y][x] == 3) {
                arrowCount++;
            }
        }

        return arrowCount; // higher = more dangerous
    }


    private static int evaluateQueenMobility(int[][] board, int queenType, String phase) {
        int mobilityScore = 0;
        int trappedPenalty = -20;
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {-1, 1}, {1, -1}, {1, 1}};

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (board[i][j] == queenType) {
                    int moveOptions = 0;
                    for (int[] dir : directions) {
                        int x = j + dir[0];
                        int y = i + dir[1];
                        while (isWithinBounds(x, y) && board[y][x] == 0) {
                            moveOptions++;
                            x += dir[0];
                            y += dir[1];
                        }
                    }
                    if (moveOptions == 0) mobilityScore += trappedPenalty;
                    else {
                        if (phase.equals("opening")) mobilityScore += moveOptions;
                        else if (phase.equals("mid")) mobilityScore += moveOptions * 2;
                        else mobilityScore += moveOptions * 3;
                    }
                }
            }
        }
        return mobilityScore;
    }

    private static int evaluateQueenSpacing(int[][] board, int queenType) {
        List<int[]> queens = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (board[i][j] == queenType) {
                    queens.add(new int[]{i, j});
                }
            }
        }

        int spacingPenalty = 0;
        for (int i = 0; i < queens.size(); i++) {
            for (int j = i + 1; j < queens.size(); j++) {
                int[] q1 = queens.get(i);
                int[] q2 = queens.get(j);
                int distance = Math.abs(q1[0] - q2[0]) + Math.abs(q1[1] - q2[1]); // Manhattan distance
                if (distance <= 2) spacingPenalty += 10;
                else if (distance <= 3) spacingPenalty += 5;
            }
        }

        return -spacingPenalty;
    }

    public static int evaluateQueenUrgency(int[] queen, int[][] board) {
        int x = queen[1];
        int y = queen[0];
        int mobility = 0;
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {-1, 1}, {1, -1}, {1, 1}};

        for (int[] dir : directions) {
            int dx = x + dir[0];
            int dy = y + dir[1];
            while (isWithinBounds(dx, dy) && board[dy][dx] == 0) {
                mobility++;
                dx += dir[0];
                dy += dir[1];
            }
        }

        return mobility; // lower = more urgent
    }



    private static int evaluateQueenTrapping(int[][] board, int enemyQueenType) {
        int penalty = 0;
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (board[i][j] == enemyQueenType) {
                    int mobility = 0;
                    for (int[] dir : directions) {
                        int x = j + dir[0], y = i + dir[1];
                        while (isWithinBounds(x, y) && board[y][x] == 0) {
                            mobility++;
                            x += dir[0];
                            y += dir[1];
                        }
                    }
                    if (mobility <= 2) penalty += 10;
                }
            }
        }
        return penalty;
    }

    private static boolean isWithinBounds(int x, int y) {
        return x >= 0 && x < 10 && y >= 0 && y < 10;
    }
}