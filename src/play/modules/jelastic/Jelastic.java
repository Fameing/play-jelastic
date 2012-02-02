package play.modules.jelastic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import play.Play;
import play.modules.jelastic.model.AuthenticationResponse;
import play.modules.jelastic.model.CreateObjectResponse;
import play.modules.jelastic.model.DeployResponse;
import play.modules.jelastic.model.UploadResponse;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

public class Jelastic {
    private Map<String, String> config;
    private String command;
    private String shema = "https";
    private int port = -1;
    private Double version = 1.0;
    private long totalSize;
    private int numSt;
    private CookieStore cookieStore = null;
    private String urlAuthentication = "/" + version + "/users/authentication/rest/signin";
    private String urlUploader = "/" + version + "/storage/uploader/rest/upload";
    private String urlCreateObject = "/deploy/createobject";
    private String urlDeploy = "/deploy/DeployArchive";

    public int getPort() {
        return port;
    }

    public String getShema() {
        return shema;
    }

    public CookieStore getCookieStore() {
        return cookieStore;
    }

    public String getUrlAuthentication() {
        return urlAuthentication;
    }

    public String getUrlUploader() {
        return urlUploader;
    }

    public String getUrlCreateObject() {
        return urlCreateObject;
    }

    public String getUrlDeploy() {
        return urlDeploy;
    }

    private static enum Command {
        DEPLOY("deploy"), PUBLISH("publish");

        private String title;

        Command(String title) {
            this.title = title;
        }

        public String getTitle() {
            return this.title;
        }

        public static Command fromString(String text) {
            if (text != null) {
                for (Command b : Command.values()) {
                    if (text.equalsIgnoreCase(b.title)) {
                        return b;
                    }
                }
            }
            return null;
        }
    }

    public Jelastic(String command) {
        this.command = command;
        initConfigurations();
/*        for (String key : config.keySet()) {
            System.out.println(key + "=" + config.get(key));
        }*/
    }

    private void initConfigurations() {
        config = new HashMap<String, String>();

        String email = Play.configuration.getProperty("jelastic.api.login");
        String password = Play.configuration.getProperty("jelastic.api.password");
        String context = Play.configuration.getProperty("jelastic.api.context");
        String environment = Play.configuration.getProperty("jelastic.api.environment");
        String apihoster = Play.configuration.getProperty("jelastic.api.apihoster");

        String app_war = System.getProperty("jelastic.app.war");
        config.put("war_file", app_war);
        email = System.getProperty("jelastic.api.login", email);
        config.put("login", email);
        password = System.getProperty("jelastic.api.password", password);
        config.put("password", password);
        context = System.getProperty("jelastic.api.context", context);
        config.put("context", context);
        environment = System.getProperty("jelastic.api.environment", environment);
        config.put("environment", environment);
        apihoster = System.getProperty("jelastic.api.apihoster", apihoster);
        config.put("apihoster", apihoster);
    }


    public static void main(String[] args) {
        File root = new File(System.getProperty("application.path"));
        Play.init(root, System.getProperty("play.id", ""));
        Thread.currentThread().setContextClassLoader(Play.classloader);


        if (args == null || args.length != 1) {
            System.out.println("bees.api.key is not set in application.conf or from --key");
        } else {
            Jelastic jelastic = new Jelastic(args[0]);
            jelastic.startProcessing();
        }

        System.out.println("-----------------");
        for (String arg : args) {
            System.out.println(arg);
        }
    }

    private void startProcessing() {
        if (Command.fromString(command).equals(Command.DEPLOY)) {
            deployApp();
        } else if (Command.fromString(command).equals(Command.PUBLISH)) {
            publishApp();
        } else {
            System.out.println("unknown command for jelastic module");
        }
    }


