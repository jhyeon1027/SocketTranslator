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
    private InputStream in;
    private OutputStream out;

    public Client(String SERVER_IP, int SERVER_PORT) {
        this.SERVER_IP = SERVER_IP;
        this.SERVER_PORT = SERVER_PORT;
    }

    public void connectToServer() {
        try {
            this.socket = new Socket(SERVER_IP, SERVER_PORT);
            // 서버와의 연결이 성립된 후에 In/OutputStream 객체를 초기화
            this.in = socket.getInputStream();
            this.out = socket.getOutputStream();
            //서버로 접속요청
            String connectMessage = "CONNECT:";
            byte[] connectMessageBytes = connectMessage.getBytes(StandardCharsets.UTF_8);
            out.write(connectMessageBytes);

            byte[] responseBuffer = new byte[1024];
            int bytesRead = in.read(responseBuffer);
            String responseMessage = new String(responseBuffer, 0, bytesRead, StandardCharsets.UTF_8);


            if (responseMessage.equals("CONNECTED")) {
                // 연결 성공일 경우
                System.out.println("연결 성공!");

            } else {
                // 연결 실패일 경우
                System.err.println("연결 실패!");
                System.out.println("서버 응답: " + responseMessage);
                // 연결 실패 후의 작업을 수행
                // 서버에서도 소켓을 종료하지만, 명시적으로 클라이언트에서도 하는게
                // 좋다고 함.
                disconnectFromServer();
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnectFromServer() {
        System.out.println("클라이언트 접속 종료");
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
