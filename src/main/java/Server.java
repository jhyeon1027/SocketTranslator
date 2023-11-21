import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;


public class Server {
    private static List<OutputStream> clientOutputStreams = new ArrayList<>();
    private static List<String> connectedUsers = new ArrayList<>(); // 연결된 클라이언트들의 사용자 이름을 저장하는 리스트
    private final int SERVER_PORT;

    NTranslator translator = new NTranslator(); // NTranslator 객체 생성
    ImageTranslator imageTranslator = new ImageTranslator(); //  ImageTranslator 객체 생성

    PDFtoImage pdftoImage = new PDFtoImage(); // PDFtoImage 객체 생성

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

                        byte[] connectBuffer = new byte[1024];
                        int connectBytesReceived = in.read(connectBuffer);
                        String connectMessage = new String(connectBuffer, 0, connectBytesReceived, StandardCharsets.UTF_8);

                        if (connectMessage.equals("CONNECT:")) {
                            // 클라이언트가 CONNECT 메시지를 보냈을 때의 처리
                            System.out.println("클라이언트가 연결되었습니다.");
                            out.write("CONNECTED:".getBytes(StandardCharsets.UTF_8));
                            // 이후에 클라이언트와의 통신 계속 진행
                        } else {
                            // 다른 메시지가 오면 연결 거부 처리 등을 수행
                            System.out.println("올바르지 않은 연결 요청입니다.");
                            out.write("CONN REF:".getBytes(StandardCharsets.UTF_8));
                            s.close(); // 연결 종료
                        }

                        byte[] buffer = new byte[2 * 1024 * 1024]; //2메가
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
                                    String responseMessage;
                                    if (translatedText.startsWith("TRANS FAIL:")){
                                        responseMessage = "TRANS FAIL:";
                                    }
                                    else{
                                        responseMessage = "TRANSLATED:" +translatedText;
                                    }
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
                                    String responseMessage = translatedText;
                                    if (Objects.equals(responseMessage, "null")){ //만약 Null값이 return된다면..
                                        responseMessage = "IMAGETRANS FAIL:";
                                    }
                                    else responseMessage = "IMAGETRANSLATED:"+responseMessage;
                                    byte[] responseMessageBytes = responseMessage.getBytes(StandardCharsets.UTF_8);
                                    out.write(responseMessageBytes);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }else if(clientMessage.startsWith("PDFTRANSLATE:")){
                                String jsonMessage = clientMessage.substring(13);
                                JSONParser parser = new JSONParser();
                                try{
                                    JSONObject json = (JSONObject) parser.parse(jsonMessage);
                                    String base64File = (String) json.get("filename");
                                    String sourceLanguage = (String) json.get("sourceLanguage");
                                    String targetLanguage = (String) json.get("targetLanguage");

                                    String responseMessage;
                                    responseMessage=pdftoImage.translateImage(base64File, sourceLanguage, targetLanguage);
                                    if(responseMessage=="Success")
                                        responseMessage="PDFTRANSLATED:";
                                    else if (responseMessage=="Fail")
                                        responseMessage="PDFTRANS FAIL:";
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

                            }else if(clientMessage.startsWith("CHAT:")){
                                tellEveryone(clientMessage, out);
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