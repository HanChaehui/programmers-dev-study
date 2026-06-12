public class VipMember extends Member{

    public VipMember(String name, String email, String phone) {
        super(name, email, phone);
    }

    @Override
    public String getGrade() {
        return "VIP";
    }

    @Override
    public String getBenefit() {
        return "10% 할인 + 무료배송";
    }
}
