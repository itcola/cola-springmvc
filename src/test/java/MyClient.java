import java.io.*;
import java.net.Socket;

public class MyClient {
    public static void main(String[] args) throws IOException {
        Socket client = new Socket("127.0.0.1", 12345);

        OutputStream outputStream = client.getOutputStream();
        FileInputStream fis = new FileInputStream("C:\\z-resource\\cola.png");
        byte[] bytes = new byte[1024];
        int len = 0;
        while ((len = fis.read(bytes)) != -1) {
            outputStream.write(bytes, 0, len);
        }
        client.shutdownOutput();
        InputStream inputStream = client.getInputStream();
        byte[] bytes1 = new byte[1024];
        int len1 = inputStream.read(bytes1);
        System.out.println(new String(bytes1,0,len1));


        client.close();
        inputStream.close();
        fis.close();

    }

}
