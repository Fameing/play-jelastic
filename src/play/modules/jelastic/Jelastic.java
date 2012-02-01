package play.modules.jelastic;

public class Jelastic {


    public Jelastic(String key, String secret) {
        System.out.println("key" + key);
        System.out.println("secret" + secret);
    }


    public static void main(String[] args) {
        for (String arg : args) {
            System.out.println(arg);
        }
    }

}
