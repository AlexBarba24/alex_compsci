
import java.util.Scanner;

//**********************************************************************
//**
//**						  APCS Reversi
//**			The classic game of reversal of fortunes
//**
//**		Written by Mr. Indelicato as an APCS Final Project
//**	Copyright (c) Tom Indelicato / Bishop Guertin High School
//**
//**********************************************************************
//** REVISION HISTORY
//**	20150427	Initial work begins: board storage, printing, prompt
//**				for human / computer players; framework added for
//**				computer and human move selection.
//**	20150428	Added logic for accepting human moves: input parsing,
//**				validity checks, piece flipping. At this point the
//**				program serves as a human v. human game
//**	20150429	Logic added to prevent end-of-game lock-up, added
//**				Winner check & announce code.
//**	20150508	Minor code clean-up
//**********************************************************************
//** TO DO LIST
//**	- Add AI for Computer (see bottom of code)
//**********************************************************************
//** BUG LIST
//**	- Can put piece on top of another piece				KILLED 0429
//**********************************************************************

public class Reversi
{
	static Scanner input;
	
	static final int EMPTY = 0;
	static final int BLACK = 1;
	static final int WHITE = 2;
	static final int HUMAN = 1;
	static final int COMPUTER = 2;
	
	static int[][] board = new int[8][8];
	static int whitePlayer, blackPlayer;
	static boolean gameOver = false;
	static int firstMove = BLACK;
	static int nextMove;

	
	public static void main(String[] args)
	//**********************************************************************
	//** main
	//**	Displays welcome banner, allows user to select human or computer 
	//**	for White and Black, resets and prints board. Play alternates 
	//**	between Black and White A) as long as there are more moves to
	//**	make, and B) until a winner is found.
	//**********************************************************************
	{
		boolean onePlayerCantMove = false;		// indicates both players blocked 

		input = new Scanner(System.in);

		System.out.println("\t\t#O#O#O  APCS Reversi  O#O#O# \n");
		System.out.println("\t\t   Author: Mr. Indelicato\n\n"); // <-- Insert Your Name Here!

		choosePlayers();

		newGame();
		printBoard();

		while (!gameOver)
		{
			if (nextMove == BLACK)
			{
				if (!moreMoves(BLACK))
				{
					System.out.println("No move for Black, White moves next");
					if (onePlayerCantMove) gameOver = true;
					onePlayerCantMove = true;
				}
				else
				{
					onePlayerCantMove = false;
					if (blackPlayer == HUMAN)
						promptHuman(BLACK);
					else
						machineStrategy(BLACK);

					printBoard();
				}
			}
			else
			{
				if (!moreMoves(WHITE))
				{
					System.out.println("No move for White, Black moves next");
					if (onePlayerCantMove) gameOver = true;
					onePlayerCantMove = true;
				}
				else
				{
					onePlayerCantMove = false;
					if (whitePlayer == HUMAN)
						promptHuman(WHITE);
					else
						machineStrategy(WHITE);

					printBoard();
				}
			}
			
			nextMove = BLACK + WHITE - nextMove;	// toggle next player
		}
		
		// Game over: Report winner
		System.out.println("*** Game Over ***");
		int blackTotal = pieceCount(BLACK);
		int whiteTotal = pieceCount(WHITE);
		
		if (blackTotal == whiteTotal)
			System.out.println("Tie Game, " + blackTotal + "-" + whiteTotal);
		if (blackTotal > whiteTotal)
			System.out.println("Black wins, " + blackTotal + "-" + whiteTotal);
		if (blackTotal < whiteTotal)
			System.out.println("White wins, " + whiteTotal + "-" + blackTotal);
 
		
	}
	
	public static void choosePlayers()
	//**********************************************************************
	//** choosePlayers
	//**	Called at the start of the game, this code prompts the user to 
	//**	indicate which player (Black or White) will be human and which
	//**	(white or Black) will be computer.
	//**
	//** NOTE: It is perfectly legal to have both players human, or both
	//**	players computer.
	//********************************************************************** 
	{
		String inputData = "";
		while (!inputData.toUpperCase().equals("H") && !inputData.toUpperCase().equals("C"))
		{
			System.out.print("Black Player -- Human or Computer (H or C): ");
			inputData = input.nextLine();
		}
		if (inputData.toUpperCase().equals("H")) blackPlayer = HUMAN; else blackPlayer = COMPUTER; 
		inputData = "";
		while (!inputData.toUpperCase().equals("H") && !inputData.toUpperCase().equals("C"))
		{
			System.out.print("White Player -- Human or Computer (H or C): ");
			inputData = input.nextLine();
		}
		if (inputData.toUpperCase().equals("H")) whitePlayer = HUMAN; else whitePlayer = COMPUTER; 
	}
	
	public static void newGame()
	//**********************************************************************
	//** newGame
	//**	Resets the Reversi board: Sets all 64 squares to EMPTY, puts the
	//**	initial four pieces in the center, flips the color of the first
	//**	player to move.
	//**********************************************************************
	{
		for (int row = 0; row < 8; row++)
			for (int col = 0; col < 8; col++)
				board[row][col] = EMPTY;
		
		board[3][3] = WHITE;
		board[3][4] = BLACK;
		board[4][3] = BLACK;
		board[4][4] = WHITE;
		
		nextMove = firstMove;
		firstMove = BLACK + WHITE - firstMove;
		
		initializeMachineLogic();	// Used to set up AI code, if needed
	}
	
