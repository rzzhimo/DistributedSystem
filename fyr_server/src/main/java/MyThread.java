class MyThread implements Runnable{ // 实现Runnable接口，作为线程的实现类
    private String name ;       // 表示线程的名称
    public MyThread(String name){
        this.name = name ;      // 通过构造方法配置name属性
    }
    @Override
    public void run(){  // 覆写run()方法，作为线程 的操作主体
        for(int i=0;i<5;i++){
            System.out.println(name + "运行，i = " + i) ;
            ServiceConsumer consumer = new ServiceConsumer();
            DataNode dataNode = consumer.allDataNode().get(0);
            try {
                dataNode.createData(name+i+"st",i+"haha");
                System.out.println("插入的值为："+dataNode.getValue(name+i+"st"));

            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }
};
