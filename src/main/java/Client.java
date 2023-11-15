import java.io.*;
import java.net.Socket;
import javax.swing.*;
import java.nio.charset.StandardCharsets;

public class Client {

    public static void main (String args[]){
        SwingUtilities.invokeLater(() -> {
            Mainpage mainpage = new Mainpage();
            mainpage.openMainpage();
        });
    }

    private final String SERVER_IP;
    private final int SERVER_PORT;
    private Socket socket;
    private OutputStream out;

    public Client(String SERVER_IP, int SERVER_PORT) {
        this.SERVER_IP = SERVER_IP;
        this.SERVER_PORT = SERVER_PORT;
    }

    public void connectToServer() {
        try {
            this.socket = new Socket(SERVER_IP, SERVER_PORT);
            // 서버와의 연결이 성립된 후에 OutputStream 객체를 초기화
            this.out = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnectFromServer() {
        System.out.println("종료하게요");
        try {
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendToServer(byte[] messageBytes) {
        if(out != null){
            try {
                out.write(messageBytes);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Socket getSocket() {
        return this.socket;
    }
}
