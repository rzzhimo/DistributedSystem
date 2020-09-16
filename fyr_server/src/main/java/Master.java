import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Master extends Remote {
    String getNodesType(String key) throws RemoteException;
    void addNodes(String key) throws RemoteException;
    String getbackUp(String master)throws RemoteException;
    String getMaster(int index)throws RemoteException;
    String getBackupUrl(int index)throws RemoteException;
    void addMpath(String path,int index)throws RemoteException;
    void addBpath(String path,int index)throws RemoteException;
    String getMpath(String bp)throws RemoteException;
    Boolean ismyMaster(String Bp,String Mp)throws RemoteException;
    void changetoM(int index)throws RemoteException;
    void moveBackup(int index)throws RemoteException;
    void addMurl(String url,int index)throws RemoteException;
    void addBurl(String url,int index)throws RemoteException;

}
