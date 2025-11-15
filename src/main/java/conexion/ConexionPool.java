package conexion;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class ConexionPool {
    private static HikariDataSource ds;

    static {
        HikariConfig config = new HikariConfig("./src/main/resources/db.properties");
        ds = new HikariDataSource(config);
   }

    public static java.sql.Connection getConnection() throws Exception {
        return ds.getConnection();
    }
}
