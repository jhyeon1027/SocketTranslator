import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static javax.swing.SwingConstants.CENTER;

public class Mainpage {
    private static Chat chatInstance; //Chat 클래스의 인스턴스를 저장하는 필드
    private static NTranslator translatorInstance;
    private static ImageTranslator imageInstance;
    private static PDFtoImage pdfInstance;

    public static class RoundedButton extends JButton {

        private Color bgColor;
        public RoundedButton(String text, Color bgColor){
            super(text);
            this.bgColor = bgColor;
            decorate();
        }

        public RoundedButton() {
            super();
            decorate();
        }

        public RoundedButton(String text) {
            super(text);
            decorate();
        }

        public RoundedButton(Action action) {
            super(action);
            decorate();
        }

        public RoundedButton(Icon icon) {
            super(icon);
            decorate();
        }

        public RoundedButton(String text, Icon icon) {
            super(text, icon);
            decorate();
        }

        protected void decorate() {
            setBorderPainted(false);
            setOpaque(false);
            setBackground(bgColor);
        }

        @Override
        protected void paintComponent(Graphics g) {
            int width = getWidth();
            int height = getHeight();

            Graphics2D graphics = (Graphics2D) g;

            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (getModel().isArmed()) {
                graphics.setColor(getBackground().darker());
            } else if (getModel().isRollover()) {
                graphics.setColor(getBackground().brighter());
            } else {
                graphics.setColor(getBackground());
            }

            if (this.getText().equals("나가기")){ // b5 버튼의 둥근 정도를 작게 조정
                graphics.fillRoundRect(0, 0, width, height, 20, 20); // 나가기 버튼만 둥근 정도를 줄임
            } else if(this.getText().equals("번역")){
                graphics.fillRoundRect(0, 0, width, height, 20, 20); // 나가기 버튼만 둥근 정도를 줄임
            } else if(this.getText().equals("복사")){
                graphics.fillRoundRect(0, 0, width, height, 10, 10); // 나가기 버튼만 둥근 정도를 줄임
            } else if(this.getText().equals("초기화")){
                graphics.fillRoundRect(0, 0, width, height, 10, 10); // 나가기 버튼만 둥근 정도를 줄임
            } else if(this.getText().equals("업로드 및 번역")){
                graphics.fillRoundRect(0, 0, width, height, 10, 10); // 나가기 버튼만 둥근 정도를 줄임
            } else{
                graphics.fillRoundRect(0, 0, width, height, 50, 50); // 다른 버튼들은 기존 둥근 정도 유지
            }

            FontMetrics fontMetrics = graphics.getFontMetrics();
            Rectangle stringBounds = fontMetrics.getStringBounds(this.getText(), graphics).getBounds();

            int textX = (width - stringBounds.width) / 2;
            int textY = (height - stringBounds.height) / 2 + fontMetrics.getAscent();

            graphics.setColor(getForeground());
            graphics.setFont(getFont());
            graphics.drawString(getText(), textX, textY);
            graphics.dispose();

            super.paintComponent(g);
        }
    }     // 버튼 둥글게 변경하는 클래스

