// FlakyService.java (제공)
class FlakyService {

    private final int failTimes;
    private int callCount = 0;
    FlakyService(int failTimes) { this.failTimes = failTimes; }

    String fetch() throws java.sql.SQLException {
        callCount++;
        if (callCount <= failTimes)
            throw new java.sql.SQLException("일시적 연결 오류 (호출 " + callCount + ")");
        return "데이터-OK";
    }
}