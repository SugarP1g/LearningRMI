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

### 7) java.rmi.Naming

#### 7.1) HelloRMIServer4.java

```java
/*
 * javac -encoding GBK -g HelloRMIServer4.java
 * java HelloRMIServer4 192.168.65.23 1099 192.168.65.23 0 HelloRMIInterface
 */
  import java.net.InetAddress;
  import java.rmi.registry.*;
  import java.rmi.Naming;

public class HelloRMIServer4
{
    public static void main ( String[] argv ) throws Exception
    {
        String              addr_0      = argv[0];
        int                 port_0      = Integer.parseInt( argv[1] );
        String              addr_1      = argv[2];
        int                 port_1      = Integer.parseInt( argv[3] );
        String              name        = argv[4];
        String              url         = String.format( "rmi://%s:%d/%s", addr_0, port_0, name );
        InetAddress         bindAddr_0  = InetAddress.getByName( addr_0 );
        InetAddress         bindAddr_1  = InetAddress.getByName( addr_1 );
        System.setProperty( "java.rmi.server.hostname", addr_1 );
        Registry            r           = LocateRegistry.createRegistry( port_0, null, new HelloRMIServerSocketFactoryImpl( bindAddr_0 ) );
        HelloRMIInterface   hello       = new HelloRMIInterfaceImpl3( port_1, bindAddr_1 );
        /*
         * https://docs.oracle.com/javase/8/docs/api/java/rmi/Naming.html
         *
         * 这一步过去用的是
         *
         * r.rebind( name, hello );
         *
         * 第一形参URL指定PORTMAPPER等价物所在，形如:
         *
         * rmi://127.0.0.1:1099/HelloRMIInterface
         */
        Naming.rebind( url, hello );
    }
}
```

#### 7.2) HelloRMIClient4.java

```java
/*
 * javac -encoding GBK -g HelloRMIClient4.java
 * java HelloRMIClient4 "rmi://192.168.65.23:1099/HelloRMIInterface" "Hello World"
 */
  import java.rmi.Naming;

public class HelloRMIClient4
{
    public static void main ( String[] argv ) throws Exception
    {
        String              url     = argv[0];
        String              sth     = argv[1];
        /*
         * 这一步过去用的是
         *
         * r = LocateRegistry.getRegistry( addr, port )
         * r.lookup( name )
         */
        HelloRMIInterface   hello   = ( HelloRMIInterface )Naming.lookup( url );
        String              resp    = hello.Echo( sth );
        System.out.println( resp );
    }
}
```

```bash
$ java HelloRMIServer4 192.168.65.23 1099 192.168.65.23 0 HelloRMIInterface
$ java HelloRMIClient4 "rmi://192.168.65.23:1099/HelloRMIInterface" "Hello World"
$ java.exe HelloRMIClient4 "rmi://192.168.65.23:1099/HelloRMIInterface" "Hello World From Windows"

java.rmi.Naming用"rmi://..."这种形式的url指定周知IP、周知端口等信息。
```

#### 7.3) Naming.rebind

java.rmi.Naming 是对 java.rmi.registry 的封装使用，没有本质区别。

