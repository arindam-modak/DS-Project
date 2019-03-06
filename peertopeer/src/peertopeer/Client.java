/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package peertopeer;
  
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
          try
        { 
            Scanner scn = new Scanner(System.in); 
              
            // getting localhost ip 
//            InetAddress ip = InetAddress.getByName("localhost"); 
      
            // establish the connection with server port 5056 
            Socket s = new Socket("172.19.16.237", 5057); 
            
            // obtaining input and out streams 
            DataInputStream dis = new DataInputStream(s.getInputStream()); 
            DataOutputStream dos = new DataOutputStream(s.getOutputStream()); 
      
            // the following loop performs the exchange of 
            // information between client and client handler 
            while (true)  
            { 
                String received = dis.readUTF(); 
                received.trim();
                if(received.equals("1"))
                {
                    String macadd = getmac();
                    dos.writeUTF(macadd);
                }
                else
                {
                    System.out.println(received);
                    String tosend = scn.nextLine(); 
                    dos.writeUTF(tosend);
                    if(tosend.equals("Exit")) 
                    { 
                        System.out.println("Closing this connection : " + s); 
                        s.close(); 
                        System.out.println("Connection closed"); 
                        break; 
                    } 

                    // printing date or time as requested by client 
                    String received2 = dis.readUTF(); 
                    System.out.println(received2);
                }
                /*String tosend = scn.nextLine(); 
                dos.writeUTF(tosend); 
                  
                // If client sends exit,close this connection  
                // and then break from the while loop 
                if(tosend.equals("Exit")) 
                { 
                    System.out.println("Closing this connection : " + s); 
                    s.close(); 
                    System.out.println("Connection closed"); 
                    break; 
                } 
                  
                // printing date or time as requested by client 
                String received = dis.readUTF(); 
                System.out.println(received); */
            } 
              
            // closing resources 
            scn.close(); 
            dis.close(); 
            dos.close(); 
        }catch(Exception e){ 
            e.printStackTrace(); 
        } 
    }
    
    public static String getmac()
    {
        InetAddress ip;
        String macadd="";
        try {
                
            ip = InetAddress.getLocalHost();
            //System.out.println("Current IP address : " + ip.getHostAddress());
            
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
                
            byte[] mac = network.getHardwareAddress();
                
            //System.out.print("Current MAC address : ");
                
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));		
            }
            //System.out.println(sb.toString());
            macadd = sb.toString();
                
        } catch (UnknownHostException e) {
            
            e.printStackTrace();
            
        } catch (SocketException e){
                
            e.printStackTrace();
                
        }
        return macadd;
    }
    
}
