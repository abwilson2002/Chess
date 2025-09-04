package chess;

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

    PieceType selection = PieceType.PAWN;
    ChessGame.TeamColor faction = ChessGame.TeamColor.WHITE;
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
        Collection<ChessMove> possibleMoves = null;

        if (selection == PieceType.PAWN) {
            if (faction == ChessGame.TeamColor.WHITE) {
                if (row == 2) {
                    possibleMoves.add(new ChessMove(myPosition, new ChessPosition(row + 1, col), null));
                    possibleMoves.add(new ChessMove(myPosition, new ChessPosition(row + 2, col), null));
                } else {
                    possibleMoves.add(new ChessMove(myPosition, new ChessPosition(row + 1, col), null));
                }
            } else { //Black team pawn moves
                if (row == 7) {
                    possibleMoves.add(new ChessMove(myPosition, new ChessPosition(row - 1, col), null));
                    possibleMoves.add(new ChessMove(myPosition, new ChessPosition(row - 2, col), null));
                } else {
                    possibleMoves.add(new ChessMove(myPosition, new ChessPosition(row - 1, col), null));
                }
            }
        } else if (selection == PieceType.ROOK) {
            possibleMoves = linearChecks(possibleMoves);
        } else if (selection == PieceType.BISHOP) {
            possibleMoves = diagonalChecks(possibleMoves);
        } else if (selection == PieceType.KNIGHT) {

        } else if (selection == PieceType.KING) {

        } else if (selection == PieceType.QUEEN) {
            possibleMoves = linearChecks(possibleMoves);
            possibleMoves = diagonalChecks(possibleMoves);
        } else {
            throw new RuntimeException("Not a valid piece");
        }
        return possibleMoves;
    }

    public Collection<ChessMove> diagonalChecks(Collection<ChessMove> possibleMoves) {
        throw new RuntimeException("Not implemented yet");
    }

    public Collection<ChessMove> linearChecks(Collection<ChessMove> possibleMoves) {
        throw new RuntimeException("Not implemented yet");
    }
}
