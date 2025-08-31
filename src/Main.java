import tracker.exceptions.NotFoundException;
import tracker.exceptions.OverlapException;

public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");
        final Demo demo = new Demo();
        try {
            demo.runDemo();
        }  catch (OverlapException | NotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}