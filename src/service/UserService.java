package service;

import DAO.UserDao;
import model.user;

public class UserService {
    public static boolean login(String username, String password) {
        if(new UserDao().usernameExist(username)){
            if(new UserDao().userExist(username, password)){
                return true;
            }else{
                return false;
            }
        } else{
            return false;
        }
    }
    public static boolean register(String username, String password) {
        if(new UserDao().usernameExist(username)){
            return false;
        }else{
            user user = new user();
            user.setUsername(username);
            user.setPassword(password);
            UserDao userDao = new UserDao();
            userDao.insert(user);
            return true;
        }
    }
    public static boolean delete(int id) {
        if(new UserDao().delete(id)!=0){
            return true;
        }else{
            return false;
        }
    }
}
