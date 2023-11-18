import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import okhttp3.*;
import java.util.Base64;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.Graphics2D;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;



public class PDFtoImage {

    private static final String IMAGE_TRANSLATE_TO_IMAGE_API_URL = "https://naveropenapi.apigw.ntruss.com/image-to-image/v1/translate";
    private static final String CLIENT_ID = "xpjbocyx3w";
    private static final String CLIENT_SECRET = "etpg19tW6Gmywy76bMR2m8ZPuFWOfVP1thwtUonk";

    private Client client;
    private PDFtoImageGUI GUI;

    public void connectToServer() {
        this.client = new Client("localhost", 7777); // 예시로 localhost와 7777 포트를 사용
        this.client.connectToServer();
        this.GUI = new PDFtoImageGUI(this, this.client); // PDFtoImage 인스턴스를 PDFtoImageGUI 생성자에 전달
        System.out.println("PDF 번역기 접속확인");
    }
    public void disconnectFromServer() {
        String exitMessage="PDFEXIT";
        byte[] exitMessageBytes = exitMessage.getBytes(StandardCharsets.UTF_8);
        this.client.sendToServer(exitMessageBytes);
        this.client.disconnectFromServer();
    }
    public boolean isSocketClosed() {
        return client.getSocket().isClosed();
    }
    public PDFtoImageGUI getGUI() {
        return this.GUI;
    }

    public int convert(File pdfFile, String imageFilePrefix) {
        int pageEnd = 0;
        try {
            PDDocument document = PDDocument.load(pdfFile);
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            pageEnd = document.getNumberOfPages();

            for (int page = 0; page < pageEnd; ++page) {
                BufferedImage image = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);

                // 이미지 크기가 1960x1960보다 크면 크기를 조절
                if (image.getWidth() > 1960 || image.getHeight() > 1960) {
                    int scaledWidth;
                    int scaledHeight;
                    if (image.getWidth() > image.getHeight()) { // 가로가 더 긴 경우
                        scaledWidth = 1960;
                        scaledHeight = (int) (image.getHeight() * (1960.0 / image.getWidth()));
                    } else { // 세로가 더 긴 경우
                        scaledHeight = 1960;
                        scaledWidth = (int) (image.getWidth() * (1960.0 / image.getHeight()));
                    }
                    Image tmp = image.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
                    BufferedImage scaledImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB);
                    Graphics2D g2d = scaledImage.createGraphics();
                    g2d.drawImage(tmp, 0, 0, null);
                    g2d.dispose();
                    image = scaledImage;
                }

                ImageIO.write(image, "JPEG", new File(imageFilePrefix + "_" + page + ".jpg"));
            }
            document.close();
            System.out.println(("이미지 나누기 끝"));
            PDFtoImageGUI.progressBar.setString("PDF 변환 진행 중 ...");

            return pageEnd;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pageEnd;
    }

    public void translateImage(String imageFilePath, String sourceLang, String targetLang) {
        OkHttpClient httpClient = new OkHttpClient().newBuilder().build();

        File imageFile = new File(imageFilePath);
        MediaType mediaType = MediaType.parse("image/jpeg");
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("source", sourceLang)
                .addFormDataPart("target", targetLang)
                .addFormDataPart("image", imageFile.getName(),
                        RequestBody.create(mediaType, imageFile))
                .build();

        Request request = new Request.Builder()
                .url(IMAGE_TRANSLATE_TO_IMAGE_API_URL)
                .method("POST", body)
                .addHeader("X-NCP-APIGW-API-KEY-ID", CLIENT_ID)
                .addHeader("X-NCP-APIGW-API-KEY", CLIENT_SECRET)
                .build();
        try {
            Response response = httpClient.newCall(request).execute();
            String responseBody = response.body().string();
            System.out.println(responseBody);
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(responseBody);
            String renderedImage = ((JSONObject) jsonObject.get("data")).get("renderedImage").toString();
            // Decode the Base64 image data and save it as a file
            byte[] imageBytes = Base64.getDecoder().decode(renderedImage);
            Files.write(Paths.get(imageFilePath), imageBytes);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void imagesToPdf(String imageFilePrefix, int pageEnd) {
        System.out.println(("PDF합치기 시작. 경로 : " + imageFilePrefix));
        try (PDDocument doc = new PDDocument()) {
            for (int page = 0; page < pageEnd; ++page) {
                String filename = imageFilePrefix + "_" + page + ".jpg";
                PDPage pdPage = new PDPage();
                doc.addPage(pdPage);
                PDImageXObject pdImage = PDImageXObject.createFromFile(filename, doc);
                PDPageContentStream contentStream = new PDPageContentStream(doc, pdPage);
                contentStream.drawImage(pdImage, 0, 0, pdPage.getMediaBox().getWidth(), pdPage.getMediaBox().getHeight());
                contentStream.close();
            }
            // Extract the directory from the file path
            File file = new File(imageFilePrefix+"_0.jpg");
            String directory = file.getParent();
            System.out.println("디렉토리:"+directory);
            // Extract the file name from the file path
            String filename = new File(imageFilePrefix).getName();
            System.out.println("파일이름:"+filename);

            // Generate the output PDF file name
            String outputPdfFile = "translated_" + filename + ".pdf";
            File outputFile = new File(directory, outputPdfFile);

            doc.save(outputFile);
            for (int page = 0; page < pageEnd; ++page) {
                filename = imageFilePrefix + "_" + page + ".jpg";
                File file2 = new File(filename);
                if(file2.delete()){
                    System.out.println(filename + " 파일 삭제 성공");
                }else{
                    System.out.println(filename + " 파일 삭제 실패");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }





    public static void main(String[] args) {
        //테스트용
        //PDFtoImage pdfToImage = new PDFtoImage();
        //pdfToImage.translateImage("C:\\Users\\wjdwh\\Downloads\\Imagetest\\Team Project (1)_0.jpg", "auto", "ko");
    }
}