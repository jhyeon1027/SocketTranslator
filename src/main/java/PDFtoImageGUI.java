import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.Toolkit;
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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class PDFtoImageGUI extends JFrame {

    private Map<String, String> languageCodeMap;
    private Client client;
    private PDFtoImage pdFtoImage;

    private JLabel titleLabel;
    private JLabel uploadLable;
    private JLabel languageLable;
    private JButton uploadButton;
    private JButton exitButton;
    private JComboBox<String> languageComboBox;
    private JLabel SuccessLabel; //PDF 구현 성공 시 나타나는 라벨
    private JLabel FailLabel; //PDF 구현 실패 시 나타나는 라벨
    private JLabel ALable; //번역된 PDF는 기존 PDF가 있던 자리에 생성됨을 알려주는 라벨

    public static JProgressBar progressBar;

    public PDFtoImageGUI(PDFtoImage pdFtoImage, Client client){
        this.pdFtoImage = pdFtoImage; //PDFtoImage 인스턴스를 저장
        this.client = client; // Client 인스턴스를 저장
        createPDFtoImageGUI();
        addWindowListener(new CustomWindowAdapter());
    }

    public void createPDFtoImageGUI(){
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Image img = toolkit.getImage("src\\main\\resources\\CATPAGO_LOGO.png");
        //@@ SocketTranslator\\src\\main\\resources\\CATPAGO_LOGO
        setIconImage(img);
        //각 프로그램별 로고이미지 부분

        ImageIcon imageIcon = new ImageIcon("src\\main\\resources\\bg_NT.png"); // Replace with your image path
        JLabel imageLabel = new JLabel("",imageIcon,JLabel.CENTER);
        imageLabel.setBounds(0, 0, imageIcon.getIconWidth(), imageIcon.getIconHeight()); // Set the bounds according to the image size
        ImageIcon imageIcon3 = new ImageIcon("src\\main\\resources\\banner_PDF.png"); // Replace with your image path
        JLabel imageLabel3 = new JLabel("",imageIcon3,JLabel.CENTER);
        imageLabel3.setBounds(0, 0, imageIcon3.getIconWidth(), imageIcon3.getIconHeight()); // Set the bounds according to the image size
        add(imageLabel3);
        ImageIcon imageIcon4 = new ImageIcon("src\\main\\resources\\bg_PDF.png"); // Replace with your image path
        JLabel imageLabel4 = new JLabel("",imageIcon4,JLabel.CENTER);
        imageLabel4.setBounds(50, 175, imageIcon4.getIconWidth(), imageIcon4.getIconHeight()); // Set the bounds according to the image size
        ImageIcon imageIcon5 = new ImageIcon("src\\main\\resources\\pdfs.png"); // Replace with your image path
        JLabel imageLabel5 = new JLabel("",imageIcon5,JLabel.CENTER);
        imageLabel5.setBounds(180, 180, imageIcon5.getIconWidth(), imageIcon5.getIconHeight()); // Set the bounds according to the image size
        add(imageLabel5); //pdfs 이미지 좀 더 찾아보기

        setTitle("CATPAGO - 언어 장벽 없이 대화하는 세상을 꿈꿉니다. ");
        setSize(525, 555);  // 크기 수정
        setLayout(null);
        setResizable(false); // 윈도우의 크기 조정을 불가능하게 한다.
        setLocationRelativeTo(null); // 실행시 화면 중앙에서 실행되는 코드.
        createLanguageCodeMap(); // 이 메서드를 호출해 languageCodeMap을 초기화해요.

        titleLabel = new JLabel(""); // 안쓰는부분
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        titleLabel.setBounds(55, 470, 200, 30);

        languageLable = new JLabel(""); //안쓰는부분 2
        languageLable.setFont(new Font("맑은 고딕", Font.PLAIN, 15));
        languageLable.setBounds(50, 70, 300, 30);

        SuccessLabel = new JLabel("PDF 변환 성공"); //@@ 위치 바꿔야 할 부분, 위에 레이블들로 테스트
        SuccessLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 15));
        SuccessLabel.setBounds(55,470,200,30);

        FailLabel = new JLabel("PDF 변환 실패"); //@@ 위치 바꿔야 할 부분, 위에 레이블들로 테스트
        FailLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 15));
        FailLabel.setBounds(50,220,300,30);

        languageComboBox = new JComboBox<>(new String[]{"한국어로", "영어로", "일본어로", "중국어(간체)로", "중국어(번체)로", "베트남어로", "인도네시아어로", "태국어로", "독일어로", "러시아어로", "스페인어로", "이탈리아어로", "프랑스어로"});
        languageComboBox.setBounds(198, 343, 115, 30);  // 크기 수정

        uploadLable = new JLabel(""); // 안쓰는 부분
        uploadLable.setFont(new Font("맑은 고딕", Font.PLAIN, 15));
        uploadLable.setBounds(50, 150, 300, 30);

        uploadButton = new Mainpage.RoundedButton(" 업로드 및 번역 ", new Color(0,169,255));
        uploadButton.setBounds(205, 380, 100, 35);
        uploadButton.addActionListener(new PDFtoImageGUI.uploadButtonListener());

        exitButton = new Mainpage.RoundedButton("나가기",new Color(155,164,181));
        exitButton.setBounds(360, 475, 100, 30);
        exitButton.addActionListener(new PDFtoImageGUI.ExitButtonListener());

        ALable = new JLabel("번역된 PDF파일은 기존 PDF파일이 있는 폴더에 저장됩니다.");
        ALable.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        ALable.setBounds(70, 300, 400, 30);

        progressBar = new JProgressBar();
        progressBar.setBounds(50, 435, 410, 30); // 위치와 크기를 조절합니다.
        progressBar.setStringPainted(true); // 진행 상태 텍스트 표시를 활성화합니다.


        add(titleLabel);
        add(uploadLable);
        add(uploadButton);
        add(exitButton);
        add(languageLable);
        add(languageComboBox);
        add(ALable);
        add(progressBar);
        add(imageLabel4);
        add(imageLabel);


        setVisible(true);
    }

    public class uploadButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // 이미지 업로드 기능을 호출
            uploadPDF();
        }
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

    public void uploadPDF() {

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                return f.getName().toLowerCase().endsWith(".pdf") || f.isDirectory();
            }

            public String getDescription() {
                return "PDF Documents";
            }
        });
        String targetLanguageDisplay = (String) languageComboBox.getSelectedItem();
        String targetLanguage = languageCodeMap.get(targetLanguageDisplay);
        int returnValue = fileChooser.showOpenDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String selectedFilePath = selectedFile.getAbsolutePath();
            selectedFilePath = selectedFilePath.replace(".pdf", "");
            int pageEnd; //PDF가 총 몇페이지인지
            pageEnd= pdFtoImage.convert(selectedFile, selectedFilePath);
            JOptionPane.showMessageDialog(null, "OK를 누르면 번역을 진행합니다.\n1분이 지나도 번역본이 저장되지 않으면 재실행해주세요.", "파일 업로드 성공!", JOptionPane.INFORMATION_MESSAGE);

            boolean allTranslated = true;
            //클라이언트가 서버에 이미지들을 이미지 갯수만큼 보내 이미지를 번역합니다.
            for(int page = 0; page < pageEnd; ++page) {
                JSONObject json = new JSONObject();
                System.out.println(("이미지 번역 시작"+page+"번"));
                String filename = selectedFilePath + "_" + page + ".jpg";
                json.put("sourceLanguage", "auto");
                json.put("targetLanguage", targetLanguage);
                json.put("filename", filename);
                String message = "PDFTRANSLATE:"+json.toString();
                byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
                //한데 묶은 메시지를 바이트패턴으로 변환 후 서버로 전송
                client.sendToServer(messageBytes);

                // 서버의 응답을 기다립니다.
                try{
                InputStream in = client.getSocket().getInputStream();
                byte[] buffer = new byte[1024];
                int bytesReceived = in.read(buffer);
                String serverMessage = new String(buffer, 0, bytesReceived, StandardCharsets.UTF_8);
                if (!serverMessage.startsWith("PDFTRANSLATE:SUCCESS")) {
                    System.out.println("번역 실패: " + page + " 페이지");
                    allTranslated = false;
                    break;
                }}
                catch (IOException ex) {
                    ex.printStackTrace();
                }

                try {
                    Thread.sleep(1000); // 1초 대기
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                progressBar.setValue((int) ((page + 1) / (double) pageEnd * 100));
                progressBar.setString("PDF 변환 진행... (" + (page + 1) + "/" + pageEnd + ")");
            }

            if (allTranslated) {
                pdFtoImage.imagesToPdf(selectedFilePath, pageEnd);
                //삭제예정SuccessLabel.setText("PDF 변환 성공");
                progressBar.setString("PDF 변환 완료");

                add(SuccessLabel);
            }
            else {
                SuccessLabel.setText("PDF 변환 실패");
                //삭제예정add(FailLabel);
            }
        }
    }
    public class ExitButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {  // 서버와의 연결 종료
                pdFtoImage.disconnectFromServer();
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
                pdFtoImage.disconnectFromServer();
                dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

}
