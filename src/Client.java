import java.io.*;
import java.net.Socket;
import javax.swing.*;

public class Client {
    private static Socket socket;
    private static PrintWriter out;
    public static void main(String[] args) {
        final String SERVER_IP = "localhost";
        final int SERVER_PORT = 7777;

        try {
            // 서버에 연결
            Socket socket = new Socket(SERVER_IP, SERVER_PORT);

            // 클라이언트와 통신하기 위한 입출력 스트림 설정
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // 서버로 메시지 전송
            out.println("안녕하세요, 서버!");

            // 서버로부터 응답 수신
            String serverResponse = in.readLine();
            System.out.println("서버로부터 수신한 응답: " + serverResponse);

            SwingUtilities.invokeLater(() -> {
                Mainpage mainpage = new Mainpage();
                mainpage.openMainpage();
            });


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static Socket getSocket() {
        return socket;
    }

    public static PrintWriter getOut() {
        return out;
    }

}
