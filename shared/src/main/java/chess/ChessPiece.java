package chess;

import java.util.*;
import java.util.Objects;

import static java.lang.Math.abs;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    ChessGame.TeamColor faction;
    ChessPiece.PieceType type;
    int row;
    int col;
    boolean moved = false;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return row == that.row && col == that.col && faction == that.faction && type == that.type && moved == that.moved;
    }

    @Override
    public int hashCode() {
        return Objects.hash(faction, type, row, col, moved);
    }

    @Override
    public String toString() {
        return "ChessPiece{" +
                "faction=" + faction +
                ", type=" + type +
                ", row=" + row +
                ", col=" + col +
                '}';
    }

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        faction = pieceColor;
        this.type = type;
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
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Set<ChessMove> possibleMoves = new HashSet<>();
        row = myPosition.getRow();
        col = myPosition.getColumn();

        if (type == PieceType.PAWN) {
            int pawnDirection = 1;
            if (faction == ChessGame.TeamColor.BLACK) {
                pawnDirection = -1;
            }
            boolean ranIntoSomething = false;
            if (row == 2 & pawnDirection == 1 || row == 7 & pawnDirection == -1) {
                if (!isSpaceFilled(board, new ChessPosition(row + pawnDirection, col))) {
                    possibleMoves.add(new ChessMove(myPosition, new ChessPosition(row + pawnDirection, col), null));
                } else {
                    ranIntoSomething = true;
                }
                if (!isSpaceFilled(board, new ChessPosition(row + (pawnDirection * 2), col)) & !ranIntoSomething) {
                    possibleMoves.add(new ChessMove(myPosition, new ChessPosition(row + (pawnDirection * 2), col), null));
                }
            } else if (row == 2 & pawnDirection == -1 || row == 7 & pawnDirection == 1) {
                if (!isSpaceFilled(board, new ChessPosition(row + pawnDirection, col))) {
                    possibleMoves.add(new ChessMove(myPosition, new ChessPosition(row + pawnDirection, col), PieceType.QUEEN));
                    possibleMoves.add(new ChessMove(myPosition, new ChessPosition(row + pawnDirection, col), PieceType.BISHOP));
                    possibleMoves.add(new ChessMove(myPosition, new ChessPosition(row + pawnDirection, col), PieceType.KNIGHT));
                    possibleMoves.add(new ChessMove(myPosition, new ChessPosition(row + pawnDirection, col), PieceType.ROOK));
                }
            } else {
                if (!isSpaceFilled(board, new ChessPosition(row + pawnDirection, col))) {
                    possibleMoves.add(new ChessMove(myPosition, new ChessPosition(row + pawnDirection, col), null));
                }
            }
            for (int i = -1; i < 2; i += 2) {
                int rowCheck = row + pawnDirection;
                int colCheck = col + i;
                ChessPosition temp = new ChessPosition(rowCheck, colCheck);
                if (withinBoard(rowCheck, colCheck)) {
                    if (isSpaceFilled(board, temp)) {
                        if (isSpaceEnemy(board, temp)) {
                            if (rowCheck == 1 || rowCheck == 8) {
                                possibleMoves.add(new ChessMove(myPosition, new ChessPosition(rowCheck, colCheck), PieceType.QUEEN));
                                possibleMoves.add(new ChessMove(myPosition, new ChessPosition(rowCheck, colCheck), PieceType.BISHOP));
                                possibleMoves.add(new ChessMove(myPosition, new ChessPosition(rowCheck, colCheck), PieceType.KNIGHT));
                                possibleMoves.add(new ChessMove(myPosition, new ChessPosition(rowCheck, colCheck), PieceType.ROOK));
                            } else {
                                possibleMoves.add(new ChessMove(myPosition, temp, null));
                            }
                        }
                    }
                }
            }

            if (board.enPassantPosition != null) {
                if (row == board.enPassantPosition.getRow() & abs(col - board.enPassantPosition.getColumn()) == 1) {
                    possibleMoves.add(new ChessMove(myPosition, new ChessPosition(row + pawnDirection, board.enPassantPosition.getColumn()), null));
                }
            }


        } else if (type == PieceType.ROOK) {
            possibleMoves = linearChecks(board, myPosition, possibleMoves);
        } else if (type == PieceType.BISHOP) {
            possibleMoves = diagonalChecks(board, myPosition, possibleMoves);
        } else if (type == PieceType.QUEEN) {
            possibleMoves = linearChecks(board, myPosition, possibleMoves);
            possibleMoves = diagonalChecks(board, myPosition, possibleMoves);
        } else if (type == PieceType.KING) {
            for (int i = -1; i < 2; i++) {
                for (int j = -1; j < 2; j++) {
                    ChessPosition temp = new ChessPosition(row + i, col + j);
                    if (withinBoard(row + i, col + j)) {
                        if (!isSpaceFilled(board, temp)) {
                            possibleMoves.add(new ChessMove(myPosition, temp, null));
                        } else if (isSpaceEnemy(board, temp)) {
                            possibleMoves.add(new ChessMove(myPosition, temp, null));
                        }
                    }
                }
            }
            if (!moved) {
                if (!board.getPiece(new ChessPosition(row, 1)).moved) {
                    if (board.getPiece(new ChessPosition(row, 2)) == null & board.getPiece(new ChessPosition(row, 3)) == null & board.getPiece(new ChessPosition(row, 4)) == null) {
                        possibleMoves.add(new ChessMove(myPosition, new ChessPosition(row, 3), null));
                    }
                } else if (!board.getPiece(new ChessPosition(row, 8)).moved) {
                    if (board.getPiece(new ChessPosition(row, 6)) == null & board.getPiece(new ChessPosition(row, 7)) == null) {
                        possibleMoves.add(new ChessMove(myPosition, new ChessPosition(row, 7), null));
                    }
                }
            }
        } else if (type == PieceType.KNIGHT) {
            int holder1 = 1;
            int holder2 = 2;
            for (int i = -1; i < 2; i += 2) {
                for (int j = -1; j < 2; j += 2) {
                    int rowCheck = row + (holder1 * i);
                    int colCheck = col + (holder2 * j);
                    ChessPosition temp = new ChessPosition(rowCheck, colCheck);
                    for (int k = 0; k < 2; k++) {
                        if (withinBoard(rowCheck, colCheck)) {
                            if (!isSpaceFilled(board, temp)) {
                                possibleMoves.add(new ChessMove(myPosition, temp, null));
                            } else if (isSpaceEnemy(board, temp)) {
                                possibleMoves.add(new ChessMove(myPosition, temp, null));
                            }
                        }
                        rowCheck = row + (holder2 * i);
                        colCheck = col + (holder1 * j);
                        temp = new ChessPosition(rowCheck, colCheck);
                    }
                }
            }
        }

        return possibleMoves;
    }

    Set<ChessMove> linearChecks(ChessBoard board, ChessPosition myPosition, Set<ChessMove> possibleMoves) {
        for (int i = -1; i < 2; i += 2) {
            possibleMoves = movesInADirection(board, myPosition, possibleMoves, row, col, i, 0);
        }
        for (int j = -1; j < 2; j += 2) {
            possibleMoves = movesInADirection(board, myPosition, possibleMoves, row, col, 0, j);
        }
        return possibleMoves;
    }

    Set<ChessMove> diagonalChecks(ChessBoard board, ChessPosition myPosition, Set<ChessMove> possibleMoves) {
        for (int i = -1; i < 2; i += 2) {
            for (int j = -1; j < 2; j += 2) {
                possibleMoves = movesInADirection(board, myPosition, possibleMoves, row, col, i, j);
            }
        }
        return possibleMoves;
    }

    Set<ChessMove> movesInADirection(ChessBoard board, ChessPosition myPosition, Set<ChessMove> possibleMoves, int rowStart, int colStart, int rowChange, int colChange) {
        int row = rowStart + rowChange;
        int col = colStart + colChange;
        ChessPosition temp = new ChessPosition(row, col);
        boolean ranIntoSomething = false;
        while (!ranIntoSomething) {
            if (withinBoard(row, col)) {
                if (isSpaceFilled(board, temp)) {
                    if (isSpaceEnemy(board, temp)) {
                        possibleMoves.add(new ChessMove(myPosition, temp, null));
                    }
                    ranIntoSomething = true;
                } else {
                    possibleMoves.add(new ChessMove(myPosition, temp, null));
                }
                row += rowChange;
                col += colChange;
                temp = new ChessPosition(row, col);
            } else {
                ranIntoSomething = true;
            }
        }
        return possibleMoves;
    }

    Boolean isSpaceFilled(ChessBoard board, ChessPosition target) {
        return board.allPieces.containsKey(target);
    }

    Boolean isSpaceEnemy(ChessBoard board, ChessPosition target) {
        return (board.allPieces.get(target).faction != this.faction);
    }

    Boolean withinBoard(int row, int col) {
        if (row < 9 & row > 0) {
            if (col < 9 & col > 0) {
                return true;
            }
        }
        return false;
    }

}
