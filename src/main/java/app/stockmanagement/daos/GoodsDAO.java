package app.stockmanagement.daos;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import app.stockmanagement.exceptions.DuplicateValueException;
import app.stockmanagement.models.Category;
import app.stockmanagement.models.Goods;
import app.stockmanagement.models.Manufacturer;
import app.stockmanagement.models.Stock;

/**
 * 
 * @author Nguyen Duc Manh
 *
 * @manhn
 */
public class GoodsDAO implements DAO<Goods> {

	@Override
	public int create(Goods g) throws SQLException, DuplicateValueException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			Goods goods = get(g.getCode());
			if (goods != null) {
				throw new DuplicateValueException(
						"Trùng mã [" + g.getCode() + "] ở trong kho [" + goods.getStock().getName() + "].");
			}
			String sql = "INSERT INTO "
					+ "Goods(GoodsCode, GoodsName, ExpiryDate, ImportedPrice, ExportedPrice, InStock, CategoryID, ManufacturerID, StockID) "
					+ "VALUES (?,?,?,?,?,?,?,?,?)";

			conn = DAOFactory.openConnection();
			pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			pstmt.setString(1, g.getCode());
			pstmt.setString(2, g.getName());
			pstmt.setDate(3, g.getExpiryDate());
			pstmt.setDouble(4, g.getImportedPrice());
			pstmt.setDouble(5, g.getExportedPrice());
			pstmt.setInt(6, g.getInStock());
			pstmt.setInt(7, g.getCategory().getId());
			pstmt.setInt(8, g.getManufacturer().getId());
			pstmt.setInt(9, g.getStock().getId());
			int rowAffected = pstmt.executeUpdate();
			if (rowAffected > 0) {
				DAOFactory.increaseQueryCount();
				System.out.printf("%d>> JDBC: %s\n", DAOFactory.getQueryCount(), sql);
				rs = pstmt.getGeneratedKeys();
				while (rs.next()) {
					return rs.getInt(1);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (pstmt != null) {
				pstmt.close();
			}
		}
		return 0;
	}

