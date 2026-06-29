public class Main {
    static void main() {
        FileLogger logger = new FileLogger();
        DataService service = new DataService(logger);

        try {
            System.out.println("최종 결과: " + service.fetchWithRetry(new FlakyService(2)));
        } catch (RuntimeException e) { System.out.println("실패 통보: " + e.getMessage()); }

        try {
            service.fetchWithRetry(new FlakyService(99));
        } catch (RuntimeException e) { System.out.println("실패 통보: " + e.getMessage()); }

        try {
            service.registerUser("kim");
        } catch (DataService.DuplicateUserIdException e) {
            System.out.println("잡힘: " + e.getMessage());
            System.out.println("원인 보존: " + e.getCause());
        }

        System.out.println("로그 파일: " + logger.getLogFilePath());

    }
}
