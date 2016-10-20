
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import mx.uach.objectpool.ObjectPool;

/**
 *
 * @author sourcemaking
 * https://sourcemaking.com/design_patterns/object_pool/java
 */

public class JDBCConnectionPool extends ObjectPool<Connection> {

  public static void main(String[] args) {
      JDBCConnectionPool pool= new JDBCConnectionPool("com.mysql.jdbc.Driver", 
              "jdbc:mysql://localhost:3306/mydb?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC ",
              "root", "1234");
      System.out.println(pool.create());
  }
    
  private String dsn, usr, pwd;

  public JDBCConnectionPool(String driver, String dsn, String usr, String pwd) {
    super();
    try {
      Class.forName(driver).newInstance();
    } catch (Exception e) {
      e.printStackTrace();
    }
    this.dsn = dsn;
    this.usr = usr;
    this.pwd = pwd;
  }

  @Override
  protected Connection create() {
    try {
      return (DriverManager.getConnection(dsn, usr, pwd));
    } catch (SQLException e) {
      e.printStackTrace();
      return (null);
    }
  }

  @Override
  public void expire(Connection obj) {
    try {
      ((Connection) obj).close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  @Override
  public boolean validate(Connection obj) {
    try {
      return (!((Connection) obj).isClosed());
    } catch (SQLException e) {
      e.printStackTrace();
      return (false);
    }
  }
}
