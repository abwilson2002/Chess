package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    PieceType selection;
    ChessGame.TeamColor faction;
    int row;
    int col;


    public ChessPiece(ChessGame.TeamColor pieceColor, PieceType type) {
        selection = type;
        faction = pieceColor;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return row == that.row && col == that.col && selection == that.selection && faction == that.faction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(selection, faction, row, col);
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return faction;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return selection;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        row = myPosition.getRow();
        col = myPosition.getColumn();
        Collection<ChessMove> possibleMoves = new ArrayList<>();

        if (selection == PieceType.PAWN) {
            int pawnBuffer = 1; //This will help me choose the direction I change row for pawn movements
            if (faction == ChessGame.TeamColor.BLACK) {
                pawnBuffer = -1;
            }
            if (row == 2 & pawnBuffer == 1 || row == 7 & pawnBuffer == -1) {
                Boolean blocked = false;
                if (!isSpaceFilled(new ChessPosition(row + pawnBuffer, col), board)) {
                    possibleMoves.add(new ChessMove(myPosition, new ChessPosition(row + pawnBuffer, col), null));
                } else {
                    blocked = true;
                }
                if (!isSpaceFilled(new ChessPosition(row + (pawnBuffer * 2), col), board) & !blocked) {
                    possibleMoves.add(new ChessMove(myPosition, new ChessPosition(row + (pawnBuffer * 2), col), null));
                }
            } else if (row == 2 & pawnBuffer == -1 || row == 7 & pawnBuffer == 1) {
                if (!isSpaceFilled(new ChessPosition(row + pawnBuffer, col), board)) {
                    possibleMoves.add(new ChessMove(myPosition, new ChessPosition(row + pawnBuffer, col), PieceType.QUEEN));
                    possibleMoves.add(new ChessMove(myPosition, new ChessPosition(row + pawnBuffer, col), PieceType.BISHOP));
                    possibleMoves.add(new ChessMove(myPosition, new ChessPosition(row + pawnBuffer, col), PieceType.ROOK));
                    possibleMoves.add(new ChessMove(myPosition, new ChessPosition(row + pawnBuffer, col), PieceType.KNIGHT));
                }
            } else {
                if (!isSpaceFilled(new ChessPosition(row + pawnBuffer, col), board)) {
                    possibleMoves.add(new ChessMove(myPosition, new ChessPosition(row + pawnBuffer, col), null));
                }
            }
            for (int i = -1; i < 2; i += 2) {
                var diagonalSpot = new ChessPosition(row + pawnBuffer, col + i);
                if (isSpaceFilled(diagonalSpot, board)) {
                    if (isSpaceFilledByEnemy(diagonalSpot, board)) {
                        if (row + pawnBuffer == 1 || row + pawnBuffer == 8) {
                            possibleMoves.add(new ChessMove(myPosition, diagonalSpot, PieceType.QUEEN));
                            possibleMoves.add(new ChessMove(myPosition, diagonalSpot, PieceType.BISHOP));
                            possibleMoves.add(new ChessMove(myPosition, diagonalSpot, PieceType.ROOK));
                            possibleMoves.add(new ChessMove(myPosition, diagonalSpot, PieceType.KNIGHT));
                        } else {
                            possibleMoves.add(new ChessMove(myPosition, diagonalSpot, null));
                        }
                    }
                }
            }
        } else if (selection == PieceType.ROOK) {
                possibleMoves = linearChecks(possibleMoves, myPosition, board);
        } else if (selection == PieceType.BISHOP) {
                possibleMoves = diagonalChecks(possibleMoves, myPosition, board);
        } else if (selection == PieceType.KNIGHT) {
            int holder1 = 1;
            int holder2 = 2;  //These two integers represent the changes in row and column for Knight movement
                for (int i = -1; i < 2; i += 2) {
                    for (int j = -1; j < 2; j += 2) {
                        if (isSpaceFilled(new ChessPosition(row + holder1, col + holder2), board)) {
                            if (isSpaceFilledByEnemy(new ChessPosition(row + holder1, col + holder2), board)) {
                                possibleMoves.add(new ChessMove(myPosition, new ChessPosition(row + holder1, col + holder2), null));
                            }
                        } else {
                            if (withinBoard(row + holder1, col + holder2)) {
                                possibleMoves.add(new ChessMove(myPosition, new ChessPosition(row + holder1, col + holder2), null));
                            }
                        }
                        if (isSpaceFilled(new ChessPosition(row + holder2, col + holder1), board)) {
                            if (isSpaceFilledByEnemy(new ChessPosition(row + holder2, col + holder1), board)) {
                                possibleMoves.add(new ChessMove(myPosition, new ChessPosition(row + holder2, col + holder1), null));
                            }
                        } else {
                            if (withinBoard(row + holder2, col + holder1)) {
                                possibleMoves.add(new ChessMove(myPosition, new ChessPosition(row + holder2, col + holder1), null));
                            }
                        }
                        holder2 *= j;
                    }
                    holder1 *= i;
                }
        } else if (selection == PieceType.KING) {
            int i = -1;
            int possibleRow;
            int possibleCol;
            while (i < 2) {
                int j = -1;
                while (j < 2) {
                    if (i == 0 & j == 0) {
                        j++;
                    } else {
                        possibleRow = row - i;
                        possibleCol = col - j;
                        if (withinBoard(possibleRow, possibleCol)) {
                            if (isSpaceFilledByEnemy(new ChessPosition(possibleRow, possibleCol), board) || !isSpaceFilled(new ChessPosition(possibleRow, possibleCol), board)) {
                                possibleMoves.add(new ChessMove(myPosition, new ChessPosition(possibleRow, possibleCol), null));
                            }
                        }
                        j += 1;
                    }
                }
                i += 1;
            }
            var filler = 0; //This line exists so I can set it as a breakpoint to debug King moves
        } else if (selection == PieceType.QUEEN) {
            possibleMoves = linearChecks(possibleMoves, myPosition, board);
            possibleMoves = diagonalChecks(possibleMoves, myPosition, board);
        } else {
            throw new RuntimeException("Not a valid piece");
        }
        return possibleMoves;
    }

    public Collection<ChessMove> diagonalChecks(Collection<ChessMove> possibleMoves, ChessPosition myPosition, ChessBoard board) {
        for (int i = -1; i < 2; i += 2) {
            for (int j = -1; j < 2; j += 2) {
                possibleMoves = addMovesInDirection(possibleMoves, myPosition.getRow(), myPosition.getColumn(), i, j, board);
            }
        }
        return possibleMoves;
    }

    public Collection<ChessMove> linearChecks(Collection<ChessMove> possibleMoves, ChessPosition myPosition, ChessBoard board) {
        for (int i = -1; i < 2; i += 2) {
            possibleMoves = addMovesInDirection(possibleMoves, myPosition.getRow(), myPosition.getColumn(), i, 0, board);
        }
        for (int j = -1; j < 2; j += 2) {
            possibleMoves = addMovesInDirection(possibleMoves, myPosition.getRow(), myPosition.getColumn(), 0, j, board);
        }
        return possibleMoves;
    }

    public Boolean isSpaceFilled(ChessPosition position, ChessBoard board) {
        ChessPiece piece = board.allPieces.get(position);
        return piece != null;
    }

    public Boolean isSpaceFilledByEnemy(ChessPosition position, ChessBoard board) {
        ChessPiece piece = board.allPieces.get(position);
        if (piece == null) {
             return true;
        }
        return (piece.getTeamColor() != this.getTeamColor());
    }

    //This function will test whether I can get the check to just follow a direction given when called
    public Collection<ChessMove> addMovesInDirection(Collection<ChessMove> possibleMoves, int rowStart, int colStart, int rowChange, int colChange, ChessBoard board) {
        ChessPosition startingPosition = new ChessPosition(rowStart, colStart);
        Boolean ranIntoSomething = false;
        int row = rowStart + rowChange;
        int col = colStart + colChange;
        ChessPosition checkedPosition = new ChessPosition(row, col);
        while (!ranIntoSomething & withinBoard(row, col)) {
            if (isSpaceFilled(checkedPosition, board)) {
                if (board.allPieces.get(checkedPosition).getTeamColor() != getTeamColor()) {
                    possibleMoves.add(new ChessMove(startingPosition, checkedPosition, null));
                }
                ranIntoSomething = true;
            } else {
                possibleMoves.add(new ChessMove(startingPosition, checkedPosition, null));
                row += rowChange;
                col += colChange;
            }
            checkedPosition = new ChessPosition(row, col);
        }
        return possibleMoves;
    }

    public Boolean withinBoard(int row, int col) {
        if (row < 9 & row > 0) {
            if (col < 9 & col > 0) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return "ChessPiece{" +
                "selection=" + selection +
                ", faction=" + faction +
                ", row=" + row +
                ", col=" + col +
                '}';
    }
}