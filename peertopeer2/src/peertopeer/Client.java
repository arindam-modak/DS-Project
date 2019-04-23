package peertopeer;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Scanner;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.io.IOException;
import java.lang.ProcessBuilder;

public class Client {

    static final int fnamesize = 255;
    static class Trienode{
         
        Trienode[] childs = new Trienode[fnamesize];
        boolean endofword;

        public Trienode() {
            endofword = false;
            for(int i=0;i<fnamesize;i++){
                childs[i]=null;
            }
        }  
    }
    static Trienode root;
    static void insert(String key) 
    { 
        int level; 
        int length = key.length(); 
        int index; 
        Trienode pCrawl = root; 
       
        for (level = 0; level < length; level++) 
        { 
            index = key.charAt(level) - '\0'; 
            if (pCrawl.childs[index] == null) 
                pCrawl.childs[index] = new Trienode(); 
       
            pCrawl = pCrawl.childs[index]; 
        } 
       
        // mark last node as leaf 
        pCrawl.endofword = true; 
    } 
    static boolean search(String key) 
    { 
        int level; 
        int length = key.length(); 
        int index; 
        Trienode pCrawl = root; 
       
        for (level = 0; level < length; level++) 
        { 
            index = key.charAt(level) - '\0'; 
       
            if (pCrawl.childs[index] == null) 
                return false; 
       
            pCrawl = pCrawl.childs[index]; 
        } 
       
        return (pCrawl != null && pCrawl.endofword); 
    } 
    public static void main(String[] args) throws IOException {
        int port = 5056;
        int flag = 0;
        Thread tt2 = null;
        int flag4 = 0;
        String myIP = null;
        String ip="172.20.33.206";
        int port1=5056;
        
        while(true)
        {
            flag=0;
            ArrayList<String> onlineClients = new ArrayList<>();
            try {
                Scanner scn = new Scanner(System.in);

                // getting localhost ip
                // InetAddress ip = InetAddress.getByName("localhost");

                // establish the connection with server port 5056
                
                Socket s = new Socket(ip, port1);
                System.out.println(ip);
                root=new Trienode();
                final File folder = new File("./src/peertopeer/");
                int c=0;
                for (final File f : folder.listFiles()) 
                {
//                    System.out.println(f.toString().substring(17));
                    insert(f.toString().substring(17));                    
                }
                
               
                
                
                // obtaining input and out streams
                DataInputStream dis = new DataInputStream(s.getInputStream());
                DataOutputStream dos = new DataOutputStream(s.getOutputStream());
                // the following loop performs the exchange of
                // information between client and client handler
                while (true) {
                    String received = dis.readUTF();
                    received = received.trim();
                    // System.out.println(received);
                    if (received.split(" ")[0].equals("IP"))
                    {
                        myIP = received.split(" ")[1];
                        for(int i=2;i<received.split(" ").length;i++)
                        {
                            onlineClients.add(received.split(" ")[i]);
                        }
                    }
                    else if (received.equals("MAC")) {
                        String macadd = getmac();
                        dos.writeUTF(macadd);
                    } else if (received.split(" ")[0].equals("OnlineClients")) {
                        System.out.println(received);

                    }
                    else if(received.split(" ")[0].equals("HaveFile?")){
                        if(search(received.split(" ")[1]))
                        {
                            dos.writeUTF("HaveFile Yes " + received.split(" ")[2]);
                        }
                        else
                        {
                            dos.writeUTF("HaveFile No " + received.split(" ")[2]);
                        }
                    }
                    else if(received.split(" ")[0].equals("ForSearching")){
                        int temp=Integer.parseInt(received.split(" ")[1]);
                        int fla=0;
                        for(int i=0;i<temp;i++)
                        {
                            String rec = dis.readUTF();
                            //System.out.println(rec);
                            if(rec.equals("Yes") && fla==0){
                                System.out.println("Yes File Exists in on of the clients");
                                fla=1;
                                
                            }
                            
                        }
                        if(fla==0)
                        {
                            System.out.println("The requested file does not exist");
                        }
                        dos.writeUTF("UNLOCKME");
                    }
                    else if (received.split(" ")[0].equals("DoYouHaveFile")) {
                        System.out.println("Starting mini server Requested filename - " + received.split(" ")[1]);
                        String filename = received.split(" ")[1];
                        int chunking = Integer.parseInt(received.split(" ")[2]);
                        DataInputStream minidis = new DataInputStream(s.getInputStream());
                        DataOutputStream minidos = new DataOutputStream(s.getOutputStream());
                        System.out.println("Hello 1");
                        //final File folder = new File("./src/peertopeer/");
                        final File folders = new File("./src/peertopeer/");
//                        List<String> result = new ArrayList<>();
                        int flag3=0;
                        if(search(filename))
                        {
                            flag3=1;
                            filename="./src/peertopeer/"+filename;
                        }
                        
                        if(flag3==1)
                        {
                            if(chunking==0)
                            {
                                port++;
                                Thread t1 = new ClientHandler2(port,filename,0);
                                t1.start();
                            }
                            else{
                                port++;
                                Thread t1 = new ClientHandler2(port,filename,1);
                                t1.start();
                                port++;
                                Thread t2 = new ClientHandler2(port,filename,2);
                                t2.start();
                            }
                        }
                        System.out.println("Hello 2");
                        received = dis.readUTF();

                        if (flag3 == 1)
                        {
                            if(chunking==0)
                                dos.writeUTF("ForPeer " + received + " " + Integer.toString(port));
                            else
                                dos.writeUTF("ForPeer " + received + " " + Integer.toString(port-1));
                        }
                            
                        else
                            dos.writeUTF("ForPeer " + received + " " + "No");
                    } else if (received.split(" ")[0].equals("Connect")) {
                        tt2.suspend();
                        int chunking = Integer.parseInt(received.split(" ")[1]);
                        String filename = dis.readUTF();
                        String replication = dis.readUTF();
                        String received2 = dis.readUTF();
                        ArrayList<String> returnIPsNoFile = new ArrayList<>();
                        ArrayList<String> returnIPs = new ArrayList<>();
                        ArrayList<String> returnPORTs = new ArrayList<>();
                        System.out.println("1 " + received2);
                        
                        for (int i = 0; i < Integer.parseInt(received2); i++) {
                            received = dis.readUTF();
                            System.out.println("2 " + received);
                            if (!received.split(" ")[0].equals("No")) {
                                returnIPs.add(received.split(" ")[2]);
                                returnPORTs.add(received.split(" ")[3]);
                            }
                            else
                            {
                                returnIPsNoFile.add(received.split(" ")[3]);
                            }
                        }

                        System.out.println("@@@@@@@@@@@@@@@@@@ ");
                        // String peerip = received.split(" ")[2];
                        // int peerport = Integer.parseInt(received.split(" ")[3]);
                        // System.out.println(peerip);
                        // System.out.println(peerport);
                        if ((returnIPs.size() >= 1 && chunking == 0)) {
                            System.out.println(returnIPs.get(0));
                            System.out.println(Integer.parseInt(returnPORTs.get(0)));
                            Thread t3 = new ClientHandler3(returnIPs.get(0), Integer.parseInt(returnPORTs.get(0)),
                                    filename, replication, returnIPsNoFile,dos,chunking);
                            t3.start();
                            // Socket peerS = new Socket(returnIPs.get(0),
                            // Integer.parseInt(returnPORTs.get(0)));
                            System.out.println("Peer to peer connected");
                        } else if((returnIPs.size() >= 1 && chunking == 1)) {
                            if(returnIPs.size() == 1)
                            {
                                System.out.println(returnIPs.get(0));
                                System.out.println(Integer.parseInt(returnPORTs.get(0)));
                                Thread t3 = new ClientHandler3(returnIPs.get(0), Integer.parseInt(returnPORTs.get(0)),
                                        filename+"1", "Replicate No", returnIPsNoFile,dos,chunking);

                                Thread t4 = new ClientHandler3(returnIPs.get(0), Integer.parseInt(returnPORTs.get(0))+1,
                                        filename+"2", replication, returnIPsNoFile,dos,chunking);
                                t3.start();
                                t4.start();
                                System.out.println("Peer to peer connected");
                                
                                t3.join();
                                t4.join();

                                FileMerger fm = new FileMerger(System.getProperty("user.dir").replace('\\', '/') + "/src/peertopeer/"+filename+"1",System.getProperty("user.dir").replace('\\', '/') + "/src/peertopeer/"+filename+"2",System.getProperty("user.dir").replace('\\', '/') + "/src/peertopeer/"+filename);
                                fm.merge();
                                insert(filename);
                                File file1 = new File(System.getProperty("user.dir").replace('\\', '/') + "/src/peertopeer/"+filename+"1"); 
                                File file2 = new File(System.getProperty("user.dir").replace('\\', '/') + "/src/peertopeer/"+filename+"2"); 

                                file1.delete();
                                file2.delete();

                                if(replication.equals("Replicate Yes"))
                                {
                                    if(returnIPsNoFile.size()>0)
                                    {
                                        dos.writeUTF("Replicate File");
                                        dos.writeUTF(returnIPsNoFile.get(0));
                                        dos.writeUTF(filename);
                                    }
                                }

                                // Socket peerS = new Socket(returnIPs.get(0),
                                // Integer.parseInt(returnPORTs.get(0)));
                                System.out.println("Files Merged Successfully");
                            }
                            else{
                                System.out.println(returnIPs.get(0));
                                System.out.println(Integer.parseInt(returnPORTs.get(0)));
                                System.out.println(returnIPs.get(1));
                                System.out.println(Integer.parseInt(returnPORTs.get(1)));
                                Thread t3 = new ClientHandler3(returnIPs.get(0), Integer.parseInt(returnPORTs.get(0)),
                                        filename+"1", "Replicate No", returnIPsNoFile,dos,chunking);

                                Thread t4 = new ClientHandler3(returnIPs.get(1), Integer.parseInt(returnPORTs.get(1))+1,
                                        filename+"2", replication, returnIPsNoFile,dos,chunking);
                                t3.start();
                                t4.start();
                                System.out.println("Peer to peer connected");

                                t3.join();
                                t4.join();

                                FileMerger fm = new FileMerger(System.getProperty("user.dir").replace('\\', '/') + "/src/peertopeer/"+filename+"1",System.getProperty("user.dir").replace('\\', '/') + "/src/peertopeer/"+filename+"2",System.getProperty("user.dir").replace('\\', '/') + "/src/peertopeer/"+filename);
                                fm.merge();
                                insert(filename);
                                File file1 = new File(System.getProperty("user.dir").replace('\\', '/') + "/src/peertopeer/"+filename+"1"); 
                                File file2 = new File(System.getProperty("user.dir").replace('\\', '/') + "/src/peertopeer/"+filename+"2"); 

                                file1.delete();
                                file2.delete();

                                if(replication.equals("Replicate Yes"))
                                {
                                    if(returnIPsNoFile.size()>0)
                                    {
                                        dos.writeUTF("Replicate File");
                                        dos.writeUTF(returnIPsNoFile.get(0));
                                        dos.writeUTF(filename);
                                    }
                                }
                                // Socket peerS = new Socket(returnIPs.get(0),
                                // Integer.parseInt(returnPORTs.get(0)));
                                System.out.println("Files Merged Successfully");
                            }
                            
                        } else {
                            System.out.println("No peer has this file!");
                        }
                        dos.writeUTF("UNLOCKME");

                        tt2.resume();
                    } else {
                        System.out.println(received);

                        if (flag == 0) {
                            tt2 = new UserRequest(scn, dos, dis);
                            tt2.start();
                            // System.out.println("out");
                            flag = 1;
                            int a = 12;
                            if (a == 13)
                                break;
                        }
                    }
                    /*
                     * String tosend = scn.nextLine(); dos.writeUTF(tosend);
                     * 
                     * // If client sends exit,close this connection // and then break from the
                     * while loop if(tosend.equals("Exit")) {
                     * System.out.println("Closing this connection : " + s); s.close();
                     * System.out.println("Connection closed"); break; }
                     * 
                     * // printing date or time as requested by client String received =
                     * dis.readUTF(); System.out.println(received);
                     */
                }

                // closing resources
                scn.close();
                dis.close();
                dos.close();
                //System.out.println("%%%%%%%%%");
                
                
            
            } catch (Exception e) {
                
                System.out.println(e.getMessage());
                System.out.println("#############");
                tt2.stop();
                //e.printStackTrace();
                String ip2 = ip;
                for (int i=0;i<onlineClients.size();i++)
                {
                    if(!ip2.equals(onlineClients.get(i).trim()))
                    {
                        ip = onlineClients.get(i);
                        break;
                    }
                }
                
                if(ip.equals(myIP))
                {
                    //port1++;
                    //port++;
                    String homeDirectory = System.getProperty("user.dir")+"\\src\\peertopeer";
                    System.out.println(homeDirectory);
                    //Process process;
                    //process = Runtime.getRuntime().exec(String.format("javac Server.java", homeDirectory));
                    //process = Runtime.getRuntime().exec(String.format("java Server", homeDirectory));
                        
                    ProcessBuilder builder = new ProcessBuilder();
                    builder.command("javac", "Server2.java");
                    builder.directory(new File(homeDirectory));
                    Process process = builder.start();
                    try{Thread.sleep(4000);}catch(InterruptedException ee){System.out.println(ee);}
                    builder = new ProcessBuilder();
                    builder.command("java", "Server2");
                    builder.directory(new File(homeDirectory));
                    process = builder.start();
                    try{Thread.sleep(4000);}catch(InterruptedException ee){System.out.println(ee);}
                    //new ProcessBuilder("C:\\Users\\arind\\Desktop\\DS-Project\\peertopeer\\src\\peertopeer\\javac Server.java").start();
                    //new ProcessBuilder("C:\\Users\\arind\\Desktop\\DS-Project\\peertopeer\\src\\peertopeer\\java Server").start();
                    //Process p = Runtime.getRuntime().exec("C:\\Users\\arind\\Desktop\\DS-Project\\peertopeer\\src\\peertopeer\\javac Server.java");
                    //Process p2 = Runtime.getRuntime().exec("C:\\Users\\arind\\Desktop\\DS-Project\\peertopeer\\src\\peertopeer\\java Server");
                }
                else
                {
                    try{Thread.sleep(12000);}catch(InterruptedException ee){System.out.println(ee);}
                }
            }
        }
    }

    public static void search(final String pattern, final File folder, List<String> result) {
        
        
        for (final File f : folder.listFiles()) {

//            if (f.isDirectory()) {
//                search(pattern, f, result);
//            }
            
            if (f.isFile()) {
                if (f.getName().matches(pattern)) {
                    result.add(f.getAbsolutePath());
                }
            }

        }
    }

    public static String getmac() {
        InetAddress ip;
        String macadd = "";
        try {

            ip = InetAddress.getLocalHost();
            // System.out.println("Current IP address : " + ip.getHostAddress());

            NetworkInterface network = NetworkInterface.getByInetAddress(ip);

            byte[] mac = network.getHardwareAddress();

            // System.out.print("Current MAC address : ");

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }
            // System.out.println(sb.toString());
            macadd = sb.toString();

        } catch (UnknownHostException e) {

            e.printStackTrace();

        } catch (SocketException e) {

            e.printStackTrace();

        }
        return macadd;
    }

}
