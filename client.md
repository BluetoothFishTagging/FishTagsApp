---
title: Client
layout: template
filename: client
---

## Android HTTP Client

[Link to Github](https://github.com/BluetoothFishTagging/FishTagsApplication/tree/master/FishTagsApp/app/src/main/java/bft/fishtagsapp/client)

1. Declare the necessary permissions in your AndroidManifest.xml:

   ```xml
   <uses-permission android:name="android.permission.INTERNET" />
   <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
   ```

2. Import the relevant packages:

   ```java
   import java.io.InputStream;
   import java.io.OutputStream;
   import java.net.HttpURLConnection;
   import java.net.URL;
   ```

3. Example GET Client:

   ```java
   public class HttpClient {
       private String url;
       private HttpURLConnection con;
       private InputStream is;
       private OutputStream os;
   
       public void connect(String url) {
           this.url = url;
           try {
               con = (HttpURLConnection) new URL(url).openConnection();
               con.setRequestMethod("GET");
               con.setDoInput(true);
               con.connect();
   
           } catch (Exception e) {
               e.printStackTrace();
           }
       }
   
       public String getResponse() throws Exception {
           is = con.getInputStream();
           byte[] b1 = new byte[1024];
           StringBuffer buffer = new StringBuffer();
   
           while (is.read(b1) != -1)
               buffer.append(new String(b1));
   
           con.disconnect();
   
           return buffer.toString();
       }
   }
   ```

4. Example POST Client with [Multipart Form](http://stackoverflow.com/questions/4526273/what-does-enctype-multipart-form-data-mean) Data:

```java
public class HttpClient {
    private String url;
    private HttpURLConnection con;
    private InputStream is;
    private OutputStream os;
    private final String boundary = "|";
    private final String delimiter = "--";

    public void connect(String url) {
        this.url = url;
        try {
            con = (HttpURLConnection) (new URL(url)).openConnection();
            con.setRequestMethod("POST");
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setRequestProperty("Connection", "Keep-Alive");
            con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            con.connect();
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
        is = con.getInputStream();
        byte[] b1 = new byte[1024];
        StringBuffer buffer = new StringBuffer();

        while (is.read(b1) != -1)
            buffer.append(new String(b1));

        con.disconnect();

        return buffer.toString();
    }

    private void writeParamData(String paramName, String value) throws Exception {
        os.write((delimiter + boundary + "\r\n").getBytes());
        os.write("Content-Type: text/plain\r\n".getBytes());
        os.write(("Content-Disposition: form-data; name=\"" + paramName + "\"\r\n").getBytes());
        os.write(("\r\n" + value + "\r\n").getBytes());
    }
}
```
