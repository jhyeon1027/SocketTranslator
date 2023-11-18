import javax.swing.*;
import java.awt.*;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.border.LineBorder;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

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
    private Map<String, String> languageCodeMap;
    private Client client;
    private String username;

    // GUI 컴포넌트 선언
    private JTextArea inputArea; // 입력 창
    private JTextArea outputArea; // 출력 창


    private JButton translateButton; // 번역 버튼
    private JButton CopyButton1;
    private JButton CopyButton2;
    private JButton ResetButton3;
    private JButton ResetButton4;


    private JButton exitButton;
    private JComboBox<String> languageComboBox; // 번역할 언어 소스 선택 콤보박스

    private NTranslator nTranslator;

    public NTranslatorGUI(NTranslator nTranslator, Client client){
        this.nTranslator = nTranslator; // NTranslator 인스턴스를 저장
        this.client = client; // Client 인스턴스를 저장
        createNTranslatorGUI();
        addWindowListener(new CustomWindowAdapter());
    }

    public void createNTranslatorGUI() {  // GUI부분@@@@

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Image img = toolkit.getImage("src\\main\\resources\\CATPAGO_LOGO.png");
        //@@ SocketTranslator\\src\\main\\resources\\CATPAGO_LOGO
        setIconImage(img);
        //각 프로그램별 로고이미지 부분

        ImageIcon imageIcon = new ImageIcon("src\\main\\resources\\bg_NT.png"); // Replace with your image path
        JLabel imageLabel = new JLabel("",imageIcon,JLabel.CENTER);
        imageLabel.setBounds(0, 0, imageIcon.getIconWidth(), imageIcon.getIconHeight()); // Set the bounds according to the image size
        ImageIcon imageIcon2 = new ImageIcon("src\\main\\resources\\arrow.png"); // Replace with your image path
        JLabel imageLabel2 = new JLabel("",imageIcon2,JLabel.CENTER);
        imageLabel2.setBounds(453, 270, imageIcon2.getIconWidth(), imageIcon2.getIconHeight()); // Set the bounds according to the image size
        add(imageLabel2);
        ImageIcon imageIcon3 = new ImageIcon("src\\main\\resources\\banner_NT.png"); // Replace with your image path
        JLabel imageLabel3 = new JLabel("",imageIcon3,JLabel.CENTER);
        imageLabel3.setBounds(0, 0, imageIcon3.getIconWidth(), imageIcon3.getIconHeight()); // Set the bounds according to the image size
        add(imageLabel3);





        // JFrame 설정
        setTitle("CATPAGO - 언어 장벽 없이 대화하는 세상을 꿈꿉니다. ");
        setSize(965, 590);
        setLayout(null);
        setResizable(false); // 윈도우의 크기 조정을 불가능하게 한다.
        setLocationRelativeTo(null); // 실행시 화면 중앙에서 실행되는 코드.
        createLanguageCodeMap(); // 이 메서드를 호출해 languageCodeMap을 초기화해요.


        // GUI 컴포넌트 초기화
        inputArea = new JTextArea(" 이곳에 내용을 지우고 입력하세요."); // 입력 창, 언어감지는 발표에서
        inputArea.setBorder(new LineBorder(new Color(0,0,0),2)); // Set a black border
        inputArea.setLineWrap(true); // 텍스트가 행 너비를 초과하면 자동으로 줄 바꿈
        //inputArea.setBounds(50, 100, 400, 400);
        JScrollPane inputScrollPane = new JScrollPane(inputArea);
        inputScrollPane.setBounds(50, 100, 400, 400);  // 크기 수정
        inputScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);


        outputArea = new JTextArea(" 번역 결과가 이곳에 표시됩니다."); // 출력 창
        outputArea.setBorder(new LineBorder(new Color(0,0,0), 2)); // Set a black border
        outputArea.setLineWrap(true); // 텍스트가 행 너비를 초과하면 자동으로 줄 바꿈
        outputArea.setEditable(false);
        JScrollPane outputScrollPane = new JScrollPane(outputArea);
        outputScrollPane.setBounds(501, 100, 400, 400);  // 크기 수정
        outputScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        translateButton = new Mainpage.RoundedButton("번역",new Color(0,169,255));
        translateButton.setBounds(350, 510, 100, 30);
        translateButton.addActionListener(new translateButtonListener());

        CopyButton1 = new Mainpage.RoundedButton("복사",new Color(137,207,243));
        CopyButton1.setBounds(50, 510, 60, 20);
        CopyButton1.addActionListener(new CopyButton1Listener());

        CopyButton2 = new Mainpage.RoundedButton("복사",new Color(137,207,243));
        CopyButton2.setBounds(501, 510, 60, 20);
        CopyButton2.addActionListener(new CopyButton2Listener());

        ResetButton3 = new Mainpage.RoundedButton("초기화",new Color(160,233,255));
        ResetButton3.setBounds(125, 510, 60, 20);
        ResetButton3.addActionListener(new ResetButtonListener());

        ResetButton4 = new Mainpage.RoundedButton("초기화",new Color(160,233,255));
        ResetButton4.setBounds(576, 510, 60, 20);
        ResetButton4.addActionListener(new ResetButtonListener());

        exitButton = new Mainpage.RoundedButton("나가기",new Color(155,164,181));
        exitButton.setBounds(801, 510, 100, 30);
        exitButton.addActionListener(new ExitButtonListener());

        // 콤보박스 초기화 및 설정
        languageComboBox = new JComboBox<>(new String[]{"한국어로", "영어로", "일본어로", "중국어(간체)로", "중국어(번체)로", "베트남어로", "인도네시아어로", "태국어로", "독일어로", "러시아어로", "스페인어로", "이탈리아어로", "프랑스어로"});
        languageComboBox.setBounds(225, 510, 120, 30);



        // 컴포넌트를 프레임에 추가
        add(inputScrollPane);
        add(outputScrollPane);
        add(translateButton);
        add(exitButton);
        add(languageComboBox);
        add(CopyButton1);
        add(CopyButton2);
        add(ResetButton3);
        add(ResetButton4);
        add(imageLabel);





        setVisible(true);
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
    public class translateButtonListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            String targetLanguageDisplay = (String) languageComboBox.getSelectedItem();
            String targetLanguage = languageCodeMap.get(targetLanguageDisplay);
            String inputText = inputArea.getText();
            String sourceLanguage = nTranslator.detectLanguage(inputText); //언어감지
            System.out.println("선택된 표시 언어: " + targetLanguageDisplay);
            System.out.println("매핑된 언어 코드: " + targetLanguage); // 받아온 언어 코드 확인하기
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

    public class CopyButton1Listener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // 입력 창의 텍스트를 클립보드에 복사
            StringSelection stringSelection = new StringSelection(inputArea.getText());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
        }
    }

    public class CopyButton2Listener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // 출력 창의 텍스트를 클립보드에 복사
            StringSelection stringSelection = new StringSelection(outputArea.getText());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
        }
    }

    public class ResetButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // 입력 창 초기화
            inputArea.setText("");
            outputArea.setText("");
        }
    }



    public class ExitButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent ev) {
            try {  // 서버와의 연결 종료
                nTranslator.disconnectFromServer();
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
                // 서버와의 연결 종료
                nTranslator.disconnectFromServer();
                dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
