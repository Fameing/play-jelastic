package play.modules.jelastic.model;

import com.google.gson.annotations.Since;

public class UploadResponse {
    //{"result":0,"file":"http://1efac12d36786c946e0b6842b84a9fb8.app.hivext.com/xssu/rest/download/D2YFUlsBWEhhVTtHQEJbRwQfFRNYQD9ILCV1BkQUQlUPDgBCY1VLQEg%3D","name":"play-test.war","request":{"fid":"123456","session":"48bx9c0db5c3b2aad07e82f5ade3c6d65a52"},"size":31821260}

    private int result;    
    @Since(1.0)
    private String file;
    @Since(1.0)
    private String name;
    @Since(1.0)
    private RequestResponse request;
    @Since(1.0)
    private int size;
    private String error;

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RequestResponse getRequest() {
        return request;
    }

    public void setRequest(RequestResponse request) {
        this.request = request;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
