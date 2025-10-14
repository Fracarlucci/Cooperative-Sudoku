package pcd.ass03.part2B.model;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Sudoku extends Remote {
    public String getId() throws RemoteException;
    public String getCreator() throws RemoteException;
    public boolean setValue(int row, int col, int value) throws RemoteException;
    public void cancelValue(int row, int col) throws RemoteException;
    public boolean isValidMove(int row, int col, int value) throws RemoteException;
}
