package chess;

import java.util.Collection;
import java.util.*;

import static java.lang.Math.abs;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    Boolean isWhiteTurn;
    ChessBoard board;


    public ChessGame() {
        isWhiteTurn = true;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        if (isWhiteTurn) {
            return TeamColor.WHITE;
        } else {
            return TeamColor.BLACK;
        }
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        if (team == TeamColor.WHITE) {
            isWhiteTurn = true;
        } else {
            isWhiteTurn = false;
        }
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }


    public Collection<ChessMove> whiteTeamsMoves() {
        Set<ChessMove> whiteMoves = new HashSet<>();

        return whiteMoves;
    }


    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {

        Collection<ChessMove> realPossibleMoves = board.allPieces.get(startPosition).pieceMoves(board, startPosition);
        for (ChessMove move : realPossibleMoves) {

        }

        return realPossibleMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece movingPiece = board.allPieces.get(move.getStartPosition());
        ChessPosition end = move.getEndPosition();
        board.allPieces.put(move.getEndPosition(), movingPiece);
        board.allPieces.remove(move.getStartPosition());

        //Castling Moves
        if (movingPiece.getPieceType() == ChessPiece.PieceType.KING) {
            int movingRow = 8;
            ChessGame.TeamColor side = ChessGame.TeamColor.WHITE;
            if (movingPiece.getTeamColor() == TeamColor.BLACK) {
                movingRow = 1;
                side = ChessGame.TeamColor.BLACK;
            }
            if (!movingPiece.moved) {
                ChessPiece movingRook = new ChessPiece(side, ChessPiece.PieceType.ROOK);
                if (move.getStartPosition().getColumn() - end.getColumn() == 2) {
                    board.allPieces.put(new ChessPosition(movingRow, 4), new ChessPiece(side, ChessPiece.PieceType.ROOK));
                    board.allPieces.remove(new ChessPosition(movingRow, 1));
                } else {
                    board.allPieces.put(new ChessPosition(movingRow, 6), movingRook);
                    board.allPieces.remove(new ChessPosition(movingRow, 8));
                }
                movingRook.moved = true;
            }
        }

        //En Passant Moves
        if (movingPiece.getPieceType() == ChessPiece.PieceType.PAWN & abs((movingPiece.row) - (end.getRow())) == 2) {
            if (getTeamTurn() == TeamColor.WHITE) {
                board.whitePawnDoubleMove = true;
            } else {
                board.blackPawnDoubleMove = true;
            }
            board.enPassantPosition = move.getEndPosition();
        } else {
            board.whitePawnDoubleMove = false;
            board.blackPawnDoubleMove = false;
            board.enPassantPosition = null;
        }
        movingPiece.moved = true;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
        this.board.resetBoard();
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }
}
