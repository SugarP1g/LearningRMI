# Java RMI入门

创建: 2020-02-22 10:00
更新: 2020-08-27 15:39
链接: http://scz.617.cn:8/network/202002221000.txt

--------------------------------------------------------------------------

## 目录

    ☆ 前言
    ☆ Java RMI
        1) HelloRMIInterface.java
        2) HelloRMIInterfaceImpl.java
        3) HelloRMIServer.java
        4) HelloRMIClient.java
        5) HelloRMIServer/HelloRMIClient不在同一台主机上时的幺蛾子
            5.0) 转储"com.sun.proxy.$Proxy0"
            5.1) Java RMI与DCE/MS RPC、ONC/Sun RPC
            5.2) HelloRMIServer2.java
            5.3) 深入LocateRegistry.createRegistry()
            5.4) 深入new HelloRMIInterfaceImpl()
            5.5) 深入r.rebind()
        6) 侦听指定IP、指定PORT
            6.1) HelloRMIServerSocketFactoryImpl.java
            6.2) HelloRMIInterfaceImpl3.java
            6.3) HelloRMIServer3.java
            6.4) 另一种方案
                6.4.1) HelloRMIInterfaceImpl8.java
                6.4.2) HelloRMIDynamicServer8.java
        7) java.rmi.Naming
            7.1) HelloRMIServer4.java
            7.2) HelloRMIClient4.java
            7.3) Naming.rebind
            7.4) Naming.lookup
        8) 分离周知端口与动态端口
            8.1) HelloRMIWellknownServer.java
            8.2) HelloRMIDynamicServer.java
            8.3) 周知端口与动态端口不在同一台主机上时的幺蛾子
            8.4) 周知端口与动态端口不在同一台主机上时的网络通信报文
            8.5) HelloRMIDynamicServer2.java
        9) JDK自带RMI相关工具
            9.1) rmiregistry
                9.1.1) inside rmiregistry
                9.1.2) 扫描识别rmiregistry
       10) 从周知端口获取所有动态端口信息
           10.1) rmiinfo.java
           10.2) rmi-dumpregistry.nse
               10.2.1) HelloRMI_6.cap部分报文解码
           10.3) rmiregistry_detect.nasl
               10.3.1) HelloRMI_7.cap部分报文解码
           10.4) jndiinfo.java
               10.4.1) jndiinfo.policy
       11) JNDI
           11.1) HelloRMIDynamicServer5.java (JNDI+RMI)
           11.2) HelloRMIClient5.java
           11.3) HelloRMIDynamicServer6.java
           11.4) HelloRMIClient6.java
       12) RMI-IIOP
           12.1) HelloRMIInterfaceImpl7.java
               12.1.1) rmic
           12.2) HelloRMIDynamicServer7.java (JNDI+CORBA)
           12.3) HelloRMIClient7.java
           12.4) orbd
               12.4.1) inside orbd
           12.5) 测试RMI-IIOP
               12.5.1) HelloRMIDynamicServer7/HelloRMIClient7不在同一台主机上时的幺蛾子
           12.6) RMI-IIOP vs RMI
    ☆ JNDI+LDAP
        1) 简版LDAP Server
        2) jndi.ldif
        3) HelloRMIInterface.java
        4) HelloRMIInterfaceImpl.java
        5) JNDILDAPServer.java
        6) JNDILDAPClient.java
        7) 编译
        8) 测试
            8.1) 为何有个GET请求404时客户端仍然正常结束
        9) HelloRMIInterfaceImpl8.java
       10) JNDILDAPServer2.java
    ☆ 后记

--------------------------------------------------------------------------

## 前言

参看

《Java RMI入门(2)》
http://scz.617.cn:8/network/202003081810.txt

《Java RMI入门(3)》
http://scz.617.cn:8/network/202003121717.txt

《Java RMI入门(4)》
http://scz.617.cn:8/network/202003191728.txt

《Java RMI入门(5)》
http://scz.617.cn:8/network/202003241127.txt

《Java RMI入门(6)》
http://scz.617.cn:8/network/202004011650.txt

《Java RMI入门(7)》
http://scz.617.cn:8/network/202004101018.txt

《Java RMI入门(8)》
http://scz.617.cn:8/network/202004141657.txt

《Java RMI入门(9)》
http://scz.617.cn:8/network/202004161823.txt

自从 99 年放弃 Java，再没有主动学习过 Java 的正经面，一直到 2019.11。这一拨学习源自试图理解 Java 漏洞所涉及的若干方面，RMI 正是其中之一。

本文是我学习 RMI 之后的笔记。不打算用一些看上去玄之又玄的概念来开场，做为程序员，一个提纲挈领的 "Hello World" 足以入门。

