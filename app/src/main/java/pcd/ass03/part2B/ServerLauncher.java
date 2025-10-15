package pcd.ass03.part2B;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import pcd.ass03.part2B.model.Server;
import pcd.ass03.part2B.model.ServerImpl;

public class ServerLauncher {
    public static void main(String[] args) {
         try {
            Server server = new ServerImpl();
            Server serverStub = (Server) UnicastRemoteObject.exportObject(server, 0);
            // Bind the remote object's stub in the registry
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("SudokuServer", serverStub);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }
}