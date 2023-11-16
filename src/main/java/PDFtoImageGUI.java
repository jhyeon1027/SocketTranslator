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

public class PDFtoImageGUI extends JFrame {
    private Client client;
    private PDFtoImage pdFtoImage;

    private JLabel titleLabel;
    private JLabel uploadLable;
    private JLabel languageLable;
    private JButton uploadButton;
    private JButton exitButton;
    private JComboBox<String> languageComboBox;
    private JLabel SuccessLabel; //PDF 구현 성공 시 나타나는 라벨
    private JLabel ALable; //번역된 PDF는 기존 PDF가 있던 자리에 생성됨을 알려주는 라벨

    public PDFtoImageGUI(PDFtoImage pdFtoImage, Client client){
        this.pdFtoImage = pdFtoImage; //PDFtoImage 인스턴스를 저장
        this.client = client; // Client 인스턴스를 저장
        createPDFtoImageGUI();
    }

    public void createPDFtoImageGUI(){
        setTitle("이미지 번역기");
        setSize(500, 450);  // 크기 수정
        setLayout(null);

        titleLabel = new JLabel("이미지 번역기");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        titleLabel.setBounds(200, 10, 200, 30);

        languageLable = new JLabel("1. 번역할 언어 선택");
        languageLable.setFont(new Font("맑은 고딕", Font.PLAIN, 15));
        languageLable.setBounds(50, 70, 300, 30);

        SuccessLabel = new JLabel("PDF 변환 성공");
        SuccessLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 15));
        SuccessLabel.setBounds(50,220,300,30);


        String[] languages = {"ko", "en", "ja", "zh-CN", "zh-TW", "vi", "id", "th", "de", "ru", "es", "it", "fr"};
        languageComboBox = new JComboBox<>(languages);
        languageComboBox.setBounds(50, 100, 100, 30);  // 크기 수정

        uploadLable = new JLabel("2. 번역할 PDF 선택");
        uploadLable.setFont(new Font("맑은 고딕", Font.PLAIN, 15));
        uploadLable.setBounds(50, 150, 300, 30);

        uploadButton = new JButton("Upload");
        uploadButton.setBounds(50, 180, 100, 30);
        uploadButton.addActionListener(new PDFtoImageGUI.uploadButtonListener());

        exitButton = new JButton("나가기"); // 채팅방을 종료하는 나가기 버튼
        exitButton.setBounds(350, 340, 100, 30);
        exitButton.addActionListener(new PDFtoImageGUI.ExitButtonListener());

        ALable = new JLabel("번역된 PDF파일은 기존 PDF파일이 있는 폴더에 저장됩니다.");
        ALable.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        ALable.setBounds(50, 370, 400, 30);


        add(titleLabel);
        add(uploadLable);
        add(uploadButton);
        add(exitButton);
        add(languageLable);
        add(languageComboBox);
        //변환 성공시 추가할거임add(SuccessLabel);
        add(ALable);

        setVisible(true);
    }

    public class uploadButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // 이미지 업로드 기능을 호출
            uploadPDF();
        }
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
        String targetLanguage = (String) languageComboBox.getSelectedItem();
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String selectedFilePath = selectedFile.getAbsolutePath();
            selectedFilePath = selectedFilePath.replace(".pdf", "");
            int pageEnd; //PDF가 총 몇페이지인지
            pageEnd= pdFtoImage.convert(selectedFile, selectedFilePath);

            //클라이언트가 서버에 이미지들을 이미지 갯수만큼 보내 이미지를 번역합니다.
            for(int page = 0; page < pageEnd; ++page) {
                JSONObject json = new JSONObject();
                System.out.println(("이미지 번역 시작"+page+"번"));
                String filename = selectedFilePath + "_" + page + ".jpg";
                json.put("sourceLanguage", "auto");
                json.put("targetLanguage", targetLanguage);
                json.put("filename", filename);
                String message = "PDFTRANSLATE:"+json.toString()+"\n";
                byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
                //한데 묶은 메시지를 바이트패턴으로 변환 후 서버로 전송
                client.sendToServer(messageBytes);
            }
            // selectedFilePath 변수에는 선택된 PDF 파일의 경로가 저장됩니다.
            // 이후 필요한 처리를 이곳에서 수행하면 됩니다.
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
}
