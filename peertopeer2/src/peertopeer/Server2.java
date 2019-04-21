
import java.awt.event.MouseListener;
import java.io.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.net.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;


public class Server2 {
    public static void main(String[] args) throws IOException {
        // server is listening on port 5057 jhjvjhvj
        ServerSocket ss = new ServerSocket(5056);
        ArrayList<String> onlineClients = new ArrayList<>();
        HashMap<String, DataOutputStream> outputstream = new HashMap<>();
        HashMap<String, DataInputStream> inputstream = new HashMap<>();
        HashMap<String, Integer> locking = new HashMap<>();
        HashMap<String, Integer> peerLocking = new HashMap<>();
        HashMap<String, Integer> FileCount = new HashMap<>();
        int chunking = 0;
        // running infinite loop for getting client requests
        while (true) {
            Socket s = null;
            try {

                // socket object to receive incoming client requests
                s = ss.accept();

                System.out.println("A new client is connected : " + s);

                System.out.println("Ip Address  =  " + s.getInetAddress());

                String temp = s.getInetAddress().toString();
                String ip = temp.substring(1);
                int i = 0;
                for (i = 0; i < onlineClients.size(); i++) {
                    if (onlineClients.get(i).equals(ip))
                        break;
                }

                // obtaining input and out streams
                DataInputStream dis = new DataInputStream(s.getInputStream());
                DataOutputStream dos = new DataOutputStream(s.getOutputStream());
                if (i == onlineClients.size()) {
                    onlineClients.add(ip);
                    outputstream.put(ip, dos);
                    inputstream.put(ip, dis);
                    locking.put(ip, 0);
                    peerLocking.put(ip,0);
                }
                System.out.println("Assigning new thread for this client");

                // create a new thread object
                Thread t = new ServerClientHandler2(s, dis, dos, ip, onlineClients, inputstream, outputstream, locking, FileCount, peerLocking, chunking);
                String allpeers = ip;
                for (i = 0; i < onlineClients.size(); i++) {
                    allpeers += " "+onlineClients.get(i);
                }
                for (i = 0; i < onlineClients.size(); i++) {
                    //System.out.println(onlineClients.get(i));
                    //System.out.println(allpeers);
                    outputstream.get(onlineClients.get(i)).writeUTF("IP "+allpeers);
                }
                // Invoking the start() method
                t.start();

            } catch (Exception e) {
                s.close();
                e.printStackTrace();
            }
        }
    }
}




class ServerClientHandler2 extends Thread {

    DateFormat fordate = new SimpleDateFormat("yyyy/MM/dd");
    DateFormat fortime = new SimpleDateFormat("hh:mm:ss");
    final DataInputStream dis;
    final DataOutputStream dos;
    final Socket s;
    String ip_address;
    String mac_address;
    int flag = 1;
    final String JDBC_Driver_Class = "com.mysql.jdbc.Driver";
    final String DB_URL = "jdbc:mysql://localhost/peers?autoReconnect=true&useSSL=false";
    final String USER = "root";
    final String PASS = "pass123";
    ArrayList<String> onlineClients;
    HashMap<String, DataInputStream> inputstream;
    HashMap<String, DataOutputStream> outputstream;
    HashMap<String, Integer> locking;
    HashMap<String, Integer> FileCount;
    HashMap<String, Integer> peerLocking;
    int chunking;
    // Constructor
    public ServerClientHandler2(Socket s, DataInputStream dis, DataOutputStream dos, String ip_address,
            ArrayList<String> onlineClients, HashMap<String, DataInputStream> inputstream,
            HashMap<String, DataOutputStream> outputstream, HashMap<String, Integer> locking,
            HashMap<String, Integer> FileCount, HashMap<String, Integer> peerLocking, int chunking) {
        this.s = s;
        this.dis = dis;
        this.dos = dos;
        this.ip_address = ip_address;
        this.onlineClients = onlineClients;
        this.inputstream = inputstream;
        this.outputstream = outputstream;
        this.locking = locking;
        this.FileCount = FileCount;
        this.peerLocking = peerLocking;
        this.chunking = chunking;
    }

