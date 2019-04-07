
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

/**
 *
 * @author Yash
 */

public class Server {
    public static void main(String[] args) throws IOException {
        // server is listening on port 5057
        ServerSocket ss = new ServerSocket(5057);
        ArrayList<String> onlineClients = new ArrayList<>();
        HashMap<String, DataOutputStream> outputstream = new HashMap<>();
        HashMap<String, DataInputStream> inputstream = new HashMap<>();
        HashMap<String, Integer> locking = new HashMap<>();
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
                }
                System.out.println("Assigning new thread for this client");

                // create a new thread object
                Thread t = new ClientHandler(s, dis, dos, ip, onlineClients, inputstream, outputstream, locking);

                // Invoking the start() method
                t.start();

            } catch (Exception e) {
                s.close();
                e.printStackTrace();
            }
        }
    }
}

class ClientHandler extends Thread {

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

    // Constructor
    public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos, String ip_address,
            ArrayList<String> onlineClients, HashMap<String, DataInputStream> inputstream,
            HashMap<String, DataOutputStream> outputstream, HashMap<String, Integer> locking) {
        this.s = s;
        this.dis = dis;
        this.dos = dos;
        this.ip_address = ip_address;
        this.onlineClients = onlineClients;
        this.inputstream = inputstream;
        this.outputstream = outputstream;
        this.locking = locking;
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
                                "Press: (1) to Enter file to search | (2) to de-register your pc | (3) To connect to a peer");
                        received = dis.readUTF();
                        // System.out.println(received);
                        if (received.split(" ")[0].equals("ForPeer")) {
                            this.outputstream.get(received.split(" ")[1])
                                    .writeUTF("Connect to " + this.ip_address + " " + received.split(" ")[2]);
                            this.locking.put(received.split(" ")[1], 0);
                        } else if (received.equals("3")) {
                            dos.writeUTF("Connect");
                            String tt = "OnlineClients :";
                            String selectclient = "";
                            for (int i = 0; i < this.onlineClients.size(); i++) {
                                tt += " " + this.onlineClients.get(i);
                                if (this.onlineClients.get(i) != this.ip_address) {
                                    selectclient = this.onlineClients.get(i);
                                }
                            }
                            System.out.println(selectclient);
                            this.outputstream.get(selectclient).writeUTF("Start Server");
                            this.outputstream.get(selectclient).writeUTF(this.ip_address);
                            System.out.println("++++++++++");
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
                            dos.writeUTF("Invalid input");
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }

        }

        try {
            // closing resources
            this.dis.close();
            this.dos.close();
            this.s.close();
            // System.out.println("B");
            for (int i = 0; i < this.onlineClients.size(); i++) {
                if (this.onlineClients.get(i).equals(this.ip_address))
                    this.onlineClients.remove(i);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
