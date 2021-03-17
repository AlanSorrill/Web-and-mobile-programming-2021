package alansorrill.networks.codeprojectone;

import java.io.File;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class HTTPConnection extends SockConnection{
    public HTTPConnection(Socket socket, Server serverRef) {
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
        Map<String, String> outHeaders = new HashMap<>();
        File reqFile = SockConnection.getFileForPath(req.relativePath);
        System.out.println("Client requested " + reqFile.getAbsolutePath());
        sendFile(outHeaders, reqFile);
    }
}