    @Override
    public void run() {
        String received;
        String toreturn;
        while (true) {
            try {
                if (flag == 0) {
                    dos.writeUTF("MAC");
                    received = dis.readUTF();
                    this.mac_address = received;
                    System.out.println("Mac of client : " + received);
                    flag = 1;
                    int sig = 0;
                    try {
                        Class.forName(JDBC_Driver_Class);
                        Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                        Statement stmt = conn.createStatement();
                        String sql = "SELECT * from peers where macaddress = '" + received + "';";

                        ResultSet rs = stmt.executeQuery(sql);
                        String ip_stored = "";
                        while (rs.next()) {
                            ip_stored = rs.getString("ipaddress");
                        }
                        if (!ip_stored.equals("")) {
                            System.out.println("Client Already registered ");

                            if (ip_stored.equals(ip_address)) {
                                System.out.println("Same IPAddress");
                                sig = 1;
                            } else {
                                System.out.println("Different IP address Need to update");
                                sig = 2;
                            }
                        }
                        stmt.close();
                        if (sig == 2) {
                            Statement stmt2 = conn.createStatement();
                            String sql2 = "UPDATE peers SET ipaddress = '" + ip_address + "' where macaddress = '"
                                    + received + "';";
                            stmt2.executeUpdate(sql2);
                            stmt2.close();
                        }

                        conn.close();

                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                    if (sig != 0)
                        continue;

                    try {
                        Class.forName(JDBC_Driver_Class);
                        Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                        Statement stmt = conn.createStatement();
                        String sql = "INSERT INTO peers VALUES ('" + received + "','" + ip_address + "');";
                        stmt.executeUpdate(sql);
                        stmt.close();
                        conn.close();
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                } else {
                    // receive the answer from client
                    if (this.locking.get(this.ip_address) == 0) {
                        dos.writeUTF(
                                "Press: (1) to Enter file to search | (2) to de-register your pc | (3) to download files ");
                    }
                    received = dis.readUTF();
                    System.out.println(received);
                    if(received.equals("UNLOCKME"))
                    {
                        this.peerLocking.put(this.ip_address, 0);
                    }
                    else if (received.split(" ")[0].equals("ForPeer")) {
                        if (received.split(" ")[2].equals("No")) {
                            this.outputstream.get(received.split(" ")[1]).writeUTF("No file found "+this.ip_address);
                        } else {
                            this.outputstream.get(received.split(" ")[1])
                                    .writeUTF("Connect to " + this.ip_address + " " + received.split(" ")[2]);
                            
                        }
                        //this.locking.put(received.split(" ")[1], 0);
                    }
                    else if(received.split(" ")[0].equals("HaveFile")){
                        if(received.split(" ")[1].equals("Yes")){
                            this.outputstream.get(received.split(" ")[2]).writeUTF("Yes");
                        }
                        else{
                            
                            this.outputstream.get(received.split(" ")[2]).writeUTF("No");
                        }
                        //this.locking.put(received.split(" ")[1], 0);
                    }
                    else if (received.equals("Replicate File"))
                    {
                        String repIp = this.dis.readUTF();
                        String repFile = this.dis.readUTF();
                        this.outputstream.get(repIp).writeUTF("Connect 0");
                        this.outputstream.get(repIp).writeUTF(repFile);
                        this.outputstream.get(repIp).writeUTF("Replicate No");
                        this.outputstream.get(repIp).writeUTF(Integer.toString(1));
                        this.dos.writeUTF("DoYouHaveFile " + repFile + " 0");
                        this.dos.writeUTF(repIp);
                    }
                    else if(received.equals("1")){
                        this.peerLocking.put(this.ip_address, 1);
                        dos.writeUTF("Enter the Name of the file you want ");
                        String filename = dis.readUTF();
                        System.out.println(filename);
                        int count=0;
                        for(int i=0;i<this.onlineClients.size();i++)
                        {
                            if(this.onlineClients.get(i)!=this.ip_address && this.peerLocking.get(this.onlineClients.get(i))==0)
                            {
                                count++;
                            }
                        }
                        dos.writeUTF("ForSearching " + Integer.toString(count));
                        for(int i=0;i<this.onlineClients.size();i++)
                        {
                            if(this.onlineClients.get(i)!=this.ip_address && this.peerLocking.get(this.onlineClients.get(i))==0)
                            {
                                this.outputstream.get(this.onlineClients.get(i)).writeUTF("HaveFile? " + filename + " "  + this.ip_address);
                            }
                        }
                        this.locking.put(this.ip_address, 1);
                    }
                    else if (received.equals("3")) {
                        this.peerLocking.put(this.ip_address, 1);
                        dos.writeUTF("Enter the Name of the file you want ");
                        String filename = dis.readUTF();
                        System.out.println(filename);
                        dos.writeUTF("Connect "+Integer.toString(this.chunking));
                        dos.writeUTF(filename);
                        
                        if(this.FileCount.containsKey(filename))
                        {
                            this.FileCount.put(filename,this.FileCount.get(filename)+1);
                            if(this.FileCount.get(filename)>4)
                            { dos.writeUTF("Replicate Yes");  this.FileCount.put(filename,0); }
                            else
                                dos.writeUTF("Replicate No");
                        } else {
                            this.FileCount.put(filename,1);
                            dos.writeUTF("Replicate No");
                        }
                        String tt = "OnlineClients :";
                        String selectclient = "";
                        dos.writeUTF(Integer.toString(this.onlineClients.size() - 1));
                        // System.out.println(Integer.toString(this.onlineClients.size() - 1));
                        for (int i = 0; i < this.onlineClients.size(); i++) {
                            tt += " " + this.onlineClients.get(i);

                            if (this.onlineClients.get(i) != this.ip_address) {
                                this.outputstream.get(this.onlineClients.get(i)).writeUTF("DoYouHaveFile " + filename + " " + Integer.toString(this.chunking));
                                this.outputstream.get(this.onlineClients.get(i)).writeUTF(this.ip_address);
                                //selectclient = this.onlineClients.get(i);
                            }
                        }
                        // System.out.println(selectclient);
                        // this.outputstream.get(selectclient).writeUTF("Start Server");
                        // this.outputstream.get(selectclient).writeUTF(this.ip_address);
                        // System.out.println("++++++++++");
                        this.locking.put(this.ip_address, 1);
                        // received = dis.readUTF();
                        // this.locking.put(selectclient, 0);
                        // System.out.println("port : " + received);
                        // System.out.println("*****************");
                        // int portnum = 5058;
                        // dos.writeUTF("Connect to " + selectclient + " " + received);

                    } else if (received.equals("2")) {
                        try {
                            Class.forName(JDBC_Driver_Class);
                            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                            Statement stmt = conn.createStatement();
                            String sql = "DELETE from peers where macaddress = '" + this.mac_address + "';";
                            stmt.executeUpdate(sql);
                            stmt.close();
                            conn.close();

                            System.out.println("Client " + this.s + " sends exit...");
                            System.out.println("Closing this connection.");
                            this.s.close();
                            System.out.println("Connection closed");
                            break;
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                    }

                    else if (received.equals("Exit")) {
                        System.out.println("Client " + this.s + " sends exit...");
                        System.out.println("Closing this connection.");
                        this.s.close();
                        System.out.println("Connection closed");
                        break;
                    }

                    else {
                        System.out.println(received);
                        dos.writeUTF("Invalid input");
                    }

                }
            } catch (IOException e) {
                //e.printStackTrace();
                break;
            }

        }

        try {
            // closing resources
            this.dis.close();
            this.dos.close();
            this.s.close();
            //System.out.println("B");
            for (int i = 0; i < this.onlineClients.size(); i++) {
                if (this.onlineClients.get(i).equals(this.ip_address))
                    this.onlineClients.remove(i);
            }
            String allpeers = this.ip_address;
            for (int i = 0; i < onlineClients.size(); i++) {
                allpeers += " "+onlineClients.get(i);
            }
            for (int i = 0; i < onlineClients.size(); i++) {
                //System.out.println(onlineClients.get(i));
                //System.out.println(allpeers);
                outputstream.get(onlineClients.get(i)).writeUTF("IP "+allpeers);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
