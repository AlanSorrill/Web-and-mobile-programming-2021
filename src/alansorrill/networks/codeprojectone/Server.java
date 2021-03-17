package alansorrill.networks.codeprojectone;

import java.io.*;
import java.net.ServerSocket;
import java.util.*;

public class Server implements Runnable{
    private final ServerSocket serverSocket;
    private final Thread thread;
    public Map<Integer, SockConnection> connections = new TreeMap();
    public RequestHandler[] requestHandlers;
    public ServerMode mode = ServerMode.proxy;


    public Server(int port, ServerMode mode) throws IOException {
        this.mode = mode;
        //open server socket
        this.serverSocket = new ServerSocket(port);

        //register list of request handlers
        this.requestHandlers = new RequestHandler[]{
                new RequestHandler("") { //redirect / to index.html
                    public void handle(HashMap<String, String> urlVars, RequestData req, SockConnection connection) {
                        File indexFile = SockConnection.getFileForPath(new String[]{"index.html"});
                        Map<String, String> headersOut = new TreeMap();
                        connection.sendFile(headersOut, indexFile);
                        //connection.sendFile();
                    }
                }
        };

        //start listen loop
        this.thread = new Thread(this);
        this.thread.start();//goto run() function
        
    }

    public void run() {
        SockConnection con = null;
        while (true) {
            try {
                //SockConnection constructor will handle request
                switch(mode){
                    case http:
                        System.out.println("Waiting for HTTP connection");
                        con = new HTTPConnection(this.serverSocket.accept(), this);
                        break;
                    case proxy:
                        System.out.println("Waiting for proxy connection");
                        con = new ProxyConnection(this.serverSocket.accept(), this);
                        break;
                }
                connections.put(con.getId(), con);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    enum ServerMode {
        http, proxy
    }
}