	public static void printBoard()
	//**********************************************************************
	//** printBoard
	//**	Does a passable job of displaying the Reversi board
	//**********************************************************************
	{
		System.out.print("\n\t     A   B   C   D   E   F   G   H");
		for (int row = 0; row < 8; row++)
		{
			System.out.print("\n\t   +---+---+---+---+---+---+---+---+\n\t " + (row + 1) + " |");

			for (int col = 0; col < 8; col++)
			{
				if (board[row][col] == EMPTY) System.out.print("   |");
				if (board[row][col] == BLACK) System.out.print(" # |");
				if (board[row][col] == WHITE) System.out.print(" O |");
			}
		}
		System.out.print("\n\t   +---+---+---+---+---+---+---+---+\n");
	}

	public static int pieceCount(int thisColor)
	//**********************************************************************
	//** pieceCount
	//**	Used to determine Winner. Counts the number of thisColor pieces
	//**	found on the board.
	//**********************************************************************
	{
		int count = 0;
		
		for (int row = 0; row < 8; row++)
			for (int col = 0; col < 8; col++)
				if (board[row][col] == thisColor) count++;
		
		return count;
	}
	
	public static boolean moreMoves(int myColor)
	//**********************************************************************
	//** moreMoves
	//**	Checks to see if there is at least one valid move for the next 
	//**	player. Scans the entire board, checking each empty place to 
	//**	see if the "myColor" player can move. If so, return true so that
	//**	s/he can move. If not, return false to say the previous player
	//**	can go again.
	//**********************************************************************
	{
		for (int row = 0; row < 8; row++)
			for (int col = 0; col < 8; col++)
				if (board[row][col] == EMPTY)
					if (checkMove(col, row, myColor))
						return true;
		return false;
	}
	
	public static boolean checkMove(int X, int Y, int myColor)
	//**********************************************************************
	//** checkMove
	//**	Determines if this square (X,Y) has a valid move for the given 
	//**	color. Using the input parameters, scan the board in each of 
	//**	the eight potential directions, returning true if a move can be
	//**	made in any of the directions.
	//**
	//** INPUT:
	//**	X, Y:			board coordinates of the square to be checked
	//**	myColor:		color of the piece being (potentially) placed
	//**
	//** OUTPUT:
	//**	true  - if a valid move is found
	//**	false - if no valid move is found
	//**********************************************************************
	{

		return  checkMoveThisDir(X, Y, -1, -1, myColor) ||
				checkMoveThisDir(X, Y, -1,  0, myColor) ||
				checkMoveThisDir(X, Y, -1,  1, myColor) ||
				checkMoveThisDir(X, Y,  0, -1, myColor) ||
				checkMoveThisDir(X, Y,  0,  1, myColor) ||
				checkMoveThisDir(X, Y,  1, -1, myColor) ||
				checkMoveThisDir(X, Y,  1,  0, myColor) ||
				checkMoveThisDir(X, Y,  1,  1, myColor);
	}
	
	public static boolean checkMoveThisDir(int X, int Y, int deltaX, int deltaY, int myColor)
	//**********************************************************************
	//** checkMoveThisDir
	//**	Determines if this square (X,Y) has a valid move in the given 
	//**	direction (deltaX, deltaY) for the given color. Using the input
	//**	parameters, scan the board quitting if a blank, a myColor piece,
	//**	or a series of Enemy pieces that don't have a myColor piece 
	//**	immediately following.
	//**
	//** INPUT:
	//**	X, Y:			board coordinates of the square to be checked
	//**	deltaX,deltaY:	direction to be checked for valid move
	//**	myColor:		color of the piece being (potentially) placed
	//**
	//** OUTPUT:
	//**	true  - if a valid move is found
	//**	false - if no valid move is found
	//**********************************************************************
	{
		if (board[Y][X] != EMPTY) return false;
		
		int enemyColor = WHITE + BLACK - myColor;

		int currX = X + deltaX;
		int currY = Y + deltaY;
		if (currX < 0 || currX > 7 || currY < 0 || currY > 7)
			return false;
		
		if (board[currY][currX] != enemyColor) return false;

		while (board[currY][currX] == enemyColor)
		{
			currX += deltaX;
			currY += deltaY;
			if (currX < 0 || currX > 7 || currY < 0 || currY > 7)
				return false;
		}
		if (board[currY][currX] != myColor) 
			return false;
		
		return true;		
	}

	public static void putPiece(int X, int Y, int myColor)
	//**********************************************************************
	//** putPiece
	//**     Now that we know there's a valid move, we have to flip the
	//**     pieces around. Following the pattern of checkMove, we scan in
	//**     eight directions (up&left, up, up&right, left, right,
	//**     down&left, down, and down&right), looking for pieces to flip.
	//**     We then put the piece down on the square, ending the flipping.
	//** INPUT:
	//**     X, Y:           coordinates of space where the piece is placed 
	//**     myColor:        color of piece being placed
	//**********************************************************************
	{
		flipPiecesThisDir(X, Y, -1, -1, myColor);
		flipPiecesThisDir(X, Y, -1,  0, myColor);
		flipPiecesThisDir(X, Y, -1,  1, myColor);
		flipPiecesThisDir(X, Y,  0, -1, myColor);
		flipPiecesThisDir(X, Y,  0,  1, myColor);
		flipPiecesThisDir(X, Y,  1, -1, myColor);
		flipPiecesThisDir(X, Y,  1,  0, myColor);
		flipPiecesThisDir(X, Y,  1,  1, myColor);
		board[Y][X] = myColor;
	}
	
