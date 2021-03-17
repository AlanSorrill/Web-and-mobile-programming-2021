package alansorrill.networks.codeprojectone;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;


public class HttpClient implements Runnable{

    private String url = null;
    private String method = null;
    private Map<String, String> headers = null;
    private Socket socket;

    public HttpClient(String url, String method, Map<String, String> headers) {
            this.url = url;
            this.method = method;
            if (headers == null) {
                this.headers = new TreeMap<>();
            } else {
                this.headers = headers;
            }
    }

    private Promise<File> promise = null;
    public Promise<File> aquire() {
        if(promise != null){
            return promise;
        }
        this.promise = new Promise<>();
        new Thread(this).start();

        return promise;
    }
    public void run(){
        try {
            URL parsed = new URL("http://" + this.url);
            String cleanAuthority = parsed.getAuthority().replaceAll(":", "port").replace(".", "dot");

            String cleanFileName = cleanAuthority + parsed.getFile().replace("/","~").replace("?","query").replace("&","and").replace("=", "equ").replaceAll(".", "dot");
            File cacheFile = SockConnection.getFileForPath(new String[]{"proxCache",cleanAuthority, cleanFileName});

            if(cacheFile.exists()){
                promise.accept(cacheFile);
                return;
            }

            int port = parsed.getPort();
            if(port == -1){
                port = 80;
            }
            this.socket = new Socket(parsed.getHost(), port);

            PrintWriter pw = new PrintWriter(this.socket.getOutputStream(), false);
            System.out.println("GET /" + parsed.getFile() + " HTTP/1.1");
            pw.println("GET /" + parsed.getFile() + " HTTP/1.1\r");
            if(headers != null){
                headers.keySet().forEach(new Consumer<String>(){

                    @Override
                    public void accept(String t) {
                        System.out.println(t + ": " + headers.get(t));
                        pw.println(t + ": " + headers.get(t) + "\r");
                    }
                    
                });
            }
            pw.println("\r");
            System.out.println("{BlankLine}");
            pw.flush();
            BufferedReader lineReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String[] firstLineParts = lineReader.readLine().split(" ");
            int respCode = Integer.parseInt(firstLineParts[1]);
            String respMsg = firstLineParts[2];
            String line;
            if(respCode != 200){
                StringBuilder err = new StringBuilder();
                while((line = lineReader.readLine()) != null){
                    err.append(line).append("\n");
                }
                promise.reject(new Exception("Got bad response " + respMsg + "\n" + err.toString()));
                return;
            }
            Map<String, String> respHeaders = new TreeMap<>();
            String lineParts[];
            while((line = lineReader.readLine())!=null){
                if(line.equals("")){
                    break;
                }
                lineParts = line.split(": ");
                respHeaders.put(lineParts[0], lineParts[1]);
            }

            
            if(!cacheFile.exists()){
                cacheFile.createNewFile();
            }
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(cacheFile));
            BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
            SockConnection.StreamTransfer(bis, bos, 8);
            socket.close();
            bos.close();
            promise.accept(cacheFile);

        } catch (MalformedURLException e) {
            // Shouldn't happen
            e.printStackTrace();
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
