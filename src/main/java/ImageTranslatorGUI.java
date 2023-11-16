import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
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
public class ImageTranslatorGUI extends JFrame {

    private Client client;
    private ImageTranslator imageTranslator;

    private JLabel titleLabel;
    private JLabel uploadLable;
    private JLabel languageLable;
    private JButton uploadButton;
    private JButton exitButton;
    private JComboBox<String> languageComboBox;
    private JTextArea originalTextArea;
    private JTextArea translatedTextArea;

    public ImageTranslatorGUI(ImageTranslator imageTranslator, Client client){
        this.imageTranslator = imageTranslator; //ImageTranslator 인스턴스를 저장
        this.client = client; // Client 인스턴스를 저장
        createImageTranslatorGUI();
    }

    public void createImageTranslatorGUI(){
        setTitle("이미지 번역기");
        setSize(1000, 900);  // 크기 수정
        setLayout(null);

        titleLabel = new JLabel("이미지 번역기");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        titleLabel.setBounds(430, 10, 200, 30);

        languageLable = new JLabel("1. 번역할 언어 선택");
        languageLable.setFont(new Font("맑은 고딕", Font.PLAIN, 15));
        languageLable.setBounds(50, 70, 300, 30);


        String[] languages = {"ko", "en", "ja", "zh-CN", "zh-TW", "vi", "id", "th", "de", "ru", "es", "it", "fr"};
        languageComboBox = new JComboBox<>(languages);
        languageComboBox.setBounds(50, 100, 100, 30);  // 크기 수정

        uploadLable = new JLabel("2. 글자를 인식할 이미지 선택");
        uploadLable.setFont(new Font("맑은 고딕", Font.PLAIN, 15));
        uploadLable.setBounds(50, 150, 300, 30);

        uploadButton = new JButton("Upload");
        uploadButton.setBounds(50, 180, 100, 30);
        uploadButton.addActionListener(new uploadButtonListener());

        exitButton = new JButton("나가기"); // 채팅방을 종료하는 나가기 버튼
        exitButton.setBounds(850, 790, 100, 30);
        exitButton.addActionListener(new ExitButtonListener());


        originalTextArea = new JTextArea("원문을 입력하세요.", 10, 20);
        originalTextArea.setLineWrap(true);
        originalTextArea.setWrapStyleWord(true);
        JScrollPane originalScrollPane = new JScrollPane(originalTextArea);
        originalScrollPane.setBounds(50, 250, 400, 500);  // 크기 수정
        originalScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        translatedTextArea = new JTextArea("번역문이 나타납니다.", 10, 20);
        translatedTextArea.setLineWrap(true);
        translatedTextArea.setWrapStyleWord(true);
        JScrollPane translatedScrollPane = new JScrollPane(translatedTextArea);
        translatedScrollPane.setBounds(550, 250, 400, 500);  // 크기 수정
        translatedScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        add(titleLabel);
        add(uploadLable);
        add(uploadButton);
        add(exitButton);
        add(languageLable);
        add(languageComboBox);
        add(originalScrollPane);
        add(translatedScrollPane);

        setVisible(true);
    }
    //이미지를 업로드 시 정보를 클라이언트가 서버에게 넘기고, 서버가 받으면 파파고에게 번역요청을 하고 다시 클라이언트에게 주는 방식으로.
    public class uploadButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // 이미지 업로드 기능을 호출
            uploadImage();
        }
    }

    public void uploadImage(){
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            JSONObject json = new JSONObject();
            try {
                // 선택된 파일을 바이트 배열로 읽어옵니다.
                byte[] fileBytes = Files.readAllBytes(selectedFile.toPath());
                // 바이트 배열을 Base64 인코딩하여 문자열로 변환합니다.
                String fileBase64 = Base64.getEncoder().encodeToString(fileBytes);

                // JSON 객체에 정보를 저장합니다.
                json.put("sourceLanguage", "auto");
                json.put("targetLanguage", languageComboBox.getSelectedItem().toString());
                json.put("image", fileBase64);
                String message = "IMAGETRANSLATE:"+json.toString();
                byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
                //한데 묶은 메시지를 바이트패턴으로 변환 후 서버로 전송
                client.sendToServer(messageBytes);
                // 서버로부터 바이트패턴으로 번역된 텍스트를 받아옵니다.
                try{
                    InputStream in = client.getSocket().getInputStream();
                    byte[] buffer = new byte[1024];
                    int bytesReceived = in.read(buffer);
                    String serverMessage = new String(buffer, 0, bytesReceived, StandardCharsets.UTF_8);
                    // 서버로부터 받은 텍스트를 JSON 객체로 변환합니다.
                    JSONParser parser = new JSONParser();
                    JSONObject jsonObject = (JSONObject) parser.parse(serverMessage);
                    // JSON 객체에서 번역된 텍스트를 추출합니다.
                    String sourceText = (String) jsonObject.get("sourceText");
                    String targetText = (String) jsonObject.get("targetText");
                    originalTextArea.setText(sourceText);
                    translatedTextArea.setText(targetText);

                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public class ExitButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {  // 서버와의 연결 종료
                imageTranslator.disconnectFromServer();
                dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

}
