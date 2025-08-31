import tracker.exceptions.NotFoundException;

public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");
        final Demo demo = new Demo();
        try {
            demo.runDemo();
        }  catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}