package alansorrill.networks.codeprojectone;

import java.io.File;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

public class ProxyConnection extends SockConnection {
    public ProxyConnection(Socket socket, Server serverRef) {
        super(socket, serverRef);
    }

    public void processRequest(RequestData req) {
        //loop through requestHandlers,
        // if any URL templates match: call handle() on it.
        HashMap<String, String> urlVars;
        for (int i = 0; i < server.requestHandlers.length; i++) {
            urlVars = server.requestHandlers[i].match(req.relativePath);
            if (urlVars != null) {
                server.requestHandlers[i].handle(urlVars, req, this);
                return;
            }
        }


        //if no requestHandlers matched, try to find and send file from public folder
        HashMap<String, String> outHeaders = new HashMap();
        File reqFile = SockConnection.getFileForPath(req.relativePath);
        if (reqFile.exists()) {
            System.out.println("Client requested cached " + reqFile.getAbsolutePath());
            sendFile(outHeaders, reqFile);
        } else {
            System.out.println("Client requested " + String.join("/", req.url));
            Map<String, String> reqHeaders = new HashMap<>();
            //reqHeaders.put("User-Agent", "Mozilla/4.0 (compatible; MSIE5.01; Windows NT)");
            URL requesteURL = null;
            try {
                requesteURL = new URL("http://" + req.url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
//            reqHeaders.put("Host", requesteURL.getHost());
//            reqHeaders.put("Connection", "close");
            //reqHeaders.put("Accept","image/gif, image/x-xbitmap, image/jpeg, image/pjpeg, */*");
            HttpClient client = new HttpClient(req.url, req.httpMethod, reqHeaders);
            client.aquire().then(new Function<File, Void>() {

                @Override
                public Void apply(File t) {
                    sendFile(new TreeMap<>(), t);
                    return null;
                }

            }).catchErr(new Function<Exception, Void>() {

                @Override
                public Void apply(Exception t) {
                    sendError(500, t.getMessage());
                    return null;
                }

            });
        }
    }
}
