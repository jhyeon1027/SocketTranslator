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

public class NTranslator extends JFrame {
    // 파파고 API 서버 주소
    private static final String LANG_DETECT_API_URL = "https://openapi.naver.com/v1/papago/detectLangs";
    private static final String TRANSLATE_API_URL = "https://openapi.naver.com/v1/papago/n2mt";
    private static final String CLIENT_ID = "NqqIry45IjCweWpEX8mJ";
    private static final String CLIENT_SECRET = "99p_Wia1iH";

    private Client client;

    private NTranslatorGUI GUI;
    public void connectToServer() {

        this.client = new Client("localhost", 7777); // 예시로 localhost와 7777 포트를 사용
        this.client.connectToServer();
        this.GUI = new NTranslatorGUI(this, this.client); // NTranslator 인스턴스를 NTranslatorGUI 생성자에 전달
        System.out.println("번역기 접속확인");

    }
    public void disconnectFromServer() {
        String exitMessage="TransEXIT";
        byte[] exitMessageBytes = exitMessage.getBytes(StandardCharsets.UTF_8);
        this.client.sendToServer(exitMessageBytes);
        this.client.disconnectFromServer();
    }

    public boolean isSocketClosed() {
        return client.getSocket().isClosed();
    }

    public NTranslator() {

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
            return "TRANS FAIL:" + e.getMessage();
        }
    }


    public static void main(String[] args) {
        //NTranslator translator = new NTranslator();
        //translator.runTranslation();
    }
}