	public static void flipPiecesThisDir(int X, int Y, int deltaX, int deltaY, int myColor)
	//**********************************************************************
	//** flipPiecesThisDir
	//**     Search a given square in a given direction, and flip pieces
	//**     around if appropriate. Using logic similar to checkMove, if
	//**     a valid move is found we go the additional step of flipping
	//**     pieces.
	//** INPUT:
	//**     X, Y:           board coordinates of square to be checked
	//**     deltaX, deltaY: direction to be checked for pieces to flip
	//**     myColor:        color of piece being (potentially) placed
	//**********************************************************************
	{
		int currX, currY;
		int enemyColor = WHITE + BLACK - myColor;
		
		currX = X + deltaX;
		currY = Y + deltaY;
		if (currX == -1 || currX == 8 || currY == -1 || currY == 8) return;
		if (board[currY][currX] != enemyColor) return;

		while(board[currY][currX] == enemyColor)
		{
			currX += deltaX;
			currY += deltaY;
			if (currX == -1 || currX == 8 || currY == -1 || currY == 8) return;
		}
		if (board[currY][currX] != myColor) return;

		// If we're here, we passed all checks, so start flipping pieces
		do
		{
			board[currY][currX] = myColor;
			currX -= deltaX;
			currY -= deltaY;
		} while (board[currY][currX] == enemyColor);
	}
	
	public static void promptHuman(int playerColor)
	//**********************************************************************
	//** promptHuman
	//**	Asks the human player to indicate where his/her next move will
	//**	be. Starts by prompting for input; input is then checked to see
	//**	if it valid (i.e., a letter-number combination). If it's valid,
	//**	the move is checked to see if it's legal (i.e., the selected
	//**	space results in tiles flipped), and if so, the piece is placed.
	//**********************************************************************
	{
		String inputData = "";
		boolean goodMove = false;
		boolean validEntry;
		int col = 0, row = 0;
		
		while (!goodMove)
		{
			validEntry = false;
			while (!validEntry)
			{
				if (playerColor == BLACK)
					System.out.print("Black");
				else
					System.out.print("White");
				System.out.print(" player, enter your move in column-row format (e.g., A5): ");
				inputData = input.nextLine();
				
				col = -1; row = -1;
				if (inputData.length() >= 2 && 
					inputData.toUpperCase().charAt(0) >= 'A' &&
					inputData.toUpperCase().charAt(0) <= 'H' &&
					inputData.charAt(1) >= '1' && inputData.charAt(1) <= '8')
				{
					col = inputData.toUpperCase().charAt(0) - 'A';
					row = Integer.parseInt(inputData.substring(1,2)) - 1;
				}
				else
					System.out.println("Invalid entry, please try again.");
				
				if (col >= 0 && col < 8 && row >= 0 && row < 8) validEntry = true;
			}
			
			if (checkMove(col, row, playerColor))
				goodMove = true;
			else
				System.out.println("Invalid location, try again.");
		}
		
		putPiece(col, row, playerColor);
	}

	// public static void promptHuman(int playerColor)
	// //**********************************************************************
	// //** promptHuman
	// //**	Asks the human player to indicate where his/her next move will
	// //**	be. Starts by prompting for input; input is then checked to see
	// //**	if it valid (i.e., a letter-number combination). If it's valid,
	// //**	the move is checked to see if it's legal (i.e., the selected
	// //**	space results in tiles flipped), and if so, the piece is placed.
	// //**********************************************************************
	// {
		
	// 	boolean c = false;
	// 	while(!c) {
	// 		int row = (int)(Math.random()*8);
	// 		int col = (int)(Math.random()*8);
	// 		if(checkMove(row,col,playerColor)) {
	// 			putPiece(row,col,playerColor);
	// 			c = true;
	// 		}
	// 	}
	// }
	//**********************************************************************
	//** MACHINE LOGIC CODE
	//**	This is where you will put your code, to make your game as
	//**	intelligent / competitive as you can.
	//**********************************************************************
	
	public static void initializeMachineLogic()
	//**********************************************************************
	//** initializeMachineLogic
	//**	Called from the newGame() method, this initializes your AI.
	//**
	//**					describe your code here
	//**	
	//**********************************************************************
	{
		
	}
	
