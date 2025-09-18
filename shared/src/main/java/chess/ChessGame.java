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
    ChessPiece whiteKing;
    ChessPosition whiteKingPos;
    ChessPiece blackKing;
    ChessPosition blackKingPos;
    boolean whiteInCheck = false;
    boolean blackInCheck = false;
    boolean gameOver = false;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return whiteInCheck == chessGame.whiteInCheck && blackInCheck == chessGame.blackInCheck && gameOver == chessGame.gameOver && Objects.equals(isWhiteTurn, chessGame.isWhiteTurn) && Objects.equals(getBoard(), chessGame.getBoard()) && Objects.equals(whiteKing, chessGame.whiteKing) && Objects.equals(blackKing, chessGame.blackKing);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isWhiteTurn, getBoard(), whiteKing, blackKing, whiteInCheck, blackInCheck, gameOver);
    }

    public ChessGame() {
        isWhiteTurn = true;
        board = new ChessBoard();
        board.resetBoard();
        whiteKing = board.allPieces.get(new ChessPosition(1,5));
        whiteKingPos = new ChessPosition(1,5);
        blackKing = board.allPieces.get(new ChessPosition(8,5));
        blackKingPos = new ChessPosition(8,5);
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


    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {

        Collection<ChessMove> realPossibleMoves = board.allPieces.get(startPosition).pieceMoves(board, startPosition);
        ChessPiece king = whiteKing;
        if (board.allPieces.get(startPosition).getTeamColor() == TeamColor.BLACK) {
            king = blackKing;
        }
        for (ChessMove move : realPossibleMoves) {
            ChessGame temp = new ChessGame();
            temp.board = this.board;
            try {
                temp.makeMove(move);
            } catch (InvalidMoveException e) {
                throw new RuntimeException(e);
            }

            if (king.kingCanMove(temp.board, startPosition)) {
                realPossibleMoves.add(move);
            }
        }
        if (realPossibleMoves.size() == 0) {
            gameOver = true;
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
        if (gameOver) {
            throw new InvalidMoveException();
        }

        ChessPiece movingPiece = board.allPieces.get(move.getStartPosition());
        ChessPosition end = move.getEndPosition();
        TeamColor side = ChessGame.TeamColor.WHITE;
        int movingRow = 8;
        if (movingPiece.getTeamColor() == TeamColor.BLACK) {
            movingRow = 1;
            side = ChessGame.TeamColor.BLACK;
        }

        //Castling Moves
        if (movingPiece.getPieceType() == ChessPiece.PieceType.KING) {
            if (!movingPiece.moved) {
                ChessPiece movingRook = new ChessPiece(side, ChessPiece.PieceType.ROOK);
                if (move.getStartPosition().getColumn() - end.getColumn() == 2) {
                    board.allPieces.put(new ChessPosition(movingRow, 4), movingRook);
                    board.allPieces.remove(new ChessPosition(movingRow, 1));
                    movingRook.moved = true;
                }
            }
            if (side == TeamColor.WHITE) {
                whiteKing = board.allPieces.get(move.getEndPosition());
                whiteKingPos = move.getEndPosition();
            } else {
                blackKing = board.allPieces.get(move.getEndPosition());
                blackKingPos = move.getEndPosition();
            }
        } else {
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
            board.allPieces.put(end, movingPiece);
            board.allPieces.remove(move.getStartPosition());
            isInCheck(TeamColor.WHITE);
            isInCheck(TeamColor.BLACK);
            isWhiteTurn = !isWhiteTurn;
        }

    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        if (teamColor == TeamColor.BLACK) {
            if (!blackKing.kingCanMove(board, blackKingPos)) {
                blackInCheck = true;
            }
            return blackInCheck;
        } else {
            if (!whiteKing.kingCanMove(board, whiteKingPos)) {
                whiteInCheck = true;
            }
            return whiteInCheck;
        }
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        ChessPiece king = whiteKing;
        if (teamColor == TeamColor.BLACK) {
            king = blackKing;
        }
        if (isInCheck(teamColor)) {
            if (!king.kingCanMove(board, new ChessPosition(king.row, king.col))) {
                gameOver = true;
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        ChessPiece king = whiteKing;
        if (teamColor == TeamColor.BLACK) {
            king = blackKing;
        }
        if (isInCheck(teamColor)) {
            if (king.kingCanMove(board, new ChessPosition(king.row, king.col))) {
                gameOver = true;
                return true;
            }
        }
        return false;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
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
