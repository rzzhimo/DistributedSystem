import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;

/**
 * RMI服务提供者
 */
public class ServiceProvider {

//    private  static TreeMap<String,String> Datas = new TreeMap<>();
//    private Map<String,DataNode> backupMap = new TreeMap<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceProvider.class);

    // 用于等待 SyncConnected 事件触发后继续执行当前线程
    private CountDownLatch latch = new CountDownLatch(1);

    // 发布 RMI 服务并注册 RMI 地址到 ZooKeeper 中
    public void publish(Remote remote, String host, int port) {
        String url = publishService(remote, host, port); // 发布 RMI 服务并返回 RMI 地址
        if (url != null) {
            ZooKeeper zk = connectServer(); // 连接 ZooKeeper 服务器并获取 ZooKeeper 对象
            if (zk != null) {
                //String path = createNode(zk, url); // 创建 ZNode 并将 RMI 地址放入 ZNode 上
                //System.out.println("path为："+path);

                try {
                    Master master = (Master) Naming.lookup("rmi://localhost:3099/Master");
                    String dataNodeUrl = String.format("rmi://%s:%d/%s", host, port, "DataNodeImpl");
                    master.addNodes(dataNodeUrl);

                    String dataNodeType = master.getNodesType(dataNodeUrl);
                    System.out.println("节点身份为：" + dataNodeType);

                    if(dataNodeType.contains("master")){
                        int index = Integer.parseInt(dataNodeType.substring(6));
                        String path = "/registry/master"+index;
                        master.addMpath(path,index);
                        master.addMurl(url,index);
                        createNode(zk,url,path);
                        watchBackup(zk,"/registry/backup"+index,index);
                    }
                    if (dataNodeType.contains("backup")) {
                        int index = Integer.parseInt(dataNodeType.substring(6));
                        String path ="/registry/backup"+index;
                        master.addBpath(path,index);
                        master.addBurl(url,index);
                        synchBack(zk, dataNodeUrl, dataNodeType);
                        String Mpath=master.getMpath(path);
                        createNode(zk,url,path);
                        watchMaster(zk, path,Mpath, dataNodeType);
                    }
                } catch (NotBoundException | MalformedURLException | RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 发布 RMI 服务
    private String publishService(Remote remote, String host, int port) {
        String url = null;
        try {
            //绑定的URL标准格式为：rmi://host:port/name
            url = String.format("rmi://%s:%d/%s", host, port, remote.getClass().getName());
            System.out.println("url为："+url.toString());
            LocateRegistry.createRegistry(port);
            //注册+命名
            Naming.rebind(url, remote);
            LOGGER.debug("publish rmi service (url: {})", url);
        } catch (RemoteException | MalformedURLException e) {
            LOGGER.error("", e);
        }
        return url;
    }

    // 连接 ZooKeeper 服务器
    private ZooKeeper connectServer() {
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(Constant.ZK_CONNECTION_STRING, Constant.ZK_SESSION_TIMEOUT, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getState() == Event.KeeperState.SyncConnected) {
                        System.out.println("ServiceProvider连接创建成功");
                        latch.countDown(); // 唤醒当前正在执行的线程
                    }
                }
            });
            latch.await(); // 使当前线程处于等待状态
        } catch (IOException | InterruptedException e) {
            LOGGER.error("", e);
        }
        return zk;
    }

    // 创建 ZNode
    private String createNode(ZooKeeper zk, String url,String pt) {
        String path =null;
        try {
            byte[] data = url.getBytes();

            // 创建一个临时性且有序的 ZNode
            path = zk.create(pt, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            System.out.println("server path为："+path);
            LOGGER.debug("create zookeeper node ({} => {})", path, url);
        } catch (KeeperException | InterruptedException e) {
            LOGGER.error("", e);
        }
        return path;
    }
    public DataNode lookupService(String url)throws RemoteException{
        DataNode dataNode = null;
        try {
            dataNode =  (DataNode) Naming.lookup(url);
        }catch (NotBoundException | MalformedURLException e){
            e.printStackTrace();
        }
        return dataNode;

    }
    public void synchBack(final ZooKeeper zk,final String backup,String dataNodeType){
        try {
            Master master= (Master) Naming.lookup("rmi://localhost:3099/Master");
            String MainUrl = master.getMaster(Integer.parseInt(dataNodeType.substring(6)));
            System.out.println("我的主节点url为："+MainUrl);
            DataNode MdataNode = lookupService(MainUrl);
            DataNode BdataNode = lookupService(backup);
            BdataNode.addByMap(MdataNode.allKeyValue());
            MdataNode.addBackup(backup,BdataNode);
            System.out.println("备份节点: "+dataNodeType+" 备份成功");
        }catch (NotBoundException | MalformedURLException | RemoteException e){
            e.printStackTrace();
        }
    }
    public void watchMaster(final ZooKeeper zk, final String backup,final String Mp,final String dataNodeType){
        try{
            System.out.println("备份节点身份为："+backup+"号码："+dataNodeType.substring(6));
            System.out.println("要watch的主节点为："+Mp);
            final int index = Integer.parseInt(dataNodeType.substring(6));
            zk.exists(Mp,new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    if(watchedEvent.getType()==Event.EventType.NodeDeleted){
                        //String Mpath = watchedEvent.getPath();
                        System.out.println("NodeDelete:"+watchedEvent.getPath());
                        try{
                            Master master= (Master) Naming.lookup("rmi://localhost:3099/Master");
                            if(master.ismyMaster(backup,Mp)){
                                System.out.println("我的主节点被删了,我变为主节点");
                                master.changetoM(index);
                                watchBackup(zk,"/registry/backup"+index,index);
                            }

                        }catch (NotBoundException | MalformedURLException | RemoteException e){
                            e.printStackTrace();
                        }

                        watchMaster(zk,backup,Mp,dataNodeType);
                    }
                }
            });
        }catch (KeeperException|InterruptedException e) {
            e.printStackTrace();
        }

    }
    public void watchBackup(final ZooKeeper zk, final String bp, final int index){
        System.out.println("我要看的backup节点为"+bp);
        try{
            zk.exists(bp,new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if(event.getType()==Event.EventType.NodeCreated){
                        if(bp.equals(event.getPath())){
                            System.out.println("备份节点创立");
                            watchBackup(zk,bp,index);
                        }
                    }
                    else if(event.getType()==Event.EventType.NodeDeleted){
                        System.out.println("备份节点删除:"+event.getPath());
                        try {

                            try {
                                Master master= (Master) Naming.lookup("rmi://localhost:3099/Master");

                                String Mainp = master.getMpath(event.getPath());
                                //System.out.println("主节点"+Mainp);
                                if(Mainp!=null){
                                    master.moveBackup(index);
                                    DataNode MdataNode = lookupService(master.getMaster(index));
                                    String url = master.getBackupUrl(index);
                                    //System.out.println("该节点的backup节点为："+url);
                                    MdataNode.removeBackup(url);
                                }
                            }catch (NotBoundException | MalformedURLException | RemoteException e){
                                e.printStackTrace();
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        watchBackup(zk,bp,index);
                    }
                }
            });
        }catch (KeeperException|InterruptedException e) {
            e.printStackTrace();
        }
    }

}

