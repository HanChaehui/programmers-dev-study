public interface Member {

    public String getName();
    public String getEmail();
    public String getPhone();
    public abstract String getGrade();
    public abstract String getBenefit();

    public void update(String name, String email, String phone);

    default void printInfo(){
        System.out.println("[" + getGrade() + "] " + getName() + " / " + getEmail() + " / " + getPhone() + " (혜택: " + getBenefit() + ")");
    }
}
