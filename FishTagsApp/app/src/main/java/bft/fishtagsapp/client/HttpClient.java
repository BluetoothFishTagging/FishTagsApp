package bft.fishtagsapp.client;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by jamiecho on 3/9/16.
 */
public class HttpClient {
    private String url;
    private HttpURLConnection con;
    private InputStream is;
    private OutputStream os;
    private final String boundary = "|";
    private final String delimiter = "--";

    public HttpClient() {
        setUrl("http://192.168.16.67:8000/");
    }

    public HttpClient(String s) {
        setUrl(s);
    }

    public void setUrl(String s) {
        url = s;
    }

    public void connect() {
        try {
            con = (HttpURLConnection) new URL(url).openConnection();
            con.setRequestMethod("GET");
            con.setDoInput(true);
            con.connect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void connectMultipart() {
        try {
            con = (HttpURLConnection) (new URL(url)).openConnection();
            con.setRequestMethod("POST");
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setRequestProperty("Connection", "Keep-Alive");
            con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            con.connect();
            //is = con.getInputStream();
            os = con.getOutputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void addFormPart(String paramName, String value) throws Exception {
        writeParamData(paramName, value);
    }

    public void addFilePart(String paramName, String fileName, byte[] data) throws Exception {
        os.write((delimiter + boundary + "\r\n").getBytes());
        os.write(("Content-Disposition: form-data; name=\"" + paramName + "\"; filename=\"" + fileName + "\"\r\n").getBytes());
        os.write(("Content-Type: application/octet-stream\r\n").getBytes());
        os.write(("Content-Transfer-Encoding: binary\r\n").getBytes());
        os.write("\r\n".getBytes());

        os.write(data);

        os.write("\r\n".getBytes());
    }

    public void finishMultipart() throws Exception {
        os.write((delimiter + boundary + delimiter + "\r\n").getBytes());
    }


    public String getResponse() throws Exception {

        try{
            StringBuffer buffer = new StringBuffer();
            is = con.getInputStream();
            byte[] b1 = new byte[1024];
            while (is.read(b1) != -1)
                buffer.append(new String(b1));
            con.disconnect();
            return buffer.toString();
        }catch(FileNotFoundException e){
            return "SERVER DOWN";
        }


    }

    private void writeParamData(String paramName, String value) throws Exception {
        os.write((delimiter + boundary + "\r\n").getBytes());
        os.write("Content-Type: text/plain\r\n".getBytes());
        os.write(("Content-Disposition: form-data; name=\"" + paramName + "\"\r\n").getBytes());
        ;
        os.write(("\r\n" + value + "\r\n").getBytes());
    }
}
