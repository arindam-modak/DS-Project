package peertopeer;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.Scanner;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

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