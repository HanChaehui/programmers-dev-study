public class Settings {
    private static Settings instance;
    private String theme = "dark";

    private Settings() {}

    static synchronized Settings getInstance() {
        if(instance == null) {
            instance = new Settings();
        }
        return instance;
    }

    String getTheme() { return theme; }
    void setTheme(String t){ theme = t; }
}