	public static void machineStrategy(int playerColor)
	//**********************************************************************
	//** machineStrategy
	//**	Determines the best place to move.
	//**
	//**					describe your code here.
	//**
	//**	The machine will assign values to different possible squares
	//**    and whichever one has the highest value is where the machine will go.
	//**
	//**	Corner +50
	//**    Danger Zone -5
	//**	X Squares -20
	//**********************************************************************
	{
		int[][] points = new int[board.length][board[0].length];
		
		numMoves++;
		
		for(int row = 0; row < board.length; row++) {
			for(int col = 0; col < board[0].length; col++) {
				int[][] boardCopy = copyBoard(board);
				if(checkMove(col,row,playerColor)) {
					if((row==0&&col==0)||(row==board.length-1&&col==0)||(row==0&&col==board[0].length-1)||(row==board.length-1&&col==board[0].length-1))
						points[row][col] += corner;
					else if((((row==0&&col==1)&&board[0][0]!=playerColor)||((row==1&&col==0)&&board[0][0]!=playerColor)||((row==0&&col==6)&&board[0][7]!=playerColor)||((row==1&&col==7)&&board[0][7]!=playerColor)||((row==6&&col==0)&&board[7][0]!=playerColor)||((row==7&&col==1)&&board[7][0]!=playerColor)||((row==7&&col==6)&&board[7][7]!=playerColor)||((row==6&&col==7)&&board[7][7]!=playerColor))&&!isSquareSecure(row, col, playerColor, boardCopy))
						points[row][col] += xSquares;
					else if(((row==0&&col==1)&&board[0][0]==playerColor)||((row==1&&col==0)&&board[0][0]==playerColor)||((row==0&&col==6)&&board[0][7]==playerColor)||((row==1&&col==7)&&board[0][7]==playerColor)||((row==6&&col==0)&&board[7][0]==playerColor)||((row==7&&col==1)&&board[7][0]==playerColor)||((row==7&&col==6)&&board[7][7]==playerColor)||((row==6&&col==7)&&board[7][7]==playerColor))
						points[row][col] -= xSquares;
					else if(((row==1&&col==1))&&board[0][0]!=playerColor||((row==1&&col==6))&&board[0][7]!=playerColor||((row==6&&col==1))&&board[7][0]!=playerColor||((row==6&&col==6))&&board[7][7]!=playerColor)
						points[row][col] += zSquares;
					else if(((row==1&&col==1))&&board[0][0]==playerColor||((row==1&&col==6))&&board[0][7]==playerColor||((row==6&&col==1))&&board[7][0]==playerColor||((row==6&&col==6))&&board[7][7]==playerColor)
						points[row][col] -= zSquares;
					else if(((row<6&&row>1)&&col==0)||((row<6&&row>1)&&col==7)||(row==0&&(col<6&&col>1))||(row==7&&(col<6&&col>1)))
						points[row][col] += edge;
					else if((row==1&&(col >= 1 && col <= board[0].length))||((row >= 1 && row <= board.length-2)&&col==1)||((row >= 1 && row <= board.length-2)&&col==board[0].length-2)||(row==board.length-2 && (col >= 1 && col <= board[0].length-2)))
						points[row][col] += dangerZone;
					else if((row >= 3 && row <= 7)&&(col >= 3 && col <= 7))
						points[row][col] += middle;
				}else {
					points[row][col] = -1000000;
				}
				if(checkMove(col,row,playerColor)) {
					if(isSquareSecure(row, col, playerColor, board))
						points[row][col]+=500;
					putPieceSim(col,row,playerColor,boardCopy);
					points[row][col]-=getNumMoves(3-playerColor,boardCopy);
					if(isVulenerableEdge(boardCopy))
						points[row][col] -= 100;
					if(numMoves < 12)
						points[row][col]-=getNumPieces(3-playerColor,boardCopy);
					else
						points[row][col]+=2*getNumPieces(3-playerColor,boardCopy);
					if(getNumZSquares(playerColor, board) < getNumZSquares(playerColor, boardCopy))
						points[row][col]-=10000;
					if(((row<6&&row>1)&&col==0))
						if(getNumOnEdge(2, boardCopy)==5)
							points[row][col]-=1000;
					if(((row<6&&row>1)&&col==7))
						if(getNumOnEdge(4, boardCopy)==5)
							points[row][col]-=1000;
					if(((col<6&&col>1)&&row==0))
						if(getNumOnEdge(1, boardCopy)==5)
							points[row][col]-=1000;
					if(((col<6&&col>1)&&row==7))
						if(getNumOnEdge(3, boardCopy)==5)
							points[row][col]-=1000;
					points[row][col] += getNumEdge(playerColor,board)-getNumEdge(playerColor,boardCopy)*-9;
					points[row][col] += getNumDangerZone(playerColor,board)-getNumDangerZone(playerColor,boardCopy)*4;
					int[][] peints = new int[8][8];
					for(int rew = 0; rew < boardCopy.length; rew++) {
						for(int cel = 0; cel < boardCopy[0].length; cel++) {
							int[][] copyBoard = copyBoard(boardCopy);
							int[][] boardCepy = copyBoard(boardCopy);
							if(checkMoveSim(cel,rew,3-playerColor, boardCopy)) {
								if((rew==0&&cel==0)||(rew==boardCopy.length-1&&cel==0)||(rew==0&&cel==boardCopy[0].length-1)||(rew==boardCopy.length-1&&cel==boardCopy[0].length-1))
									peints[rew][cel] += corner;
								else if(((rew==0&&cel==1)&&boardCopy[0][0]==0)||((rew==1&&cel==0)&&boardCopy[0][0]==0)||((rew==0&&cel==6)&&boardCopy[0][7]==0)||((rew==1&&cel==7)&&boardCopy[0][7]==0)||((rew==6&&cel==0)&&boardCopy[7][0]==0)||((rew==7&&cel==1)&&boardCopy[7][0]==0)||((rew==7&&cel==6)&&boardCopy[7][7]==0)||((rew==6&&cel==7)&&boardCopy[7][7]==0))
									peints[rew][cel] += xSquares;
								else if(((rew==1&&cel==1))&&boardCopy[0][0]==0||((rew==1&&cel==6))&&boardCopy[0][7]==0||((rew==6&&cel==1))&&boardCopy[7][0]==0||((rew==6&&cel==6))&&boardCopy[7][7]==0)
									peints[rew][cel] += zSquares;
								else if(((rew<6&&rew>1)&&cel==0)||((rew<6&&rew>1)&&cel==7)||(rew==0&&(cel<6&&cel>1))||(rew==7&&(cel<6&&cel>1)))
									peints[rew][cel] += edge;
								else if((rew==1&&(cel >= 1 && cel <= boardCopy[0].length))||((rew >= 1 && rew <= boardCopy.length-2)&&cel==1)||((rew >= 1 && rew <= boardCopy.length-2)&&cel==boardCopy[0].length-2)||(rew==boardCopy.length-2 && (cel >= 1 && cel <= boardCopy[0].length-2)))
									peints[rew][cel] += dangerZone;
								else if((rew >= 3 && rew <= 7)&&(cel >= 3 && cel <= 7))
									peints[rew][cel] += middle;
							}else {
								peints[rew][cel] = -1000000;
							}
							if(checkMoveSim(cel,rew,3-playerColor, copyBoard)) {
								if(isSquareSecure(cel, rew, 3-playerColor, boardCopy))
									peints[rew][cel]+=500;
								putPieceSim(cel,rew,3-playerColor,copyBoard);
								peints[rew][cel]-=getNumMoves(playerColor,copyBoard);
								if(isVulenerableEdge(copyBoard))
									peints[rew][cel] -= 100;
								if(numMoves < 16)
									peints[rew][cel]-=getNumPieces(playerColor,copyBoard);
								else
									peints[rew][cel]+=2*getNumPieces(playerColor,copyBoard);
								if(getNumZSquares(3-playerColor, board) < getNumZSquares(3-playerColor, copyBoard))
									peints[rew][cel]-=800;
								peints[rew][cel] += getNumEdge(3-playerColor,board)-getNumEdge(3-playerColor,copyBoard)*-9;
								peints[rew][cel] += getNumDangerZone(3-playerColor,board)-getNumDangerZone(3-playerColor,copyBoard)*4;
								
							}
						}
					}							
					int max = -100000000;
					for(int rew = 0; rew < peints.length; rew++) {
						for(int cel = 0; cel < peints[0].length; cel++) {
							if(peints[rew][cel] > max)
								max = peints[rew][cel];
						}
					}
					points[row][col] -= max/4;
				}
			}
		}

		

		boolean stopMiniMax = false;
		if(numMoves > 25) {
			for(int row = 0; row < board.length&&!stopMiniMax; row++) {
				for(int col = 0; col < board[0].length&&!stopMiniMax; col++) {
					if(checkMove(col,row,playerColor)) {
						int[][] boardCopy = copyBoard(board);
						double miniMax =miniMax(row,col,boardCopy,playerColor,true);
						int point = (int) (miniMax*1000000);
						if(point == 1000000)
							stopMiniMax = true;
						points[row][col] = point;
					}else {
						points[row][col] = -500;
					}
				}
			}
		}
		int max = -100000000;
		for(int row = 0; row < points.length; row++) {
			for(int col = 0; col < points[0].length; col++) {
				if(points[row][col] > max)
					max = points[row][col];
			}
		}
		System.out.print("With a weight of: "+max);
		int moveRow = 0;
		int moveCol = 0;
		for(int row = 0; row < points.length; row++) {
			for(int col = 0; col < points[0].length; col++) {
				if(points[row][col] == max) {
					moveRow = row;
					moveCol = col;
				}
					
			}
		}
		String temp = "";
		switch(moveCol) {
			case 0:
				temp = "A";
				break;
			case 1:
				temp = "B";
				break;
			case 2:
				temp = "C";
				break;
			case 3:
				temp = "D";
				break;
			case 4:
				temp = "E";
				break;
			case 5:
				temp = "F";
				break;
			case 6:
				temp = "G";
				break;
			case 7:
				temp = "H";
				break;
		}
		temp += (moveRow+1);
		System.out.println(", Computer placed piece at location: " + temp);
		putPiece(moveCol,moveRow,playerColor);
	}
	
