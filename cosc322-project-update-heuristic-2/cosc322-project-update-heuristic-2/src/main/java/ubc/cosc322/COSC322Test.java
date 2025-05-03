
package ubc.cosc322;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sfs2x.client.entities.Room;
import ubc.cosc322.Tree.MonteCarloTree;
import ygraph.ai.smartfox.games.BaseGameGUI;
import ygraph.ai.smartfox.games.GameClient;
import ygraph.ai.smartfox.games.GameMessage;
import ygraph.ai.smartfox.games.GamePlayer;
import ygraph.ai.smartfox.games.amazons.AmazonsGameMessage;


/**
 * An example illustrating how to implement a GamePlayer
 * @author Yong Gao (yong.gao@ubc.ca)
 * Jan 5, 2021
 *
 */
public class COSC322Test extends GamePlayer{

    private GameClient gameClient = null; 
    private BaseGameGUI gamegui = null;

	private int[][] board;
	
    private String userName;
    private String passwd;

	private boolean isWhite;
	
    /**
     * The main method
     * @param args for name and passwd (current, any string would work)
     */
    public static void main(String[] args) {				 
    	COSC322Test player = new COSC322Test("team11", "team11");
    	
    	if(player.getGameGUI() == null) {
    		player.Go();
    	}
    	else {
    		BaseGameGUI.sys_setup();
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                	player.Go();
                }
            });
    	}
    }
	
    /**
     * Any name and passwd 
     * @param userName
      * @param passwd
     */
    public COSC322Test(String userName, String passwd) {
    	this.userName = userName;
    	this.passwd = passwd;
    	
    	//To make a GUI-based player, create an instance of BaseGameGUI
    	//and implement the method getGameGUI() accordingly
   		this.gamegui = new BaseGameGUI(this);
    }

    @Override
    public void onLogin() {

		System.out.println("Congratualations!!! "
		+ "I am called because the server indicated that the login is successfully");
		System.out.println("The next step is to find a room and join it: "
				+ "the gameClient instance created in my constructor knows how!"); 

		List<Room> roomList = gameClient.getRoomList();
		System.out.println("The available room/roms is/are:");

		for(int i = 0; i < roomList.size(); i++){
			System.out.println(roomList.get(i).getName());
		}

//		gameClient.joinRoom(roomList.get(6).getName());

		userName = gameClient.getUserName();
		if(getGameGUI() != null){
			getGameGUI().setRoomInformation(gameClient.getRoomList());
		}
    }

    @Override
    public boolean handleGameMessage(String messageType, Map<String, Object> msgDetails) {
    	//This method will be called by the GameClient when it receives a game-related message
    	//from the server.
	
    	//For a detailed description of the message types and format, 
    	//see the method GamePlayer.handleGameMessage() in the game-client-api document. 
		switch (messageType) {
			case GameMessage.GAME_ACTION_START:
				// Check if we are white, because white does not move first
				isWhite = msgDetails.get(AmazonsGameMessage.PLAYER_WHITE).equals(getGameClient().getUserName());
				if (!isWhite){
					makeBestMove();
				}
				break;
			case GameMessage.GAME_STATE_BOARD:
				ArrayList<Integer> initialBoardArray = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.GAME_STATE);

				this.board = new int[10][10];
				for (int i = 0; i < 10; i++) {
					for (int j = 0; j < 10; j++) {
						int index = (i + 1) * 11 + (j + 1);
						board[i][j] = initialBoardArray.get(index);
					}
				}

				// Print initial board state
				printBoardState();

				getGameGUI().setGameState(initialBoardArray);
				if(isWhite){
					System.out.println("Playing as white");
				} else{
					System.out.println("Playing as black");
				}
				break;
			case GameMessage.GAME_ACTION_MOVE:
				getGameGUI().updateGameState(msgDetails);
				updateBoard(msgDetails);
				printBoardState();

				// Add delay before making the best move
//				try {
//					Thread.sleep(1000); // Delay for 1000 milliseconds (1 second)
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}

				makeBestMove();
				break;
			default:
				return false;
		}
    	    	
    	return true;   	
    }

	private void makeBestMove() {
		System.out.println("Executing makeBestMove()...");

		if(isWhite)
			System.out.println("Playing as white");
		else
			System.out.println("Playing as black");

		MonteCarloTree mcts = new MonteCarloTree();
		int[][] bestMove = mcts.search(isWhite, this.board);

		if (bestMove != null) {
			System.out.println("Best Move Found: " + (bestMove[0][0] + 1) + "," + (bestMove[0][1] + 1) + " → " + (bestMove[1][0] + 1) + "," + (bestMove[1][1] + 1));

			Map<String, Object> moveMessage = new HashMap<>();
			moveMessage.put(AmazonsGameMessage.QUEEN_POS_CURR, new ArrayList<>(Arrays.asList(bestMove[0][0] + 1, bestMove[0][1] + 1)));
			moveMessage.put(AmazonsGameMessage.QUEEN_POS_NEXT, new ArrayList<>(Arrays.asList(bestMove[1][0] + 1, bestMove[1][1] + 1)));
			moveMessage.put(AmazonsGameMessage.ARROW_POS, new ArrayList<>(Arrays.asList(bestMove[2][0] + 1, bestMove[2][1] + 1)));

			gameClient.sendMoveMessage(moveMessage);
			getGameGUI().updateGameState(moveMessage);
			updateBoard(moveMessage);
			System.out.println("Move Sent to Server: " + moveMessage);
		} else {
			System.out.println("No valid move found.");
		}
	}

	private void printBoardState() {
		if (this.board == null) {
			System.out.println("Board state is null.");
			return;
		}

		System.out.println("Current Board State:");
		for(int i = 9; i >= 0; i--){
			System.out.println(Arrays.toString(board[i]));
		}
		System.out.println();
	}

	private void updateBoard(Map<String, Object> msgDetails) {
		if (board == null) {
			System.out.println("Error: Board is null.");
			return;
		}

		// Extract move data from msgDetails
		ArrayList<Integer> queenOld = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.QUEEN_POS_CURR);
		ArrayList<Integer> queenNew = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.QUEEN_POS_NEXT);
		ArrayList<Integer> arrow = (ArrayList<Integer>) msgDetails.get(AmazonsGameMessage.ARROW_POS);

		// Convert 1-based indexing to 0-based indexing
		int oldX = queenOld.get(1) - 1, oldY = queenOld.get(0) - 1;
		int newX = queenNew.get(1) - 1, newY = queenNew.get(0) - 1;
		int arrowX = arrow.get(1) - 1, arrowY = arrow.get(0) - 1;

		// Determine if it's a Black or White queen moving
		int movingQueen = board[oldY][oldX]; // Get the piece at old position

//		if (movingQueen != 1 && movingQueen != 2) {
//			System.out.println("Error: No valid queen at " + (oldX + 1) + ", " + (oldY + 1));
//			return;
//		}

		// Move the queen to new position
		board[newY][newX] = movingQueen;  // Move queen
		board[oldY][oldX] = 0;            // Clear old position

		// Place the arrow
		board[arrowY][arrowX] = 3;  // Arrow is always 3
	}

    @Override
    public String userName() {
    	return userName;
    }

	@Override
	public GameClient getGameClient() {
		return this.gameClient;
	}

	@Override
	public BaseGameGUI getGameGUI() {
		return  this.gamegui;
	}

	@Override
	public void connect() {
    	gameClient = new GameClient(userName, passwd, this);	
	}
}