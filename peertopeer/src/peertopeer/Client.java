
import java.io.*; 
import java.net.*; 
import java.util.*; 
import java.util.Scanner; 
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException; 
import java.awt.*;  
import java.awt.event.*;

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
            Socket s = new Socket("172.19.16.233", 5057);

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

                } else if (received.split(" ")[0].equals("DoYouHaveFile")) {
                    System.out.println("Starting mini server Requested filename - " + received.split(" ")[1]);
                    String filename = received.split(" ")[1];
                    port += 1;
                    DataInputStream minidis = new DataInputStream(s.getInputStream());
                    DataOutputStream minidos = new DataOutputStream(s.getOutputStream());
                    System.out.println("Hello 1");
                    final File folder = new File(".");

                    List<String> result = new ArrayList<>();

                    search(filename, folder, result);

                    int flag3 = 0;
                    for (String rs : result) {
                        System.out.println(rs);
                        flag3 = 1;
                    }
                    Thread t = new ClientHandler2(port, result.get(0));
                    t.start();
                    System.out.println("Hello 2");
                    received = dis.readUTF();

                    if (flag3 == 1)
                        dos.writeUTF("ForPeer " + received + " " + Integer.toString(port));
                    else
                        dos.writeUTF("ForPeer " + received + " " + "No");
                } else if(received.split(" ")[0].equals("Connect"))
                {
                    String filename = dis.readUTF();
                    String received2 = dis.readUTF();
                    ArrayList<String> returnIPs = new ArrayList<>(); 
                    ArrayList<String> returnPORTs = new ArrayList<>(); 
                    System.out.println("1 "+received2);
                    for (int i=0;i<Integer.parseInt(received2);i++)
                    {
                        received = dis.readUTF();
                        System.out.println("2 "+received);
                        if(!received.equals("No file found"))
                        {
                            returnIPs.add(received.split(" ")[2]);
                            returnPORTs.add(received.split(" ")[3]);
                        }

                    }
                    
                    System.out.println("@@@@@@@@@@@@@@@@@@ ");
                    //String peerip = received.split(" ")[2];
                    //int peerport = Integer.parseInt(received.split(" ")[3]);
                    //System.out.println(peerip);
                    //System.out.println(peerport);
                    if(returnIPs.size()>=1)
                    {
                        Thread t3 = new ClientHandler3(returnIPs.get(0),Integer.parseInt(returnPORTs.get(0)),filename);
                        t3.start();
                        //Socket peerS = new Socket(returnIPs.get(0), Integer.parseInt(returnPORTs.get(0)));
                        System.out.println("Peer to peer connected");
                    }
                    else{
                        System.out.println("No peer has this file!");
                    }
                }
                else
                {
                    System.out.println(received);

                    if(flag==0) 
                    {
                        tt2 = new UserRequest2(scn,dos,dis);
                        tt2.start();
                        //System.out.println("out");
                        flag=1;
                        int a = 12;
                        if(a==13) break;
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

    public static void search(final String pattern, final File folder, List<String> result) {
        for (final File f : folder.listFiles()) {

            if (f.isDirectory()) {
                search(pattern, f, result);
            }

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

class ClientHandler2 extends Thread {
    final int SOCKET_PORT;
    final String filename;

    // Constructor
    public ClientHandler2(int port, String filename) {
        this.SOCKET_PORT = port;
        this.filename = filename;
    }

    @Override
    public void run() {
        try {
            FileInputStream fis = null;
            BufferedInputStream bis = null;
            OutputStream os = null;
            ServerSocket servsock = null;
            Socket sock = null;
            try {
                servsock = new ServerSocket(SOCKET_PORT);
                while (true) {
                    System.out.println("Waiting...");
                    try {
                        sock = servsock.accept();
                        System.out.println("Accepted connection : " + sock);
                        // send file
                        File myFile = new File(this.filename);
                        byte[] mybytearray = new byte[(int) myFile.length()];
                        fis = new FileInputStream(myFile);
                        bis = new BufferedInputStream(fis);
                        bis.read(mybytearray, 0, mybytearray.length);
                        os = sock.getOutputStream();
                        System.out.println("Sending " + this.filename + "(" + mybytearray.length + " bytes)");
                        os.write(mybytearray, 0, mybytearray.length);
                        os.flush();
                        System.out.println("Done.");
                    } finally {
                        if (bis != null)
                            bis.close();
                        if (os != null)
                            os.close();
                        if (sock != null)
                            sock.close();
                    }
                }
            } finally {
                if (servsock != null)
                    servsock.close();
            }
        } catch (Exception e) {

        }

    }
}

class ClientHandler3 extends Thread {
    public final int SOCKET_PORT;      // you may change this
    public final String SERVER ;  // localhost
    public final String FILE_TO_RECEIVED ;  // you may change this, I give a
                                                            // different name because i don't want to
                                                            // overwrite the one used by server...

  public final static int FILE_SIZE = 16022386; // file size temporary hard coded
                                               // should bigger than the file to be downloaded
    // Constructor
    public ClientHandler3( String SERVER, int SOCKET_PORT, String filename) {
        this.SERVER = SERVER;
        this.SOCKET_PORT = SOCKET_PORT;
        this.FILE_TO_RECEIVED = "C:/Users/arind/Desktop/"+filename;
    }

    @Override
    public void run() {
        try {

            int bytesRead;
            int current = 0;
            FileOutputStream fos = null;
            BufferedOutputStream bos = null;
            Socket sock = null;
            try {
            sock = new Socket(SERVER, SOCKET_PORT);
            System.out.println("Connecting...");

            // receive file
            byte [] mybytearray  = new byte [FILE_SIZE];
            InputStream is = sock.getInputStream();
            fos = new FileOutputStream(FILE_TO_RECEIVED);
            bos = new BufferedOutputStream(fos);
            bytesRead = is.read(mybytearray,0,mybytearray.length);
            current = bytesRead;

            do {
                bytesRead =
                    is.read(mybytearray, current, (mybytearray.length-current));
                if(bytesRead >= 0) current += bytesRead;
            } while(bytesRead > -1);

            bos.write(mybytearray, 0 , current);
            bos.flush();
            System.out.println("File " + FILE_TO_RECEIVED
                + " downloaded (" + current + " bytes read)");
            }
            finally {
            if (fos != null) fos.close();
            if (bos != null) bos.close();
            if (sock != null) sock.close();
            }
            
        } catch (Exception e) {
            ;
        }
        


    }

}

class UserRequest2 extends Thread {
    final Scanner scn;
    final DataOutputStream dos;
    final DataInputStream dis;
    // Constructor
    public UserRequest2(Scanner scn,DataOutputStream dos,DataInputStream dis) {
        this.scn = scn;
        this.dos = dos;
        this.dis = dis;
    }

    @Override
    public void run() {
        while(true)
        {
            //System.out.println("abcd2");
            String hh = scn.nextLine();
            try {
                dos.writeUTF(hh);
            } catch (Exception e) {
                ;
            }
            //System.out.println("ez pz");
        }
    }

}