	public static double miniMax(int row, int col, int nboard[][], int playerColor, boolean playerTurn) {
		putPieceSim(col,row,playerColor,nboard);

		//check if someone won
		int playerTotal;
		int oppTotal;
		if (getNumMoves(playerColor,nboard) == 0 && getNumMoves(3-playerColor,nboard) == 0){
			if(playerTurn) {
				playerTotal = getNumPieces(playerColor,nboard);
				oppTotal = getNumPieces(3-playerColor,nboard);
			}else {
				playerTotal = getNumPieces(3-playerColor,nboard);
				oppTotal = getNumPieces(playerColor,nboard);
			}
			if (playerTotal == oppTotal)
				return 1;
			if (playerTotal > oppTotal)
				return 1;
			if (playerTotal < oppTotal)
				return 0;
		}//125
		if(getNumMoves(3-playerColor,nboard)==0) {
			double totalMoves = 0;
			double winRate = 0;
			for(int r = 0; r < 8; r++) {
				for(int c = 0; c < 8; c++) {
					if(checkMoveSim(c,r,playerColor,nboard)) {
						int[][] boardCopy = copyBoard(nboard);
						winRate += miniMax(r,c,boardCopy,playerColor,playerTurn);
						totalMoves++;
					}
				}
			}
			return winRate/totalMoves;
		}
		
		
		double totalMoves = 0;
		double winRate = 0;
		for(int r = 0; r < 8; r++) {
			for(int c = 0; c < 8; c++) {
				if(checkMoveSim(c,r,3-playerColor,nboard)) {
					int[][] boardCopy = copyBoard(nboard);
					winRate += miniMax(r,c,boardCopy,3-playerColor,!playerTurn);
					totalMoves++;
				}
			}
		}
		return winRate/totalMoves;
	}
	
