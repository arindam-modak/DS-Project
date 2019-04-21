package peertopeer;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Scanner;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

class ClientHandler3 extends Thread {
    public final int SOCKET_PORT; // you may change this
    public final String SERVER; // localhost
    public final String FILE_TO_RECEIVED; // you may change this, I give a
                                          // different name because i don't want to
                                          // overwrite the one used by server...

    public final static int FILE_SIZE = 80000000; // file size temporary hard coded
                                                  // should bigger than the file to be downloaded
    ArrayList<String> returnIPsNoFile;
    String replication;
    DataOutputStream dos;
    String filename;
    int chunking;
    // Constructor

    public ClientHandler3(String SERVER, int SOCKET_PORT, String filename,String replication,ArrayList<String> returnIPsNoFile, DataOutputStream dos, int chunking) {
        this.SERVER = SERVER;
        this.SOCKET_PORT = SOCKET_PORT;
        this.filename = filename;
        this.FILE_TO_RECEIVED = System.getProperty("user.dir").replace('\\', '/') + "/src/peertopeer/" + filename;
        this.returnIPsNoFile = returnIPsNoFile;
        this.replication = replication;
        this.dos = dos;
        this.chunking = chunking;
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
                byte[] mybytearray = new byte[FILE_SIZE];
                InputStream is = sock.getInputStream();
                fos = new FileOutputStream(FILE_TO_RECEIVED);
                bos = new BufferedOutputStream(fos);
                bytesRead = is.read(mybytearray, 0, mybytearray.length);
                current = bytesRead;
                
                int Counter = 0;
                do {
                    System.out.println(Counter);
                    Counter++;
                    bytesRead = is.read(mybytearray, current, (mybytearray.length - current));
                    if (bytesRead >= 0)
                        current += bytesRead;
                } while (bytesRead > -1);

                bos.write(mybytearray, 0, current);
                bos.flush();
                System.out.println("File " + FILE_TO_RECEIVED + " downloaded (" + current + " bytes read)");
                
                if(this.replication.equals("Replicate Yes"))
                {
                    if(this.returnIPsNoFile.size()>0)
                    {
                        this.dos.writeUTF("Replicate File");
                        this.dos.writeUTF(this.returnIPsNoFile.get(0));
                        if(this.chunking==1)
                            this.dos.writeUTF(this.filename.substring(0,this.filename.length()-1));    
                        else
                        this.dos.writeUTF(this.filename);
                    }
                }
                
            } finally {
                if (fos != null)
                    fos.close();
                if (bos != null)
                    bos.close();
                if (sock != null)
                    sock.close();
            }

        } catch (Exception e) {
            ;
        }

    }

}
