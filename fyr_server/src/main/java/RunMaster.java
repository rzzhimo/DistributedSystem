public class RunMaster {
    public static void main(String[] args) throws Exception {

        MasterProvider masterProvider = new MasterProvider();
        Master master = new MasterImpl();

       masterProvider.publish(master);
        Thread.sleep(Long.MAX_VALUE);
    }
}
