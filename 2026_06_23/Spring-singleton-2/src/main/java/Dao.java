import java.sql.Connection;

public class Dao {
}

interface ConnectionMaker {
    String makeConnection();
}

class SimpleConnectionMaker implements ConnectionMaker {
    private static final SimpleConnectionMaker instance = new SimpleConnectionMaker();

    private SimpleConnectionMaker() {}
    static SimpleConnectionMaker getInstance() { return instance; }

    @Override
    public String makeConnection() {
        return "DB 연결";
    }
}

class UserDao {
    private static final UserDao instance = new UserDao();
    private ConnectionMaker connectionMaker = SimpleConnectionMaker.getInstance();

    private UserDao() {}
    static UserDao getInstance() { return instance; }

    String findUser(String userId) {
        return userId + " 조회 [" + connectionMaker.makeConnection() + "]";
    }
}