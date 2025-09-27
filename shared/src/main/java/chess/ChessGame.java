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
        return whiteInCheck == chessGame.whiteInCheck && blackInCheck == chessGame.blackInCheck && gameOver == chessGame.gameOver && Objects.equals(isWhiteTurn, chessGame.isWhiteTurn) && Objects.equals(getBoard(), chessGame.getBoard());
    }

    @Override
    public int hashCode() {
        return Objects.hash(isWhiteTurn, getBoard(), whiteInCheck, blackInCheck, gameOver);
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
        ChessPiece tempPiece = board.allPieces.get(startPosition);
        Collection<ChessMove> realPossibleMoves = new ArrayList<>();
        if (tempPiece == null) {
            return realPossibleMoves;
        }
        Collection<ChessMove> possibleMoves = tempPiece.pieceMoves(board, startPosition);
        for (ChessMove move : possibleMoves) {
            ChessGame temp = new ChessGame();
            temp.setBoard(this.board);
            tempPiece = temp.board.allPieces.get(startPosition);
            ChessPiece king = temp.whiteKing;
            ChessPosition kingPosition = temp.whiteKingPos;
            if (tempPiece.getTeamColor() == TeamColor.BLACK) {
                king = temp.blackKing;
                kingPosition = temp.blackKingPos;
            }
            temp.board.allPieces.put(move.getEndPosition(), tempPiece);
            temp.board.allPieces.remove(move.getStartPosition());
            if (tempPiece.getPieceType() == ChessPiece.PieceType.KING) {
                kingPosition = move.getEndPosition();
                king = tempPiece;
            }

            if (king.kingCanMove(temp.board, kingPosition)) {
                realPossibleMoves.add(move);
            }
        }
        return realPossibleMoves;
    }

    public void getKingPosition() {
        for (Map.Entry<ChessPosition, ChessPiece> piece : board.allPieces.entrySet()) {
            if (piece.getValue().type == ChessPiece.PieceType.KING) {
                if (piece.getValue().faction == TeamColor.WHITE) {
                    whiteKingPos = piece.getKey();
                    whiteKing = piece.getValue();
                } else {
                    blackKingPos = piece.getKey();
                    blackKing = piece.getValue();
                }
            }
        }
    }

    public boolean isTeamsTurn(ChessPosition target) {
        TeamColor targetFaction = board.allPieces.get(target).getTeamColor();
        if ((isWhiteTurn & targetFaction == TeamColor.WHITE) || (!isWhiteTurn & targetFaction == TeamColor.BLACK)) {
            return true;
        }
        return false;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        Collection<ChessMove> validMoves = validMoves(move.getStartPosition());
        if (!validMoves.contains(move) || !isTeamsTurn(move.getStartPosition())) {
            throw new InvalidMoveException();
        }

        ChessPiece movingPiece = board.allPieces.get(move.getStartPosition());
        ChessPosition start = move.getStartPosition();
        ChessPosition end = move.getEndPosition();
        TeamColor side = ChessGame.TeamColor.WHITE;
        int movingRow = 1;
        if (movingPiece.getTeamColor() == TeamColor.BLACK) {
            movingRow = 8;
            side = ChessGame.TeamColor.BLACK;
        }

        //Castling Moves
        if (movingPiece.getPieceType() == ChessPiece.PieceType.KING) {
            if (!movingPiece.moved & move.getStartPosition().getColumn() == 5) {
                int moveDistance = move.getStartPosition().getColumn() - end.getColumn();
                if (abs(moveDistance) == 2) {
                    int rookSpot = 8;
                    if (moveDistance == 2) {
                        rookSpot = 1;
                    }
                    ChessPiece movingRook = board.allPieces.get(new ChessPosition(movingRow, rookSpot));
                    board.allPieces.put(new ChessPosition(movingRow, (end.getColumn() + (moveDistance / 2))), movingRook);
                    board.allPieces.remove(new ChessPosition(movingRow, rookSpot));
                    movingRook.moved = true;
                }
            }
        } else {
            if (movingPiece.getPieceType() == ChessPiece.PieceType.PAWN) {
                if ((movingPiece.faction == TeamColor.WHITE & board.blackPawnDoubleMove) || (movingPiece.faction == TeamColor.BLACK & board.whitePawnDoubleMove)) {
                    if (end.getColumn() == board.enPassantPosition.getColumn() & (end.getRow() == 3 || end.getRow() == 6)) {
                        board.allPieces.remove(board.enPassantPosition);
                    }
                }
                if (abs((start.getRow()) - (end.getRow())) == 2) {
                    if (getTeamTurn() == TeamColor.WHITE) {
                        board.whitePawnDoubleMove = true;
                    } else {
                        board.blackPawnDoubleMove = true;
                    }
                    board.enPassantPosition = move.getEndPosition();
                } else if (end.getRow() == 8 || end.getRow() == 1) {
                    movingPiece.type = move.pawnUpgrade;
                } else {
                    board.whitePawnDoubleMove = false;
                    board.blackPawnDoubleMove = false;
                    board.enPassantPosition = null;
                }
            }
        }
        movingPiece.moved = true;
        board.allPieces.put(end, movingPiece);
        board.allPieces.remove(move.getStartPosition());
        isInCheck(TeamColor.WHITE);
        isInCheck(TeamColor.BLACK);
        isWhiteTurn = !isWhiteTurn;
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
        if (isInCheck(teamColor)) {
            ChessPiece king = whiteKing;
            int currentRow = whiteKingPos.getRow();
            int currentCol = whiteKingPos.getColumn();
            if (teamColor == TeamColor.BLACK) {
                king = blackKing;
                currentRow = blackKingPos.getRow();
                currentCol = blackKingPos.getColumn();
            }
            return noMovesPossible(teamColor);
        }
        return false;
    }

    public boolean noMovesPossible(TeamColor teamColor) {
        boolean noMovesPossible = true;
        Set<ChessPosition> allFactionPieces = new HashSet<>();
        for (Map.Entry<ChessPosition, ChessPiece> piece : board.allPieces.entrySet()) {
            if (piece.getValue().faction == teamColor) {
                allFactionPieces.add(piece.getKey());
            }
        }
        for (ChessPosition piece : allFactionPieces) {
            if (!validMoves(piece).isEmpty()) {
                noMovesPossible = false;
            }
        }
        return noMovesPossible;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            if (noMovesPossible(teamColor)) {
                gameOver = true;
                return true;
            } else {
                return false;
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
        this.board = board.clone();
        getKingPosition();
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
