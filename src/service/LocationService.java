package service;

import DAO.LocationDao;
import model.location;

import java.util.ArrayList;
import java.util.List;

public class LocationService {
    public static boolean addLocation(location location) {
        String name = location.getName();
        if(new LocationDao().locationExist(name)){
            return false;
        }else if(new LocationDao().addressExist(location.getLatitude(),location.getLongitude())){
            return false;
        }else{
            if(new LocationDao().insert(location)==0){
                return false;
            }else{
                return true;
            }
        }
    }
    public static boolean deleteLocation(location location) {
        if(new LocationDao().locationExist(location.getName())){
            if(new LocationDao().delete(location.getId())==0){
                return false;
            }else{
                return true;
            }
        }else{
            return true;
        }
    }
    public static boolean updateLocation(location location) {

            if(new LocationDao().update(location)==0){
                return false;
            } else{
                return true;
            }

    }
    public static location getLocation(String name) {
        location location = new LocationDao().findByName(name);
        if(location==null){
            return null;
        }else{
            return location;
        }
    }
    public static List<location> getAllLocation() {
        List<location> locations = new LocationDao().findAll();
        return locations;
    }
}
