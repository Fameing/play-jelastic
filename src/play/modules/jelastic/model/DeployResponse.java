package play.modules.jelastic.model;

public class DeployResponse {
    
    //{"response":{"result":0,"source":"HX","responses":[{"result":0,"source":"HX","nodeid":11170,"out":"Starting tomcat: [  OK  ]\r\n"}]},"result":0,"source":"HX","debug":{"time":18311,"cpu":{"usage":0.013653264,"time":20}}}

    private int result;
    private String source;
    private String error;
    private DebugResponse debug;
    private Response response;

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getSource() {
        return source;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public DebugResponse getDebug() {
        return debug;
    }

    public void setDebug(DebugResponse debug) {
        this.debug = debug;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public static class Response{
        private int result;
        private String source;
        private Responses[] responses;

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

        public Responses[] getResponses() {
            return responses;
        }

        public void setResponses(Responses[] responses) {
            this.responses = responses;
        }
    }

    public static class Responses {
        private int result;
        private String source;
        private int nodeid;
        private String out;

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

        public int getNodeid() {
            return nodeid;
        }

        public void setNodeid(int nodeid) {
            this.nodeid = nodeid;
        }

        public String getOut() {
            return out;
        }

        public void setOut(String out) {
            this.out = out;
        }
    }
    
    
}
