import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Chat extends JFrame {
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

    public Chat() {
        this.username = JOptionPane.showInputDialog("사용자 이름을 입력하세요 :");
        this.writer = Client.getOut();
        sendUsername();
        createChatUI();
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
        //.addActionListener(new SendButtonListener());

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
        //exitButton.addActionListener(new ExitButtonListener());

        setLayout(null);

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
        writer.println("USERNAME:"+username);
    }

    /*public void setUpNetworking(Socket sock) {
        try {
            writer = new PrintWriter(sock.getOutputStream());
            // 서버에 사용자 이름 전송
            writer.println(username);
            writer.flush();

            System.out.println("Networking established");

            // 여기에서 서버로부터 메시지를 읽는 스레드 시작
            Thread readerThread = new Thread(new IncomingReader());
            readerThread.start();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }*/



    /*public class SendButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent ev) {
            try {
                // 클라이언트가 메시지를 보낼 때 사용자 이름을 붙여서 서버로 전송
                writer.println(messageField.getText());
                writer.flush();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            messageField.setText("");
            messageField.requestFocus();
        }
    }*/

    /*public class ExitButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent ev) {
            try {
                // 클라이언트가 나갈 때 서버에 나갔음을 알리는 메시지 전송
                writer.flush();

                // 서버와의 연결 종료
                sock.close();

                // 클라이언트 종료
                System.exit(0);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }*/

    /*public class IncomingReader implements Runnable {
        public void run() {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("[Server] Users: ")) {
                        String[] users = line.substring("[Server] Users: ".length()).split(", ");
                        SwingUtilities.invokeLater(() -> updateUserList(users));
                    } else {
                        String finalLine = line;
                        if (TranslationCheckbox.isSelected() && !finalLine.startsWith("[" + username + "]")) {
                            String message = finalLine.substring(finalLine.indexOf("] ") + 2);
                            String sourceLanguage = nTranslator.detectLanguage(message);
                            String targetLanguage = (String) languageComboBox.getSelectedItem();
                            String translatedMessage = nTranslator.translateText(message, sourceLanguage, targetLanguage);
                            finalLine = finalLine.substring(0, finalLine.indexOf("] ") + 2) + translatedMessage;
                        }
                        final String finalLineForLambda = finalLine;
                        SwingUtilities.invokeLater(() -> chatArea.append(finalLineForLambda + " \n"));
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }*/

    /*private void updateUserList(String[] users) {
        DefaultListModel<String> model = new DefaultListModel<>();
        for (String user : users) {
            model.addElement(user);
        }
        userList.setModel(model);
    }*/
}