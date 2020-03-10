import com.sun.deploy.util.StringUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class MyServer {
    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(12345);

        while (true) {
            final Socket client = server.accept();

            new Thread(new Runnable() {
                public void run() {
                    try {
                        InputStream inputStream = client.getInputStream();

                        FileOutputStream fos = new FileOutputStream("C:\\z-resource\\JavaEE\\" + System.currentTimeMillis() + ".png");

                        byte[] bytes = new byte[1024];
                        int len = 0;
                        while ((len = inputStream.read(bytes)) != -1) {
                            fos.write(bytes, 0, len);
                        }

                        OutputStream outputStream = client.getOutputStream();
                        outputStream.write((client.getInetAddress().getHostAddress()+"您的数据传输成功").getBytes());

                        outputStream.close();
                        client.close();
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }).start();
        }

    }
}