	public static void putPieceSim(int X, int Y, int myColor, int board[][])
	//**********************************************************************
	//** putPiece
	//**     Now that we know there's a valid move, we have to flip the
	//**     pieces around. Following the pattern of checkMove, we scan in
	//**     eight directions (up&left, up, up&right, left, right,
	//**     down&left, down, and down&right), looking for pieces to flip.
	//**     We then put the piece down on the square, ending the flipping.
	//** INPUT:
	//**     X, Y:           coordinates of space where the piece is placed 
	//**     myColor:        color of piece being placed
	//**********************************************************************
	{
		flipPiecesThisDirSim(X, Y, -1, -1, myColor,board);
		flipPiecesThisDirSim(X, Y, -1,  0, myColor,board);
		flipPiecesThisDirSim(X, Y, -1,  1, myColor,board);
		flipPiecesThisDirSim(X, Y,  0, -1, myColor,board);
		flipPiecesThisDirSim(X, Y,  0,  1, myColor,board);
		flipPiecesThisDirSim(X, Y,  1, -1, myColor,board);
		flipPiecesThisDirSim(X, Y,  1,  0, myColor,board);
		flipPiecesThisDirSim(X, Y,  1,  1, myColor,board);
		board[Y][X] = myColor;
	}
	
	public static void flipPiecesThisDirSim(int X, int Y, int deltaX, int deltaY, int myColor, int board[][])
	//**********************************************************************
	//** flipPiecesThisDir
	//**     Search a given square in a given direction, and flip pieces
	//**     around if appropriate. Using logic similar to checkMove, if
	//**     a valid move is found we go the additional step of flipping
	//**     pieces.
	//** INPUT:
	//**     X, Y:           board coordinates of square to be checked
	//**     deltaX, deltaY: direction to be checked for pieces to flip
	//**     myColor:        color of piece being (potentially) placed
	//**********************************************************************
	{
		int currX, currY;
		int enemyColor = WHITE + BLACK - myColor;
		
		currX = X + deltaX;
		currY = Y + deltaY;
		if (currX == -1 || currX == 8 || currY == -1 || currY == 8) return;
		if (board[currY][currX] != enemyColor) return;

		while(board[currY][currX] == enemyColor)
		{
			currX += deltaX;
			currY += deltaY;
			if (currX == -1 || currX == 8 || currY == -1 || currY == 8) return;
		}
		if (board[currY][currX] != myColor) return;

		// If we're here, we passed all checks, so start flipping pieces
		do
		{
			board[currY][currX] = myColor;
			currX -= deltaX;
			currY -= deltaY;
		} while (board[currY][currX] == enemyColor);
	}
	
	public static int getNumMoves(int playerColor, int[][]nboard) {
		int count = 0;
		for(int row = 0; row < 8; row++) {
			for(int col = 0; col < 8; col++) {
				if(checkMoveSim(col,row,playerColor,nboard))
					count++;
			}
		}
		return count;
	}
	
	public static int getNumDangerZone(int playerColor, int[][]board) {
		int count = 0;
		for(int row = 0; row < board.length; row++) {
			for(int col = 0; col < board[0].length; col++) {
				if((row==1&&(col >= 1 && col <= board[0].length))||((row >= 1 && row <= board.length-2)&&col==1)||((row >= 1 && row <= board.length-2)&&col==board[0].length-2)||(row==board.length-2 && (col >= 1 && col <= board[0].length-2)))
					count++;
			}
		}
		return count;
	}
	