任何有过 DCE/MS RPC、ONC/Sun RPC 编程、协议分析、漏洞挖掘经历的读者很容易理解本篇笔记，假设本文面向的读者是这一类的，只不过没有接触过 Java RMI。

## Java RMI

**RMI** 是 "Remote Method Invocation" 的缩写。

### 1) HelloRMIInterface.java

```java
/*
 * javac -encoding GBK -g HelloRMIInterface.java
 */
  import java.rmi.*;

/*
 * The Interface must always be public and extend Remote.
 *
 * All methods described in the Remote interface must list RemoteException
 * in their throws clause.
 */
  public interface HelloRMIInterface extends Remote
  {
    public String Echo ( String sth ) throws RemoteException;
  }
```

### 2) HelloRMIInterfaceImpl.java

```java
/*
 * javac -encoding GBK -g HelloRMIInterfaceImpl.java
 */
  import java.rmi.RemoteException;
  import java.rmi.server.UnicastRemoteObject;

public class HelloRMIInterfaceImpl extends UnicastRemoteObject implements HelloRMIInterface
{
    private static final long   serialVersionUID    = 0x5120131473637a00L;

    protected HelloRMIInterfaceImpl () throws RemoteException
    {
        super();
    }
    
    @Override
    public String Echo ( String sth ) throws RemoteException
    {
        /*
         * 故意加一对[]，将来抓包时便于识别请求、响应
         */
        return( "[" + sth + "]" );
    }
}
```

### 3) HelloRMIServer.java

```java
/*
 * javac -encoding GBK -g HelloRMIServer.java
 * java HelloRMIServer 1099 HelloRMIInterface
 */
  import java.rmi.registry.*;

public class HelloRMIServer
{
    public static void main ( String[] argv ) throws Exception
    {
        int                 port    = Integer.parseInt( argv[0] );
        String              name    = argv[1];
        /*
         * https://docs.oracle.com/javase/8/docs/api/java/rmi/registry/LocateRegistry.html
         *
         * port默认使用1099/TCP，addr默认使用"0.0.0.0"
         */
        Registry            r       = LocateRegistry.createRegistry( port );
        HelloRMIInterface   hello   = new HelloRMIInterfaceImpl();
        /*
         * https://docs.oracle.com/javase/8/docs/api/java/rmi/registry/Registry.html
         *
         * 第一形参内容任意，起唯一标识作用
         */
        r.rebind( name, hello );
    }
}
```

### 4) HelloRMIClient.java

```java
/*
 * javac -encoding GBK -g HelloRMIClient.java
 * java HelloRMIClient 192.168.65.23 1099 HelloRMIInterface "Hello World"
 */
  import java.rmi.registry.*;

public class HelloRMIClient
{
    public static void main ( String[] argv ) throws Exception
    {
        String              addr    = argv[0];
        int                 port    = Integer.parseInt( argv[1] );
        String              name    = argv[2];
        String              sth     = argv[3];
        /*
         * https://docs.oracle.com/javase/8/docs/api/java/rmi/registry/LocateRegistry.html
         */
        Registry            r       = LocateRegistry.getRegistry( addr, port );
        HelloRMIInterface   hello   = ( HelloRMIInterface )r.lookup( name );
        String              resp    = hello.Echo( sth );
        System.out.println( resp );
    }
}
```

启动服务端:

```bash
$ java HelloRMIServer 1099 HelloRMIInterface
```

测试客户端:

```bash
$ java HelloRMIClient 127.0.0.1 1099 HelloRMIInterface "Hello World"
[Hello World]
```

### 5) HelloRMIServer/HelloRMIClient 不在同一台主机上时的幺蛾子

假设 Linux 是 192.168.65.23，Windows 是 192.168.68.1。

在 Linux 中启动 HelloRMIServer:

```bash
$ java HelloRMIServer 1099 HelloRMIInterface
```

用 `netstat`、`lsof` 确认服务端侦听 "0.0.0.0:1099/TCP"。在客户端用 `nc` 确认远程可达服务端的1099/TCP。

在Windows中放两个类:

- HelloRMIClient.class
- HelloRMIInterface.class

在 Windows 中运行 HelloRMIClient:

