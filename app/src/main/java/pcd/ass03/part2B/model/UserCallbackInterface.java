package pcd.ass03.part2B.model;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface UserCallbackInterface extends Remote {
    String getPlayerId() throws RemoteException;
    public void notifyUser(SudokuGrid sudoku) throws RemoteException;
    public void notifySudokuListUpdate(String sudokuId, String creator) throws RemoteException;
}
