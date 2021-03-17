package alansorrill.networks.codeprojectone;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public abstract class SockConnection implements Runnable {
    private final Socket socket;
    private static int count = 0;
    private static int bufferSize = 8; //in kilobytes
    private final Integer id;
    protected final Server server;
    private final Thread thread;
    private BufferedReader reader;
    private PrintWriter printer;

    public SockConnection(Socket socket, Server serverRef) {
        //store references
        this.socket = socket;
        this.server = serverRef;
        this.id = SockConnection.count++;
        //create new thread to handle connection
        this.thread = new Thread(this);
        //start thread (go to this.run() method)
        thread.start();
    }


    @Override
    public void run() {
        //new thread starts here
        if (socket.isConnected()) {
            try {
                //create reader and printer to be able to send and receive data as strings
                this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                this.printer = new PrintWriter(socket.getOutputStream(), true);
                System.out.println("Connected");
                //handle rest of connection
                this.onConnected();
            } catch (IOException e) {
                //This really shouldn't happen unless the internet is being naughty
                e.printStackTrace();
            }
        } else {
            System.err.println("Socket disconnected");
            onClose();
        }
    }

    public void onConnected() throws IOException {
        //consume HTTP
        String l = this.reader.readLine();
        if(l == null){
            System.err.println("Empty first line");
            this.thread.stop();
            return;
        }
        String[] firstLine = this.reader.readLine().split(" ");
    
       
        //parse request into object for organization
        RequestData req = new RequestData(firstLine[0], firstLine[1]);

        String line;
        String[] parts;
        //read headers into request object's header map
        while ((line = this.reader.readLine()) != null) {
            if (line.equals("")) {
                break;
            }
            parts = line.split(": ");
            req.addHeader(parts[0], parts[1]);
        }

        processRequest(req);
    }

    public abstract void processRequest(RequestData req);

    public void sendFile(Map<String, String> headers, File file) {
        //overloaded function, statusCode and statusMessage default to 200 OK
        sendFile(headers, file, 200, "OK");
    }

    public static void StreamTransfer(BufferedInputStream fis, BufferedOutputStream bos, int bufferSize){
        try {

            byte[] buffer = new byte[1024 * bufferSize];

            while ((fis.read(buffer)) > 0) {
                //read the file bufferSize kb at a time, and stream it to the socket
                bos.write(buffer);
            }
            bos.flush();
        } catch (FileNotFoundException e) {
            //Shouldn't happen, bad requests should be handled elsewhere
            e.printStackTrace();
        } catch (IOException e) {
            //Shouldn't happen
            e.printStackTrace();
        }
    }


    public void sendFile(Map<String, String> headers, File file, int statusCode, String statusMessage) {
        if (!file.exists()) {
            //tries to send 404.html. If it can't even find that, it will just send the error line
            if (statusCode == 404) {
                sendError(404, "File used to support error messages not found", headers);
                return;
            }
            sendFile(headers, SockConnection.getFileForPath(new String[]{"404.html"}), 404, "File not found");
            return;
        }

        //File exists, send it
        System.out.println("Sending file " + file.getAbsolutePath());
        printer.println("HTTP/1.1 " + statusCode + " " + statusMessage);
        //send headers
        for (String key : headers.keySet()) {
            printer.println(key + ": " + headers.get(key));
        }
        //send blank line per http
        printer.println("");

        try {
            
            BufferedInputStream fis = new BufferedInputStream(new FileInputStream(file));
            BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
            //printer and writer are great for writing strings line by line
            //but for non-text files, a buffered stream transfer is required
            StreamTransfer(fis, bos, bufferSize);
            socket.close();
        } catch (FileNotFoundException e) {
            //Shouldn't happen, bad requests should be handled elsewhere
            e.printStackTrace();
        } catch (IOException e) {
            //Shouldn't happen
            e.printStackTrace();
        }
    }

    public void onClose() {
        //close socket if it's still open
        if(!socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                //shouldn't happen
                e.printStackTrace();
            }
        }
        //remove reference, hopefully causing garbage collection
        server.connections.remove(id);
        //halt thread to help with garbage collection
        thread.stop();
    }

    public void sendError(int statusCode, String statusMessage) {
        //overloaded function, headers are optional
        sendError(statusCode, statusMessage, null);
    }

    public void sendError(int statusCode, String statusMessage, Map<String, String> headers) {
        if (!socket.isConnected()) {
            //socket already closed
            System.err.println("Cannot send " + statusCode + ": " + statusMessage);
            System.err.println("Socket " + socket.getRemoteSocketAddress().toString() + " already closed");
            return;
        }
        System.err.println(statusCode + ": " + statusMessage);
        printer.println("HTTP/1.1 " + statusCode + " " + statusMessage);
        //write headers if any are given
        if (headers != null) {
            for (String key : headers.keySet()) {
                printer.println(key + ": " + headers.get(key));
            }
        }
        //write blank line per http
        printer.println("");
        onClose();

    }


    public Integer getId() {
        return id;
    }


    public static File getFileForPath(String[] path) {
        String fileSep = System.getProperty("file.separator");
        return new File(System.getProperty("user.dir") + fileSep + "public" + fileSep + String.join(fileSep, path));
    }


}