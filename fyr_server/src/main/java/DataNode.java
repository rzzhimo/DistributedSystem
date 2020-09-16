import org.apache.zookeeper.ZooKeeper;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.TreeMap;

public interface DataNode extends Remote {
    Boolean createData(String key,String value) throws RemoteException;
    Boolean modify(String key,String newValue)throws RemoteException;
    String getValue(String key)throws RemoteException;
    Boolean deleteData(String key)throws RemoteException;
    TreeMap<String,String> allKeyValue()throws RemoteException;
    void addByMap(TreeMap<String,String> map)throws RemoteException;
    void setBackup(Map<String,DataNode> map)throws RemoteException;
    void addBackup(String path,DataNode dataNode)throws RemoteException;
    void removeBackup(String path)throws RemoteException;
    Map<String,DataNode> getbackupMap()throws RemoteException;



}
