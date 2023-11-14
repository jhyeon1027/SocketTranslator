import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {

    private static List<String> connectedUsers = new ArrayList<>(); // 연결된 클라이언트들의 사용자 이름을 저장하는 리스트

    // 연결된 클라이언트들의 출력 스트림(username)을 저장하는 리스트
    private String getUserListMessage() {
        return "[Server] Users: " + String.join(", ", connectedUsers);
    }
    public static void main(String[] args) {
        final int PORT = 7777;

        try {
            // 서버 소켓 생성
            ServerSocket ss = new ServerSocket(PORT);
            System.out.println("서버가 " + PORT + " 포트에서 대기 중...");

            while (true) {
                // 클라이언트 연결을 기다림
                Socket s = ss.accept();
                System.out.println("클라이언트가 연결되었습니다.");

                // 클라이언트와 통신하기 위한 입출력 스트림 설정
                BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                PrintWriter out = new PrintWriter(s.getOutputStream(), true);

                // 클라이언트로부터 메시지 수신 및 응답
                String clientMessage = in.readLine();
                System.out.println("클라이언트로부터 수신한 메시지: " + clientMessage);
                // 수신한 메시지가 "USERNAME:"으로 시작하면 사용자 이름으로 처리
                if (clientMessage.startsWith("USERNAME:")) {
                    String username = clientMessage.substring(9); // "USERNAME:" 다음에 오는 문자열을 사용자 이름으로 간주
                    connectedUsers.add(username);
                    System.out.println("Connected users: " + connectedUsers);
                }
                // 클라이언트에게 응답 전송
                out.println("서버가 메시지를 수신했습니다.");


            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void receiveUsername(BufferedReader in) {
        try {
            String username = in.readLine();
            connectedUsers.add(username);
            System.out.println("Connected users: " + connectedUsers);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
