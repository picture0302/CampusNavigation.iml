package DAO;

import java.nio.file.Path;
import java.util.List;

public interface BaseDao <E>{
    int insert(E e);
    int update(E e);
    int delete(int id);
    E findById(int id);
    List<E> findAll();
}