```bash
$ java.exe HelloRMIClient 192.168.65.23 1099 HelloRMIInterface "Hello World From Windows"
Exception in thread "main" java.rmi.ConnectException: Connection refused to host: 127.0.0.1; nested exception is:
        java.net.ConnectException: Connection refused: connect
        at sun.rmi.transport.tcp.TCPEndpoint.newSocket(TCPEndpoint.java:619)
        at sun.rmi.transport.tcp.TCPChannel.createConnection(TCPChannel.java:216)
        at sun.rmi.transport.tcp.TCPChannel.newConnection(TCPChannel.java:202)
        at sun.rmi.server.UnicastRef.invoke(UnicastRef.java:129)
        at java.rmi.server.RemoteObjectInvocationHandler.invokeRemoteMethod(RemoteObjectInvocationHandler.java:227)
        at java.rmi.server.RemoteObjectInvocationHandler.invoke(RemoteObjectInvocationHandler.java:179)
        at com.sun.proxy.$Proxy0.Echo(Unknown Source)
        at HelloRMIClient.main(HelloRMIClient.java:20)
Caused by: java.net.ConnectException: Connection refused: connect
        at java.net.DualStackPlainSocketImpl.connect0(Native Method)
        at java.net.DualStackPlainSocketImpl.socketConnect(DualStackPlainSocketImpl.java:79)
        at java.net.AbstractPlainSocketImpl.doConnect(AbstractPlainSocketImpl.java:350)
        at java.net.AbstractPlainSocketImpl.connectToAddress(AbstractPlainSocketImpl.java:206)
        at java.net.AbstractPlainSocketImpl.connect(AbstractPlainSocketImpl.java:188)
        at java.net.PlainSocketImpl.connect(PlainSocketImpl.java:172)
        at java.net.SocksSocketImpl.connect(SocksSocketImpl.java:392)
        at java.net.Socket.connect(Socket.java:589)
        at java.net.Socket.connect(Socket.java:538)
        at java.net.Socket.<init>(Socket.java:434)
        at java.net.Socket.<init>(Socket.java:211)
        at sun.rmi.transport.proxy.RMIDirectSocketFactory.createSocket(RMIDirectSocketFactory.java:40)
        at sun.rmi.transport.proxy.RMIMasterSocketFactory.createSocket(RMIMasterSocketFactory.java:148)
        at sun.rmi.transport.tcp.TCPEndpoint.newSocket(TCPEndpoint.java:613)
        ... 7 more
```

居然抛出异常，后面我会剖析发生了什么。

#### 5.0) 转储 "com.sun.proxy.$Proxy0"

调用栈回溯中出现 "com.sun.proxy.$Proxy0"，这是动态代理机制。有办法把这个动态生成的类从内存中转储出来，其中一个办法是:

```bash
$ mkdir com\sun\proxy
$ java.exe -Dsun.misc.ProxyGenerator.saveGeneratedFiles=true HelloRMIClient 192.168.65.23 1099 HelloRMIInterface "Hello World From Windows"
$ dir com\sun\proxy\$Proxy0.class
```

用 JD-GUI 看 `$Proxy0.class`，没什么可看的，这都是固定套路式的代码，真正起作用的是调用栈回溯中的 `sun.rmi.server.UnicastRef.invoke()`。

#### 5.1) Java RMI 与 DCE/MS RPC、ONC/Sun RPC

开始以为什么报文都没有发往 192.168.65.23，以为客户端直接尝试连接 127.0.0.1。

用Wireshark抓包，发现"192.168.65.1"已经与"192.168.65.23:1099"有交互，抓包观察到"JRMI Call"和"JRMI ReturnData"。在后者的hexdump中看到 "127.0.0.1"，

估计客户端按此指示尝试连接 127.0.0.1 的某个端口。

搜索前面那个异常信息，发现官方有段解释。参看:

