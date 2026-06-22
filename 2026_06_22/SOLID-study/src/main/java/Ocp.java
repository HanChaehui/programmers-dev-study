public class Ocp {
}

interface DiscountPolicy {
    int discount(int price);
}

class BasicDiscount implements DiscountPolicy {

    @Override
    public int discount(int price) {
        return price;
    }
}

class GoldDiscount implements DiscountPolicy {

    @Override
    public int discount(int price) {
        return price * 90 / 100;
    }
}

class VipDiscount implements DiscountPolicy {

    @Override
    public int discount(int price) {
        return price * 80 / 100;
    }
}

