import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Base64;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.nio.file.Files;



public class Server {
    private static List<OutputStream> clientOutputStreams = new ArrayList<>();
    private static List<String> connectedUsers = new ArrayList<>(); // 연결된 클라이언트들의 사용자 이름을 저장하는 리스트
    private final int SERVER_PORT;

    NTranslator translator = new NTranslator(); // NTranslator 객체 생성
    ImageTranslator imageTranslator = new ImageTranslator(); //  ImageTranslator 객체 생성

    public Server(int SERVER_PORT) {
        this.SERVER_PORT = SERVER_PORT;
    }

    public void startServer() {
        try {
            ServerSocket ss = new ServerSocket(SERVER_PORT);
            System.out.println("서버가 " + SERVER_PORT + " 포트에서 대기 중...");

            while (true) {
                Socket s = ss.accept();
                new Thread(() -> {
                    try {
                        System.out.println("클라이언트가 연결되었습니다.");

                        InputStream in = s.getInputStream();
                        OutputStream out = s.getOutputStream();
                        clientOutputStreams.add(out);

                        byte[] buffer = new byte[2 * 1024 * 1024];
                        int bytesReceived;
                        while ((bytesReceived = in.read(buffer)) != -1) {
                            String clientMessage = new String(buffer, 0, bytesReceived, StandardCharsets.UTF_8);
                            System.out.println("클라이언트로부터 수신한 메시지: " + clientMessage);

                            if (clientMessage.startsWith("USERNAME:")) {
                                String username = clientMessage.substring(9);
                                connectedUsers.add(username);
                                //사용자 명단 업데이트
                                String userListMessage = "USERLIST:" + String.join(",", connectedUsers);
                                tellEveryone(userListMessage, out);
                                try {
                                    Thread.sleep(200); // 200 밀리초 대기. 같이 전해지는 오류때문에
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                System.out.println("Connected users: " + connectedUsers);
                                // 클라이언트가 연결되었다는 메시지를 모든 클라이언트에게 전송
                                String connectionMessage = "JOIN:"+"["+username + "님이 입장하였습니다.]";
                                tellEveryone(connectionMessage, out);
                            } else if (clientMessage.startsWith("EXIT:")) {
                                String username = clientMessage.substring(5);
                                connectedUsers.remove(username);
                                clientOutputStreams.remove(out);
                                //사용자 명단 업데이트
                                String userListMessage = "USERLIST:" + String.join(",", connectedUsers);
                                tellEveryone(userListMessage, out);

                                System.out.println("User exited: " + username);
                                System.out.println("Connected users: " + connectedUsers);
                                // 클라이언트가 연결 해제 되었다는 메시지를 모든 클라이언트에게 전송
                                String connectionMessage = "eXIT:"+"[" + username + "님이 퇴장하였습니다.]";
                                tellEveryone(connectionMessage, out);
                                break;
                            } else if(clientMessage.startsWith("TransEXIT")){
                                System.out.println("번역기가 종료되었습니다.");
                                clientOutputStreams.remove(out);
                                break;
                            } else if(clientMessage.startsWith("ImageEXIT")){
                                System.out.println("이미지 번역기가 종료되었습니다.");
                                clientOutputStreams.remove(out);
                                break;
                            }else if (clientMessage.startsWith("TRANSLATE:")) {
                                String jsonMessage = clientMessage.substring(10);
                                JSONParser parser = new JSONParser();
                                try {
                                    JSONObject json = (JSONObject) parser.parse(jsonMessage);
                                    String inputText = (String) json.get("inputText");
                                    String sourceLanguage = (String) json.get("sourceLanguage");
                                    String targetLanguage = (String) json.get("targetLanguage");
                                    // 이 위에까지가 번역정보. 이 아래부터는 번역기능 구현
                                    String translatedText = translator.translateText(inputText, sourceLanguage, targetLanguage);
                                    // 번역 결과를 클라이언트에게 전송
                                    String responseMessage = /*"TRANSLATE:" +일단없앰*/translatedText;
                                    byte[] responseMessageBytes = responseMessage.getBytes(StandardCharsets.UTF_8);
                                    out.write(responseMessageBytes);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            } else if(clientMessage.startsWith("IMAGETRANSLATE:")){
                                String jsonMessage = clientMessage.substring(15);
                                JSONParser parser = new JSONParser();
                                try{
                                    JSONObject json = (JSONObject) parser.parse(jsonMessage);
                                    String base64Image = (String) json.get("image");
                                    byte[] imageBytes = Base64.getDecoder().decode(base64Image);
                                    String sourceLanguage = (String) json.get("sourceLanguage");
                                    String targetLanguage = (String) json.get("targetLanguage");
                                    File imageFile = File.createTempFile("temp_image", ".jpg");
                                    Files.write(imageFile.toPath(), imageBytes);
                                    String translatedText = imageTranslator.translateImage(imageFile, sourceLanguage, targetLanguage);
                                    String responseMessage = /*"IMAGETRANSLATE:" +일단없앰*/translatedText;
                                    byte[] responseMessageBytes = responseMessage.getBytes(StandardCharsets.UTF_8);
                                    out.write(responseMessageBytes);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }else if(clientMessage.startsWith("LiveTRANSLATE:")){
                                String jsonMessage = clientMessage.substring(14);
                                JSONParser parser = new JSONParser();
                                try {
                                    JSONObject json = (JSONObject) parser.parse(jsonMessage);
                                    String username = (String) json.get("Username");
                                    String inputText = (String) json.get("inputText");
                                    String sourceLanguage = (String) json.get("sourceLanguage");
                                    String targetLanguage = (String) json.get("targetLanguage");
                                    // 이 위에까지가 번역정보. 이 아래부터는 번역기능 구현
                                    String translatedText = translator.translateText(inputText, sourceLanguage, targetLanguage);
                                    // 번역 결과를 클라이언트에게 전송
                                    String responseMessage = "TranslatedText:"+username+": "+translatedText;
                                    byte[] responseMessageBytes = responseMessage.getBytes(StandardCharsets.UTF_8);
                                    out.write(responseMessageBytes);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                            }else {
                                tellEveryone(clientMessage, out);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //서버가 메시지를 받았을 때 모든 클라이언트에게 전송하는 메서드..
    public static void tellEveryone(String message, OutputStream sender) {
        System.out.println("Sending message: " + message);
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        for (OutputStream out : clientOutputStreams) {
            try {
                out.write(messageBytes);
                out.flush();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
        System.out.println("Message sent: " + message);
    }

    public static void main(String[] args) {
        Server server = new Server(7777);
        server.startServer();
    }
}