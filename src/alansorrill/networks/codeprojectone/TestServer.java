package alansorrill.networks.codeprojectone;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class TestServer {
    public static void main(String[] args) {
        try {
            ServerSocket ssk = new ServerSocket(80);
            while(true){
                Socket s = ssk.accept();
                BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
                String line;
                while((line = br.readLine()) != null){
                    System.out.println(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
