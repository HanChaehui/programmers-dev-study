public class TicketMachine {

    private static final TicketMachine machine = new TicketMachine();
    private int num = 1;

    private TicketMachine() {}
    static TicketMachine getInstance() {
        return machine;
    }

    int issue() {return num++;}
}
