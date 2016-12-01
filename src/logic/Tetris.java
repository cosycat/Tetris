package logic;

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import graphics.*;

/**
 * This is the <code>class</code> that handles the main game logic.
 * The game starts immediately when created (TODO: start via Main class).
 **/
public class Tetris implements ActionListener, KeyListener {
	
	// PROPERTIES
	
	final int rows;
	public int getRows() { return rows; }
	final int columns;
	public int getColumns() { return columns; }
		
	/**
	 * The width of the game. Defined by the size of the window.
	 **/
	public int getGameWidth() { return (int) panel.getWindow().getSize().getWidth(); }	// The width of the Game
	/**
	 * The size of a single square {@link Piece}. It's defined by {@link getGameWidth()}.
	 **/
	public int getPieceSize() { return getGameWidth() / rows; }
	/**
	 * The height of the game is defined by the {@link getGameWidth()} and {@link getPieceSize()}
	 **/
	public int getGameHeight() { return getPieceSize() * columns; }
	
	/**
	 * The timer responsible for dropping the pieces down one row.
	 **/
	final Timer updateTimer;
	public Timer getTimer() { return updateTimer; }
	/**
	 * How often the {@link currentShape} drops down one row in drops per second.
	 **/
	float speed = 1;
	final float initialSpeed; // In case the speed changes during the game we wan't to restart it with this.
	final float speedRaisePerLevel = 0.5f;	// By how much the speed gets raised per level.
	boolean isPaused = false;
	public boolean getPaused() { return isPaused; }
	boolean gameOver = false;
	
	/**
	 * The <code>gameBoard</code> keeps track of all the fallen {@link Piece}s.
	 * The array represents the x and y coordinates for each piece.
	 * [0][0] is the top left corner.
	 * <p>
	 * The {@link currentShape} is stored separately.
	 **/
	Piece[][] gameBoard;
	
	/**
	 * The Shape which is currently falling down.
	 * It stores all the falling {@link Piece}s.
	 **/
	Shape currentShape;
	public Shape getCurrentShape() { return currentShape; }
	Shape nextShape;
	public Shape getNextShape() { return nextShape; }
	
	/**
	 * Keeps track of the exact number of lines deleted in the current game.
	 **/
	int linesDeleted = 0;
	/**
	 * The current Level that determines the <code>speed</code> and the <code>score</code>
	 * gain per level.
	 **/
	int level = 1;
	/**
	 * How many lines does the player have to delete to get to the next <code>level</code>.
	 **/
	final int linesPerLevel = 5;
	/**
	 * The current score is calculated by speed and lines removed.
	 **/
	int currentScore = 0;
	public int getCurrentScore() { return currentScore; }
	
	/**
	 * The parent <code>JPanel</code> in which this <code>Tetris</code> is displayed in.
	 * (TODO: The {@link Main} <code>class</code> should know the Game Logic
	 * <code>class</code>es and the Visual <code>class</code>es. They shouldn't know
	 * anything about each other.)
	 **/
	final GamePanel panel; 
	
	
	// INITIALISING
	
	/**
	 * Initialises the Game and sets the speed and the number of rows.
	 * 
	 * @param panel  the GamePanel this game is displayed in (TODO: replace with Main method)
	 * @param speed  the game's {@link speed}
	 * TODO:
	 **/
	public Tetris (GamePanel panel, float speed, int rows, int columns) {
		this.panel = panel;
		
		this.rows = rows;
		this.columns = columns;
		this.speed = speed;
		this.initialSpeed = speed;
		
		gameBoard = new Piece[rows][columns];
		
		updateTimer = new Timer((int)(1000/speed), this);

		startGame();
		
		System.out.println("GameLogic created! Game started!");
	}
	
	/**
	 * Starts the Timer and therefore the game.
	 **/
	public void startGame () {
		updateTimer.start();
	}
	
	/**
	 * Restarts the Game with the same initial speed.
	 **/
	public void restartGame() {
		panel.restartGame();
		gameBoard = new Piece[rows][columns];
		currentShape = null;
		nextShape = null;
		currentScore = 0;
		speed = initialSpeed;
		gameOver = false;
		updateTimer.setDelay((int)(1000/speed));
		updateTimer.restart();
	}
	
	/**
	 * Ends the game and goes back to the Start Screen.
	 **/
	public void endGame() {
		panel.getWindow().endGame();
		System.out.println("ENDGAME");
	}
	
	
	// TIMER EVENT
	
	/**
	 * Responsible for the game flow.
	 * Gets called every time the timer is at 0. 
	 **/
	public void actionPerformed (ActionEvent e) {
				
		if (currentShape == null) {
			recalcSpeed();
			checkFullRows();
			generateNewShape();
		} else {
			currentShape.dropDownOne();
		}
		
		//System.out.println("actionPerformed!");
		panel.repaint();
		
	}
	
	/**
	 * Recalculates the speed with the score.
	 */
	void recalcSpeed() {
		speed = ((currentScore / linesPerLevel) * speedRaisePerLevel) + initialSpeed;
		updateTimer.setDelay((int)(1000/speed));
	}
	
	
	// GAME UPDATE METHODS
	
	/**
	 * This generates new Pieces according to the next Shape at the top of the board at a random x position.
	 */
	void generateNewShape() {
	
		if (nextShape == null) {
			generateNextShape();
		}
		currentShape = nextShape;
		
		if (currentShape.checkGameOver()) {
			doGameOver();
			return;
		}
		
		generateNextShape();	
		
		//System.out.println("generatedNewShape");
		
	}
	
