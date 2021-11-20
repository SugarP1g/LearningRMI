package com.SugarP1g.example1;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class HelloRMIServer {
    public static void main(String[] argv) throws Exception {
        int port = Integer.parseInt(argv[0]);
        String name = argv[1];
        /*
         * https://docs.oracle.com/javase/8/docs/api/java/rmi/registry/LocateRegistry.html
         *
         * port默认使用1099/TCP，addr默认使用"0.0.0.0"
         */
        Registry r = LocateRegistry.createRegistry(port);
        HelloRMIInterface hello = new HelloRMIInterfaceImpl();
        /*
         * https://docs.oracle.com/javase/8/docs/api/java/rmi/registry/Registry.html
         *
         * 第一形参内容任意，起唯一标识作用
         */
        r.rebind(name, hello);
    }
}
