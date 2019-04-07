
import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 *
 * @author Yash
 */
public class Client {

    /**
     * @param args the command line arguments
     */

    public static void main(String[] args) throws IOException {
        int port = 5057;
        int flag = 0;
        Thread tt2;
        try {
            Scanner scn = new Scanner(System.in);

            // getting localhost ip
            // InetAddress ip = InetAddress.getByName("localhost");

            // establish the connection with server port 5056
            Socket s = new Socket("192.168.43.38", 5057);

            // obtaining input and out streams
            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());

            // the following loop performs the exchange of
            // information between client and client handler
            while (true) {

                String received = dis.readUTF();
                received = received.trim();
                // System.out.println(received);
                if (received.equals("MAC")) {
                    String macadd = getmac();
                    dos.writeUTF(macadd);
                } else if (received.split(" ")[0].equals("OnlineClients")) {
                    System.out.println(received);

                } else if (received.equals("Start Server")) {
                    System.out.println("Starting mini server");
                    port += 1;
                    DataInputStream minidis = new DataInputStream(s.getInputStream());
                    DataOutputStream minidos = new DataOutputStream(s.getOutputStream());
                    System.out.println("Hello 1");
                    Thread t = new ClientHandler2(port);
                    t.start();
                    System.out.println("Hello 2");
                    received = dis.readUTF();
                    dos.writeUTF("ForPeer " + received + " " + Integer.toString(port));
                } else if (received.split(" ")[0].equals("Connect")) {

                    String peerip = received.split(" ")[2];
                    int peerport = Integer.parseInt(received.split(" ")[3]);
                    Socket peerS = new Socket(peerip, peerport);
                } else {
                    System.out.println(received);

                    if (flag == 0) {
                        tt2 = new UserRequest2(scn, dos, dis);
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
        } catch (Exception e) {
            e.printStackTrace();
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

class ClientHandler2 extends Thread {
    final int port;

    // Constructor
    public ClientHandler2(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        ServerSocket ss = null;
        try {
            ss = new ServerSocket(this.port);
        } catch (Exception e) {
            ;
        }
        while (true) {
            Socket s = null;
            try {
                s = ss.accept();
                System.out.println("Ip Address  =  " + s.getInetAddress());
                String temp = s.getInetAddress().toString();
                String ip = temp.substring(1);
                DataInputStream dis = new DataInputStream(s.getInputStream());
                DataOutputStream dos = new DataOutputStream(s.getOutputStream());
            } catch (Exception e) {
                try {
                    s.close();
                } catch (Exception ee) {
                    ;
                }
                e.printStackTrace();
            }
        }

    }
}

class UserRequest2 extends Thread {
    final Scanner scn;
    final DataOutputStream dos;
    final DataInputStream dis;

    // Constructor
    public UserRequest2(Scanner scn, DataOutputStream dos, DataInputStream dis) {
        this.scn = scn;
        this.dos = dos;
        this.dis = dis;
    }

    @Override
    public void run() {
        while (true) {
            // System.out.println("abcd2");
            String hh = scn.nextLine();
            try {
                dos.writeUTF(hh);
            } catch (Exception e) {
                ;
            }
            // System.out.println("ez pz");
        }
    }

}