	/**
	 * Generates a new Random Shape for the next Shape.
	 **/
	void generateNextShape() {
		nextShape = ShapePrototypes.getRandomShape(this);
	}
	
	
	
	/**
	 * Checks all rows and deletes them if they are full with deleteRow().
	 **/
	void checkFullRows() {
		
		// Check each y row.
		for (int y = 0; y < columns; y++) {
			boolean full = true;
			// Check each x in the y rows.
			for (int x = 0; x < rows; x++) {
				// If one x isn't occupied, the y row isn't full and we can jump to the next y loop
				if (isPositionOccupied(x, y) == false) {
					full = false;
					break;
				}
			}
			if (full) {
				deleteRow(y);
				y--; // We have to check this line again because all the lines dropped down one.
				currentScore++;
			}
		}
		
		//System.out.println("deletedFullRows");
	}
	
	/**
	 * Deletes the specified y row and drops all the pieces above by one.
	 **/
	void deleteRow(int y) {
		for (int x = 0; x < rows; x++) {
			// Delete every piece in the y row.
			gameBoard[x][y] = null;
			
			// TODO: Animate Line Removal (pause game, send message to panel, wait for panel to finish animation, unpause).
			// or by setting a variable +1 and if the variable is >0 the update Timer drops every piece and sets it -1. (This would just wait with dropping the line(s) one tic instead of animating sth)
			
			// Drop every row above the y row down by one (xrow for xrow)
			for (int i = y; i > 0; i--) {
				gameBoard[x][i] = gameBoard[x][i-1];
			}
		}
	}
	
	
	/**
	 * If a Piece can't be placed anymore this Game Over Method should end the Game.
	 **/
	void doGameOver() {
		updateTimer.stop();
		gameOver = true;
		currentShape = null;
		nextShape = null;
		panel.doGameOver();
	}
	
	
	// KEY EVENT METHODS
	
	public void keyTyped(KeyEvent e) {}
	public void keyReleased(KeyEvent e) {}
	
	/**
	 * Handles the Key Pressed Event.
	 **/
	public void keyPressed(KeyEvent e) {
		
		if (gameOver) {
			switch(e.getKeyCode()) {
				case KeyEvent.VK_SPACE:
					restartGame();
					break;
				case KeyEvent.VK_ESCAPE:
					endGame();
					break;
				default:
					break;
			}
			return;
		}
		
		if (e.getKeyCode() == KeyEvent.VK_P) {
			pauseGame();
			return;
		}
		
		if (isPaused) {
			return;
		}
		
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			endGame();
			return;
		}
		
		if (currentShape == null) {
			return;
		}
		
		switch(e.getKeyCode()) {
			case KeyEvent.VK_LEFT:
				currentShape.tryMoveLeft();
				break;
			case KeyEvent.VK_RIGHT:
				currentShape.tryMoveRight();
				break;
			case KeyEvent.VK_DOWN:
				currentShape.dropDownOne(); // drop down one row
				//updateTimer.restart();
				break;
			case KeyEvent.VK_SPACE:
				while(currentShape.dropDownOne()) {} // drop down to the bottom.
				//updateTimer.restart();	// Set the Timer to 0, so the Piece get's added to the board immediately and can't be moved anymore.
				break;
			case KeyEvent.VK_UP:
				currentShape.rotate();
				break;
			default:
				// Non-relevant Key Pressed. Ignore.
				break;
		}
		
		panel.repaint();
		//System.out.println("Key Pressed!");
	}
	
	/**
	 * Pauses the game.
	 **/
	void pauseGame() {
		isPaused = !isPaused;
		if (isPaused) {
			updateTimer.stop();
		} else {
			updateTimer.start();
		}
		panel.changePauseLabel();
	}
	
	
	
	// GAME BOARD METHODS
	
	/**
	 * Checks if the position (x, y) is occupied
	 * Only static pieces count, not the falling Pieces from the current Shape.
	 * All the positions outside the game Board count as occupied.
	 **/
	public boolean isPositionOccupied(int x, int y) {
		if (x >= rows || x < 0 || y >= columns || y < 0) {
			return true;
		}
		if (gameBoard[x][y] != null) {
			return true;
		}
		return false;
	}
	
	/**
	 * Returns the piece at the (x, y) position or null if there is none.
	 * Should first be checked with isPositionOccupied(x, y).
	 **/
	public Piece getPieceAt(int x, int y) {
		if (isPositionOccupied(x, y) == false) {
			System.out.println("Tetris::getPieceAt -- ERROR: Trying to get a piece at an empty position.");
			return null;
		} else {
			return gameBoard[x][y];
		}
	}
	
	// TODO: Check which (protected?) allows only access inside same package.
	boolean setPieceAt(int x, int y, Piece p) {
		if (isPositionOccupied(x, y)) {
			System.out.println("Tetris::getPieceAt -- ERROR: Trying to set a piece where one already exists.");
			return false;
		} else {
			gameBoard[x][y] = p;
			return true;
		}
	}
	
	/**
	 * Sets the current Shape to null.
	 * In the next Timer update the next Shape will be set to the current Shape.
	 **/
	public void resetCurrentShape() {
		currentShape = null;
	}
	
}







