public class Main {
    public static void main(String[] args){
        String message = "[손정우님이 입장하였습니다.]";
        String[] users = message.substring("USERLIST:".length()).split(",");
    }
}