	@Override
	public boolean update(Goods g) throws SQLException, DuplicateValueException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		boolean success = false;
		try {
			Goods goods = get(g.getCode());
			if (goods != null) {
				if (goods.getId() != g.getId())
					throw new DuplicateValueException(
							"Trùng mã [" + g.getCode() + "] ở trong kho [" + goods.getStock().getName() + "].");
			}
			String sql = "UPDATE Goods SET GoodsCode = ?, GoodsName = ?, ExpiryDate = ?, ImportedPrice = ?, "
					+ "ExportedPrice = ?, InStock = ?, CategoryID = ?, ManufacturerID = ?, StockID = ? WHERE GoodsID = ?";

			conn = DAOFactory.openConnection();
			System.out.println(g.toString());
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, g.getCode());
			pstmt.setString(2, g.getName());
			pstmt.setDate(3, g.getExpiryDate());
			pstmt.setDouble(4, g.getImportedPrice());
			pstmt.setDouble(5, g.getExportedPrice());
			pstmt.setInt(6, g.getInStock());
			pstmt.setInt(7, g.getCategory().getId());
			pstmt.setInt(8, g.getManufacturer().getId());
			pstmt.setInt(9, g.getStock().getId());
			pstmt.setInt(10, g.getId());
			int rowAffected = pstmt.executeUpdate();
			if (rowAffected == 1) {
				DAOFactory.increaseQueryCount();
				System.out.printf("%d>> JDBC: %s\n", DAOFactory.getQueryCount(), sql);
				success = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (pstmt != null) {
				pstmt.close();
			}
		}
		return success;
	}

	@Override
	public boolean delete(Goods g) throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		boolean success = false;
		try {
			conn = DAOFactory.openConnection();
			String sql = "DELETE FROM Goods WHERE GoodsID = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, g.getId());
			int rowAffected = pstmt.executeUpdate();
			pstmt.close();
			if (rowAffected == 1) {
				success = true;
			}
			DAOFactory.increaseQueryCount();
			System.out.printf("%d>> JDBC: %s\n", DAOFactory.getQueryCount(), sql);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (pstmt != null) {
				pstmt.close();
			}
		}
		return success;
	}

	@Override
	public Goods get(int id) throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = DAOFactory.openConnection();
			String sql = "SELECT * FROM Goods WHERE GoodsID = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, id);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				Goods g = new Goods();
				g.setId(rs.getInt("GoodsID"));
				g.setCode(rs.getString("GoodsCode"));
				g.setName(rs.getString("GoodsName"));
				g.setExpiryDate(rs.getDate("ExpiryDate"));
				g.setImportedPrice(rs.getInt("ImportedPrice"));
				g.setExportedPrice(rs.getInt("ExportedPrice"));
				g.setInStock(rs.getInt("InStock"));
				g.setManufacturer(DAOFactory.getManufacturerDAO().get(rs.getInt("ManufacturerID")));
				g.setStock(DAOFactory.getStockDAO().get(rs.getInt("StockID")));
				g.setCategory(DAOFactory.getCategoryDAO().get(rs.getInt("CategoryID")));
				DAOFactory.increaseQueryCount();
				System.out.printf("%d>> JDBC: %s\n", DAOFactory.getQueryCount(), sql);
				return g;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (pstmt != null) {
				pstmt.close();
			}
		}
		return null;
	}

	@Override
	public Goods get(String uniqueColumn) throws SQLException {
		if (uniqueColumn == null)
			throw new SQLException("Bạn không thể để trống tham số truyền vào!");
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = DAOFactory.openConnection();
			String sql = "SELECT * FROM Goods WHERE GoodsCode = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, uniqueColumn);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				Goods g = new Goods();
				g.setId(rs.getInt("GoodsID"));
				g.setCode(rs.getString("GoodsCode"));
				g.setName(rs.getString("GoodsName"));
				g.setExpiryDate(rs.getDate("ExpiryDate"));
				g.setImportedPrice(rs.getInt("ImportedPrice"));
				g.setExportedPrice(rs.getInt("ExportedPrice"));
				g.setInStock(rs.getInt("InStock"));
				g.setManufacturer(DAOFactory.getManufacturerDAO().get(rs.getInt("ManufacturerID")));
				g.setStock(DAOFactory.getStockDAO().get(rs.getInt("StockID")));
				g.setCategory(DAOFactory.getCategoryDAO().get(rs.getInt("CategoryID")));
				DAOFactory.increaseQueryCount();
				System.out.printf("%d>> JDBC: %s\n", DAOFactory.getQueryCount(), sql);
				return g;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (pstmt != null) {
				pstmt.close();
			}
		}
		return null;
	}

	@Override
	public List<Goods> getAll() throws SQLException {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		List<Goods> list = new ArrayList<>();
		try {
			conn = DAOFactory.openConnection();
			String sql = "SELECT * FROM Goods";
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				int id = rs.getInt("GoodsID");
				String code = rs.getString("GoodsCode");
				String name = rs.getString("GoodsName");
				Date expiryDate = rs.getDate("ExpiryDate");
				int importedPrice = rs.getInt("ImportedPrice");
				int exportedPrice = rs.getInt("ExportedPrice");
				int inStock = rs.getInt("InStock");
				Category category = DAOFactory.getCategoryDAO().get(rs.getInt("CategoryID"));
				Manufacturer manufacturer = DAOFactory.getManufacturerDAO().get(rs.getInt("ManufacturerID"));
				Stock stock = DAOFactory.getStockDAO().get(rs.getInt("StockID"));
				list.add(new Goods(id, code, name, expiryDate, importedPrice, exportedPrice, inStock, category,
						manufacturer, stock));
			}
			DAOFactory.increaseQueryCount();
			System.out.printf("%d>> JDBC: %s\n", DAOFactory.getQueryCount(), sql);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (stmt != null) {
				stmt.close();
			}
		}
		return list;
	}

	@Override
	public List<Goods> getAll(OrderBy orderBy) throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<Goods> list = new ArrayList<>();
		try {
			conn = DAOFactory.openConnection();
			String sql = "";
			if (orderBy != null) {
				String ob = "";
				switch (orderBy) {
				case CATEGORY:
					ob = "Categories.CategoryID";
					break;
				case MANUFACTURER:
					ob = "Manufacturers.ManufacturerID";
					break;
				}
				sql = "SELECT * FROM Goods inner join Stocks on Goods.StockID = Stocks.StockID "
						+ "inner join Categories on Goods.CategoryID = Categories.CategoryID "
						+ "inner join Manufacturers on Goods.ManufacturerID = Manufacturers.ManufacturerID "
						+ "WHERE Manufacturers.active = 1 and Categories.active = 1 and Stocks.active = 1 and Goods.StockID = ? ORDER BY " + ob;
			}
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				int id = rs.getInt("GoodsID");
				String code = rs.getString("GoodsCode");
				String name = rs.getString("GoodsName");
				Date expiryDate = rs.getDate("ExpiryDate");
				int importedPrice = rs.getInt("ImportedPrice");
				int exportedPrice = rs.getInt("ExportedPrice");
				int inStock = rs.getInt("InStock");
				Category category = DAOFactory.getCategoryDAO().get(rs.getInt("CategoryID"));
				Manufacturer manufacturer = DAOFactory.getManufacturerDAO().get(rs.getInt("ManufacturerID"));
				Stock stock = DAOFactory.getStockDAO().get(rs.getInt("StockID"));
				list.add(new Goods(id, code, name, expiryDate, importedPrice, exportedPrice, inStock, category,
						manufacturer, stock));
			}
			DAOFactory.increaseQueryCount();
			System.out.printf("%d>> JDBC: %s\n", DAOFactory.getQueryCount(), sql);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (pstmt != null) {
				pstmt.close();
			}
		}
		return list;
	}

	public List<Goods> getGoodsInStock(Stock s, OrderBy orderBy) throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<Goods> list = new ArrayList<>();
		try {
			conn = DAOFactory.openConnection();
			String sql = "";
			if (orderBy != null) {
				String ob = "";
				switch (orderBy) {
				case CATEGORY:
					ob = "Categories.CategoryID";
					break;
				case MANUFACTURER:
					ob = "Manufacturers.ManufacturerID";
					break;
				}
				sql = "SELECT * FROM Goods inner join Stocks on Goods.StockID = Stocks.StockID "
						+ "inner join Categories on Goods.CategoryID = Categories.CategoryID "
						+ "inner join Manufacturers on Goods.ManufacturerID = Manufacturers.ManufacturerID "
						+ "WHERE Manufacturers.active = 1 and Categories.active = 1 and Stocks.active = 1 and Goods.StockID = ? ORDER BY " + ob;
			} else {
				sql = "SELECT * FROM Goods inner join Stocks on Goods.StockID = Stocks.StockID "
						+ "inner join Categories on Goods.CategoryID = Categories.CategoryID "
						+ "inner join Manufacturers on Goods.ManufacturerID = Manufacturers.ManufacturerID "
						+ "WHERE Manufacturers.active = 1 and Categories.active = 1 and Stocks.active = 1 and Goods.StockID = ?";
			}
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, s.getId());
			rs = pstmt.executeQuery();
			while (rs.next()) {
				int id = rs.getInt("GoodsID");
				String code = rs.getString("GoodsCode");
				String name = rs.getString("GoodsName");
				Date expiryDate = rs.getDate("ExpiryDate");
				int importedPrice = rs.getInt("ImportedPrice");
				int exportedPrice = rs.getInt("ExportedPrice");
				int inStock = rs.getInt("InStock");
				Category category = DAOFactory.getCategoryDAO().get(rs.getInt("CategoryID"));
				Manufacturer manufacturer = DAOFactory.getManufacturerDAO().get(rs.getInt("ManufacturerID"));
				Stock stock = DAOFactory.getStockDAO().get(rs.getInt("StockID"));
				list.add(new Goods(id, code, name, expiryDate, importedPrice, exportedPrice, inStock, category,
						manufacturer, stock));
			}
			DAOFactory.increaseQueryCount();
			System.out.printf("%d>> JDBC: %s\n", DAOFactory.getQueryCount(), sql);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (pstmt != null) {
				pstmt.close();
			}
		}
		return list;
	}

	public List<Goods> getGoodsInStock(Stock s, String search, OrderBy orderBy) throws SQLException {
		if (s == null)
			throw new SQLException("Stock null");
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<Goods> list = new ArrayList<>();
		try {
			conn = DAOFactory.openConnection();
			String sql = "";
			if (search == null || search.trim().isEmpty()) {
				if (orderBy != null) {
					String ob = "";
					switch (orderBy) {
					case CATEGORY:
						ob = "CategoryID";
						break;
					case MANUFACTURER:
						ob = "ManufacturerID";
						break;
					}
					sql = "SELECT * FROM Goods WHERE StockID = ? ORDER BY " + ob;
				} else {
					sql = "SELECT * FROM Goods WHERE StockID = ?";
				}
				pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, s.getId());
			} else {
				search = search.replaceAll("\\s+", " ").trim();
				if (orderBy != null) {
					String ob = "";
					switch (orderBy) {
					case CATEGORY:
						ob = "CategoryID";
						break;
					case MANUFACTURER:
						ob = "ManufacturerID";
						break;
					}
					sql = "SELECT * FROM Goods WHERE StockID = ? AND (GoodsCode like ? OR GoodsName like ?) ORDER BY "
							+ ob;
				} else {
					sql = "SELECT * FROM Goods WHERE StockID = ? AND (GoodsCode like ? OR GoodsName like ?)";
				}
				pstmt = conn.prepareStatement(sql);
				pstmt.setInt(1, s.getId());
				pstmt.setString(2, "%" + search + "%");
				pstmt.setString(3, "%" + search + "%");
			}
			rs = pstmt.executeQuery();
			while (rs.next()) {
				int id = rs.getInt("GoodsID");
				String code = rs.getString("GoodsCode");
				String name = rs.getString("GoodsName");
				Date expiryDate = rs.getDate("ExpiryDate");
				int importedPrice = rs.getInt("ImportedPrice");
				int exportedPrice = rs.getInt("ExportedPrice");
				int inStock = rs.getInt("InStock");
				Category category = DAOFactory.getCategoryDAO().get(rs.getInt("CategoryID"));
				Manufacturer manufacturer = DAOFactory.getManufacturerDAO().get(rs.getInt("ManufacturerID"));
				Stock stock = DAOFactory.getStockDAO().get(rs.getInt("StockID"));
				list.add(new Goods(id, code, name, expiryDate, importedPrice, exportedPrice, inStock, category,
						manufacturer, stock));
			}
			DAOFactory.increaseQueryCount();
			System.out.printf("%d>> JDBC: %s\n", DAOFactory.getQueryCount(), sql);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (pstmt != null) {
				pstmt.close();
			}
		}
		return list;
	}

	@Override
	public List<Goods> find(String search) throws SQLException {
		if (search == null) {
			search = "";
		}
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<Goods> list = new ArrayList<>();
		try {
			conn = DAOFactory.openConnection();
			String sql = "SELECT * FROM Goods WHERE GoodsCode like ? OR GoodsName like ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, "%" + search + "%");
			pstmt.setString(2, "%" + search + "%");
			rs = pstmt.executeQuery();
			while (rs.next()) {
				int id = rs.getInt("GoodsID");
				String code = rs.getString("GoodsCode");
				String name = rs.getString("GoodsName");
				Date expiryDate = rs.getDate("ExpiryDate");
				int importedPrice = rs.getInt("ImportedPrice");
				int exportedPrice = rs.getInt("ExportedPrice");
				int inStock = rs.getInt("InStock");
				Category category = DAOFactory.getCategoryDAO().get(rs.getInt("CategoryID"));
				Manufacturer manufacturer = DAOFactory.getManufacturerDAO().get(rs.getInt("ManufacturerID"));
				Stock stock = DAOFactory.getStockDAO().get(rs.getInt("StockID"));
				list.add(new Goods(id, code, name, expiryDate, importedPrice, exportedPrice, inStock, category,
						manufacturer, stock));
			}
			DAOFactory.increaseQueryCount();
			System.out.printf("%d>> JDBC: %s\n", DAOFactory.getQueryCount(), sql);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (pstmt != null) {
				pstmt.close();
			}
		}
		return list;
	}

	@Override
	public List<Goods> find(String search, OrderBy orderBy) throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<Goods> list = new ArrayList<>();
		try {
			conn = DAOFactory.openConnection();
			String sql = "";
			if (search == null || search.trim().isEmpty()) {
				if (orderBy != null) {
					String ob = "";
					switch (orderBy) {
					case CATEGORY:
						ob = "CategoryID";
						break;
					case MANUFACTURER:
						ob = "ManufacturerID";
						break;
					}
					sql = "SELECT * FROM Goods ORDER BY " + ob;
				} else {
					sql = "SELECT * FROM Goods";
				}
				pstmt = conn.prepareStatement(sql);
			} else {
				search = search.replaceAll("\\s+", " ").trim();
				if (orderBy != null) {
					String ob = "";
					switch (orderBy) {
					case CATEGORY:
						ob = "CategoryID";
						break;
					case MANUFACTURER:
						ob = "ManufacturerID";
						break;
					}
					sql = "SELECT * FROM Goods WHERE GoodsCode like ? OR GoodsName like ? ORDER BY " + ob;
				} else {
					sql = "SELECT * FROM Goods WHERE GoodsCode like ? OR GoodsName like ?";
				}
				pstmt = conn.prepareStatement(sql);
				pstmt.setString(1, "%" + search + "%");
				pstmt.setString(2, "%" + search + "%");
			}
			rs = pstmt.executeQuery();
			while (rs.next()) {
				int id = rs.getInt("GoodsID");
				String code = rs.getString("GoodsCode");
				String name = rs.getString("GoodsName");
				Date expiryDate = rs.getDate("ExpiryDate");
				int importedPrice = rs.getInt("ImportedPrice");
				int exportedPrice = rs.getInt("ExportedPrice");
				int inStock = rs.getInt("InStock");
				Category category = DAOFactory.getCategoryDAO().get(rs.getInt("CategoryID"));
				Manufacturer manufacturer = DAOFactory.getManufacturerDAO().get(rs.getInt("ManufacturerID"));
				Stock stock = DAOFactory.getStockDAO().get(rs.getInt("StockID"));
				list.add(new Goods(id, code, name, expiryDate, importedPrice, exportedPrice, inStock, category,
						manufacturer, stock));
			}
			DAOFactory.increaseQueryCount();
			System.out.printf("%d>> JDBC: %s\n", DAOFactory.getQueryCount(), sql);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (rs != null) {
				rs.close();
			}
			if (pstmt != null) {
				pstmt.close();
			}
		}
		return list;
	}

}
