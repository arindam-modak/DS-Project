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
    final int part;

    // Constructor
    public ClientHandler2(int port, String filename, int part) {
        this.SOCKET_PORT = port;
        this.filename = filename;
        this.part = part;
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
                        if(part==1)
                        {
                            byte[] mybytearray = new byte[(int) myFile.length()/2];
                            fis = new FileInputStream(myFile);
                            bis = new BufferedInputStream(fis);
                            bis.read(mybytearray, 0, mybytearray.length);
                            os = sock.getOutputStream();
                            System.out.println("Sending " + this.filename + "(" + mybytearray.length + " bytes)");
                            os.write(mybytearray, 0, mybytearray.length);
                            os.flush();
                            System.out.println("Done.");
                        }
                        else if(part==2)
                        {
                            System.out.println("1");
                            byte[] mybytearray = new byte[(int) (myFile.length()-myFile.length()/2)];
                            byte[] mybytearray2 = new byte[(int) (myFile.length())];
                            fis = new FileInputStream(myFile);
                            System.out.println("2");
                            bis = new BufferedInputStream(fis);
                            int off = (int)myFile.length()/2;
                            System.out.println("3");
                            bis.read(mybytearray2, 0 , mybytearray2.length);
                            for(int i=off;i<mybytearray2.length;i++)
                                mybytearray[i-off] = mybytearray2[i];
                            System.out.println("4");
                            os = sock.getOutputStream();
                            System.out.println("Sending " + this.filename + "(" + mybytearray.length + " bytes)");
                            os.write(mybytearray, 0, mybytearray.length);
                            os.flush();
                            System.out.println("Done.");
                        
                        }
                        else
                        {
                            byte[] mybytearray = new byte[(int) myFile.length()];
                            fis = new FileInputStream(myFile);
                            bis = new BufferedInputStream(fis);
                            bis.read(mybytearray, 0, mybytearray.length);
                            os = sock.getOutputStream();
                            System.out.println("Sending " + this.filename + "(" + mybytearray.length + " bytes)");
                            os.write(mybytearray, 0, mybytearray.length);
                            os.flush();
                            System.out.println("Done.");
                        }
                        
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