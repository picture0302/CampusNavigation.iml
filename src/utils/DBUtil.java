package utils;

import java.sql.*;

public class DBUtil {
    //这些数据之后不会再更改，所以可以写死
    private static final String URL = "jdbc:mysql://localhost:3306/camnav?useUnicode=true&characterEncoding=utf8&useSSl=false&serverTimezone=GMT%2B8";
    private static final String USER = "root";
    private static final String PASSWORD = "0715xlhc";
    // 静态代码块只执行一次，驱动只需要加载一次
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Connection getConnection()  {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     *  封装通用的关闭的方法
     *      方法名 close （）
     *      参数表 PreparedStatement ps ResultSet rs Connection conn
     *      返回值  无
     */
    public static void close(Connection conn, PreparedStatement ps, ResultSet rs) {
        try {
            if(rs != null) {
                rs.close();
            }
            if(ps != null) {
                ps.close();
            }
            if(conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
