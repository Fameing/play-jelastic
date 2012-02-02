package play.modules.jelastic.model;

public class RequestResponse {
    //"request":{"fid":"123456","session":"48bx9c0db5c3b2aad07e82f5ade3c6d65a52"}
    
    private String fid;
    private String session;

    public String getFid() {
        return fid;
    }

    public void setFid(String fid) {
        this.fid = fid;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }
}
