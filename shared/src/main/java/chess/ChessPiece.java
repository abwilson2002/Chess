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
            int pawnBuffer = 1; //This will be the amount I change row for their movements
            if (faction == ChessGame.TeamColor.BLACK) {
                pawnBuffer = -1;
            }
            if (row == 2 & pawnBuffer == 1 || row == 7 & pawnBuffer == -1) {
                    possibleMoves.add(new ChessMove(myPosition, new ChessPosition(row + pawnBuffer, col), null));
                    possibleMoves.add(new ChessMove(myPosition, new ChessPosition(row + (pawnBuffer * 2), col), null));
            } else if (row == 2 & pawnBuffer == -1 || row == 7 & pawnBuffer == 1) {
                possibleMoves.add(new ChessMove(myPosition, new ChessPosition(row + (pawnBuffer * 2), col), PieceType.QUEEN));
            } else {
                possibleMoves.add(new ChessMove(myPosition, new ChessPosition(row + pawnBuffer, col), null));
            }
        } else if (selection == PieceType.ROOK) {
            possibleMoves = linearChecks(possibleMoves, myPosition, board);
        } else if (selection == PieceType.BISHOP) {
            possibleMoves = diagonalChecks(possibleMoves, myPosition, board);
        } else if (selection == PieceType.KNIGHT) {

        } else if (selection == PieceType.KING) {
            int i = -1;
            int j = -1;
            int possibleRow;
            int possibleCol;
            while (i < 2) {
                while (j < 2) {
                    if (i == 0 & j == 0) {
                        j++;
                    } else {
                        possibleRow = row - i;
                        possibleCol = col - j;
                        if (possibleRow >= 1 & possibleRow <= 8 & possibleCol >= 1 & possibleCol <= 8) {
                            possibleMoves.add(new ChessMove(myPosition, new ChessPosition(possibleRow, possibleCol), null));
                        }
                        j += 1;
                    }
                }
                i += 1;
            }
        } else if (selection == PieceType.QUEEN) {
            possibleMoves = linearChecks(possibleMoves, myPosition, board);
            possibleMoves = diagonalChecks(possibleMoves, myPosition, board);
        } else {
            throw new RuntimeException("Not a valid piece");
        }
        return possibleMoves;
    }

    public Collection<ChessMove> diagonalChecks(Collection<ChessMove> possibleMoves, ChessPosition myPosition, ChessBoard board) {
        throw new RuntimeException("Not implemented yet");
    }

    public Collection<ChessMove> linearChecks(Collection<ChessMove> possibleMoves, ChessPosition myPosition, ChessBoard board) {
        for (int i = row + 1; i < 9; i++) {
            if (isSpaceFilled(new ChessPosition(i, col), board)) {
                possibleMoves.add(new ChessMove(myPosition, new ChessPosition(i, col), null));
            } else {
                i = 9;
            }
        }
        for (int i = row - 1; i > 0; i--) {
            if (isSpaceFilled(new ChessPosition(i, col), board)) {
                possibleMoves.add(new ChessMove(myPosition, new ChessPosition(i, col), null));
            } else {
                i = 0;
            }
        }
        for (int j = col + 1; j < 9; j++) {
            if (isSpaceFilled(new ChessPosition(row, j), board)) {
                possibleMoves.add(new ChessMove(myPosition, new ChessPosition(row, j), null));
            } else {
                j = 9;
            }
        }
        for (int j = col - 1; j > 0; j--) {
            if (isSpaceFilled(new ChessPosition(row, j), board)) {
                possibleMoves.add(new ChessMove(myPosition, new ChessPosition(row, j), null));
            } else {
                j = 0;
            }
        }
        return possibleMoves;
    }

    public Boolean isSpaceFilled(ChessPosition position, ChessBoard board) {
        ChessPiece piece = board.allPieces.get(position);
        return piece != null;
    }

    //This function will test whether I can get the check to just follow a direction given when called
    public Collection<ChessMove> addMovesInDirection(Collection<ChessMove> possibleMoves, ChessPiece piece, int rowStart, int colStart, int rowChange, int colChange, ChessBoard board) {
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
}