    private void deployApp() {
        System.out.print("Authentication processing...");
        AuthenticationResponse authenticationResponse = authentication();
        if (authenticationResponse.getResult() == 0) {
            System.out.println(" : OK");
            System.out.println("File Uploading processing ...");
            UploadResponse uploadResponse = upload(authenticationResponse);
            if (uploadResponse.getResult() == 0) {
                System.out.println("File Uploading : OK");
                System.out.print("Registration project processing...");
                CreateObjectResponse createObjectResponse = createObject(uploadResponse, authenticationResponse);
                if (createObjectResponse.getResult() == 0 && createObjectResponse.getResponse().getResult() == 0) {
                    System.out.println(" : OK");
                    System.out.print("Deploy processing...");
                    DeployResponse deployResponse = deploy(authenticationResponse, uploadResponse);
                    if (deployResponse.getResult() == 0 && deployResponse.getResponse().getResult() ==0 && deployResponse.getResponse().getResponses()[0].getResult() == 0) {
                        System.out.println(" : OK");
                    } else {
                        System.err.println(" : " + deployResponse.getError());
                    }
                } else {
                    System.err.println(" : " + createObjectResponse.getError());
                }
            } else {
                System.err.println(" : " + uploadResponse.getError());
            }
        } else {
            System.err.println(" : " + authenticationResponse.getError());
        }
    }

    private void publishApp() {
        System.out.print("Authentication processing...");
        AuthenticationResponse authenticationResponse = authentication();
        if (authenticationResponse.getResult() == 0) {
            System.out.println(" : OK");
            System.out.print("File Uploading processing ...");
            UploadResponse uploadResponse = upload(authenticationResponse);
            if (uploadResponse.getResult() == 0) {
                System.out.println("File Uploaded : OK");
                System.out.print("Registration File processing...");
                CreateObjectResponse createObjectResponse = createObject(uploadResponse, authenticationResponse);
                if (createObjectResponse.getResult() == 0 && createObjectResponse.getResponse().getResult() == 0) {
                    System.out.println(" : OK");
                } else {
                    System.err.println(" : " + createObjectResponse.getError());
                }
            } else {
                System.err.println(" : " + uploadResponse.getError());
            }
        } else {
            System.err.println(" : " + authenticationResponse.getError());
        }

    }


