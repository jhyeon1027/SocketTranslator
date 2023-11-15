import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

public class NTranslator extends JFrame {
    // 파파고 API 서버 주소
    private static final String LANG_DETECT_API_URL = "https://openapi.naver.com/v1/papago/detectLangs";
    private static final String TRANSLATE_API_URL = "https://openapi.naver.com/v1/papago/n2mt";
    private static final String CLIENT_ID = "NqqIry45IjCweWpEX8mJ";
    private static final String CLIENT_SECRET = "99p_Wia1iH";

    // GUI 컴포넌트 선언
    private JTextArea inputArea; // 입력 창
    private JTextArea outputArea; // 출력 창
    private JButton translateButton; // 번역 버튼
    private JComboBox<String> languageComboBox; // 번역할 언어 소스 선택 콤보박스

    public NTranslator() {
        // JFrame 설정
        setTitle("텍스트 번역기");
        setSize(500, 500);
        setLayout(null);

        // GUI 컴포넌트 초기화
        this.inputArea = new JTextArea(); // 입력 창
        inputArea.setLineWrap(true); // 텍스트가 행 너비를 초과하면 자동으로 줄 바꿈
        inputArea.setBounds(80, 20, 320, 150);

        outputArea = new JTextArea(); // 출력 창
        outputArea.setLineWrap(true); // 텍스트가 행 너비를 초과하면 자동으로 줄 바꿈
        JScrollPane outputScrollPane = new JScrollPane(outputArea);
        outputArea.setBounds(80, 250, 320, 150);
        outputArea.setEditable(false);

        translateButton = new JButton("번역");
        translateButton.setBounds(200, 195, 100, 30);
        translateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 번역 기능을 구현
                String inputText = inputArea.getText();
                String sourceLanguage = detectLanguage(inputText); //언어감지
                String targetLanguage = (String) languageComboBox.getSelectedItem();
                System.out.println("원본메시지: " + inputText); //없애도 되는 부분 검수용
                System.out.println("타겟언어: " + targetLanguage);
                System.out.println("소스언어: " + sourceLanguage);
                // 목적언어는 콤보박스 선택
                // 여기에 실제 번역 로직을 추가해야 합니다.
                String translatedText = translateText(inputText, sourceLanguage, targetLanguage);
                // 번역 결과를 outputArea에 표시
                outputArea.setText("번역 결과: " + translatedText);
            }
        });

        // 콤보박스 초기화 및 설정
        languageComboBox = new JComboBox<>(new String[]{"ko", "en", "ja", "zh-CN", "zh-TW", "vi", "id", "th", "de", "ru", "es", "it", "fr"});
        languageComboBox.setBounds(80, 195, 100, 30);

        // 컴포넌트를 프레임에 추가
        add(inputArea);
        add(outputArea);
        add(translateButton);
        add(languageComboBox);
    }

    public String detectLanguage(String text) {

        String query;
        try {
            query = URLEncoder.encode(text, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("인코딩 실패", e);
        }

        Map<String, String> requestHeaders = new HashMap<>();
        requestHeaders.put("X-Naver-Client-Id", CLIENT_ID);
        requestHeaders.put("X-Naver-Client-Secret", CLIENT_SECRET);

        String responseBody = post(LANG_DETECT_API_URL, requestHeaders, query);

        JSONParser parser = new JSONParser();
        String langCode = "";
        try {
            JSONObject json = (JSONObject) parser.parse(responseBody);
            langCode = (String) json.get("langCode");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return langCode;
    }

    private static String post(String LANG_DETECT_API_URL, Map<String, String> requestHeaders, String text){
        HttpURLConnection con = connect(LANG_DETECT_API_URL);
        String postParams =  "query="  + text; //원본언어: 한국어 (ko) -> 목적언어: 영어 (en)
        try {
            con.setRequestMethod("POST");
            for(Map.Entry<String, String> header :requestHeaders.entrySet()) {
                con.setRequestProperty(header.getKey(), header.getValue());
            }

            con.setDoOutput(true);
            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                wr.write(postParams.getBytes());
                wr.flush();
            }

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // 정상 응답
                return readBody(con.getInputStream());
            } else {  // 에러 응답
                return readBody(con.getErrorStream());
            }
        } catch (IOException e) {
            throw new RuntimeException("API 요청과 응답 실패", e);
        } finally {
            con.disconnect();
        }
    }

    private static HttpURLConnection connect(String apiUrl){
        try {
            URL url = new URL(apiUrl);
            return (HttpURLConnection)url.openConnection();
        } catch (MalformedURLException e) {
            throw new RuntimeException("API URL이 잘못되었습니다. : " + apiUrl, e);
        } catch (IOException e) {
            throw new RuntimeException("연결이 실패했습니다. : " + apiUrl, e);
        }
    }

    private static String readBody(InputStream body){
        InputStreamReader streamReader = new InputStreamReader(body);

        try (BufferedReader lineReader = new BufferedReader(streamReader)) {
            StringBuilder responseBody = new StringBuilder();

            String line;
            while ((line = lineReader.readLine()) != null) {
                responseBody.append(line);
            }

            return responseBody.toString();
        } catch (IOException e) {
            throw new RuntimeException("API 응답을 읽는데 실패했습니다.", e);
        }
    }



    String translateText(String text, String sourceLang, String targetLang) {
        try {
            if (sourceLang.equals(targetLang)) {
                return text;
            }
            // 파파고 API 호출 및 실제 번역 로직을 여기에 추가
            text = URLEncoder.encode(text, "UTF-8");
            URL url = new URL(TRANSLATE_API_URL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("X-Naver-Client-Id", CLIENT_ID);
            con.setRequestProperty("X-Naver-Client-Secret", CLIENT_SECRET);

            String postParams = "source=" + sourceLang + "&target=" + targetLang + "&text=" + text;
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(postParams);
            wr.flush();
            wr.close();

            int responseCode = con.getResponseCode();
            BufferedReader br;
            if (responseCode == 200) {
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }

            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();

            // JSON 결과를 파싱하여 translatedText 추출
            JSONParser parser = new JSONParser();
            JSONObject jsonResponse = (JSONObject) parser.parse(response.toString());
            JSONObject messageObject = (JSONObject) jsonResponse.get("message");
            JSONObject resultObject = (JSONObject) messageObject.get("result");
            String translatedText = (String) resultObject.get("translatedText");

            return translatedText;
        } catch (Exception e) {
            return "번역 실패: " + e.getMessage();
        }
    }

    public void runTranslation() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setVisible(true);
            }
        });
    }

    public static void main(String[] args) {
        NTranslator translator = new NTranslator();
        translator.runTranslation();
    }
}
