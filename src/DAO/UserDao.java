package DAO;

import model.user;
import utils.DBUtil;

import java.util.List;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class UserDao implements BaseDao<user>{
    @Override
    public int insert(user user) {
        String sql = "insert into user(name,password,statue)"+"values (?,?,?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try{
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setInt(3, 1);
            if (ps.executeUpdate() == 0){
                throw new SQLException("创建用户失败，没有行受到影响");
            }
            try(ResultSet rs = ps.getGeneratedKeys()) {
                if(rs.next()){
                    return rs.getInt(1);
                }else{
                    throw new SQLException("创建用户失败，无用户id返回值");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        } finally {
            DBUtil.close(conn, ps, null);
        }
    }

    @Override
    public int update(user user) {
        String sql = "update user set name=?,password=? where id=? and statue = 1";
        try(Connection conn = DBUtil.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setInt(3, user.getId());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int delete(int id) {
        String sql = "update user set statue = ? where id=? and statue = 1";
        try(Connection conn = DBUtil.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, 0);
            ps.setInt(2, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            return 0;
        }
    }
    public int delete(String name) {
        String sql = "update user set statue = ? where name=? and statue = 1";
        try(Connection conn = DBUtil.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, 0);
            ps.setString(2, name);
            return ps.executeUpdate();
        } catch (SQLException e) {
            return 0;
        }
    }
    @Override
    public user findById(int id) {
        user user = new user();
        String sql = "select * from user where id=? and statue = 1";
        try(Connection conn = DBUtil.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return mapResultSetToPath(rs);
            } else{
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private user mapResultSetToPath(ResultSet rs) {
        user user = new user();
        try{
            user.setUsername(rs.getString("name"));
            user.setPassword(rs.getString("password"));
            user.setId(rs.getInt("id"));
            user.setStatue(rs.getInt("statue"));
            return user;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<user> findAll() {
        String sql = "select * from user where statue = 1";
        List<user> list = new ArrayList<>();
        try(Connection conn = DBUtil.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                list.add(mapResultSetToPath(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public boolean usernameExist(String username) {
        String sql = "select * from user where name=? and statue = 1";
        try(Connection conn = DBUtil.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return true;
            } else{
                return false;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean userExist(String name,String password) {
        String sql = "select * from user where name=? and password=? and statue = 1";
        try(Connection conn = DBUtil.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return true;
            }else{
                return false;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