    private AuthenticationResponse authentication() {
        AuthenticationResponse authenticationResponse = null;
        try {
            DefaultHttpClient httpclient = new DefaultHttpClient();
            httpclient = wrapClient(httpclient);
            List<NameValuePair> qparams = new ArrayList<NameValuePair>();
            qparams.add(new BasicNameValuePair("login", config.get("login")));
            qparams.add(new BasicNameValuePair("password", config.get("password")));
            URI uri = URIUtils.createURI(getShema(), config.get("apihoster"), getPort(), getUrlAuthentication(), URLEncodedUtils.format(qparams, "UTF-8"), null);
            HttpGet httpGet = new HttpGet(uri);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String responseBody = httpclient.execute(httpGet, responseHandler);
            Gson gson = new GsonBuilder().setVersion(version).create();
            authenticationResponse = gson.fromJson(responseBody, AuthenticationResponse.class);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return authenticationResponse;
    }

    public UploadResponse upload(AuthenticationResponse authenticationResponse) {
        UploadResponse uploadResponse = null;
        try {
            DefaultHttpClient httpclient = new DefaultHttpClient();
            httpclient = wrapClient(httpclient);
            httpclient.setCookieStore(getCookieStore());

            final File file = new File(config.get("war_file"));
            if (!file.exists()) {
                throw new IllegalArgumentException("First build artifact and try again. Artifact not found .. ");
            }

            CustomMultiPartEntity multipartEntity = new CustomMultiPartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, new CustomMultiPartEntity.ProgressListener() {
                public void transferred(long num) {
                    if (((int) ((num / (float) totalSize) * 100)) != numSt) {
                        System.out.println("File Uploading : [" + (int) ((num / (float) totalSize) * 100) + "%]");
                        numSt = ((int) ((num / (float) totalSize) * 100));
                    }
                }
            });

            multipartEntity.addPart("fid", new StringBody("123456"));
            multipartEntity.addPart("session", new StringBody(authenticationResponse.getSession()));
            multipartEntity.addPart("file", new FileBody(file));
            totalSize = multipartEntity.getContentLength();

            URI uri = URIUtils.createURI(getShema(), config.get("apihoster"), getPort(), getUrlUploader(), null, null);
            HttpPost httpPost = new HttpPost(uri);
            httpPost.setEntity(multipartEntity);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String responseBody = httpclient.execute(httpPost, responseHandler);
            Gson gson = new GsonBuilder().setVersion(version).create();
            uploadResponse = gson.fromJson(responseBody, UploadResponse.class);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return uploadResponse;
    }

    public CreateObjectResponse createObject(UploadResponse upLoader, AuthenticationResponse authentication) {
        CreateObjectResponse createObjectResponse = null;
        try {
            DefaultHttpClient httpclient = new DefaultHttpClient();
            httpclient = wrapClient(httpclient);
            httpclient.setCookieStore(getCookieStore());
            List<NameValuePair> nameValuePairList = new ArrayList<NameValuePair>();
            nameValuePairList.add(new BasicNameValuePair("charset", "UTF-8"));
            nameValuePairList.add(new BasicNameValuePair("session", authentication.getSession()));
            nameValuePairList.add(new BasicNameValuePair("type", "JDeploy"));
            nameValuePairList.add(new BasicNameValuePair("data", "{'name':'" + upLoader.getName() + "', 'archive':'" + upLoader.getFile() + "', 'link':0, 'size':" + upLoader.getSize() + ", 'comment':'" + upLoader.getName() + "'}"));

            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(nameValuePairList, "UTF-8");

            URI uri = URIUtils.createURI(getShema(), config.get("apihoster"), getPort(), getUrlCreateObject(), null, null);
            HttpPost httpPost = new HttpPost(uri);
            httpPost.setEntity(entity);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String responseBody = httpclient.execute(httpPost, responseHandler);
            Gson gson = new GsonBuilder().setVersion(version).create();
            createObjectResponse = gson.fromJson(responseBody, CreateObjectResponse.class);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return createObjectResponse;
    }

    public DeployResponse deploy(AuthenticationResponse authentication, UploadResponse upLoader) {
        DeployResponse deployResponse = null;
        try {
            DefaultHttpClient httpclient = new DefaultHttpClient();
            httpclient = wrapClient(httpclient);
            httpclient.setCookieStore(getCookieStore());
            List<NameValuePair> qparams = new ArrayList<NameValuePair>();
            qparams.add(new BasicNameValuePair("charset", "UTF-8"));
            qparams.add(new BasicNameValuePair("session", authentication.getSession()));
            qparams.add(new BasicNameValuePair("archiveUri", upLoader.getFile()));
            qparams.add(new BasicNameValuePair("archiveName", upLoader.getName()));
            qparams.add(new BasicNameValuePair("newContext", config.get("context")));
            qparams.add(new BasicNameValuePair("domain", config.get("environment")));

            URI uri = URIUtils.createURI(getShema(), config.get("apihoster"), getPort(), getUrlDeploy(), URLEncodedUtils.format(qparams, "UTF-8"), null);
            HttpGet httpPost = new HttpGet(uri);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String responseBody = httpclient.execute(httpPost, responseHandler);
            Gson gson = new GsonBuilder().setVersion(version).create();
            deployResponse = gson.fromJson(responseBody, DeployResponse.class);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return deployResponse;
    }


    public static DefaultHttpClient wrapClient(DefaultHttpClient base) {
        try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            X509TrustManager tm = new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };
            ctx.init(null, new TrustManager[]{tm}, null);
            SSLSocketFactory ssf = new SSLSocketFactory(ctx);
            ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            ClientConnectionManager ccm = base.getConnectionManager();
            SchemeRegistry sr = ccm.getSchemeRegistry();
            sr.register(new Scheme("https", ssf, 443));
            return new DefaultHttpClient(ccm, base.getParams());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
