public class URLInfo {
    String URL; // The URL itself
    String contentType; // The type of content pertaining to the URL
    int contentLength; // Length of the content
    String lastModified; // Last time the page has been modified
    long expiration;
    String contentEncoding;


    public URLInfo(String url, String cType, int cLength, String lModified, long expir, String cEncoding) {
        URL = url;
        contentType = cType;
        contentLength = cLength;
        lastModified = lModified;
        expiration = expir;
        contentEncoding = cEncoding;
    }

    public String getURL() {
        return URL;
    }

    public String getContentType() {
        return contentType;
    }

    public int getContentLength() {
        return contentLength;
    }

    public String getLastModified() {
        return lastModified;
    }

    public long getExpiration() {
        return expiration;
    }

    public String getContentEncoding() {
        return contentEncoding;
    }
}