[https://docs.oracle.com/javase/8/docs/technotes/guides/rmi/faq.html](https://docs.oracle.com/javase/8/docs/technotes/guides/rmi/faq.html)

A.1 Why do I get an exception for an unexpected hostname and/or port number when I call Naming.lookup?

它这个标题不直接匹配 HelloRMIClient.java，但回答的内容是匹配的。

下面是一种解决方案，启动 HelloRMIServer 时指定一个JVM参数:

```bash
$ java -Djava.rmi.server.hostname=192.168.65.23 HelloRMIServer 1099 HelloRMIInterface
```

重新在 Windows 中测试 HelloRMIClient，这次成功:

```bash
$ java.exe HelloRMIClient 192.168.65.23 1099 HelloRMIInterface "Hello World From Windows"
[Hello World From Windows]
```

抓包，发现 "-Djava.rmi.server.hostname=" 会改变 "JRMI ReturnData" 中的 "127.0.0.1"，这次变成 "192.168.65.23"。

客户端收到 "JRMI ReturnData" 之后，新建了一条到 "192.168.65.23:38070" 的TCP连接，端口号 38070(0x94b6) 也在 "JRMI ReturnData" 中指明。

正常情况下可以翻看 Java RMI 相关文档、JDK 源码，或者逆一下 rt.jar，以搞清楚其中的代码逻辑。不过此刻没心情这么折腾，我用其他办法来试图理解发生了什么。

很多年前对 Windows 平台的 DCE/MS RPC 和 *nix 平台的 ONC/Sun RPC 有过深入研究，2002 年我写过它们之间的简单对比:

| DCE/MS RPC                        | ONC/Sun RPC                          |
| --------------------------------- | ------------------------------------ |
| .idl                              | .x                                   |
| MIDL编译器                        | rpcgen                               |
| NDR                               | XDR                                  |
| endpoint mapper(135/TCP、135/UDP) | RPCBIND/PORTMAPPER(111/TCP、111/UDP) |

上面第四行的东西侦听固定周知端口，那些侦听动态端口的 RPC 服务将自己所侦听的动态端口注册(汇报)给第四行。RPC 客户端首先向第四行查询，以获取动态端口号，继而访问动态端口。

Java RMI 既然也是 RPC 的一种，想必 1099/TCP 地位相当于前述第四行，38070/TCP 是动态端口，每次重启 HelloRMIServer，动态端口会变。

HelloRMIClient 访问 1099/TCP 获取动态端口，对于 Java RMI 来说，还有一个动态 IP 的概念；HelloRMI_1.cap 中的 "JRMI Call" 和 "JRMI ReturnData" 对应这个过程；源码中 `r.lookup()` 对应这个过程。

HelloRMIClient 访问"动态IP+动态端口"进行真正的 RPC 调用，HelloRMI_1.cap 中第二条 TCP 连接(38070/TCP)对应这个过程，源码中 `hello.Echo()` 对应这个过程。

JVM 参数 "-Djava.rmi.server.hostname=" 会影响动态 IP。你可能看过一些其他手段，比如修改 /etc/hosts、配置域名解析之类的，其本质是让动态 IP 符合预期。

最常见的手段是先用 hosname 取服务端主机名，再用 ifconfig 取服务端 IP，在 /etc/hosts 中增加一条"服务端IP 服务端主机名"，重启 HelloRMIServer；我不推荐这种方案。

前面这些内容完全是基于历史经验从架构上猜测而写，非官方描述，切勿当真。没动力翻文档，RPC 就这么点事，换汤不换药，猫叫咪咪、咪咪叫猫罢了。

在服务端查看 HelloRMIServer 侦听的端口:

```bash
$ lsof -lnPR +c0 +f g -o1 -c /java/ | grep IPv4
java    53597 2151     1000   12u  IPv4      RW,ND             696864       0t0       TCP *:1099 (LISTEN)
java    53597 2151     1000   13u  IPv4      RW,ND             696865       0t0       TCP *:38070 (LISTEN)

$ netstat -natp | grep java
tcp        0      0 0.0.0.0:1099            0.0.0.0:*               LISTEN      53597/java
tcp        0      0 0.0.0.0:38070           0.0.0.0:*               LISTEN      53597/java
```

#### 5.2) HelloRMIServer2.java

这个例子不用JVM参数来指定动态IP，而是在源码中设置它。

```java
/*
 * javac -encoding GBK -g HelloRMIServer2.java
 * java HelloRMIServer2 192.168.65.23 1099 HelloRMIInterface
 */
  import java.rmi.registry.*;

public class HelloRMIServer2
{
    public static void main ( String[] argv ) throws Exception
    {
        String              addr    = argv[0];
        int                 port    = Integer.parseInt( argv[1] );
        String              name    = argv[2];
        /*
         * 指定动态IP，而不是默认的"127.0.0.1"。这句必须在createRegistry()
         * 之前，而不是rebind()之前。
         */
        System.setProperty( "java.rmi.server.hostname", addr );
        Registry            r       = LocateRegistry.createRegistry( port );
        HelloRMIInterface   hello   = new HelloRMIInterfaceImpl();
        r.rebind( name, hello );
    }
}
```

```bash
$ java HelloRMIServer2 192.168.65.23 1099 HelloRMIInterface
$ java HelloRMIClient 192.168.65.23 1099 HelloRMIInterface "Hello World"
$ java.exe HelloRMIClient 192.168.65.23 1099 HelloRMIInterface "Hello World From Windows"
```

#### 5.3) 深入LocateRegistry.createRegistry()

这几小节是后补的，揭示 127.0.0.1 在哪个环节出场，java.rmi.server.hostname 又如何生效。

```bash
java -agentlib:jdwp=transport=dt_socket,address=192.168.65.23:8005,server=y,suspend=y \
HelloRMIServer 1099 HelloRMIInterface

jdb -connect com.sun.jdi.SocketAttach:hostname=192.168.65.23,port=8005

stop at sun.rmi.transport.tcp.TCPEndpoint:119
stop at sun.rmi.transport.tcp.TCPEndpoint:131
stop in java.net.Inet4Address.getHostAddress
```

参看:

http://hg.openjdk.java.net/jdk8u/jdk8u/jdk/file/jdk8u232-ga/src/share/classes/sun/rmi/registry/RegistryImpl.java
http://hg.openjdk.java.net/jdk8u/jdk8u/jdk/file/jdk8u232-ga/src/share/classes/sun/rmi/transport/tcp/TCPEndpoint.java
http://hg.openjdk.java.net/jdk8u/jdk8u/jdk/file/jdk8u232-ga/src/share/classes/sun/rmi/server/UnicastServerRef.java
http://hg.openjdk.java.net/jdk8u/jdk8u/jdk/file/jdk8u232-ga/src/share/classes/sun/rmi/transport/tcp/TCPTransport.java
http://hg.openjdk.java.net/jdk8u/jdk8u/jdk/file/jdk8u232-ga/src/share/classes/java/net/ServerSocket.java

简化版调用关系:

```java
LocateRegistry.createRegistry                                   // 8u232
  RegistryImpl.<init>                                           // LocateRegistry:203
    LiveRef.<init>                                              // RegistryImpl:197
                                                                // lref = new LiveRef(id, port)
                                                                // 返回"127.0.0.1:1099"
      TCPEndpoint.<clinit>                                      // LiveRef:93
                                                                // this(objID, TCPEndpoint.getLocalEndpoint(port), true)
                                                                // 先进入TCPEndpoint的static代码块
        localHostKnown = true                                   // TCPEndpoint:107
        TCPEndpoint.getHostnameProperty                         // TCPEndpoint:108
                                                                // localHost = getHostnameProperty()
                                                                // 设置ep.localHost
          GetPropertyAction("java.rmi.server.hostname")         // TCPEndpoint:97
                                                                // 给JVM参数以机会
        if (localHost == null)                                  // TCPEndpoint:111
                                                                // 假设指定过"java.rmi.server.hostname"，则localHost不为null
                                                                // 此时后面的代码都不会去，localHostKnown保持为true
        InetAddress.getLocalHost                                // TCPEndpoint:113
          Inet4AddressImpl.getLocalHostName                     // InetAddress:1475
                                                                // local = impl.getLocalHostName()
                                                                // 与hostname命令的返回结果同步，一般是"localhost"
          if (local.equals("localhost"))                        // InetAddress:1481
          Inet4AddressImpl.loopbackAddress()                    // InetAddress:1482
                                                                // 返回"localhost/127.0.0.1"
        localHostKnown = false                                  // TCPEndpoint:119
        Inet4Address.getHostAddress                             // TCPEndpoint:131
                                                                // localHost = localAddr.getHostAddress()
                                                                // 设置ep.localHost
      TCPEndpoint.getLocalEndpoint                              // LiveRef:93
                                                                // this(objID, TCPEndpoint.getLocalEndpoint(port), true)
        TCPEndpoint.resampleLocalHost                           // TCPEndpoint:201
          TCPEndpoint.getHostnameProperty                       // TCPEndpoint:256
                                                                // 再给"java.rmi.server.hostname"一次机会
            GetPropertyAction("java.rmi.server.hostname")       // TCPEndpoint:97
          return localHost                                      // TCPEndpoint:281
                                                                // 返回ep.localHost
        TCPEndpoint.<init>                                      // TCPEndpoint:207
          this.host = host                                      // TCPEndpoint:172
                                                                // ep.host=127.0.0.1
      LiveRef.<init>                                            // LiveRef:93
                                                                // this(objID, TCPEndpoint.getLocalEndpoint(port), true)
        ep = endpoint                                           // LiveRef:64
                                                                // ep等于[127.0.0.1:port]
    UnicastServerRef.<init>                                     // RegistryImpl:198
                                                                // 第二形参等于RegistryImpl::registryFilter
      this.filter = filter                                      // UnicastServerRef:160
    RegistryImpl.setup                                          // RegistryImpl:198
      UnicastServerRef.exportObject                             // RegistryImpl:213
                                                                // uref此时对应"127.0.0.1:1099"
        UnicastServerRef.setSkeleton                            // UnicastServerRef:232
        LiveRef.exportObject                                    // UnicastServerRef:237
                                                                // ref此时对应"127.0.0.1:1099"
          TCPEndpoint.exportObject                              // LiveRef:147
                                                                // ep此时对应"127.0.0.1:1099"
            TCPTransport.exportObject                           // TCPEndpoint:411
              TCPTransport.listen                               // TCPTransport:254
                                                                // 这个listen()的含义很复杂，不只是TCP层的listen
                                                                // 缺省情况下侦听"0.0.0.0:port"，无论前面的ep是什么
                TCPEndpoint.newServerSocket                     // TCPTransport:335
                  RMIMasterSocketFactory.createServerSocket     // TCPEndpoint:666
                    RMIDirectSocketFactory.createServerSocket   // RMIMasterSocketFactory:345
                      ServerSocket.<init>                       // RMIDirectSocketFactory:45
                                                                // new ServerSocket(port)
                        ServerSocket.<init>                     // ServerSocket:143
                                                                // this(port, 50, null)
                                                                // 重载过的另一个版本，第三形参bindAddr等于null
                          InetSocketAddress.<init>              // ServerSocket:252
                                                                // new InetSocketAddress(bindAddr, port)
                            addr == null ? InetAddress.anyLocalAddress() : addr
                                                                // InetSocketAddress:188
                                                                // addr为null时，替换成"0.0.0.0"
                          ServerSocket.bind                     // ServerSocket:252
                                                                // 这个bind()实际包含了bind+listen
                            AbstractPlainSocketImpl.bind        // ServerSocket:390
                            AbstractPlainSocketImpl.listen      // ServerSocket:391
                  if (listenPort == 0)                          // TCPEndpoint:670
                                                                // 若前面port为0，条件满足，转去"TCPEndpoint:671"
                  TCPEndpoint.setDefaultPort                    // TCPEndpoint:671
                                                                // setDefaultPort(server.getLocalPort(), csf, ssf)
                                                                // server此时包含localport信息，即侦听哪个端口
                    ep.port = port                              // TCPEndpoint:335
                                                                // 在此修正ref中的端口信息
                new NewThreadAction(new AcceptLoop())           // TCPTransport:341
                t.start()                                       // TCPTransport:344
                                                                // 单开一个线程去accept()
```

`LocateRegistry.createRegistry()` 并不受 "java.rmi.server.hostname" 影响，虽然流程中有涉及，但最终的 `listen()` 操作与之无关，缺省情况下侦听 "0.0.0.0:port"。

#### 5.4) 深入new HelloRMIInterfaceImpl()

hello 是 UnicastRemoteObject 子类，后者是 RemoteServer 子类，后者是 RemoteObject 子类，后者有 ref 成员，ref 中包含动态 IP、动态端口信息。

参看:

[http://hg.openjdk.java.net/jdk8u/jdk8u/jdk/file/jdk8u232-ga/src/share/classes/java/rmi/server/UnicastRemoteObject.java](http://hg.openjdk.java.net/jdk8u/jdk8u/jdk/file/jdk8u232-ga/src/share/classes/java/rmi/server/UnicastRemoteObject.java)

简化版调用关系:

```java
hello = new HelloRMIInterfaceImpl()     // HelloRMIServer:19
  UnicastRemoteObject.<init>            // UnicastRemoteObject:180
                                        // this(0)
                                        // 0表示将来随机分配端口
    UnicastRemoteObject.exportObject    // UnicastRemoteObject:198
                                        // exportObject(this, port)
      UnicastServerRef.<init>           // UnicastRemoteObject:320
                                        // new UnicastServerRef(port)
        LiveRef.<init>                  // UnicastServerRef:168
                                        // new LiveRef(port)
          this(new ObjID(), port)       // LiveRef:74
                                        // 缺省情况下返回"127.0.0.1:port"
      UnicastRemoteObject.exportObject  // UnicastRemoteObject:320
                                        // 重载过的另一个版本
                                        // exportObject(obj, new UnicastServerRef(port))
        ((UnicastRemoteObject) obj).ref = sref
                                        // UnicastRemoteObject:381
                                        // hello.ref被赋值"127.0.0.1:0"
        UnicastServerRef.exportObject   // UnicastRemoteObject:383
                                        // sref.exportObject(obj, null, false)
                                        // sref此时对应"127.0.0.1:0"
                                        // 缺省情况下侦听"0.0.0.0:port"，无论sref是什么
                                        // 若之前port为0，后面会用TCPEndpoint.setDefaultPort()
                                        // 修正ref中的端口信息，但不会修正IP
```

缺省情况下 UnicastServerRef() 只会对应 "127.0.0.1:port"，于是，缺省情况下 hello.ref 只会对应 "127.0.0.1:port"，除非指定过 "java.rmi.server.hostname"。

与 `LocateRegistry.createRegistry()` 一样，hello 缺省情况下侦听 "0.0.0.0:port"，侦听的这个 IP 并不受 "java.rmi.server.hostname"影响。

"java.rmi.server.hostname" 只影响 hello.ref。

#### 5.5) 深入r.rebind()

```java
HelloRMIServer.main
  sun.rmi.registry.RegistryImpl.rebind  // HelloRMIServer:25
    java.util.Hashtable<K,V>.put        // RegistryImpl:277
                                        // this.bindings.put(name, obj)
```

hello.ref 指明了动态 IP、动态端口。`r.rebind()` 直接将 hello 放到 RegistryImpl.bindings 中去了，这是绑定操作的本质。

将来 `lookup()` 会在 RegistryImpl.bindings 中根据 name 找 hello，从而知道动态 IP、动态端口。

HelloRMIServer 中 r 来自 LocateRegistry.createRegistry()，不涉及 RegistryImpl_Stub，不涉及 socket 通信。

### 6) 侦听指定IP、指定PORT

从前面 netstat 的输出可以看到，HelloRMIServer 会侦听两个端口，一个是周知端口，另一个是动态端口，这两个端口均侦听在 "0.0.0.0" 上。即使指定那个 JVM 参数或等价操作，仅仅影响 "JRMI ReturnData" 中的动态 IP 字段，HelloRMIServer 的动态端口实际仍然侦听在 "0.0.0.0" 上。

现在想让周知端口、动态端口分别侦听在指定 IP 上，比如 "192.168.65.23"、"127.0.0.1"。此外，不想让系统随机指定动态端口，想自己指定动态端口。这是可
以做到的。

#### 6.1) HelloRMIServerSocketFactoryImpl.java

```java
/*
 * javac -encoding GBK -g HelloRMIServerSocketFactoryImpl.java
 */
  import java.io.*;
  import java.net.*;
  import java.rmi.server.RMIServerSocketFactory;

public class HelloRMIServerSocketFactoryImpl implements RMIServerSocketFactory
{
    /*
     * https://docs.oracle.com/javase/8/docs/api/java/net/InetAddress.html
     */
    private InetAddress bindAddr;

    public HelloRMIServerSocketFactoryImpl ( InetAddress bindAddr )
    {
        this.bindAddr   = bindAddr;
    }
    
    /*
     * https://docs.oracle.com/javase/8/docs/api/java/rmi/server/RMIServerSocketFactory.html
     */
    @Override
    public ServerSocket createServerSocket ( int port ) throws IOException
    {
        /*
         * https://docs.oracle.com/javase/8/docs/api/java/net/ServerSocket.html
         */
        return new ServerSocket( port, 0, bindAddr );
    }
    
    /*
     * https://docs.oracle.com/javase/8/docs/api/java/lang/Object.html
     *
     * An implementation of this interface should implement Object.equals(java.lang.Object)
     * to return true when passed an instance that represents the same
     * (functionally equivalent) server socket factory, and false otherwise
     * (and it should also implement Object.hashCode() consistently with
     * its Object.equals implementation).
     */
    @Override
    public boolean equals ( Object obj )
    {
        return obj != null && this.getClass() == obj.getClass() && this.bindAddr.equals( ((HelloRMIServerSocketFactoryImpl)obj).bindAddr );
    }
}
```

#### 6.2) HelloRMIInterfaceImpl3.java

```java
/*
 * javac -encoding GBK -g HelloRMIInterfaceImpl3.java
 */
  import java.net.InetAddress;
  import java.rmi.RemoteException;
  import java.rmi.server.UnicastRemoteObject;

public class HelloRMIInterfaceImpl3 extends UnicastRemoteObject implements HelloRMIInterface
{
    private static final long   serialVersionUID    = 0x5120131473637a01L;

    protected HelloRMIInterfaceImpl3 ( int port, InetAddress bindAddr ) throws RemoteException
    {
        /*
         * https://docs.oracle.com/javase/8/docs/api/java/rmi/server/UnicastRemoteObject.html
         *
         * if port is zero, an anonymous port is chosen
         */
        super( port, null, new HelloRMIServerSocketFactoryImpl( bindAddr ) );
    }
    
    @Override
    public String Echo ( String sth ) throws RemoteException
    {
        return( "[" + sth + "]" );
    }
}
```

#### 6.3) HelloRMIServer3.java

```java
/*
 * javac -encoding GBK -g HelloRMIServer3.java
 * java HelloRMIServer3 192.168.65.23 1099 127.0.0.1 0 HelloRMIInterface
 */
  import java.net.InetAddress;
  import java.rmi.registry.*;

public class HelloRMIServer3
{
    public static void main ( String[] argv ) throws Exception
    {
        String              addr_0      = argv[0];
        int                 port_0      = Integer.parseInt( argv[1] );
        String              addr_1      = argv[2];
        int                 port_1      = Integer.parseInt( argv[3] );
        String              name        = argv[4];
        /*
         * https://docs.oracle.com/javase/8/docs/api/java/net/InetAddress.html
         */
        InetAddress         bindAddr_0  = InetAddress.getByName( addr_0 );
        InetAddress         bindAddr_1  = InetAddress.getByName( addr_1 );
        System.setProperty( "java.rmi.server.hostname", addr_1 );
        /*
         * https://docs.oracle.com/javase/8/docs/api/java/rmi/registry/LocateRegistry.html
         */
        Registry            r           = LocateRegistry.createRegistry( port_0, null, new HelloRMIServerSocketFactoryImpl( bindAddr_0 ) );
        /*
         * if port is zero, an anonymous port is chosen
         */
        HelloRMIInterface   hello       = new HelloRMIInterfaceImpl3( port_1, bindAddr_1 );
        r.rebind( name, hello );
    }
}
```

让系统随机指定动态端口:

```bash
$ java HelloRMIServer3 192.168.65.23 1099 127.0.0.1 0 HelloRMIInterface

$ netstat -natp | grep java
tcp        0      0 127.0.0.1:33949         0.0.0.0:*               LISTEN      66878/java
tcp        0      0 192.168.65.23:1099      0.0.0.0:*               LISTEN      66878/java

$ java HelloRMIClient 192.168.65.23 1099 HelloRMIInterface "Hello World"
```

服务端显式指定两个IP、两个端口:

```bash
$ java HelloRMIServer3 192.168.65.23 1098 192.168.65.23 1100 HelloRMIInterface

$ netstat -natp | grep java
tcp        0      0 192.168.65.23:1098      0.0.0.0:*               LISTEN      67510/java
tcp        0      0 192.168.65.23:1100      0.0.0.0:*               LISTEN      67510/java
```

在Windows 中测试 HelloRMIClient，注意服务端周知端口被人为设置成 1098/TCP，客户端需要同步改变:

```bash
$ java.exe HelloRMIClient 192.168.65.23 1098 HelloRMIInterface "Hello World From Windows"
```

尽管可以明确指定 HelloRMIServer3 侦听的动态端口，比如前述 1100/TCP，但 HelloRMIClient 不需要关心这种变化，HelloRMIClient 始终会通过周知端口或者说主端口去隐式获取动态端口并发起RPC调用。

#### 6.4) 另一种方案

本小节是后补的，提前引入了"分离周知端口与动态端口"的设定，如果感到困惑，可以回头再来看本小节。

##### 6.4.1) HelloRMIInterfaceImpl8.java

```java
/*
 * javac -encoding GBK -g HelloRMIInterfaceImpl8.java
 */
  import java.rmi.RemoteException;

/*
 * 故意不继承java.rmi.server.UnicastRemoteObject，以演示另一种用法
 */
  public class HelloRMIInterfaceImpl8 implements HelloRMIInterface
  {
    @Override
    public String Echo ( String sth ) throws RemoteException
    {
        return( "[" + sth + "]" );
    }
  }
```

##### 6.4.2) HelloRMIDynamicServer8.java

```java
/*
 * javac -encoding GBK -g HelloRMIDynamicServer8.java
 * java HelloRMIDynamicServer8 192.168.65.23 1099 192.168.65.23 0 HelloRMIInterface
 */
  import java.net.InetAddress;
  import java.rmi.registry.*;
  import java.rmi.server.UnicastRemoteObject;

public class HelloRMIDynamicServer8
{
    public static void main ( String[] argv ) throws Exception
    {
        String                  addr_0      = argv[0];
        int                     port_0      = Integer.parseInt( argv[1] );
        String                  addr_1      = argv[2];
        int                     port_1      = Integer.parseInt( argv[3] );
        String                  name        = argv[4];
        InetAddress             bindAddr_1  = InetAddress.getByName( addr_1 );
        Registry                r           = LocateRegistry.getRegistry( addr_0, port_0 );
        /*
         * HelloRMIInterfaceImpl8没有继承UnicastRemoteObject，这次演示另一
         * 种用法。
         */
        HelloRMIInterface       obj         = new HelloRMIInterfaceImpl8();
        /*
         * if port is zero, an anonymous port is chosen
         */
        HelloRMIInterface       hello       = ( HelloRMIInterface )UnicastRemoteObject.exportObject
        (
            /*
             * 如果直接将"new HelloRMIInterfaceImpl8()"置于此处，后面的
             * r.rebind()无法形成阻塞，进程退出，动态端口关闭。
             */
            obj,
            port_1,
            null,
            new HelloRMIServerSocketFactoryImpl( bindAddr_1 )
        );
        r.rebind( name, hello );
    }
}
```

侦听周知端口、动态端口，其中动态端口显式指定成 1314/TCP:

```bash
$ rmiregistry 1099
$ java HelloRMIDynamicServer8 192.168.65.23 1099 192.168.65.23 1314 HelloRMIInterface
```

检查服务端侦听的端口:

```bash
$ netstat -nltp | egrep "rmiregistry|java"
tcp        0      0 192.168.65.23:1314      0.0.0.0:*               LISTEN      10475/java
tcp        0      0 0.0.0.0:1099            0.0.0.0:*               LISTEN      5676/rmiregistry
```

执行客户端:

```bash
$ java HelloRMIClient 192.168.65.23 1099 HelloRMIInterface "Hello World"
$ java.exe HelloRMIClient 192.168.65.23 1099 HelloRMIInterface "Hello World From Windows"
```
