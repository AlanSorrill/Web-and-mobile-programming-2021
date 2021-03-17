package alansorrill.networks.codeprojectone;

import java.util.HashMap;

/**
 * Request Handler allows for default file serving behavior to be overridden.
 * The server will check each requested url against the template, allowing for paramaeters
 * i.e. if the template was localhost:3000/users/$userId, this request handler would match
 * for localhost:3000/users/alan, as well as any other username. These values are saved in a map.
 */
public abstract class RequestHandler {
    private String[] template;

    public RequestHandler(String template) {
        this.template = template.split("/");
    }

    public HashMap<String, String> match(String[] url) {
        HashMap<String, String> params = new HashMap();
        for (int i = 0; i < url.length; i++) {
            if (i >= template.length) {
                return null;
            }
            if (template[i].startsWith("$")) {
                params.put(template[i].substring(1), url[i]);
            } else if (!template[i].equals(url[i])) {
                return null;
            }

        }
        return params;
    }

    public abstract void handle(HashMap<String, String> urlVars, RequestData req, SockConnection connection);
}
