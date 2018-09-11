package app.stockmanagement.daos;

import java.sql.SQLException;
import java.util.List;

import app.stockmanagement.exceptions.DuplicateValueException;

public interface DAO<E> {

	public int create(E e) throws SQLException, DuplicateValueException;

	public boolean update(E e) throws SQLException, DuplicateValueException;

	public boolean delete(E e) throws SQLException;

	public E get(int id) throws SQLException;
	
	public E get(String uniqueColumn) throws SQLException;
	
	public List<E> getAll() throws SQLException;
		
	public List<E> getAll(OrderBy orderby) throws SQLException;

	public List<E> find(String property) throws SQLException;
	
	public List<E> find(String property, OrderBy orderBy) throws SQLException;

}
