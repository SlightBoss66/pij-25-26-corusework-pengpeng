package pij.board;

public class InvalidBoardFileException extends Exception {
    public InvalidBoardFileException(String message) { super(message); }
    public InvalidBoardFileException(String message, Throwable cause) { super(message, cause); }
}