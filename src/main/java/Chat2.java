import org.json.simple.JSONObject;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Chat2 extends JFrame {
    private Map<String, String> languageCodeMap;

    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JList<String> userList;
    private JButton exitButton;
    private JComboBox<String> languageComboBox;
    private JCheckBox TranslationCheckbox;
    private PrintWriter writer;

    private Client client;
    private String username;
    private NTranslator nTranslator;


    public static void main(String[] args){
        Chat2 chat = new Chat2();
    }
    public Chat2() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Image img = toolkit.getImage("src/main/resources/CATPAGO_LOGO.png");
        //@@ SocketTranslator/src/main/resources/CATPAGO_LOGO
        setIconImage(img);
        //각 프로그램별 로고이미지 부분


        this.nTranslator = new NTranslator();
        createChatUI();
        String username = JOptionPane.showInputDialog("사용자 이름을 입력하세요 :");
        setUsername(username);
        connectToServer();
        addWindowListener(new CustomWindowAdapter());
        // 스크롤이 항상 아래로 이동하도록 설정
        DefaultCaret caret = (DefaultCaret) chatArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
    }
    public boolean isSocketClosed() {
        return client.getSocket().isClosed();
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public void connectToServer() {
        this.client = new Client("localhost", 7777); // 예시로 localhost와 7777 포트를 사용
        this.client.connectToServer();
        sendUsername();
        Thread thread = new Thread(new Chat2.IncomingReader());
        thread.start();
    }
    public void disconnectFromServer() {
        this.client.disconnectFromServer();
    }


    public void createChatUI() { // GUI부분@@@@
        setTitle("CATPAGO - 언어 장벽 없이 대화하는 세상을 꿈꿉니다. ");
        ImageIcon imageIcon = new ImageIcon("src/main/resources/bg_NT.png"); // Replace with your image path
        JLabel imageLabel = new JLabel("",imageIcon,JLabel.CENTER);
        imageLabel.setBounds(0, 0, imageIcon.getIconWidth(), imageIcon.getIconHeight()); // Set the bounds according to the image size
        ImageIcon imageIcon2 = new ImageIcon("src/main/resources/bg_CHAT.png"); // Replace with your image path
        JLabel imageLabel2 = new JLabel("",imageIcon2,JLabel.CENTER);
        imageLabel2.setBounds(798, 97, imageIcon2.getIconWidth(), imageIcon2.getIconHeight()); // Set the bounds according to the image size
        ImageIcon imageIcon3 = new ImageIcon("src/main/resources/banner_CHAT.png"); // Replace with your image path
        JLabel imageLabel3 = new JLabel("",imageIcon3,JLabel.CENTER);
        imageLabel3.setBounds(0, 0, imageIcon3.getIconWidth(), imageIcon3.getIconHeight()); // Set the bounds according to the image size

        chatArea = new JTextArea(); // 대화 내용이 보이는 박스
        chatArea.setEditable(false); // 대화 박스에 임의로 수정 불가능하게 만들기
        chatArea.setBorder(new LineBorder(new Color(0,0,0), 2)); // Set a black border
        chatArea.setFont(new Font("맑은 고딕", Font.PLAIN, 15));


        // JScrollPane를 사용하여 chatArea를 감싸 스크롤 추가
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        messageField = new JTextField(); // 메시지를 입력할 수 있는 박스
        messageField.setBorder(new LineBorder(new Color(0,0,0), 1)); // Set a black border

        sendButton = new Mainpage.RoundedButton("전송", new Color(0,169,255));
        sendButton.addActionListener(new Chat2.SendButtonListener());

        messageField.addActionListener(new ActionListener() { //엔터키로 메시지를 보낼 수 있도록
            @Override
            public void actionPerformed(ActionEvent e) {
                sendButton.doClick(); // sendButton 클릭 이벤트 호출
            }
        });

        userList = new JList<>(); //
        // 대화 참가자 목록이 나타나는 박스

        // 번역할 언어 선택 콤보박스
        languageComboBox = new JComboBox<>(new String[]{"한국어로", "영어로", "일본어로", "중국어(간체)로", "중국어(번체)로", "베트남어로", "인도네시아어로", "태국어로", "독일어로", "러시아어로", "스페인어로", "이탈리아어로", "프랑스어로"});
        languageComboBox.setBounds(580, 510, 110, 20);

        // 번역 체크박스
        TranslationCheckbox = new JCheckBox("실시간 번역");
        TranslationCheckbox.setBounds(692, 509, 88, 20);

        exitButton = new Mainpage.RoundedButton("나가기",new Color(155,164,181));
        exitButton.addActionListener(new Chat2.ExitButtonListener());

        setLayout(null);
        setResizable(false); // 윈도우의 크기 조정을 불가능하게 한다.
        setLocationRelativeTo(null); // 실행시 화면 중앙에서 실행되는 코드.
        createLanguageCodeMap(); // 이 메서드를 호출해 languageCodeMap을 초기화해요.


        chatScrollPane.setBounds(50, 100, 730, 397);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        chatScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        messageField.setBounds(50, 505, 425, 30);
        sendButton.setBounds(482, 505, 90, 30);
        userList.setBounds(801, 100, 100, 397);
        exitButton.setBounds(801, 505, 100, 30);


        add(chatScrollPane); // 수정된 chatScrollPane를 추가
        add(messageField);
        add(sendButton);
        add(userList);
        add(exitButton);
        add(languageComboBox);
        add(TranslationCheckbox);

        add(imageLabel2);
        add(imageLabel3);
        add(imageLabel);


        setSize(965, 590);
        setVisible(true);
        messageField.requestFocus(); // 실행시 메시지 입력창에 커서가 위치하도록
    }

    public void createLanguageCodeMap() {
        languageCodeMap = new HashMap<>();
        languageCodeMap.put("한국어로", "ko");
        languageCodeMap.put("영어로", "en");
        languageCodeMap.put("일본어로", "ja");
        languageCodeMap.put("중국어(간체)로", "zh-CN");
        languageCodeMap.put("중국어(번체)로", "zh-TW");
        languageCodeMap.put("베트남어로", "vi");
        languageCodeMap.put("인도네시아어로", "id");
        languageCodeMap.put("태국어로", "th");
        languageCodeMap.put("독일어로", "de");
        languageCodeMap.put("러시아어로", "ru");
        languageCodeMap.put("스페인어로", "es");
        languageCodeMap.put("이탈리아어로", "it");
        languageCodeMap.put("프랑스어로", "fr");

        System.out.println(languageCodeMap);

    }

    public void sendUsername() {
        String message = "USERNAME:" + username;
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        client.sendToServer(messageBytes);
    }


    public class SendButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent ev) {
            try {
                // 클라이언트가 메시지를 보낼 때 사용자 이름을 붙여서 서버로 전송
                String inputMessage = messageField.getText().trim();  // 메시지 양 끝의 공백을 제거
                if (!inputMessage.isEmpty()) {  // 공백을 제거한 후 메시지가 비어있지 않은 경우에만 전송
                    String message = "CHAT:"+username + ": " + inputMessage;
                    byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
                    client.sendToServer(messageBytes);

                    // 채팅창에 내가 보낸 메시지 추가
                    message=message.substring("CHAT:".length());
                    chatArea.append(message + "\n");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            messageField.setText("");
            messageField.requestFocus();
        }
    }


    public class IncomingReader implements Runnable {
        @Override
        public void run() {
            try {
                InputStream in = client.getSocket().getInputStream();
                byte[] buffer = new byte[1024];
                int bytesReceived;
                while ((bytesReceived = in.read(buffer)) != -1) {
                    String message = new String(buffer, 0, bytesReceived, StandardCharsets.UTF_8);
                    System.out.println("Received message: " + message); // 콘솔에 메시지 출력

                    // 다른 사용자가 보낸 메시지만 chatArea에 추가
                    // 서버로부터 받은 메시지가 사용자 명단 업데이트인 경우
                    if(message.startsWith("JOIN:")||message.startsWith("eXIT:")){
                        String joinExit = message.substring(message.indexOf(":")+1);
                        chatArea.append(joinExit+"\n");
                    }
                    else if(message.startsWith("TranslatedText:")){
                        String text = message.substring(message.indexOf(":") + 1);
                        chatArea.append(text + "\n");
                    } else if (message.startsWith("USERLIST:")) {
                        String[] users = message.substring("USERLIST:".length()).split(",");
                        updateUserList(users);
                    } else if (message.startsWith("CHAT:")) {
                        message= message.substring("CHAT:".length());
                        System.out.println("제거된메시지:"+message);
                        if(!message.startsWith(username + ": ")){
                            if(TranslationCheckbox.isSelected()) {
                                String text = message.substring(message.indexOf(":") + 2);
                                String sourceLanguage = nTranslator.detectLanguage(text);
                                String targetLanguageDisplay = (String) languageComboBox.getSelectedItem();
                                String targetLanguage = languageCodeMap.get(targetLanguageDisplay);
                                JSONObject json = new JSONObject();
                                String sendUsername = message.substring(0, message.indexOf(":"));
                                json.put("Username", sendUsername);
                                json.put("inputText", text);
                                json.put("sourceLanguage", sourceLanguage);
                                json.put("targetLanguage", targetLanguage);
                                String messageR = "LiveTRANSLATE:" + json.toString();
                                byte[] messageBytes = messageR.getBytes(StandardCharsets.UTF_8);
                                client.sendToServer(messageBytes);

                            } else{
                                chatArea.append(message + "\n");
                            }
                        }

                    }
                    // 스크롤을 항상 아래로 이동
                    chatArea.setCaretPosition(chatArea.getDocument().getLength());
                }
            } catch (SocketException e) {
                System.out.println("Chat closed");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    public class ExitButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent ev) {
            try {
                // 클라이언트가 나갈 때 서버에 나갔음을 알리는 메시지 전송
                String exitMessage = "EXIT:" + username;
                byte[] exitMessageBytes = exitMessage.getBytes(StandardCharsets.UTF_8);
                client.sendToServer(exitMessageBytes);

                // 서버와의 연결 종료
                //.getSocket().close();
                client.disconnectFromServer();

                // 채팅 내용 초기화
                chatArea.setText("");



                // Chat 창
                dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private class CustomWindowAdapter extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent e) {
            try {
                // 클라이언트가 나갈 때 서버에 나갔음을 알리는 메시지 전송
                String exitMessage = "EXIT:" + username;
                byte[] exitMessageBytes = exitMessage.getBytes(StandardCharsets.UTF_8);
                client.sendToServer(exitMessageBytes);

                // 서버와의 연결 종료
                //.getSocket().close();
                client.disconnectFromServer();

                // 채팅 내용 초기화
                chatArea.setText("");



                // Chat 창
                dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void updateUserList(String[] users) {
        DefaultListModel<String> model = new DefaultListModel<>();
        for (String user : users) {
            model.addElement(user);
        }
        userList.setModel(model);
    }


}