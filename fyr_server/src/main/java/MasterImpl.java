import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;

/*
* 用来存储nodes的metadata，clien先通过master获得nodes的信息，然后通过信息来访问DataNode*/
public class MasterImpl extends UnicastRemoteObject implements Master {
    private static final long serialVersionUID = 1L;

    protected MasterImpl() throws RemoteException {
        super();
    }
    //用来存储nodes的metadata
    private final static TreeMap<String,String> nodesType = new TreeMap<>();
    //private final static TreeMap<String,LinkedList<String>> MasAndBack = new TreeMap<>();
    private final static List<String> masterurl = new LinkedList<>();
    private final static List<String> backupurl = new LinkedList<>();
    private final static List<String> Mpath = new LinkedList<>();
    private final static List<String> Bpath = new LinkedList<>();
    @Override
    public void addNodes(String key) throws RemoteException{
        int i =nodesType.size()/2;
        String nodeType = "master"+nodesType.size()/2;
        if(nodesType.size()%2==0){
            masterurl.add(i,key);
            System.out.println("此时masterurl有："+masterurl.toArray().toString());
        }
        else if(nodesType.size()%2!=0){
            nodeType = "backup"+nodesType.size()/2;
            backupurl.add(i,key);
            System.out.println("此时backupurl有"+backupurl.toArray().toString());

        }
        nodesType.put(key,nodeType);
    }
    @Override
    public String getNodesType(String key) throws RemoteException{

        return nodesType.get(key);
    }
    @Override
    public String getbackUp(String master)throws RemoteException{
        int backi=Integer.parseInt(master.substring(7));
        System.out.println("back num:"+backi);
        return backupurl.get(backi);
    }
    @Override
    public  String getMaster(int index)throws RemoteException{

        return masterurl.get(index);
    }
    @Override
    public String getBackupUrl(int index)throws RemoteException{
        return backupurl.get(index);
    }
    @Override
    public void addMpath(String path,int index)throws RemoteException{
        Mpath.add(index,path);
        System.out.println(Mpath.toArray().toString());
    }
    @Override
    public void addBpath(String path,int index)throws RemoteException{
        Bpath.add(index,path);
        System.out.println(Bpath.toArray().toString());
    }
    @Override
    public  Boolean ismyMaster(String Bp,String Mp)throws RemoteException{
        int Bindex=0;
        int Mindex=0;
        for (int i =0;i<Bpath.size();i++){
            if(Bpath.get(i).equals(Bp)){
                Bindex = i;
                break;
            }
        }
        for (int j =0;j<Mpath.size();j++){
            if(Mpath.get(j).equals(Mp)){
                Mindex = j;
                break;
            }
        }
        if(Bindex==Mindex){
            return true;
        }else {
            return false;
        }
    }
    @Override
    public void changetoM(int index)throws RemoteException{

        String bpurl = backupurl.get(index);
        String mpurl = masterurl.get(index);
        backupurl.remove(bpurl);
        masterurl.remove(mpurl);
        masterurl.add(index,bpurl);

        String newtype = "master"+index;
        nodesType.remove(mpurl);
        nodesType.put(bpurl,newtype);
    }
    @Override
    public String getMpath(String bp)throws RemoteException{
        int Bindex=0;
        for (int i =0;i<Bpath.size();i++){
            if(Bpath.get(i).equals(bp)){
                Bindex = i;
                break;
            }
        }
        return Mpath.get(Bindex);
    }
    @Override
    public void moveBackup(int index)throws RemoteException{
        String bpurl = backupurl.get(index);
        Bpath.remove(Bpath.get(index));
        backupurl.remove(bpurl);
        nodesType.remove(bpurl);
    }
    @Override
    public void addMurl(String url,int index)throws RemoteException{
        masterurl.add(index,url);
    }
    @Override
    public void addBurl(String url,int index)throws RemoteException{
        backupurl.add(index,url);
    }
}
