public enum PricePlan {
    LITE(10),
    BASIC(20),
    PREMIUM(30);

    private final int capacity;

    PricePlan(int capacity){
        this.capacity = capacity;
    }

    public int getCapacity() {
        return capacity;
    }

    public static PricePlan from(int choice){
        switch(choice){
            case 1:
                return PricePlan.LITE;
            case 2:
                return PricePlan.BASIC;
            case 3:
                return PricePlan.PREMIUM;
            default:
                return null;
        }
    }
}
