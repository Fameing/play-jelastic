package play.modules.jelastic.model;

public class CreateObjectResponse {
    //{"response":{"id":13967,"result":0,"source":"HX","object":{"id":13967,"developer":1163,"uploadDate":1328180128158}},"result":0,"source":"HX","debug":{"time":487,"cpu":{"usage":1.7983403,"time":70}}}
    //{"response":null,"result":1704,"source":"HX","error":"org.mozilla.javascript.EcmaError: ReferenceError: \"data\" is not defined.","line":3,"debug":{"time":50,"cpu":{"usage":9.980208,"time":40}}}
    private Response response;
    private int result;
    private String source;
    private String error;
    private DebugResponse debug;

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
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

    public static class Response {
        private int id;
        private int result;
        private String source;
        private ObjectResponse object;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
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

        public ObjectResponse getObject() {
            return object;
        }

        public void setObject(ObjectResponse object) {
            this.object = object;
        }
    }

    public static class ObjectResponse {
        private int id;
        private int developer;
        private Long uploadDate;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getDeveloper() {
            return developer;
        }

        public void setDeveloper(int developer) {
            this.developer = developer;
        }

        public Long getUploadDate() {
            return uploadDate;
        }

        public void setUploadDate(Long uploadDate) {
            this.uploadDate = uploadDate;
        }
    }
}
