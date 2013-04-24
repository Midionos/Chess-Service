package parser;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Board {
	// Board square notation:
	private final int[] numbers = {0, 8, 7, 6, 5, 4, 3, 2, 1};
	private final char[] letters = {'0', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
	private Map<Character, Integer> letter;
	
	// Variables used to load/save FEN:
	// The piece to move now.
	String currentMove;
	// State of castling
	private String castling;
	// If there's enpassant pawn
	private String enPassant;
	// Number of halfmoves
	private String halfMoves;
	// Full number of moves
	private String fullMoves;

	// Holds references to pieces 
	// Piece object contains name, color and reference to board square its in
	private List<BoardPiece> pieces;

	// Board squares
	// These that hold a piece contain reference to piece object (so board squares and piece are circle referenced)
	Map<Character, BoardSquare[]> squares;
	
	class BoardPiece {
		String name;
		String color;
		BoardSquare square;
		
		public BoardPiece(String name, String color) {
			this.name = name;
			this.color = color;
		}
	}
	
	class BoardSquare {
		char x;
		int y;
		BoardPiece piece;
		
		public BoardSquare(char x, int y) {
			this.x = x;
			this.y = y;
		}
	}
	
	/**
	 * Constructor
	 */
	public Board() {
		this.pieces = new LinkedList<BoardPiece>();
		
		this.letter = new HashMap<Character, Integer>();
		this.letter.put('a', 1);
		this.letter.put('b', 2);
		this.letter.put('c', 3);
		this.letter.put('d', 4);
		this.letter.put('e', 5);
		this.letter.put('f', 6);
		this.letter.put('g', 7);
		this.letter.put('h', 8);
		
		this.squares = new HashMap<Character, BoardSquare[]>();
		this.squares.put('a', new BoardSquare[8]);
		this.squares.put('b', new BoardSquare[8]);
		this.squares.put('c', new BoardSquare[8]);
		this.squares.put('d', new BoardSquare[8]);
		this.squares.put('e', new BoardSquare[8]);
		this.squares.put('f', new BoardSquare[8]);
		this.squares.put('g', new BoardSquare[8]);
		this.squares.put('h', new BoardSquare[8]);
		for(char keyVar: this.squares.keySet()) {
			for(int j=1 ; j<=8 ; j++) {
				this.squares.get(keyVar)[j] = new BoardSquare(keyVar, j);
			}
		}
	}
	
	/**
	 * Create piece objects and place a reference to them for square they're in.
	 */
	public void addPiece(String name, String color, char x, int y) {
		BoardPiece newPiece = new BoardPiece(name, color);
		newPiece.square = this.squares.get(x)[y];
		this.pieces.add(newPiece);
		this.squares.get(x)[y].piece = newPiece;
	}

	/**
	 * Search for pieces by name, color and either (or both) of coordinates.
	 * @return An array of matches - corresponding indexes of pieces array.
	 */
	public List<Integer> getPiece(String name, String color, char x, int y) {
		List<Integer> result = new LinkedList<Integer>();
		for(int i=0 ; i<this.pieces.size() ; i++) {
			if(this.pieces.get(i).name == name && this.pieces.get(i).color == color 
					&& this.pieces.get(i).square != null 
					&& ((x && this.pieces.get(i).square.x==x) || !x) 
					&& ((y && this.pieces.get(i).square.y==y) || !y)) {
				result.push(i);
			}
		}
		return result;
	}

	/**
	 * Switches the current move
	 */
	public void switchMove() {
		if(this.currentMove.equals("white")) {
			this.currentMove = "black";
		} else {
			this.currentMove = "white";
		}
	}
	
	/**
	 * Simple move function with from&to variables
	 */
	public void makeMove(char fromX, int fromY, char toX, int toY, boolean capture) {
		BoardPiece previousPiece = this.squares.get(fromX)[fromY].piece;
		previousPiece.square = this.squares.get(toX)[toY];
		if(capture && this.squares.get(toX)[toY].piece != null) {
			this.squares.get(toX)[toY].piece.square = null;
		}
		this.squares.get(toX)[toY].piece = previousPiece;
		this.squares.get(fromX)[fromY].piece = null;
	}
	
	/**
	 * @return The current FEN.
	 */
	public String currentFEN(boolean reduced) {
		String FEN = "";
		for(int num=8 ; num>=1 ; num--) {
			int emptyCounter = 0;
			for(char keyVar: this.squares.keySet()) {
				if(this.squares.get(keyVar)[num].piece != null) {
					if(emptyCounter != 0) {
						FEN += emptyCounter;
						emptyCounter = 0;
					}
					String pieceName = this.squares.get(keyVar)[num].piece.name;
					String pieceColor = this.squares.get(keyVar)[num].piece.color;
					char name;
					if(pieceName.equals("rook")) {
						name = 'r';
					} else if(pieceName.equals("bishop")) {
						name = 'b';
					} else if(pieceName.equals("queen")) {
						name = 'q';
					} else if(pieceName.equals("king")) {
						name = 'k';
					} else if(pieceName.equals("pawn")) {
						name = 'p';
					} else if(pieceName.equals("knight")) {
						name = 'n';
					}
					if(pieceColor.equals("white")) {
						name = name.toUpperCase();
						FEN += name;
					} else { 
						FEN += name;
					}
				} else {
					emptyCounter++;
				}
			}
			if(emptyCounter != 0) {
				FEN += emptyCounter;
			}
			if(num != 1) {
				FEN += "/";
			}
		}
		FEN += " "+this.currentMove.substring(0, 1);
		FEN += " "+this.castling;
		FEN += " "+this.enPassant;
		if(!reduced) {
			FEN += " "+this.halfMoves;
			FEN += " "+this.fullMoves;
		}
		return FEN;
	}

	/**
	 * Prototype function used to load FEN into board
	 */
	public void loadFEN(String FEN) {
		for(char keyVar: this.squares.keySet()) {
			for(int j=1 ; j<=8 ; j++) {
				this.squares.get(keyVar)[j].piece = null;
			}
		}
		this.pieces = new LinkedList<BoardPiece>();
	
		String[] FENArray = FEN.split(" ");
		String[] boardArray = FENArray[0].split("/");
		for(int lines=1 ; lines<=8 ; lines++) {
			var line = boardArray[lines-1].split("");
			int colsY = 1;
			for(int cols=1 ; cols<=line.length ; cols++) {
				char letter = line[cols-1];
				String color;
				if (/[rbqkpn]/.test(letter)) {
					color = "black";
				} else if (/[RBQKPN]/.test(letter)) {
					color = "white";
				} else {
					colsY = parseInt(colsY) + parseInt(letter);
					continue;
				}
				switch(letter.toLowerCase()) {
					case 'r':
						name = "rook";
						break;
					case 'b':
						name = "bishop";
						break;
					case 'q':
						name = "queen";
						break;
					case 'k':
						name = "king";
						break;
					case 'p':
						name = "pawn";
						break;
					case 'n':
						name = "knight";
						break;
					default:
						break;
				}
				var x = this.letters[colsY];
				var y = this.numbers[lines];
				this.addPiece(name, color, x, y);
				colsY++;
			}
		}
		if(FENArray[1].equals("b")) {
			this.currentMove = "black";
		} else {
			this.currentMove = "white";
		}
		this.castling = FENArray[2];
		this.enPassant = FENArray[3];
		this.halfMoves = FENArray[4];
		this.fullMoves = FENArray[5];
	}
	
	/**
	 * Handles the castling
	 */
	public void castle(String castling) {
		int line;
		if(this.currentMove.equals("white")) {
			line = 1;
		} else {
			line = 8;
		}
			
		if(/^O-O\+?$/.test(castling)) {
			this.makeMove("e", line, "g", line);
			this.makeMove("h", line, "f", line);
		} else {
			this.makeMove("e", line, "c", line);
			this.makeMove("a", line, "d", line);
		}
	
		var castlestrip;
	
		if(this.currentMove.equals("white")) {
			castlestrip = /[KQ]/g;
		} else {
			castlestrip = /[kq]/g;
		}
	
		this.enPassant = "-";
		this.halfMoves++;
		if(this.currentMove.equals("black")) {
			this.fullMoves++;
		}
		this.castling = this.castling.replace(castlestrip, "");
		if(this.castling.equals("")) {
			this.castling = "-";
		}
		this.switchMove();
	}
	
	/**
	 * MoveHandler
	 */
	public void moveHandler(String piece, char fromX, int fromY, char toX, int toY, boolean capture, boolean promotion, String promoteTo, varNum) {
		// Make piece move
		this.makeMove(fromX, fromY, toX, toY, capture);
		if(piece.equals("pawn")) {
			// White pawns move "up", black move "down"
			var mod;
			if(this.currentMove.equals("white")) { 
				mod = 1;
			} else {
				mod = -1;
			}
			// if enPassant capture, manually remove piece, as makeMove is simple and doesn't handle this
			if(capture && toX+toY==this.enPassant) {
				this.squares[toX][toY - mod].piece.square = null;
				this.squares[toX][toY - mod].piece = null;
			}
			// Set enPassant if needed
			if(Math.abs(toY - fromY)==2 && ((toX!='a' && this.squares.get(this.letters[this.letter[toX]-1])[toY].piece!=undefined 
												&& this.squares.get(this.letters[this.letter[toX]-1])[toY].piece.color!=this.currentMove 
												&& this.squares.get(this.letters[this.letter[toX]-1])[toY].piece.name.equals("pawn")) || 
											(toX!='h' && this.squares.get(this.letters[this.letter[toX]+1])[toY].piece!=undefined 
												&& this.squares.get(this.letters[this.letter[toX]+1])[toY].piece.color!=this.currentMove 
												&& this.squares.get(this.letters[this.letter[toX]+1])[toY].piece.name.equals("pawn")
													))) {
				this.enPassant = toX + (parseInt(toY) - mod);
			} else {
				this.enPassant = "-";
			}
	
			// Set the promotion piece if so
			if(promotion) {
				this.squares.get(toX)[toY].piece.name = promoteTo;
			}
		} else {
			this.enPassant = "-";
			// Handle castling if rook moves
			if(piece.equals("rook") && this.castling.equals("-")) {
				if(fromX=='a' && fromY==8) {
					this.castling = this.castling.replace(/q/, "");
				} else if(fromX=='h' && fromY==8) {
					this.castling = this.castling.replace(/k/, "");
				} else if(fromX=='a' && fromY==1) {
					this.castling = this.castling.replace(/Q/, "");
				} else if(fromX=='h' && fromY==1) {
					this.castling = this.castling.replace(/K/, "");
				}
			}
			if(piece.equals("king") && this.castling != "-") {
				if (this.currentMove.equals("white")) {
					this.castling = this.castling.replace(/K/, "");
					this.castling = this.castling.replace(/Q/, "");
				} else {
					this.castling = this.castling.replace(/k/, "");
					this.castling = this.castling.replace(/q/, "");
				}
			}
			// If castling is empty after above
			if(this.castling.equals("")) {
				this.castling = "-";
			}
		}
	
		if(piece.equals("pawn") || promotion || capture) {
			this.halfMoves = 0;
		} else {
			this.halfMoves++;
		}
		if(this.currentMove.equals("black")) {
			this.fullMoves++;
		}
		this.switchMove();
	}
}