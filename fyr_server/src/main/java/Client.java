import java.security.MessageDigest;
import java.util.LinkedList;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * RMI客户端
 */
public class Client {

    public static void main(String[] args) throws Exception {


        //Map<String,DataNode> backupMap = consumer.getBackup();
        while (true){
            ServiceConsumer consumer = new ServiceConsumer();
            DataNode dataNode = null;
            LinkedList<DataNode> allDatanode = consumer.allDataNode();

            System.out.println("请选择操作:1、create;2、modify;3、getValue;4、deleteData;5、某个key所在节点所有数据;6、某个key所在节点的备份节点和它的数据");
            Scanner lll = new Scanner(System.in);
            int choice = lll.nextInt();
            //System.out.println(choice);
            switch (choice){
                case 1:

                    System.out.println("create:请输入key和value");
                    Scanner kAv = new Scanner(System.in);
                    String key = kAv.next();
                    String value = kAv.next();
                    int hash =key.hashCode();
                    int index = hash%allDatanode.size();
                    dataNode = allDatanode.get(index);

                    try {
                        dataNode.createData(key,value);
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    System.out.println("创造成功");
                    break;
                case 2:
                    System.out.println("modify:请输入key和value");
                    Scanner kAv1 = new Scanner(System.in);
                    String key1 = kAv1.next();
                    String value1 = kAv1.next();
                    hash=key1.hashCode();
                    index = hash%allDatanode.size();
                    dataNode = allDatanode.get(index);
                    Boolean r1 =dataNode.modify(key1,value1);
                    if(r1==true){
                        System.out.println("modify 成功了");
                    }else {
                        System.out.println("modify 失败了");
                    }
                    break;
                case 3:
                    System.out.println("getValue:请输入key");
                    Scanner kAv2 = new Scanner(System.in);
                    String key2 = kAv2.next();


                    hash=key2.hashCode();
                    index = hash%allDatanode.size();
                    dataNode = allDatanode.get(index);
                    String r2 = dataNode.getValue(key2);
                    System.out.println(key2+"对应的value为："+r2);
                    break;
                case 4:
                    System.out.println("deleteDate:请输入key");
                    Scanner kAv3 = new Scanner(System.in);
                    String key3 = kAv3.next();

                    hash=key3.hashCode();
                    index = hash%allDatanode.size();
                    dataNode = allDatanode.get(index);

                    Boolean r3 = dataNode.deleteData(key3);
                    if(r3==true){
                        System.out.println("deleteDate 成功了");
                    }else {
                        System.out.println("deleteDate 失败了");
                    }
                    break;
                case 5:
                    System.out.println(":请输入key");
                    Scanner kAv4 = new Scanner(System.in);
                    String key4 = kAv4.next();

                    hash=key4.hashCode();
                    index = hash%allDatanode.size();
                    dataNode = allDatanode.get(index);

                    System.out.println(dataNode.allKeyValue().toString());
                    break;
                case 6:
                    System.out.println(":请输入key");
                    Scanner kAv5 = new Scanner(System.in);
                    String key5 = kAv5.next();

                    hash=key5.hashCode();
                    index = hash%allDatanode.size();
                    dataNode = allDatanode.get(index);

                    Map<String,DataNode> backupMap = dataNode.getbackupMap();
                    if(backupMap!=null){
                        System.out.println("该节点的备份的rmi为:"+backupMap.keySet().toString());
                        for (DataNode back:backupMap.values()){
                            System.out.println("该节点的备份的rmi数据为:"+back.allKeyValue().toString());
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }
}