	public static boolean isSquareSecure(int row, int col, int playerColor, int[][]board){
		for(int i = row+1; i < 8; i++){
			if(board[i][col]==EMPTY){
				for(int j = row-1; j>=0; j--){
					if(board[j][col]==EMPTY)
						return false;
					if(board[j][col]==3-playerColor)
						return false;
				}
			}
			else if(board[i][col]==3-playerColor){
				for(int j = row-1; j>=0; j--){
					if(board[j][col]==EMPTY)
						return false;
					if(board[j][col]==3-playerColor)
						break;
				}
			}
		}
		for(int i = row-1; i >= 0; i--){
			if(board[i][col]==EMPTY){
				for(int j = row+1; j<8; j++){
					if(board[j][col]==EMPTY)
						return false;
					if(board[j][col]==3-playerColor)
						return false;
				}
			}
			else if(board[i][col]==3-playerColor){
				for(int j = row+1; j<8; j++){
					if(board[j][col]==EMPTY)
						return false;
					if(board[j][col]==3-playerColor)
						break;
				}
			}
		}
		for(int i = col+1; i < 8; i++){
			if(board[row][i]==EMPTY){
				for(int j = col-1; j>=0; j--){
					if(board[j][col]==EMPTY)
						return false;
					if(board[j][col]==3-playerColor)
						return false;
				}
			}
			else if(board[i][col]==3-playerColor){
				for(int j = col-1; j>=0; j--){
					if(board[j][col]==EMPTY)
						return false;
					if(board[j][col]==3-playerColor)
						break;
				}
			}
		}
		for(int i = col-1; i >= 0; i--){
			if(board[row][i]==EMPTY){
				for(int j = col+1; j<8; j++){
					if(board[j][col]==EMPTY)
						return false;
					if(board[j][col]==3-playerColor)
						return false;
				}
			}
			else if(board[i][col]==3-playerColor){
				for(int j = col+1; j<8; j++){
					if(board[j][col]==EMPTY)
						return false;
					if(board[j][col]==3-playerColor)
						break;
				}
			}
		}
		for(int i = 1; row+i<8&&col+i<8&&row+i>=0&&col+i>=0; i++){
			if(board[row+i][col+i]==EMPTY){
				for(int j = -1; row+j<8&&col+j<8&&row+j>=0&&col+j>=0; j--){
					if(board[row+j][col+j]==EMPTY)
						return false;
					if(board[row+j][col+j]==3-playerColor)
						return false;
				}
			}
			else if(board[row+i][col+i]==3-playerColor){
				for(int j = -1; row+j<8&&col+j<8&&row+j>=0&&col+j>=0; j--){
					if(board[row+j][col+j]==EMPTY)
						return false;
					if(board[row+j][col+j]==3-playerColor)
						break;
				}
			}
		}
		for(int i = -1; row+i<8&&col+i<8&&row+i>=0&&col+i>=0; i--){
			if(board[row+i][col+i]==EMPTY){
				for(int j = 1; row+j<8&&col+j<8&&row+j>=0&&col+j>=0; j++){
					if(board[row+j][col+j]==EMPTY)
						return false;
					if(board[row+j][col+j]==3-playerColor)
						return false;
				}
			}
			else if(board[row+i][col+i]==3-playerColor){
				for(int j = 1; row+j<8&&col+j<8&&row+j>=0&&col+j>=0; j++){
					if(board[row+j][col+j]==EMPTY)
						return false;
					if(board[row+j][col+j]==3-playerColor)
						break;
				}
			}
		}
		for(int i = 1; row+i<8&&col-i<8&&row+i>=0&&col-i>=0; i++){
			if(board[row+i][col-i]==EMPTY){
				for(int j = -1; row+j<8&&col-j<8&&row+j>=0&&col-j>=0; j--){
					if(board[row+j][col-j]==EMPTY)
						return false;
					if(board[row+j][col-j]==3-playerColor)
						return false;
				}
			}
			else if(board[row+i][col-i]==3-playerColor){
				for(int j = -1; row+j<8&&col-j<8&&row+j>=0&&col-j>=0; j--){
					if(board[row+j][col-j]==EMPTY)
						return false;
					if(board[row+j][col-j]==3-playerColor)
						break;
				}
			}
		}
		for(int i = -1; row+i<8&&col-i<8&&row+i>=0&&col-i>=0; i--){
			if(board[row+i][col-i]==EMPTY){
				for(int j = 1; row+j<8&&col-j<8&&row+j>=0&&col-j>=0; j++){
					if(board[row+j][col-j]==EMPTY)
						return false;
					if(board[row+j][col-j]==3-playerColor)
						return false;
				}
			}
			else if(board[row+i][col-i]==3-playerColor){
				for(int j = 1; row+j<8&&col-j<8&&row+j>=0&&col-j>=0; j++){
					if(board[row+j][col-j]==EMPTY)
						return false;
					if(board[row+j][col-j]==3-playerColor)
						break;
				}
			}
		}
		return true;
	}

	public static boolean isVulenerableEdge(int[][]board) {
		boolean retVal;
		int count = 0;
		for(int i = 1; i < 7; i++) {
			if(board[0][i]==0) {
				count ++;
			}
		}
		if(count < 3)
			return true;
		else
			count = 0;
		for(int i = 1; i < 7; i++) {
			if(board[7][i]==0) {
				count ++;
			}
		}
		if(count < 3)
			return true;
		else
			count = 0;
		for(int i = 1; i < 7; i++) {
			if(board[i][0]==0) {
				count ++;
			}
		}
		if(count < 3)
			return true;
		else
			count = 0;
		for(int i = 1; i < 7; i++) {
			if(board[i][7]==0) {
				count ++;
			}
		}
		if(count < 3)
			return true;
		else
			count = 0;
		return false;
	}
	
