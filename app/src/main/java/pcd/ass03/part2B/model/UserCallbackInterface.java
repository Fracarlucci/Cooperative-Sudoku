package pcd.ass03.part2B.model;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface UserCallbackInterface extends Remote {

    String getPlayerId() throws RemoteException;

    void notifyUser(SudokuGrid sudoku) throws RemoteException;

    void notifySudokuListUpdate(String sudokuId, String creator) throws RemoteException;
}
