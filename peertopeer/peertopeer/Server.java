package peertopeer;

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
        // server is listening on port 5057 jhjvjhvj
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
                Thread t = new ServerClientHandler(s, dis, dos, ip, onlineClients, inputstream, outputstream, locking);

                // Invoking the start() method
                t.start();

            } catch (Exception e) {
                s.close();
                e.printStackTrace();
            }
        }
    }
}
