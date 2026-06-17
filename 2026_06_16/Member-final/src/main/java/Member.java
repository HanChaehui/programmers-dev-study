public interface Member {

    public String getName();
    public String getEmail();
    public String getPhone();
    public String getGrade();
    public String getBenefit();
    void update(String name, String email, String phone);

    default void printInfo(){
        System.out.println("[" + getGrade() + "] " + getName() + " / "
                + getEmail() + " / " + getPhone() + " (혜택: " + getBenefit() + ")");
    }
}
