package alansorrill.networks.codeprojectone;

import java.util.HashMap;

class RequestData {
    String url;
    String httpMethod;
    String[] relativePath;
    HashMap<String, String> headers = new HashMap();
    HashMap<String, String> query = new HashMap();

    public RequestData(String httpMethod, String url) {
        this.httpMethod = httpMethod;
        this.url = url;
        if (url.contains("?")) {
            String[] queryParts = url.split("/?")[1].split("&");
            String[] queryPart;
            for (int i = 0; i < queryParts.length; i++) {
                queryPart = queryParts[i].split("=");
                query.put(queryPart[0], queryPart[1]);
            }
            url = url.split("/?")[0];
        }
        relativePath = url.split("/");
    }

    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }
}