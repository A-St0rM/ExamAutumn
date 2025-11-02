package app.DAO;

import app.entities.Guide;

import java.util.List;

public interface IDAO<T, I> {

    List<T> getAll();
    T getById(I id);
    T create(T t);
    T update(T t);
    boolean delete(I id);
}
