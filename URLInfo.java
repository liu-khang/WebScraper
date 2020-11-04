import java.util.Date;

public class URLInfo {
    String URL;
    String contentType;
    int contentLength;
    String lastModified;
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
