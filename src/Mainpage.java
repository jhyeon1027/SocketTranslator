import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;

public class Mainpage {
    private static Chat chatInstance; //Chat 클래스의 인스턴스를 저장하는 필드

    public void openMainpage(){
        JFrame f = new JFrame("My Program"); // JFrame 객체를 생성한다.
        f.setResizable(false); // 윈도우의 크기 조정을 불가능하게 한다.
        JButton b1 = new JButton("텍스트 번역"); // JButton 객체를 생성한다.
        JButton b2 = new JButton("이미지 번역"); // JButton 객체를 생성한다.
        JButton b3 = new JButton("채팅방"); // JButton 객체를 생성한다.
        b1.setBounds(100, 50, 200, 50); // 버튼의 위치와 크기를 설정한다.
        b2.setBounds(100, 120, 200, 50); // 버튼의 위치와 크기를 설정한다.
        b3.setBounds(100, 190, 200, 50); // 버튼의 위치와 크기를 설정한다.
        f.add(b1); // 버튼을 윈도우에 추가한다.
        f.add(b2); // 버튼을 윈도우에 추가한다.
        f.add(b3); // 버튼을 윈도우에 추가한다.
        f.setSize(400, 400); // 윈도우의 크기를 설정한다.
        f.setLayout(null); // 윈도우의 레이아웃을 설정한다.
        f.setVisible(true); // 윈도우의 가시성을 설정한다.


        // b1 버튼에 액션 리스너를 추가한다.
       /* b1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // b1 버튼이 클릭되면 NTranslator 클래스를 실행한다.
                if (translatorInstance == null){ //번역기가 실행중이지 않다면
                    translatorInstance = new NTranslator();
                    translatorInstance.runTranslation();
                }
                else{
                    translatorInstance.setVisible(true);
                }
            }
        });*/

        /*b2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // b2 버튼이 클릭되면 ImageTranslator 클래스를 실행한다.
                if (imageInstance == null){ //번역기가 실행중이지 않다면

                }
                else{
                    translatorInstance.setVisible(true);
                }
            }
        });*/

        // b3 버튼에 액션 리스너를 추가한다.
       b3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // b3 버튼이 클릭되면 Chat 클래스를 실행한다.
                if (chatInstance == null) { //채팅방을 실행중이지 않다면
                    chatInstance = new Chat();
                } else {
                    chatInstance.setVisible(true);
                }
            }
        });
    }


    //private static Client chatInstance; //Chat 클래스의 인스턴스를 저장하는 필드

    public static void main(String[] args) {


    }
}
