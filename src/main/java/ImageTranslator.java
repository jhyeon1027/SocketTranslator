import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.*;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
public class ImageTranslator {
    private static final String IMAGE_TRANSLATE_API_URL = "https://naveropenapi.apigw.ntruss.com/image-to-text/v1/translate";
    private static final String CLIENT_ID = "xpjbocyx3w";
    private static final String CLIENT_SECRET = "etpg19tW6Gmywy76bMR2m8ZPuFWOfVP1thwtUonk";

    private Client client;
    private ImageTranslatorGUI GUI;

    public void connectToServer() {
        this.client = new Client("localhost", 7777); // 예시로 localhost와 7777 포트를 사용
        this.client.connectToServer();
        this.GUI = new ImageTranslatorGUI(this, this.client); // ImageTranslator 인스턴스를 ImageTranslatorGUI 생성자에 전달
        System.out.println("이미지 번역기 접속확인");
    }

    public void disconnectFromServer() {
        String exitMessage="ImageEXIT";
        byte[] exitMessageBytes = exitMessage.getBytes(StandardCharsets.UTF_8);
        this.client.sendToServer(exitMessageBytes);
        this.client.disconnectFromServer();
    }
    public boolean isSocketClosed() {
        return client.getSocket().isClosed();
    }

    //아래부턴 번역기능
   String translateImage(File imageFile, String source, String target) {
        OkHttpClient httpClient = new OkHttpClient();

        MediaType mediaType = MediaType.parse("multipart/form-data; boundary=BOUNDARY");

        RequestBody requestBody = new MultipartBody.Builder("BOUNDARY")
                .setType(MultipartBody.FORM)
                .addFormDataPart("source",source)
                .addFormDataPart("target", target)
                .addFormDataPart("image", imageFile.getName(),
                        RequestBody.create(MediaType.parse("application/octet-stream"), imageFile))
                .build();

        Request request = new Request.Builder()
                .url(IMAGE_TRANSLATE_API_URL)
                .post(requestBody)
                .addHeader("Content-Type", "multipart/form-data")
                .addHeader("X-NCP-APIGW-API-KEY-ID", CLIENT_ID)
                .addHeader("X-NCP-APIGW-API-KEY", CLIENT_SECRET)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {

            if (!response.isSuccessful()) {
                System.out.println("Unexpected response code: " + response.code());
                System.out.println("Response body: " + response.body().string());
                throw new IOException("Unexpected response code: " + response.code());
            }

            String responseBody = response.body().string();
            System.out.println("Response body: " + responseBody);

            Gson gson = new Gson();
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
            String sourceText = jsonResponse.getAsJsonObject("data").get("sourceText").getAsString();
            String targetText = jsonResponse.getAsJsonObject("data").get("targetText").getAsString();
            // 결과를 Map에 저장
            Map<String, String> result = new HashMap<>();
            result.put("sourceText", sourceText);
            result.put("targetText", targetText);

            // Map을 JSON 문자열로 변환
            String resultJson = gson.toJson(result);
            return resultJson;

        } catch (IOException e) {
            e.printStackTrace();
            // 사용자에게 알리는 팝업 메시지
            JOptionPane.showMessageDialog(null, "오류가 발생했습니다: " + e.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

}