	public static int getNumPieces(int playerColor, int[][]board) {
		int count = 0;
		for(int row = 0; row < board.length; row++) {
			for(int col = 0; col < board[0].length; col++) {
				if(board[row][col]==playerColor)
					count++;
			}
		}
		return count;
	}
	
	public static int[][] copyBoard(int[][] oldBoard){
		int[][] retVal = new int[oldBoard.length][oldBoard[0].length];
		for(int row = 0; row < oldBoard.length; row++) {
			for(int col = 0; col < oldBoard[0].length; col++) {
				retVal[row][col] = oldBoard[row][col];
			}
		}
		return retVal;
	}
	
	public static int getNumOnEdge(int edge, int[][]board){
		int count = 0;
		switch(edge){
			case 1:
				for(int i = 1; i < 7; i++)
					if(board[0][i]!=EMPTY)
						count++;
				break;
			case 2:
				for(int i = 1; i < 7; i++)
					if(board[i][0]!=EMPTY)
						count++;
				break;
			case 3:
				for(int i = 1; i < 7; i++)
					if(board[7][i]!=EMPTY)
						count++;
				break;
			case 4:
				for(int i = 1; i < 7; i++)
					if(board[i][7]!=EMPTY)
						count++;
				break;
		}
		return count;
	}

	public static int getNumZSquares(int playerColor, int[][]board) {
		int count = 0;
		if(board[1][1]==playerColor && board[0][0]!=playerColor)
			count++;
		if(board[1][6]==playerColor && board[0][7]!=playerColor)
			count++;
		if(board[6][1]==playerColor && board[7][0]!=playerColor)
			count++;
		if(board[6][6]==playerColor && board[7][7]!=playerColor)
			count++;
		return count;
	}
	
	public static int getNumEdge(int playerColor, int[][]board) {
		int count = 0;
	
		for(int row = 0; row < board.length; row++) 
			for(int col = 0; col < board[0].length; col++) 
				if(((row<6&&row>1)&&col==0)||((row<6&&row>1)&&col==7)||(row==0&&(col<6&&col>1))||(row==7&&(col<6&&col>1)))
					if(board[row][col]==playerColor)
						count++;
		return count;
	}

	public static boolean checkMoveSim(int X, int Y, int myColor, int[][] board)
	//**********************************************************************
	//** checkMove
	//**	Determines if this square (X,Y) has a valid move for the given 
	//**	color. Using the input parameters, scan the board in each of 
	//**	the eight potential directions, returning true if a move can be
	//**	made in any of the directions.
	//**
	//** INPUT:
	//**	X, Y:			board coordinates of the square to be checked
	//**	myColor:		color of the piece being (potentially) placed
	//**
	//** OUTPUT:
	//**	true  - if a valid move is found
	//**	false - if no valid move is found
	//**********************************************************************
	{

		return  checkMoveThisDirSim(X, Y, -1, -1, myColor, board) ||
				checkMoveThisDirSim(X, Y, -1,  0, myColor, board) ||
				checkMoveThisDirSim(X, Y, -1,  1, myColor, board) ||
				checkMoveThisDirSim(X, Y,  0, -1, myColor, board) ||
				checkMoveThisDirSim(X, Y,  0,  1, myColor, board) ||
				checkMoveThisDirSim(X, Y,  1, -1, myColor, board) ||
				checkMoveThisDirSim(X, Y,  1,  0, myColor, board) ||
				checkMoveThisDirSim(X, Y,  1,  1, myColor, board);
	}
	
	public static boolean checkMoveThisDirSim(int X, int Y, int deltaX, int deltaY, int myColor, int[][] board)
	//**********************************************************************
	//** checkMoveThisDir
	//**	Determines if this square (X,Y) has a valid move in the given 
	//**	direction (deltaX, deltaY) for the given color. Using the input
	//**	parameters, scan the board quitting if a blank, a myColor piece,
	//**	or a series of Enemy pieces that don't have a myColor piece 
	//**	immediately following.
	//**
	//** INPUT:
	//**	X, Y:			board coordinates of the square to be checked
	//**	deltaX,deltaY:	direction to be checked for valid move
	//**	myColor:		color of the piece being (potentially) placed
	//**
	//** OUTPUT:
	//**	true  - if a valid move is found
	//**	false - if no valid move is found
	//**********************************************************************
	{
		if (board[Y][X] != EMPTY) return false;
		
		int enemyColor = WHITE + BLACK - myColor;

		int currX = X + deltaX;
		int currY = Y + deltaY;
		if (currX < 0 || currX > 7 || currY < 0 || currY > 7)
			return false;
		
		if (board[currY][currX] != enemyColor) return false;

		while (board[currY][currX] == enemyColor)
		{
			currX += deltaX;
			currY += deltaY;
			if (currX < 0 || currX > 7 || currY < 0 || currY > 7)
				return false;
		}
		if (board[currY][currX] != myColor) 
			return false;
		
		return true;		
	}
	
	//*******************************
	//** Machine Logic Variables
	//*******************************
	static final int edge = 60;
	static final int corner = 1000000;
	static final int dangerZone = -70;
	static final int xSquares = -1250;
	static final int zSquares = -1200;
	static final int middle = 50;
	static int numMoves = 0;
}
