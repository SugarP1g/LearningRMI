package com.SugarP1g.example1;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class HelloRMIClient {
    public static void main(String[] argv) throws Exception {
        String addr = argv[0];
        int port = Integer.parseInt(argv[1]);
        String name = argv[2];
        String sth = argv[3];
        /*
         * https://docs.oracle.com/javase/8/docs/api/java/rmi/registry/LocateRegistry.html
         */
        Registry r = LocateRegistry.getRegistry(addr, port);
        HelloRMIInterface hello = (HelloRMIInterface) r.lookup(name);
        String resp = hello.Echo(sth);
        System.out.println(resp);
    }
}
