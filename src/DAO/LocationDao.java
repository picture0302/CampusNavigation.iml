package DAO;

import model.location;
import utils.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static utils.DBUtil.getConnection;

public class LocationDao implements BaseDao<location>{
    @Override
    public int delete(int id) {
        String sql="update location set statue = ? where id=?";
        try(Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, 0);
            ps.setInt(2, id);//第一个数的意思是后面的参数在表中占据的列数
            return ps.executeUpdate();
        } catch (SQLException e) {
            return 0;
        }
    }

    @Override
    public int insert(location location) {
        if(loc(location)){
            String sql="update location set statue = ?,latitude = ?,longitude =? where name=?";
            try(Connection conn = getConnection()) {
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, 1);
                ps.setDouble(2, location.getLatitude());
                ps.setDouble(3, location.getLongitude());
                ps.setString(4, location.getName());//第一个数的意思是后面的参数在表中占据的列数
                return ps.executeUpdate();
            } catch (SQLException e) {
                return 0;
            }
        }else{
            String sql = "insert into location (name,latitude,longitude,description,statue)"+ "values(?,?,?,?,?)";
            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try{
                conn = getConnection();
                ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, location.getName());
                ps.setDouble(2, location.getLatitude());
                ps.setDouble(3, location.getLongitude());
                ps.setString(4, location.getDescription());
                ps.setInt(5,1);
                if(ps.executeUpdate()==0){
                    throw new SQLException("创建地点失败，没有行受影响");
                }
                try(ResultSet keys = ps.getGeneratedKeys()) {
                    if(keys.next()){
                        location.setId(keys.getInt(1));
                        return location.getId();
                    } else{
                        return 0;
                    }
                }
            } catch (SQLException e) {
                return 0;
            }finally {
                DBUtil.close(conn, ps, rs);
            }
        }
    }
    //根据id修改数据
    @Override
    public int update(location location) {
        String sql = "update location set name=?,latitude=?,longitude=?,description=? where id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, location.getName());
            ps.setDouble(2, location.getLatitude());
            ps.setDouble(3, location.getLongitude());
            ps.setString(4, location.getDescription());
            ps.setInt(5, location.getId());
            return ps.executeUpdate();
        } catch (SQLException e) {
            return 0;
        } finally {
            DBUtil.close(conn, ps, null);
        }
    }

    @Override
    public location findById(int id) {
        String sql = "select * from location where id=? && statue=1";
        try(Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return mapResultSetToLocation(rs);
            } else{
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public location findByName(String name) {
        String sql = "select * from location where name=? && statue=1";
        try(Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return mapResultSetToLocation(rs);
            } else{
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }



    @Override
    public List<location> findAll() {
        List<location> locations = new ArrayList<>();
        String sql = "SELECT * FROM location where statue=1";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                location loc = mapResultSetToLocation(rs);
                // 确保必要字段不为null才添加到列表
                Double lat = loc.getLatitude();
                Double lng = loc.getLongitude();
                if (loc.getName() != null && lat != null && lng != null) {
                    locations.add(loc);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return locations;
    }
    protected location mapResultSetToLocation(ResultSet rs) throws SQLException {
        location loc = new location();

        // 安全地处理可能为null的字段
        String name = rs.getString("name");
        if (rs.wasNull()) name = null;
        loc.setName(name);

        Double latitude = rs.getDouble("latitude");
        if (rs.wasNull()) latitude = -1.0;
        loc.setLatitude(latitude);

        Double longitude = rs.getDouble("longitude");
        if (rs.wasNull()) longitude = -1.0;
        loc.setLongitude(longitude);

        // 处理其他可能为null的字段
        Integer id = rs.getInt("id");
        if (rs.wasNull()) id = null;
        loc.setId(id);

        String description = rs.getString("description");
        if (rs.wasNull()) description = null;
        loc.setDescription(description);

        Integer statue = rs.getInt("statue");
        if (rs.wasNull()) statue = null;
        loc.setStatue(statue);

        return loc;
    }
    public boolean locationExist(String name) {
        String sql = "select * from location where name=? && statue=1";
        try(Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public boolean addressExist(double latitude, double longitude) {
        String sql = "select * from location where latitude=? and longitude=? && statue=1";
        try(Connection conn = getConnection()) {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setDouble(1, latitude);
            ps.setDouble(2, longitude);
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
    private boolean loc(location location) {
        String sql = "select * from location where name=? ";
        try (Connection conn = getConnection()){
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, location.getName());
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
