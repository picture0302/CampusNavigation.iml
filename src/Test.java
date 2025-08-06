import DAO.LocationDao;
import DAO.PathDao;
import model.location;
import model.path;
import service.LocationService;
import service.UserService;

public class Test {
    public static void main(String[] args) {
        int id = 1;
        path l1 = new PathDao().findById(1);
        System.out.println(l1);
    }
}
