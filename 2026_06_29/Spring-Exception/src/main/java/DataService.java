import java.sql.SQLException;

public class DataService {
    private FileLogger logger;
    DataService(FileLogger logger) { this.logger = logger; }

    String fetchWithRetry(FlakyService flaky) {
        int maxRetry = 3;
        for(int i = 0; i < maxRetry; i++) {
            try {
                String result = flaky.fetch();
                logger.log("INFO", i + "번째 시도 실패: " + result);
                return result;
            } catch (SQLException e) {
                logger.log("WARN", i + "번째 시도 실패: " + e.getMessage());
            }
        }
        logger.log("ERROR", "재시도 " + maxRetry + "회 모두 실패");
        throw new RuntimeException("재시도 " + maxRetry + "회 모두 실패했습니다.");
    }

    void avoidByThrows(FlakyService f) throws SQLException {
        f.fetch();
    }

    void avoidByRethrow(FlakyService f) throws SQLException {
        try {
            f.fetch();
        } catch (SQLException e) {
            logger.log("WARN", "회피: 여기서 처리하지 않고 호출자에게 넘김 - " + e.getMessage());
            throw e;
        }
    }

    static class DuplicateUserIdException extends RuntimeException {
        DuplicateUserIdException(String id, Throwable cause) {
            super("이미 존재하는 아이디입니다: " + id, cause);
        }
    }

    void insertUser(String id) throws SQLException {
        throw new SQLException("Duplicate entry", "23000");
    }

    void registerUser(String id) {
        try {
            insertUser(id);
        } catch (SQLException e) {
            if("23000".equals(e.getSQLState())) {
                logger.log("ERROR", "아이디 중복: " + id);
                throw new DuplicateUserIdException(id, e);
            }
            logger.log("ERROR", "회원 저장 중 DB 오류: " + id);
            throw new RuntimeException("회원 저장 중 DB 오류", e);
        }
    }
}
