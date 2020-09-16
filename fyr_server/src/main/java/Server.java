import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * 服务发布
 */
public class Server {

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("please using command: java Server <rmi_host> <rmi_port>");
            System.exit(-1);
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);

        ServiceProvider provider = new ServiceProvider();

        DataNode dataNode = new DataNodeImpl();
        provider.publish(dataNode, host, port);

        Thread.sleep(Long.MAX_VALUE);
    }
}

