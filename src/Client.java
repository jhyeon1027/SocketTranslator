import java.io.*;
import java.net.Socket;
import javax.swing.*;

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
    private PrintWriter out;

    public Client(String SERVER_IP, int SERVER_PORT) {
        this.SERVER_IP = SERVER_IP;
        this.SERVER_PORT = SERVER_PORT;
    }

    public void connectToServer() {
        try {
            this.socket = new Socket(SERVER_IP, SERVER_PORT);
            // 서버와의 연결이 성립된 후에 PrintWriter 객체를 초기화
            this.out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendToServer(String message) {
        // 메시지 전송 메서드
        if(out != null){
            out.println(message);
        }
    }
}
