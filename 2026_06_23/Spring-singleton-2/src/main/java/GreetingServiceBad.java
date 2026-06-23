public class GreetingServiceBad {
    private static final GreetingServiceBad instance = new GreetingServiceBad();
    private String name;

    private GreetingServiceBad() {}
    static GreetingServiceBad getInstance() { return instance; }

    String greet(String reqName)  {
        this.name = reqName;
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
        }
        return this.name;
    }
}