[http://hg.openjdk.java.net/jdk8u/jdk8u/jdk/file/jdk8u232-ga/src/share/classes/java/rmi/Naming.java](http://hg.openjdk.java.net/jdk8u/jdk8u/jdk/file/jdk8u232-ga/src/share/classes/java/rmi/Naming.java)

```java
public static void rebind ( String name, Remote obj )
    throws RemoteException, MalformedURLException
{
    ParsedNamingURL parsed      = parseURL( name );
    Registry        registry    = getRegistry( parsed );
    if ( obj == null )
    {
        throw new NullPointerException( "cannot bind to null" );
    }
    registry.rebind( parsed.name, obj );
}
```

#### 7.4) Naming.lookup

```java
public static Remote lookup ( String name )
    throws NotBoundException, MalformedURLException, RemoteException
{
    ParsedNamingURL parsed      = parseURL( name );
    Registry        registry    = getRegistry( parsed );
    if ( parsed.name == null )
    {
        return registry;
    }
    return registry.lookup( parsed.name );
}
```

### 8) 分离周知端口与动态端口

就 RPC 架构来说，周知端口提供的服务与动态端口提供的服务完全两码事。前面的 HelloRMIServer 为了演示便捷，将这两种端口放在同一个 main() 侦听，可以分离它们到不同进程中去，但没法分离它们到不同主机中去，Java RMI 对此有安全限制。

这种分离是一种自然而然的需求，周知端口只有一个，动态端口可以有很多，对应不同的远程服务。

#### 8.1) HelloRMIWellknownServer.java

```java
/*
 * javac -encoding GBK -g HelloRMIWellknownServer.java
 * java HelloRMIWellknownServer 192.168.65.23 1099 192.168.65.23
 */
  import java.net.InetAddress;
  import java.rmi.registry.*;

public class HelloRMIWellknownServer
{
    public static void main ( String[] argv ) throws Exception
    {
        /*
         * 变量命名故意如此，以与HelloRMIServer4.java产生更直观的对比
         */
        String              addr_0      = argv[0];
        int                 port_0      = Integer.parseInt( argv[1] );
        String              addr_1      = argv[2];
        InetAddress         bindAddr_0  = InetAddress.getByName( addr_0 );
        /*
         * 这个设置只影响"JRMI ReturnData"中的动态IP字段，不影响动态端口实
         * 际侦听的地址。
         */
        System.setProperty( "java.rmi.server.hostname", addr_1 );
        /*
         * 这会侦听周知端口，应该是有个异步机制在背后，不需要单开一个线程
         * 放这句代码。
         */
        Registry            r           = LocateRegistry.createRegistry( port_0, null, new HelloRMIServerSocketFactoryImpl( bindAddr_0 ) );
        /*
         * 类似C语言的getchar()，最简单的阻塞。否则本进程结束，周知端口关
         * 闭。这个阻塞不影响对周知端口的访问。老年程序员的脑洞就是大。
         */
        System.in.read();
    }
}
```

上述代码中关于 "java.rmi.server.hostname" 的注释是错误的，一个半月后随着对 RMI 机制的深入调试，回过头来修正文字。事实上本例中 `System.setProperty()` 没有任何用处，废代码一条。如下命令不影响客户端的远程访问:

```bash
$ java HelloRMIWellknownServer 192.168.65.23 1099 127.0.0.1
```

#### 8.2) HelloRMIDynamicServer.java

```java
/*
 * javac -encoding GBK -g HelloRMIDynamicServer.java
 * java HelloRMIDynamicServer 192.168.65.23 1099 192.168.65.23 0 HelloRMIInterface
 */
  import java.net.InetAddress;
  import java.rmi.registry.*;

public class HelloRMIDynamicServer
{
    public static void main ( String[] argv ) throws Exception
    {
        String              addr_0      = argv[0];
        int                 port_0      = Integer.parseInt( argv[1] );
        String              addr_1      = argv[2];
        int                 port_1      = Integer.parseInt( argv[3] );
        String              name        = argv[4];
        InetAddress         bindAddr_1  = InetAddress.getByName( addr_1 );
        /*
         * getRegistry()并不会发起到周知端口的TCP连接
         */
        Registry            r           = LocateRegistry.getRegistry( addr_0, port_0 );
        HelloRMIInterface   hello       = new HelloRMIInterfaceImpl3( port_1, bindAddr_1 );
        /*
         * 向周知端口注册(汇报)动态端口，等待客户端前来访问。rebind()会发
         * 起到周知端口的TCP连接。
         */
        r.rebind( name, hello );
    }
}
```

先侦听周知端口:

```bash
$ java HelloRMIWellknownServer 192.168.65.23 1099 192.168.65.23
```

再侦听动态端口:

```bash
$ java HelloRMIDynamicServer 192.168.65.23 1099 192.168.65.23 0 HelloRMIInterface
```

在 Windows 中执行客户端:

```bash
$ java.exe HelloRMIClient4 "rmi://192.168.65.23:1099/HelloRMIInterface" "Hello World From Windows"
```

#### 8.3) 周知端口与动态端口不在同一台主机上时的幺蛾子

试图让周知端口跑在 192.168.65.23 上，让动态端口跑在 192.168.65.20 上，失败。

在 192.168.65.23 上:

```bash
$ java HelloRMIWellknownServer 192.168.65.23 1099 192.168.65.20
```

在 192.168.65.20 上:

```bash
$ ls -1
HelloRMIDynamicServer.class
HelloRMIInterface.class
HelloRMIInterfaceImpl3.class
HelloRMIServerSocketFactoryImpl.class

$ java_8_232 HelloRMIDynamicServer 192.168.65.23 1099 192.168.65.20 0 HelloRMIInterface
Exception in thread "main" java.rmi.ServerException: RemoteException occurred in server thread; nested exception is:
        java.rmi.AccessException: Registry.rebind disallowed; origin /192.168.65.20 is non-local host
        at sun.rmi.server.UnicastServerRef.dispatch(UnicastServerRef.java:389)
        at sun.rmi.transport.Transport$1.run(Transport.java:200)
        at sun.rmi.transport.Transport$1.run(Transport.java:197)
        at java.security.AccessController.doPrivileged(Native Method)
        at sun.rmi.transport.Transport.serviceCall(Transport.java:196)
        at sun.rmi.transport.tcp.TCPTransport.handleMessages(TCPTransport.java:573)
        at sun.rmi.transport.tcp.TCPTransport$ConnectionHandler.run0(TCPTransport.java:834)
        at sun.rmi.transport.tcp.TCPTransport$ConnectionHandler.lambda$run$0(TCPTransport.java:688)
        at java.security.AccessController.doPrivileged(Native Method)
        at sun.rmi.transport.tcp.TCPTransport$ConnectionHandler.run(TCPTransport.java:687)
        at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
        at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
        at java.lang.Thread.run(Thread.java:748)
        at sun.rmi.transport.StreamRemoteCall.exceptionReceivedFromServer(StreamRemoteCall.java:303)
        at sun.rmi.transport.StreamRemoteCall.executeCall(StreamRemoteCall.java:279)
        at sun.rmi.server.UnicastRef.invoke(UnicastRef.java:375)
        at sun.rmi.registry.RegistryImpl_Stub.rebind(RegistryImpl_Stub.java:158)
        at HelloRMIDynamicServer.main(HelloRMIDynamicServer.java:27)
Caused by: java.rmi.AccessException: Registry.rebind disallowed; origin /192.168.65.20 is non-local host
        at sun.rmi.registry.RegistryImpl.checkAccess(RegistryImpl.java:350)
        at sun.rmi.registry.RegistryImpl_Skel.dispatch(RegistryImpl_Skel.java:142)
        at sun.rmi.server.UnicastServerRef.oldDispatch(UnicastServerRef.java:469)
        at sun.rmi.server.UnicastServerRef.dispatch(UnicastServerRef.java:301)
        at sun.rmi.transport.Transport$1.run(Transport.java:200)
        at sun.rmi.transport.Transport$1.run(Transport.java:197)
        at java.security.AccessController.doPrivileged(Native Method)
        at sun.rmi.transport.Transport.serviceCall(Transport.java:196)
        at sun.rmi.transport.tcp.TCPTransport.handleMessages(TCPTransport.java:573)
        at sun.rmi.transport.tcp.TCPTransport$ConnectionHandler.run0(TCPTransport.java:834)
        at sun.rmi.transport.tcp.TCPTransport$ConnectionHandler.lambda$run$0(TCPTransport.java:688)
        at java.security.AccessController.doPrivileged(Native Method)
        at sun.rmi.transport.tcp.TCPTransport$ConnectionHandler.run(TCPTransport.java:687)
        at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
        at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
        at java.lang.Thread.run(Thread.java:748)

HelloRMIDynamicServer抛出异常。从调用栈回溯中注意到:

sun.rmi.registry.RegistryImpl.checkAccess()
```

参看:

[http://hg.openjdk.java.net/jdk8u/jdk8u/jdk/file/jdk8u232-ga/src/share/classes/sun/rmi/registry/RegistryImpl.java](http://hg.openjdk.java.net/jdk8u/jdk8u/jdk/file/jdk8u232-ga/src/share/classes/sun/rmi/registry/RegistryImpl.java)

```java
/**
 * Check that the caller has access to perform indicated operation.
 * The client must be on same the same host as this server.
 */
  public static void checkAccess(String op) throws AccessException
```

`checkAccess()` 会检查 `rebind()` 的源 IP 与目标 IP 是否位于同一主机，不是则抛出异常 java.rmi.AccessException。

从 TCP 层看没有限制，前述检查是 Java RMI 自己加的，出于安全考虑？这大大限制了 Java RMI 的分布式应用。搜了一下，没有官方绕过方案。

自己 Patch rt.jar 就比较扯了，不考虑这种 Hacking 方案，无论静态还是动态 Patch。

#### 8.4) 周知端口与动态端口不在同一台主机上时的网络通信报文

可以在 192.168.65.23 上用 tcpdump 抓包:

```bash
$ tcpdump -i ens33 -s 68 -ntpq "tcp port 1099"
$ tcpdump -i ens33 -s 4096 -ntpqX "tcp port 1099"
$ tcpdump -i ens33 -s 4096 -ntpq -w HelloRMI_2.cap "tcp port 1099"
```

也可以直接在 VMnet8 上用 Wireshark 抓两台虚拟机之间的通信。这两种方案不等价，后者 MTU 是 1500，较大的 "JRMI ReturnData" 分散到两个TCP报文中，Wireshark 没有重组它们。而 HelloRMI_2.cap 中 "JRMI ReturnData" 是单个TCP报文。

观察 HelloRMI_2.cap 中 "JRMI ReturnData"，发现 192.168.65.23 已经在抛异常:

```bash
java.rmi.AccessException: Registry.rebind disallowed; origin /192.168.65.20 is non-local host
```

正是 checkAccess() 做的检查，只不过周知端口将异常通过 "JRMI ReturnData" 送至动态端口，没有在 Console 上直接显示异常。

可以调试周知端口，通过断点确认流程经过 `checkAccess()`。

#### 8.5) HelloRMIDynamicServer2.java

本例使用 java.rmi.Naming。

```java
/*
 * javac -encoding GBK -g HelloRMIDynamicServer2.java
 * java HelloRMIDynamicServer2 192.168.65.23 1099 192.168.65.23 0 HelloRMIInterface
 */
  import java.net.InetAddress;
  import java.rmi.Naming;

public class HelloRMIDynamicServer2
{
    public static void main ( String[] argv ) throws Exception
    {
        String              addr_0      = argv[0];
        int                 port_0      = Integer.parseInt( argv[1] );
        String              addr_1      = argv[2];
        int                 port_1      = Integer.parseInt( argv[3] );
        String              name        = argv[4];
        String              url         = String.format( "rmi://%s:%d/%s", addr_0, port_0, name );
        InetAddress         bindAddr_1  = InetAddress.getByName( addr_1 );
        HelloRMIInterface   hello       = new HelloRMIInterfaceImpl3( port_1, bindAddr_1 );
        Naming.rebind( url, hello );
    }
}
```

在 Linux 中启动两个服务端:

```bash
$ java HelloRMIWellknownServer 192.168.65.23 1099 192.168.65.23
$ java HelloRMIDynamicServer2 192.168.65.23 1099 192.168.65.23 0 HelloRMIInterface
```

在 Windows 中执行客户端:

```bash
$ java.exe HelloRMIClient4 "rmi://192.168.65.23:1099/HelloRMIInterface" "Hello World From Windows"
```

### 9) JDK自带RMI相关工具

#### 9.1) rmiregistry

JDK 自带 rmiregistry 用来单独提供周知端口服务，可以指定端口号。

rmiregistry 的地位相当于 ONC/Sun RPC 的 rpcbind。

```bash
$ rmiregistry 1099

$ netstat -natp | grep 1099
tcp        0      0 0.0.0.0:1099            0.0.0.0:*               LISTEN      55074/rmiregistry
```

上面这条命令侦听周知端口，相当于:

```bash
$ java HelloRMIWellknownServer 0.0.0.0 1099 192.168.65.23
```

rmiregistry 不像 HelloRMIWellknownServer，后者可以指定周知端口侦听什么 IP，前者只能让周知端口侦听 0.0.0.0。

测试 rmiregistry 是否可用:

```bash
$ java HelloRMIDynamicServer2 192.168.65.23 1099 192.168.65.23 0 HelloRMIInterface

$ netstat -natp | grep java
tcp        0      0 192.168.65.23:38063     0.0.0.0:*               LISTEN      56281/java

$ java HelloRMIClient4 "rmi://127.0.0.1:1099/HelloRMIInterface" "Hello World"
$ java.exe HelloRMIClient4 "rmi://192.168.65.23:1099/HelloRMIInterface" "Hello World From Windows"
```

##### 9.1.1) inside rmiregistry

不知上哪找 rmiregistry 的源码，用 IDA 逆一下，main() 中在调 JLI_Launch()。

参看:

[http://hg.openjdk.java.net/jdk8u/jdk8u/jdk/file/tip/src/share/bin/java.c](http://hg.openjdk.java.net/jdk8u/jdk8u/jdk/file/tip/src/share/bin/java.c)

```c
/*
 * Entry point.
 */
  int
  JLI_Launch(int argc, char ** argv,              /* main argc, argc */
        int jargc, const char** jargv,          /* java args */
        int appclassc, const char** appclassv,  /* app classpath */
        const char* fullversion,                /* full version defined */
        const char* dotversion,                 /* dot version defined */
        const char* pname,                      /* program name */
        const char* lname,                      /* launcher name */
        jboolean javaargs,                      /* JAVA_ARGS */
        jboolean cpwildcard,                    /* classpath wildcard*/
        jboolean javaw,                         /* windows-only javaw */
        jint ergo                               /* ergonomics class policy */
  )
```

可以用 jinfo 查看 rmiregistry 进程:

```bash
$ jinfo 55074
...
sun.java.command = sun.rmi.registry.RegistryImpl 1099
...

"rmiregistry 1099"相当于:

$ java sun.rmi.registry.RegistryImpl 1099
```

参看:

[http://hg.openjdk.java.net/jdk8u/jdk8u/jdk/file/jdk8u232-ga/src/share/classes/sun/rmi/registry/RegistryImpl.java](http://hg.openjdk.java.net/jdk8u/jdk8u/jdk/file/jdk8u232-ga/src/share/classes/sun/rmi/registry/RegistryImpl.java)

`createRegistry()` 调的就是 `RegistryImpl()`。RegistryImpl.java 中有 `main()`:

```java
/**
 * Main program to start a registry. <br>
 * The port number can be specified on the command line.
 */
  public static void main(String args[])
  {
  ...
        final int regPort = (args.length >= 1) ? Integer.parseInt(args[0])
                                               : Registry.REGISTRY_PORT;
        try {
            registry = AccessController.doPrivileged(
                new PrivilegedExceptionAction<RegistryImpl>() {
                    public RegistryImpl run() throws RemoteException {
                        return new RegistryImpl(regPort);
                    }
                }, getAccessControlContext(regPort));
        } catch (PrivilegedActionException ex) {
            throw (RemoteException) ex.getException();
        }

        // prevent registry from exiting
        while (true) {
            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException e) {
            }
        }
    } catch (NumberFormatException e) {
        System.err.println(MessageFormat.format(
            getTextResource("rmiregistry.port.badnumber"),
            args[0] ));
        System.err.println(MessageFormat.format(
            getTextResource("rmiregistry.usage"),
            "rmiregistry" ));
    } catch (Exception e) {
        e.printStackTrace();
    }
    System.exit(1);
  }
```

从源码看出，这个 `main()` 只能指定端口，不能指定 IP。

##### 9.1.2) 扫描识别rmiregistry

Nessus 有个插件 rmi_remote_object_detect.nasl，核心操作对应 rmi_connect()，可以抓包看看它触发的通信报文。

```bash
$ vi rmi_remote_object_detect_mini.nasl
```

```c
#
# (C) Tenable Network Security, Inc.
#

include("compat.inc");

if ( description )
{
    script_id( 22363 );
    exit( 0 );
}

include("byte_func.inc");
include("global_settings.inc");
include("misc_func.inc");
include("audit.inc");
include("rmi.inc");

port    = 1099;
#
# verify we can connect to this port using RMI
#
soc     = rmi_connect( port:port );
close( soc );
```

侦听周知端口:

```bash
$ rmiregistry 1099
```

在 Windows 上运行 Nessus 插件:

```bash
$ nasl -t 192.168.65.23 rmi_remote_object_detect_mini.nasl
```

这个没有输出，只是来触发通信的，抓包(HelloRMI_8.cap)。

```
No.     len   Protocol src                   dst                   sport  dport  Info
      4 61    RMI      192.168.65.1          192.168.65.23         58334  1099   JRMI, Version: 2, StreamProtocol

Internet Protocol Version 4, Src: 192.168.65.1, Dst: 192.168.65.23
Transmission Control Protocol, Src Port: 58334, Dst Port: 1099, Seq: 1, Ack: 1, Len: 7
Java RMI
    Magic: 0x4a524d49
    Version: 2
    Protocol: StreamProtocol (0x4b)

0030                    4a 52 4d 49 00 02 4b                  JRMI..K
```

```
No.     len   Protocol src                   dst                   sport  dport  Info
      6 73    RMI      192.168.65.23         192.168.65.1          1099   58334  JRMI, ProtocolAck

Internet Protocol Version 4, Src: 192.168.65.23, Dst: 192.168.65.1
Transmission Control Protocol, Src Port: 1099, Dst Port: 58334, Seq: 1, Ack: 8, Len: 19
Java RMI
    Input Stream Message: ProtocolAck (0x4e)
    EndPointIdentifier
        Length: 12
        Hostname: 192.168.65.1
        Port: 58334

0030                    4e 00 0c 31 39 32 2e 31 36 38         N..192.168
0040  2e 36 35 2e 31 00 00 e3 de                        .65.1....
```

这部分有官方文档，参看:

- 10.2 RMI Transport Protocol
- [https://docs.oracle.com/javase/8/docs/platform/rmi/spec/rmi-protocol3.html](https://docs.oracle.com/javase/8/docs/platform/rmi/spec/rmi-protocol3.html)

一种可行的扫描方案，向 1099/TCP 发送 "4a 52 4d 49 00 02 4b"，启用读超时的情况下尝试读取23或更多字节的响应数据。如果响应数据长度不在[14,22]闭区间，服务端不是 rmiregistry 或等价服务。

检查响应数据前两字节是否是 "4e 00"；0x4e 表示 ProtocolAck，接下来的0其实是另一个2字节长度字段的高字节；如果服务端确为 rmiregistry 或等价服务，响应数据 `buf[1:3]` 是个长度字段，指明后面的 IP 串长度，结尾没有 NUL 字符；这个长度最大 15、最小 7，其高字节必是 0。[14,22] 是这么来的:

```
14=1+2+7+4
22=1+2+15+4
```

有人可能会想，为什么不检查响应数据中的 IP 是否等于请求包源 IP？考虑 NAT 情形，不建议这样做。

快速扫描方案，向 1099/TCP 发送 "4a 52 4d 49 00 02 4b"，启用读超时的情况下尝试读取2字节的响应数据，检查响应数据是否等于 "4e 00"。

### 10) 从周知端口获取所有动态端口信息

#### 10.1) rmiinfo.java

ONC/Sun RPC 有个 rpcinfo，可以列出向 rpcbind 注册过的所有动态端口。

DCE/MS RPC 当年没有官方工具干类似的事，但有相应 API。我写过 135dump.c，还写过 NASL 版本。

Java RMI 有相应 API 干类似的事。

```java
/*
 * javac -encoding GBK -g rmiinfo.java
 * java rmiinfo 192.168.65.23 1099
 */
  import java.rmi.registry.*;

public class rmiinfo
{
    public static void main ( String[] argv ) throws Exception
    {
        String      addr    = argv[0];
        int         port    = Integer.parseInt( argv[1] );
        Registry    r       = LocateRegistry.getRegistry( addr, port );
        String[]    names   = r.list();
        // for ( int i = 0; i < names.length; i++ )
        // {
        //     System.out.println( names[i] );
        // }
        for ( String name : names )
        {
            System.out.println( name );
        }
    }
}
```

侦听周知端口、动态端口:

```bash
$ rmiregistry 1099
$ java HelloRMIDynamicServer2 192.168.65.23 1099 192.168.65.23 0 HelloRMIInterface
```

用 rmiinfo 向周知端口查询所有注册过来的 name:

```bash
$ java.exe rmiinfo 192.168.65.23 1099
HelloRMIInterface
```

本例只有一个动态端口向周知端口注册过，rmiinfo 只返回一个 name，现实世界中可能返回很多 name。

`r.list()` 这个API太弱了，只返回 name，不返回与之对应的动态端口号。如果用标准 Java API 进行 RPC 调用，有 name 就够了。如果想绕过周知端口直接访问动态端口，只有 name 是不行的。

抓包看 `r.list()` 的通信报文(HelloRMI_3.cap)。起初我以为底层返回了动态端口，只是上层 API 只返回 name，结果 "JRMI ReturnData" 中确实只有 name 信息。

`r.lookup()` 对应的 "JRMI ReturnData" 中包含 name 对应的动态端口，但 API 没有显式返回这个信息。总的来说，Java RMI 就不想让你知道动态端口这回事，想跟你玩点玄之又玄的其他概念。

一个可行的办法是自己写 Java RMI 客户端，做协议封装、解码。不过有个更简单的办法，参看后面的 jndiinfo.java。
