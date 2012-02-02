package play.modules.jelastic.model;

import com.google.gson.annotations.Since;

public class AuthenticationResponse {
    //{"uid":1163,"result":0,"source":"HX","session":"48bx56bede84ee34cbb2113d18d3abd053e2","email":"Igor.Yova@gmail.com","name":"Igor Yova","debug":{"time":47,"cpu":{"usage":0.0,"time":0}}}

    @Since(1.0)
    private int uid;
    private int result;
    @Since(1.0)
    private String source;
    @Since(1.0)
    private String session;
    @Since(1.0)
    private String email;
    @Since(1.0)
    private String name;
    
    private String error;
    private DebugResponse debug;

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public DebugResponse getDebug() {
        return debug;
    }

    public void setDebug(DebugResponse debug) {
        this.debug = debug;
    }
}
