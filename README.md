# DS-Project
Peer to peer distributed file sharing application

- To start open the peertopeer project in netbeans
- If you want chunking set the chunking flag in Server.java to 0 else set to 1
- Run the Server.java file
- Similarly run the Client.java file after entering the server IP (in the Client.java file Main) in same pc as well as some other pc which will be the peers. 
- On connecting with server, each client will get list of services they can access. 
- Peers can send request using the UserRequest.java class dedicated for user input running on separate thread.
- On entring option(1) the the client will be promted to enter filename to search. So distributed search will be performed and thus if anyone has the file the user will get to know about it.
- On entering option(3) the client will be promted to enter filename to download. Then the server will broadcast this request to all peers.
- The peers will search the file in their sharable folder respectively using a already constructed trie-data structure and start their mini server for file transfer on a separate thread (ClientHandler2.java) and send the port for this mini server to server.
- Server on receiving the message from each client it will send the IPs and Ports of peers having this file and then the requested client can select a peer and start a connection (ClientHandler3.java) and get the file.
- If the main server crashes for some reason then automatically within 10 seaconds one of the online clients will become the server (Server2.java) or the super peer and all the online peers will be connected to it and thus the system won't crash as a whole.
