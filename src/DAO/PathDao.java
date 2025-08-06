package DAO;

import model.path;
import utils.DBUtil;

import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PathDao implements BaseDao<path>{
    @Override
    public int insert(path path) {
        String sql = "insert into path(distance, endid,startid,statue)"+" values(?,?,?,?)";
        PreparedStatement ps = null;
        Connection conn = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setDouble(1, path.getDistance());
            ps.setInt(2, path.getEndid());
            ps.setInt(3, path.getStartid());
            ps.setInt(4,1);
            if(ps.executeUpdate()==0){
                throw new SQLException("创建路径失败，没有行受到影响");
            }
            try{
                rs = ps.getGeneratedKeys();
                if(rs.next()){
                    path.setId(rs.getInt(1));
                    return path.getId();
                } else{
                    throw new SQLException("创建路径失败，没有返回id");
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            return 0;
        } finally {
            DBUtil.close(conn, ps, rs);
        }
    }

    @Override
    public int update(path path) {
        String sql = "update path set distance = ?,endid = ?,startid = ? where id = ?";
        PreparedStatement ps = null;
        Connection conn = null;
        try{
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setDouble(1, path.getDistance());
            ps.setInt(2, path.getEndid());
            ps.setInt(3, path.getStartid());
            ps.setInt(4, path.getId());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int delete(int id) {
        String sql = "update path set statue = ? where id=?";
        try(Connection conn = DBUtil.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, 0);
            ps.setInt(2, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            return 0;
        }
    }

    @Override
    public path findById(int id) {
        String sql = "select * from path where id=? and statue=1";
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

    private path mapResultSetToPath(ResultSet rs) {
        path path = new path();
        try{
            path.setId(rs.getInt("id"));
            path.setEndid(rs.getInt("endid"));
            path.setStartid(rs.getInt("startid"));
            path.setDistance(rs.getDouble("distance"));
            path.setStatue(rs.getInt("statue"));
            return path;
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public List<path> findAll() {
        String sql = "select * from path where statue=1";
        List<path> paths = new ArrayList<>();
        try(Connection conn = DBUtil.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                paths.add(mapResultSetToPath(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return paths;
    }

    public List<path> findPathsBetweenLocations(int startid,int endid) {
        String sql = "select * from path where startid=? and endid=? and statue=1";
        List<path> paths = new ArrayList<>();
        try(Connection conn = DBUtil.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, endid);
            ps.setInt(2, startid);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                paths.add(mapResultSetToPath(rs));
            }
            return paths;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public List<path> findByLocation(int id) {
        String sql = "select distance from path where endid=? or startid =? and statue=1";
        List<path> paths = new ArrayList<>();
        try(Connection conn = DBUtil.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ps.setInt(2, id);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                paths.add(mapResultSetToPath(rs));
            }
            return paths;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
