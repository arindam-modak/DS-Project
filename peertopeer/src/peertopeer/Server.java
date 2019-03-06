/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package peertopeer;
  
import java.io.*; 
import java.text.*; 
import java.util.*; 
import java.net.*; 
  
/**
 *
 * @author Yash
 */
public class Server
{
    public static void main(String[] args) throws IOException  
    { 
        // server is listening on port 5056 
        ServerSocket ss = new ServerSocket(5057); 
          
        // running infinite loop for getting 
        // client request 
        while (true)  
        { 
            Socket s = null; 
              
            try 
            { 
                // socket object to receive incoming client requests 
                s = ss.accept(); 
                System.out.println("A new client is connected : " + s); 
//                String socket_info = s.toString();
                System.out.println(s.getInetAddress());
                String temp=s.getInetAddress().toString();
                String ip=temp.substring(1);
                
// obtaining input and out streams 
                DataInputStream dis = new DataInputStream(s.getInputStream()); 
                DataOutputStream dos = new DataOutputStream(s.getOutputStream()); 
                  
                System.out.println("Assigning new thread for this client"); 
  
                // create a new thread object 
                Thread t = new ClientHandler(s, dis, dos,ip); 
  
                // Invoking the start() method 
                t.start(); 
                  
            } 
            catch (Exception e){ 
                s.close(); 
                e.printStackTrace(); 
            } 
        } 
    } 
}

class ClientHandler extends Thread  
{ 
    DateFormat fordate = new SimpleDateFormat("yyyy/MM/dd"); 
    DateFormat fortime = new SimpleDateFormat("hh:mm:ss"); 
    final DataInputStream dis; 
    final DataOutputStream dos; 
    final Socket s; 
    String ip_address;
    int flag=0;
  
    // Constructor 
    public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos,String ip_address)  
    { 
        this.s = s; 
        this.dis = dis; 
        this.dos = dos; 
        this.ip_address=ip_address;
    } 
  
    @Override
    public void run()  
    { 
        String received; 
        String toreturn; 
        while (true)  
        { 
            try { 
                
                if(flag==0)
                // Ask user what he wants 
                {
                    dos.writeUTF("1"); 
                    received = dis.readUTF();
                    System.out.println("Mac of client : "+received);
                    flag=1;
                }
                else
                {
                        // receive the answer from client 
                        dos.writeUTF("Enter file to search");
                        received = dis.readUTF(); 
                        
                        if(received.equals("Exit")) 
                        {  
                            System.out.println("Client " + this.s + " sends exit..."); 
                            System.out.println("Closing this connection."); 
                            this.s.close(); 
                            System.out.println("Connection closed"); 
                            break; 
                        } 

                        // creating Date object 
                        Date date = new Date(); 

                        // write on output stream based on the 
                        // answer from the client 
                        switch (received) { 

                            case "Date" : 
                                toreturn = fordate.format(date); 
                                dos.writeUTF(toreturn); 
                                break; 

                            case "Time" : 
                                toreturn = fortime.format(date); 
                                dos.writeUTF(toreturn); 
                                break; 

                            default: 
                                dos.writeUTF("Invalid input"); 
                                break; 
                        } 
                }
            } catch (IOException e) { 
                e.printStackTrace(); 
            } 
           
        } 
          
        try
        { 
            // closing resources 
            this.dis.close(); 
            this.dos.close(); 
              
        }catch(IOException e){ 
            e.printStackTrace(); 
        } 
    } 
} 

