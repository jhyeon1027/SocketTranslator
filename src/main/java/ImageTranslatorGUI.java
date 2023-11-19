import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.imageio.IIOImage;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.border.LineBorder;
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
public class ImageTranslatorGUI extends JFrame {

    private Map<String, String> languageCodeMap;
    private Client client;
    private ImageTranslator imageTranslator;

    private JLabel titleLabel;
    private JLabel uploadLable;
    private JLabel languageLable;
    private JButton uploadButton;
    private JButton exitButton;

    private JButton CopyButton1;
    private JButton CopyButton2;
    private JButton ResetButton3;
    private JButton ResetButton4;

    private JComboBox<String> languageComboBox;
    private JTextArea originalTextArea;
    private JTextArea translatedTextArea;

    public ImageTranslatorGUI(ImageTranslator imageTranslator, Client client){
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Image img = toolkit.getImage("src\\main\\resources\\CATPAGO_LOGO.png");
        //@@ SocketTranslator\\src\\main\\resources\\CATPAGO_LOGO
        setIconImage(img);
        //각 프로그램별 로고이미지 부분

        this.imageTranslator = imageTranslator; //ImageTranslator 인스턴스를 저장
        this.client = client; // Client 인스턴스를 저장
        createImageTranslatorGUI();
        addWindowListener(new CustomWindowAdapter());
    }

