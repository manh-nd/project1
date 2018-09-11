package app.stockmanagement.controllers;

import java.sql.SQLException;
import java.util.List;

import app.stockmanagement.daos.DAOFactory;
import app.stockmanagement.daos.OrderBy;
import app.stockmanagement.exceptions.DuplicateValueException;
import app.stockmanagement.models.Category;
import app.stockmanagement.models.Goods;
import app.stockmanagement.models.Manufacturer;
import app.stockmanagement.models.Stock;

public class Controller {
	
	public static List<Stock> loadStocks(boolean havingGoods) throws SQLException {
		return DAOFactory.getStockDAO().getStocks(havingGoods);
	}
	
	public static List<Category> loadCategories() throws SQLException {
		return DAOFactory.getCategoryDAO().getAll();
	}
	
	public static List<Manufacturer> loadManufacturers() throws SQLException {
		return DAOFactory.getManufacturerDAO().getAll();
	}
	
	public static List<Goods> loadGoodsInSock(Stock s, OrderBy orderBy) throws SQLException{
		return DAOFactory.getGoodsDAO().getGoodsInStock(s, orderBy);
	}
	
	public static int createGoods(Goods g) throws SQLException, DuplicateValueException {
		return DAOFactory.getGoodsDAO().create(g);
	}
	
	public static boolean updateGoods(Goods g) throws SQLException, DuplicateValueException {
		return DAOFactory.getGoodsDAO().update(g);
	}
	
	public static boolean deleteGoods(Goods g) throws SQLException {
		return DAOFactory.getGoodsDAO().delete(g);
	}
	
	public static int createStock(Stock s) throws SQLException, DuplicateValueException {
		return DAOFactory.getStockDAO().create(s);
	}
	
	public static boolean updateStock(Stock s) throws SQLException, DuplicateValueException {
		return DAOFactory.getStockDAO().update(s);
	}
	
	public static int createCategory(Category c) throws SQLException, DuplicateValueException {
		return DAOFactory.getCategoryDAO().create(c);
	}
	
	public static boolean updateCategory(Category c) throws SQLException, DuplicateValueException {
		return DAOFactory.getCategoryDAO().update(c);
	}
	
	public static int createManufacturer(Manufacturer m) throws SQLException, DuplicateValueException {
		return DAOFactory.getManufacturerDAO().create(m);
	}
	
	public static boolean updateManufacturer(Manufacturer m) throws SQLException, DuplicateValueException {
		return DAOFactory.getManufacturerDAO().update(m);
	}
	
	public static boolean deleteCategory(Category c) throws SQLException {
		return DAOFactory.getCategoryDAO().delete(c);
	}
	
	public static boolean deleteManufacturer(Manufacturer m) throws SQLException {
		return DAOFactory.getManufacturerDAO().delete(m);
	}
	
	public static boolean deleteStock(Stock s) throws SQLException {
		return DAOFactory.getStockDAO().delete(s);
	}
	
	public static Stock getStockByName(String name) throws SQLException {
		return DAOFactory.getStockDAO().get(name);
	}
	
	public static Category getCategoryByName(String name) throws SQLException {
		return DAOFactory.getCategoryDAO().get(name);
	}
	
	public static Manufacturer getManufacturerByName(String name) throws SQLException {
		return DAOFactory.getManufacturerDAO().get(name);
	}
	
	public static void exit(boolean b) {
		if(b) {
			DAOFactory.disconect();
		}
	}
	
}
