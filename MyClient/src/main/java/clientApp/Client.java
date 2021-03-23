package clientApp;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Client implements Closeable {

    private static Client instance;

    private Socket socket;
    private DataInputStream is;
    private DataOutputStream os;
    private byte[] buffer;
    private final int PORT = 1234;

    public static Client getInstance(){
        if (instance == null) {
            instance = new Client();
        }
        return instance;
    }

    private Client(){
        try {
            this.socket = new Socket("localhost", PORT);
            this.is = new DataInputStream(socket.getInputStream());
            this.os = new DataOutputStream(socket.getOutputStream());
            this.buffer = new byte[512];
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Отправка сообщений
    public void sendMessage(String msg) {
        try {
            os.write(msg.getBytes(StandardCharsets.UTF_8));
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Чтение сообщений
    public String readMessage() {
        String msg = "";
        try {
            int bytesRead = is.read(buffer);
            msg = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return msg;
    }

    @Override
    public void close() throws IOException {
        is.close();
        os.close();
    }
}
