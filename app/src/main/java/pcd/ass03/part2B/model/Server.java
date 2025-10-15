package pcd.ass03.part2B.model;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Optional;

import pcd.ass03.part2B.model.message.SetValueMessage;
import pcd.ass03.part2B.model.message.SelectCellMessage;
import pcd.ass03.part2B.model.message.UnselectCellMessage;

public interface Server extends Remote {
    
    // Ho utilizzato un'interfaccia perchè RMI passa solo oggetti remoti attraverso interfacce Remote, mai classi concrete.
    public void registerPlayer(UserCallbackInterface player) throws RemoteException;

    public SudokuGrid joinGame(String playerId, String gridId) throws RemoteException;

    public void leaveGame(String playerId, String gridId) throws RemoteException;

    public SudokuGrid createSudoku(String creator) throws RemoteException;
    
    public List<SudokuGrid> getSudokus() throws RemoteException;

    public Optional<SudokuGrid> getSudoku(String gridId) throws RemoteException;

    public boolean setCellValue(SetValueMessage msg) throws RemoteException;

    public boolean selectCell(SelectCellMessage msg) throws RemoteException;

    public boolean unselectCell(UnselectCellMessage msg) throws RemoteException;
}
