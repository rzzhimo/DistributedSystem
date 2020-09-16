
public class testlock {
    public static void main(String args[]){
        MyThread mt1 = new MyThread("线程A") ;    // 实例化对象
        MyThread mt2 = new MyThread("线程B") ;    // 实例化对象
        Thread t1 = new Thread(mt1) ;       // 实例化Thread类对象
        Thread t2 = new Thread(mt2) ;       // 实例化Thread类对象
        t1.start() ;    // 启动多线程
        t2.start() ;    // 启动多线程
    }
}
