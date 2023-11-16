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

import javax.imageio.ImageIO;
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


    public int convert(File pdfFile, String imageFilePrefix) {
        int pageEnd = 0;
        try {
            PDDocument document = PDDocument.load(pdfFile);
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            pageEnd = document.getNumberOfPages();
            // Remove '.pdf' from the image file prefix
            for (int page = 0; page < pageEnd; ++page) {
                BufferedImage image = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);
                ImageIO.write(image, "JPEG", new File(imageFilePrefix + "_" + page + ".jpg"));
            }
            document.close();
            System.out.println(("이미지 나누기 끝"));
            return pageEnd;
            //imagesToPdf(imageFilePrefix, pageEnd);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pageEnd;
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void translateImage(String imageFilePath, String sourceLang, String targetLang) {
        OkHttpClient client = new OkHttpClient().newBuilder().build();

        MediaType mediaType = MediaType.parse("image/jpeg");
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("source", sourceLang)
                .addFormDataPart("target", targetLang)
                .addFormDataPart("image", "image.jpg",
                        RequestBody.create(mediaType, new File(imageFilePath)))
                .build();

        Request request = new Request.Builder()
                .url(IMAGE_TRANSLATE_TO_IMAGE_API_URL)
                .method("POST", body)
                .addHeader("X-NCP-APIGW-API-KEY-ID", CLIENT_ID)
                .addHeader("X-NCP-APIGW-API-KEY", CLIENT_SECRET)
                .build();

        try {
            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();
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



    public static void main(String[] args) {
    }
}