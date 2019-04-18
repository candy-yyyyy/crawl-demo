import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbUtil {
	public static Connection getConnection(){
		Connection conn=null;
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");//�ҵ�oracle���������ڵ���
			String url="jdbc:oracle:thin:@localhost:1521:ORCL"; //URL��ַ
			String username="scott";
			String password="123456";
			conn=DriverManager.getConnection(url, username, password);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return conn;
	}
	public static void close(PreparedStatement pstmt){
		if(pstmt !=null){
			try {
				pstmt.close();
			} catch (SQLException e) {
				
				e.printStackTrace();
			}
		}
	}
	
	public static void close(Connection conn){
		if(conn !=null){
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void close(ResultSet rs){
		if(rs !=null){
			try {
				rs.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}