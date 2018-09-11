package app.stockmanagement.daos;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
/**
 * 
 * @author Nguyen Duc Manh
 *
 * @manhn
 * 
 */
public class DAOFactory {
	
	private static Connection connection;
	
	private static int queryCount = 0;
	
	private static CategoryDAO categoryDAO;
	private static StockDAO stockDAO;
	private static ManufacturerDAO manufacturerDAO;
	private static GoodsDAO goodsDAO;

	public static Connection openConnection() throws SQLException {
		try {
			if (connection == null || connection.isClosed()) {
				System.err.println("## Mở 1 connection mới...");
				Class.forName("com.mysql.jdbc.Driver");
				
				Properties props = new Properties();
				props.load(ClassLoader.getSystemResourceAsStream("app/stockmanagement/dbinfo/db.properties"));
				
				String user = props.getProperty("user");
				String password = props.getProperty("password");
				String dburl = props.getProperty("dburl");
				
				connection =  DriverManager.getConnection(dburl, user, password);
			}else {
				System.err.println("## Sử dụng connection hiện tại đang mở...");
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
			throw new SQLException("Kết nối tới CSDL thất bại!");
		}
		return connection;
	}

	public static void disconect() {
		try {
			if (connection != null) {
				connection.close();
				System.err.println("## Đã đóng kết nối!");
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}
	
	public static void increaseQueryCount() {
		queryCount++;
	}
	
	public static int getQueryCount() {
		return queryCount;
	}

	public static StockDAO getStockDAO() {
		if(stockDAO==null) {
			stockDAO = new StockDAO();
		}
		return stockDAO;
	}

	public static GoodsDAO getGoodsDAO() {
		if(goodsDAO==null) {
			goodsDAO = new GoodsDAO();
		}
		return goodsDAO;
	}

	public static ManufacturerDAO getManufacturerDAO() {
		if(manufacturerDAO==null) {
			manufacturerDAO = new ManufacturerDAO();
		}
		return manufacturerDAO;
	}

	public static CategoryDAO getCategoryDAO() {
		if(categoryDAO==null) {
			categoryDAO = new CategoryDAO();
		}
		return categoryDAO;
	}

}
