package alansorrill.networks.codeprojectone;

import java.io.IOException;

public class Main {
    //entry point
    public static void main(String[] args) {
        try {
            //call server constructor
            Server httpServer = new Server(3000, Server.ServerMode.http);
            Server proxyServer = new Server(3001, Server.ServerMode.proxy);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