    private void createLanguageCodeMap() {
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

    public void createImageTranslatorGUI(){
        ImageIcon imageIcon = new ImageIcon("src\\main\\resources\\bg_NT.png"); // Replace with your image path
        JLabel imageLabel = new JLabel("",imageIcon,JLabel.CENTER);
        imageLabel.setBounds(0, 0, imageIcon.getIconWidth(), imageIcon.getIconHeight()); // Set the bounds according to the image size
        ImageIcon imageIcon2 = new ImageIcon("src\\main\\resources\\arrow.png"); // Replace with your image path
        JLabel imageLabel2 = new JLabel("",imageIcon2,JLabel.CENTER);
        imageLabel2.setBounds(453, 270, imageIcon2.getIconWidth(), imageIcon2.getIconHeight()); // Set the bounds according to the image size
        add(imageLabel2);
        ImageIcon imageIcon3 = new ImageIcon("src\\main\\resources\\banner_IMAGE.png"); // Replace with your image path
        JLabel imageLabel3 = new JLabel("",imageIcon3,JLabel.CENTER);
        imageLabel3.setBounds(0, 0, imageIcon3.getIconWidth(), imageIcon3.getIconHeight()); // Set the bounds according to the image size
        add(imageLabel3);

        setTitle("CATPAGO - 언어 장벽 없이 대화하는 세상을 꿈꿉니다. ");
        setSize(965, 590);  // 크기 수정
        setLayout(null);
        setResizable(false); // 윈도우의 크기 조정을 불가능하게 한다.
        setLocationRelativeTo(null); // 실행시 화면 중앙에서 실행되는 코드.
        createLanguageCodeMap(); // 이 메서드를 호출해 languageCodeMap을 초기화해요.


        titleLabel = new JLabel("");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        titleLabel.setBounds(430, 10, 200, 30); //안쓰는 레이블, 배너로 처리할 부분

        languageLable = new JLabel("1. 번역할 언어 선택"); // 안쓰는 파트
        languageLable.setFont(new Font("맑은 고딕", Font.PLAIN, 15));
        languageLable.setBounds(50, 10, 300, 30);


        languageComboBox = new JComboBox<>(new String[]{"한국어로", "영어로", "일본어로", "중국어(간체)로", "중국어(번체)로", "베트남어로", "인도네시아어로", "태국어로", "독일어로", "러시아어로", "스페인어로", "이탈리아어로", "프랑스어로"});
        languageComboBox.setBounds(225, 505, 120, 30);  // 크기 수정

        uploadLable = new JLabel("2. 글자를 인식할 이미지 선택"); // 안쓰는 파트
        uploadLable.setFont(new Font("맑은 고딕", Font.PLAIN, 15));
        uploadLable.setBounds(400, 10, 300, 30);

        uploadButton = new Mainpage.RoundedButton("업로드 및 번역", new Color(0,169,255));
        uploadButton.setBounds(350, 505, 100, 30);
        uploadButton.addActionListener(new uploadButtonListener());

        exitButton = new Mainpage.RoundedButton("나가기",new Color(155,164,181));
        exitButton.setBounds(801, 505, 100, 30);
        exitButton.addActionListener(new ExitButtonListener());

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


        originalTextArea = new JTextArea(" 이미지 텍스트 변환 결과가 이곳에 표시됩니다.\n 인식이 올바르지 않은 부분은 직접 수정하여 복사할 수 있습니다.");
        originalTextArea.setBorder(new LineBorder(new Color(0,0,0),2)); // Set a black border
        originalTextArea.setEditable(true);
        originalTextArea.setLineWrap(true);
        originalTextArea.setWrapStyleWord(true);
        JScrollPane originalScrollPane = new JScrollPane(originalTextArea);
        originalScrollPane.setBounds(50, 100, 400, 400);  // 크기 수정
        originalScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

        translatedTextArea = new JTextArea(" 번역 결과가 이곳에 표시됩니다.", 10, 20);
        translatedTextArea.setBorder(new LineBorder(new Color(0,0,0),2)); // Set a black border
        translatedTextArea.setLineWrap(true);
        translatedTextArea.setWrapStyleWord(true);
        JScrollPane translatedScrollPane = new JScrollPane(translatedTextArea);
        translatedScrollPane.setBounds(501, 100, 400, 400);  // 크기 수정
        translatedScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        translatedTextArea.setEditable(false);


        add(titleLabel);
        add(uploadLable);
        add(uploadButton);
        add(exitButton);
        add(languageLable);
        add(languageComboBox);
        add(originalScrollPane);
        add(translatedScrollPane);
        add(CopyButton1);
        add(CopyButton2);
        add(ResetButton3);
        add(ResetButton4);
        add(imageLabel);

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
                //선택된 이미지 파일의 품질을 낮춥니다.
                BufferedImage image = ImageIO.read(selectedFile);
                if (image == null) {
                    System.out.println("Image is null");
                    return;
                }
                System.out.println("Original image dimension: " + image.getWidth() + "x" + image.getHeight());

                BufferedImage resizedImage = new BufferedImage(image.getWidth()/2, image.getHeight()/2, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = resizedImage.createGraphics();
                g.drawImage(image, 0, 0, image.getWidth()/2, image.getHeight()/2, null);
                g.dispose();
                System.out.println("Resized image dimension: " + resizedImage.getWidth() + "x" + resizedImage.getHeight());

                // 이미지를 바이트 배열로 변환합니다.
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                // 압축률을 조절하여 이미지를 저장합니다.
                ImageOutputStream imageOutput = ImageIO.createImageOutputStream(baos);
                ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
                ImageWriteParam param = writer.getDefaultWriteParam();
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(0.5f); // 0.5 is 50% quality. Adjust this value as needed.
                writer.setOutput(imageOutput);
                writer.write(null, new IIOImage(resizedImage, null, null), param);
                writer.dispose();

                byte[] imageInByte = baos.toByteArray();
                baos.close();
                System.out.println("Byte array length: " + imageInByte.length);

                // 바이트 배열을 Base64 인코딩하여 문자열로 변환합니다.
                String targetLanguageDisplay = (String) languageComboBox.getSelectedItem();
                String targetLanguage = languageCodeMap.get(targetLanguageDisplay);
                String fileBase64 = Base64.getEncoder().encodeToString(imageInByte);
                System.out.println("Base64 string: " + fileBase64);
                // JSON 객체에 정보를 저장합니다.
                json.put("sourceLanguage", "auto");
                json.put("targetLanguage", targetLanguage);
                json.put("image", fileBase64);

                //JSON 객체를 문자열로 변환
                String message = "IMAGETRANSLATE:"+json.toString();
                byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
                //한데 묶은 메시지를 바이트패턴으로 변환 후 서버로 전송
                client.sendToServer(messageBytes);
                // 서버로부터 바이트패턴으로 번역된 텍스트를 받아옵니다.
                try{
                    InputStream in = client.getSocket().getInputStream();
                    byte[] buffer = new byte[2*1024*1024];
                    int bytesReceived = in.read(buffer);
                    String serverMessage = new String(buffer, 0, bytesReceived, StandardCharsets.UTF_8);
                    // 서버로부터 받은 텍스트를 JSON 객체로 변환합니다.
                    if (serverMessage.equals("IMAGETRANSLATE:FAIL")){
                        JOptionPane.showMessageDialog(null, "오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
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

    public class CopyButton1Listener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // 입력 창의 텍스트를 클립보드에 복사
            StringSelection stringSelection = new StringSelection(originalTextArea.getText());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
        }
    }

    public class CopyButton2Listener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // 출력 창의 텍스트를 클립보드에 복사
            StringSelection stringSelection = new StringSelection(translatedTextArea.getText());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
        }
    }

    public class ResetButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // 입력 창 초기화
            originalTextArea.setText("");
            translatedTextArea.setText("");
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
    private class CustomWindowAdapter extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent e) {
            try {
                // 서버와의 연결 종료
                imageTranslator.disconnectFromServer();
                dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

}
