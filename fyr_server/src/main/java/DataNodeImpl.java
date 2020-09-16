import com.sun.activation.registries.MailcapParseException;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;

public class DataNodeImpl extends UnicastRemoteObject implements DataNode{
    private static final long serialVersionUID = 1L;

    protected DataNodeImpl() throws RemoteException {
        super();

    }
    private  static TreeMap<String,String> Datas = new TreeMap<>();
    private Map<String,DataNode> backupMap = new TreeMap<>();

    private CountDownLatch latch = new CountDownLatch(1);

    @Override
    synchronized public Boolean createData(String key,String value)throws RemoteException{
        Datas.put(key,value);
        if(backupMap!=null){
            for (DataNode backup:backupMap.values()){
                backup.createData(key,value);
            }
        }
        return true;
    }
    @Override
    synchronized public Boolean modify(String key,String newValue)throws RemoteException{
        if(Datas.containsKey(key)){
            Datas.put(key,newValue);
            if(backupMap!=null){
                for (DataNode backup:backupMap.values()){
                    backup.modify(key,newValue);
                }
            }
            return true;
        }else {
            return false;
        }
    }
    @Override
    public String getValue(String key)throws RemoteException{
        if(Datas.containsKey(key)){
            return Datas.get(key);
        }else {
            return null;
        }

    }
    @Override
    synchronized public Boolean deleteData(String key)throws RemoteException{
        if(Datas.containsKey(key)){
            Datas.remove(key);
        }
        if(backupMap!=null){
            for (DataNode backup:backupMap.values()){
                backup.deleteData(key);
            }
        }
        return true;
    }

    @Override
    public void addByMap(TreeMap<String,String> map)throws RemoteException{
        Datas = map;
    }
    @Override
    public void setBackup(Map<String,DataNode> map)throws RemoteException{
        backupMap = map;
    }
    @Override
    public TreeMap<String,String> allKeyValue()throws RemoteException{
        return Datas;
    }

    @Override
    public void addBackup(String path,DataNode dataNode)throws RemoteException{
        backupMap.put(path,dataNode);
    }
    @Override
    public void removeBackup(String path)throws RemoteException{
        backupMap.remove(path);
    }
    @Override
    public Map<String,DataNode> getbackupMap()throws RemoteException{
        return backupMap;
    }
}
