import org.json.simple.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.net.SocketException;

import java.net.Socket;

public class Chat2 extends JFrame {
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
        Image img = toolkit.getImage("src\\main\\resources\\CATPAGO_LOGO.png");
        //@@ SocketTranslator\\src\\main\\resources\\CATPAGO_LOGO
        setIconImage(img);
        //각 프로그램별 로고이미지 부분
        this.nTranslator = new NTranslator();
        createChatUI();
        String username = JOptionPane.showInputDialog("사용자 이름을 입력하세요 :");
        setUsername(username);
        connectToServer();

    }
    public boolean isSocketClosed() {
        return client.getSocket().isClosed();
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public void connectToServer() {
        this.client = new Client("124.53.154.105", 7777); // 예시로 localhost와 7777 포트를 사용
        this.client.connectToServer();
        sendUsername();
        Thread thread = new Thread(new Chat2.IncomingReader());
        thread.start();
    }
    public void disconnectFromServer() {
        this.client.disconnectFromServer();
    }


    public void createChatUI() {
        setTitle("Chat Client");

        chatArea = new JTextArea(); // 대화 내용이 보이는 박스
        chatArea.setEditable(false); // 대화 박스에 임의로 수정 불가능하게 만들기

        // JScrollPane를 사용하여 chatArea를 감싸 스크롤 추가
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        messageField = new JTextField(); // 메시지를 입력할 수 있는 박스

        sendButton = new JButton("전송"); // 메시지를 전송하는 버튼
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
        languageComboBox = new JComboBox<>(new String[]{"ko", "en", "ja", "zh-CN", "zh-TW", "vi", "id", "th", "de", "ru", "es", "it", "fr"});
        languageComboBox.setBounds(220, 220, 90, 20);

        // 번역 체크박스
        TranslationCheckbox = new JCheckBox("실시간 번역");
        TranslationCheckbox.setBounds(320, 220, 100, 20);

        exitButton = new JButton("나가기"); // 채팅방을 종료하는 나가기 버튼
        exitButton.addActionListener(new Chat2.ExitButtonListener());

        setLayout(null);
        setResizable(false); // 윈도우의 크기 조정을 불가능하게 한다.
        setLocationRelativeTo(null); // 실행시 화면 중앙에서 실행되는 코드.

        chatScrollPane.setBounds(10, 10, 300, 200);
        messageField.setBounds(10, 250, 200, 30);
        sendButton.setBounds(220, 250, 90, 30);
        userList.setBounds(320, 10, 100, 200);
        exitButton.setBounds(320, 250, 100, 30);

        add(chatScrollPane); // 수정된 chatScrollPane를 추가
        add(messageField);
        add(sendButton);
        add(userList);
        add(exitButton);
        add(languageComboBox);
        add(TranslationCheckbox);

        setSize(450, 330);
        setVisible(true);
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
                String message = username + ": " + messageField.getText();
                byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
                client.sendToServer(messageBytes);

                // 채팅창에 내가 보낸 메시지 추가
                chatArea.append(message + "\n");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            messageField.setText("");
            messageField.requestFocus();
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
                    } else if (!message.startsWith(username + ": ")) {
                        if(TranslationCheckbox.isSelected()) {
                            String text = message.substring(message.indexOf(":") + 2);
                            String sourceLanguage = nTranslator.detectLanguage(text);
                            String targetLanguage = (String) languageComboBox.getSelectedItem();
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
            } catch (SocketException e) {
                System.out.println("Chat closed");
            } catch (IOException ex) {
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