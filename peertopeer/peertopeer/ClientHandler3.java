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

    public final static int FILE_SIZE = 20000000; // file size temporary hard coded
                                                  // should bigger than the file to be downloaded
    // Constructor

    public ClientHandler3(String SERVER, int SOCKET_PORT, String filename) {
        this.SERVER = SERVER;
        this.SOCKET_PORT = SOCKET_PORT;
        this.FILE_TO_RECEIVED = System.getProperty("user.dir").replace('\\', '/') + '/' + filename;
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

                do {
                    bytesRead = is.read(mybytearray, current, (mybytearray.length - current));
                    if (bytesRead >= 0)
                        current += bytesRead;
                } while (bytesRead > -1);

                bos.write(mybytearray, 0, current);
                bos.flush();
                System.out.println("File " + FILE_TO_RECEIVED + " downloaded (" + current + " bytes read)");
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