    public void openMainpage() {
        JFrame f = new JFrame("CATPAGO - 언어 장벽 없이 대화하는 세상을 꿈꿉니다. "); // JFrame 객체를 생성한다.
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        //이미지는 모두 상대경로로 지정
        Image img = toolkit.getImage("src\\main\\resources\\CATPAGO_LOGO.png");
        //@@ SocketTranslator\\src\\main\\resources\\CATPAGO_LOGO
        f.setIconImage(img);

        ImageIcon imageIcon = new ImageIcon("src\\main\\resources\\CATPAGO_2.png"); // Replace with your image path
        //@@SocketTranslator\\src\\main\\resources\\CATPAGO_2

        ImageIcon imageIcon2 = new ImageIcon("src\\main\\resources\\bg.png");
        //@@SocketTranslator\\src\\main\\resources\\bg

        // Create a JLabel with the imageIcon
        JLabel imageLabel = new JLabel("",imageIcon,JLabel.CENTER);
        JLabel imageLabel2 = new JLabel("",imageIcon2,JLabel.CENTER);


        imageLabel.setBounds(0, 0, imageIcon.getIconWidth(), imageIcon.getIconHeight()); // Set the bounds according to the image size
        imageLabel2.setBounds(0, 500, imageIcon2.getIconWidth(), imageIcon2.getIconHeight()); // Set the bounds according to the image size





        f.setResizable(false); // 윈도우의 크기 조정을 불가능하게 한다.
        RoundedButton b1 = new RoundedButton("텍스트 번역",new Color(131,162,255)); // JButton 객체를 생성한다.
        RoundedButton b2 = new RoundedButton("이미지 번역",new Color(180,189,255)); // JButton 객체를 생성한다.
        RoundedButton b3 = new RoundedButton(" PDF  번역",new Color(255,227,187)); // JButton 객체를 생성한다.
        RoundedButton b4 = new RoundedButton("글로벌 채팅",new Color(255,210,143)); // JButton 객체를 생성한다.
        RoundedButton b5 = new RoundedButton("나가기", new Color(155,164,181)); // JButton 객체를 생성한다.


        b1.setBounds(125, 460, 250, 65); // 버튼의 위치와 크기를 설정한다.
        b2.setBounds(125, 550, 250, 65); // 버튼의 위치와 크기를 설정한다.
        b3.setBounds(125, 640, 250, 65); // 버튼의 위치와 크기를 설정한다.
        b4.setBounds(125, 730, 250, 65); // 버튼의 위치와 크기를 설정한다.
        b5.setBounds(380, 830, 100, 30); // 버튼의 위치와 크기를 설정한다.

        f.add(b1); // 버튼을 윈도우에 추가한다.
        f.add(b2); // 버튼을 윈도우에 추가한다.
        f.add(b3); // 버튼을 윈도우에 추가한다.
        f.add(b4); // 버튼을 윈도우에 추가한다.
        f.add(b5); // 버튼을 윈도우에 추가한다.

        f.setLocation(702,390); // fhd 기준 화면 중앙에서 실행되는 코드
        f.setSize(516, 910); // 윈도우의 크기를 설정한다.
        f.setLayout(null); // 윈도우의 레이아웃을 설정한다.
        f.setVisible(true); // 윈도우의 가시성을 설정한다.
        // GUI부분@@@@

        // 프로그램에 이미지 추가
        f.add(imageLabel);
        f.add(imageLabel2);

        // b1 버튼에 액션 리스너를 추가한다.
        b1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // b1 버튼이 클릭되면 NTranslator 클래스를 실행한다.
                if (translatorInstance == null|| translatorInstance.isSocketClosed()){ //번역기가 실행중이지 않다면
                    translatorInstance = new NTranslator();
                    translatorInstance.connectToServer();
                }
                else{
                    translatorInstance.setVisible(true);
                }
            }
        });

        b2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // b2 버튼이 클릭되면 ImageTranslator 클래스를 실행한다.
                if (imageInstance == null||imageInstance.isSocketClosed()){ //번역기가 실행중이지 않다면
                    imageInstance = new ImageTranslator();
                    imageInstance.connectToServer();
                }
                else{
                    translatorInstance.setVisible(true);
                }
            }
        });

        // b3 버튼에 액션 리스너를 추가한다.
        b3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // b3 버튼이 클릭되면 PDFtoImageGUI 클래스를 실행한다.
                if (pdfInstance == null||pdfInstance.isSocketClosed()){
                    pdfInstance = new PDFtoImage();
                    pdfInstance.connectToServer();
                }
                else{
                    pdfInstance.getGUI().setVisible(true);
                }
            }
        });
        b4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (chatInstance == null || chatInstance.isSocketClosed()) {
                    String username = null;

                    // 사용자에게 이름 입력 받기
                    while (true) {
                        username = JOptionPane.showInputDialog("사용자 이름을 입력하세요:");
                        if (username == null) {
                            // 사용자가 취소 버튼을 눌렀을 경우
                            break;
                        } else if (!username.trim().isEmpty()) {
                            // 사용자 이름이 비어있지 않은 경우
                            if (chatInstance == null) {
                                chatInstance = new Chat();
                            }

                            chatInstance.setUsername(username);

                            // 비동기적으로 connectToServer 메서드 호출
                            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                                @Override
                                protected Void doInBackground() throws Exception {
                                    chatInstance.connectToServer();
                                    return null;
                                }

                                @Override
                                protected void done() {
                                    // GUI를 나타내는 코드
                                    chatInstance.setVisible(true);
                                }
                            };

                            worker.execute();  // SwingWorker 실행
                            break;
                        } else {
                            JOptionPane.showMessageDialog(null, "사용자 이름을 입력하세요.");
                        }
                    }
                } else {
                    chatInstance.setVisible(true);
                }
            }
        });
        b5.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                // b5 버튼이 클릭되면 프로그램을 종료한다.
                System.exit(0);
            }
        });

        f.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);

                System.exit(0); // 프로그램 종료
            }
        });


    }
}
