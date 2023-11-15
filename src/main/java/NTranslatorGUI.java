import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.*;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class NTranslatorGUI extends JFrame{

    private Client client;
    private String username;

    // GUI 컴포넌트 선언
    private JTextArea inputArea; // 입력 창
    private JTextArea outputArea; // 출력 창
    private JButton translateButton; // 번역 버튼
    private JButton exitButton;
    private JComboBox<String> languageComboBox; // 번역할 언어 소스 선택 콤보박스

    private NTranslator nTranslator;

    public NTranslatorGUI(NTranslator nTranslator, Client client){
        this.nTranslator = nTranslator; // NTranslator 인스턴스를 저장
        this.client = client; // Client 인스턴스를 저장
        createNTranslatorGUI();
    }

    public void createNTranslatorGUI() {
        // JFrame 설정
        setTitle("텍스트 번역기");
        setSize(500, 500);
        setLayout(null);

        // GUI 컴포넌트 초기화
        inputArea = new JTextArea(); // 입력 창
        inputArea.setLineWrap(true); // 텍스트가 행 너비를 초과하면 자동으로 줄 바꿈
        inputArea.setBounds(80, 20, 320, 150);

        outputArea = new JTextArea(); // 출력 창
        outputArea.setLineWrap(true); // 텍스트가 행 너비를 초과하면 자동으로 줄 바꿈
        JScrollPane outputScrollPane = new JScrollPane(outputArea);
        outputArea.setBounds(80, 250, 320, 150);
        outputArea.setEditable(false);

        translateButton = new JButton("번역");
        translateButton.setBounds(200, 195, 100, 30);
        translateButton.addActionListener(new translateButtonListener());


        exitButton = new JButton("나가기"); // 채팅방을 종료하는 나가기 버튼
        exitButton.setBounds(320, 195, 100, 30);
        exitButton.addActionListener(new ExitButtonListener());

        // 콤보박스 초기화 및 설정
        languageComboBox = new JComboBox<>(new String[]{"ko", "en", "ja", "zh-CN", "zh-TW", "vi", "id", "th", "de", "ru", "es", "it", "fr"});
        languageComboBox.setBounds(80, 195, 100, 30);

        // 컴포넌트를 프레임에 추가
        add(inputArea);
        add(outputArea);
        add(translateButton);
        add(exitButton);
        add(languageComboBox);



        setVisible(true);
    }
    public class translateButtonListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            String inputText = inputArea.getText();
            String sourceLanguage = nTranslator.detectLanguage(inputText); //언어감지
            String targetLanguage = (String) languageComboBox.getSelectedItem(); //타켓언어는 콤보박스에서 선택한 언어로
            System.out.println("원본메시지: " + inputText); //여기부터 아래는없애도 되는 부분 검수용
            System.out.println("타겟언어: " + targetLanguage); //
            System.out.println("소스언어: " + sourceLanguage); //

            JSONObject json = new JSONObject();
            json.put("inputText", inputText);
            json.put("sourceLanguage", sourceLanguage);
            json.put("targetLanguage", targetLanguage);

            String message = "TRANSLATE:" + json.toString();
            byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);

            client.sendToServer(messageBytes);
           try{
                InputStream in = client.getSocket().getInputStream();
                byte[] buffer = new byte[1024];
                int bytesReceived = in.read(buffer);
                String serverMessage = new String(buffer, 0, bytesReceived, StandardCharsets.UTF_8);
                outputArea.setText(serverMessage);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }


    public class ExitButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent ev) {
            try {

                // 서버와의 연결 종료
                nTranslator.disconnectFromServer();

                dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
