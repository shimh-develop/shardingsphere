package shimh;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestA {

    DataSource dataSource;

    @BeforeAll
    public void init() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.apache.shardingsphere.driver.ShardingSphereDriver");
        config.setJdbcUrl("jdbc:shardingsphere:classpath:shardingsphere.yaml");
        dataSource = new HikariDataSource(config);
    }

    @Test
    public void test1() throws Exception {
        String sql = "insert into t_adress(name) values (?),(?)";
        try (Connection connection = dataSource.getConnection();) {
            connection.setAutoCommit(false);
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, "北京");
            ps.setString(2, "上海");
            ps.executeUpdate();
            connection.commit();
        }
    }

    @Test
    public void test2() throws Exception {
        String sql = "select id,name from t_adress where id = 1";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                long id = rs.getLong("id");
                String name = rs.getString("name");
                System.out.println(String.format("id:%s, name:%s", id, name));
            }
        }
    }
}
