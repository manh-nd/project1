package app.stockmanagement.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import app.stockmanagement.exceptions.DuplicateValueException;
import app.stockmanagement.models.Stock;

/**
 * 
 * @author Nguyen Duc Manh
 *
 * @manhn
 */
public class StockDAO implements DAO<Stock> {

	@Override
	public int create(Stock s) throws SQLException, DuplicateValueException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			Stock stock = get(s.getName());
			if (stock != null) {
				throw new DuplicateValueException("Tên kho hàng này đã tồn tại!");
			}
			String sql = "INSERT INTO Stocks(StockName) VALUES (?)";

			conn = DAOFactory.openConnection();
			pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			pstmt.setString(1, s.getName());
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
	public boolean update(Stock s) throws SQLException, DuplicateValueException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			Stock stock = get(s.getName());
			if (stock != null) {
				if (s.getId() != stock.getId()) {
					throw new DuplicateValueException("Trùng tên kho hàng [" + s.getName() + "].");
				}
			}
			String sql = "UPDATE Stocks SET StockName = ? WHERE StockID = ?";

			conn = DAOFactory.openConnection();
			pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			pstmt.setString(1, s.getName());
			pstmt.setInt(2, s.getId());
			int rowAffected = pstmt.executeUpdate();
			if (rowAffected > 0) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (pstmt != null) {
				pstmt.close();
			}
		}
		return false;
	}

	@Override
	public boolean delete(Stock s) throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = DAOFactory.openConnection();
			String sql = "UPDATE Stocks SET active = 0 WHERE StockID = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, s.getId());
			int rowAffected = pstmt.executeUpdate();
			if (rowAffected > 0) {
				DAOFactory.increaseQueryCount();
				System.out.printf("%d>> JDBC: %s\n", DAOFactory.getQueryCount(), sql);
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (pstmt != null) {
				pstmt.close();
			}
		}
		return false;
	}

	@Override
	public Stock get(int id) throws SQLException {
		Stock s = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = DAOFactory.openConnection();
			String sql = "SELECT * FROM Stocks WHERE StockID = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, id);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				s = new Stock(rs.getInt(1), rs.getString(2), rs.getBoolean(3));
			}
			DAOFactory.increaseQueryCount();
			System.out.printf("%d>> JDBC: %s\n", DAOFactory.getQueryCount(), sql);
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
		return s;
	}

	@Override
	public Stock get(String uniqueColumn) throws SQLException {
		if (uniqueColumn == null)
			throw new SQLException("Bạn không thể để trống tham số truyền vào!");
		Stock s = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = DAOFactory.openConnection();
			String sql = "SELECT * FROM Stocks WHERE StockName = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, uniqueColumn);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				s = new Stock(rs.getInt(1), rs.getString(2), rs.getBoolean(3));
			}
			DAOFactory.increaseQueryCount();
			System.out.printf("%d>> JDBC: %s\n", DAOFactory.getQueryCount(), sql);
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
		return s;
	}

	@Override
	public List<Stock> find(String search) throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<Stock> list = new ArrayList<>();
		try {
			conn = DAOFactory.openConnection();
			String sql = "SELECT * FROM Stocks WHERE StockName like ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, "%" + search + "%");
			rs = pstmt.executeQuery();
			while (rs.next()) {
				int id = rs.getInt(1);
				String name = rs.getString(2);
				boolean active = rs.getBoolean(3);
				list.add(new Stock(id, name, active));
			}
			DAOFactory.increaseQueryCount();
			System.out.printf("%d>> JDBC: %s\n", DAOFactory.getQueryCount(), sql);
		} catch (Exception e) {
			throw e;
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
	public List<Stock> find(String property, OrderBy orderBy) throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<Stock> list = new ArrayList<>();
		try {
			conn = DAOFactory.openConnection();
			String sql = "SELECT * FROM Stocks WHERE StockName like ? ORDER BY ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, "%" + property + "%");
			pstmt.setString(2, "%" + orderBy + "%");
			rs = pstmt.executeQuery();
			while (rs.next()) {
				int id = rs.getInt(1);
				String name = rs.getString(2);
				boolean active = rs.getBoolean(3);
				list.add(new Stock(id, name, active));
			}
			DAOFactory.increaseQueryCount();
			System.out.printf("%d>> JDBC: %s\n", DAOFactory.getQueryCount(), sql);
		} catch (Exception e) {
			throw e;
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
	public List<Stock> getAll() throws SQLException {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		List<Stock> list = new ArrayList<>();
		try {
			conn = DAOFactory.openConnection();
			String sql = "SELECT * FROM Stocks WHERE active = 1";
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				int id = rs.getInt(1);
				String name = rs.getString(2);
				boolean active = rs.getBoolean(3);
				list.add(new Stock(id, name, active));
			}
			DAOFactory.increaseQueryCount();
			System.out.printf("%d>> JDBC: %s\n", DAOFactory.getQueryCount(), sql);
		} catch (Exception e) {
			throw e;
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
		return list;
	}

	@Override
	public List<Stock> getAll(OrderBy orderby) throws SQLException {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		List<Stock> list = new ArrayList<>();
		try {
			conn = DAOFactory.openConnection();
			String sql = "SELECT * FROM Stocks";
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				int id = rs.getInt(1);
				String name = rs.getString(2);
				boolean active = rs.getBoolean(3);
				list.add(new Stock(id, name, active));
			}
			DAOFactory.increaseQueryCount();
			System.out.printf("%d>> JDBC: %s\n", DAOFactory.getQueryCount(), sql);
		} catch (Exception e) {
			throw e;
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
		return list;
	}

	public List<Stock> getStocks(boolean havingGoods) throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<Stock> list = new ArrayList<>();
		try {
			conn = DAOFactory.openConnection();
			String sql = "";
			if (havingGoods) {
				sql = "SELECT distinct goods.StockID, StockName, active FROM stocks inner join goods on stocks.StockID = goods.StockID WHERE InStock > 0 and active = 1";
				pstmt = conn.prepareStatement(sql);
			} else {
				sql = "SELECT distinct stocks.StockID, StockName, active FROM stocks WHERE active = 1";
				pstmt = conn.prepareStatement(sql);
			}
			rs = pstmt.executeQuery();
			while (rs.next()) {
				int id = rs.getInt(1);
				String name = rs.getString(2);
				boolean active = rs.getBoolean(3);
				list.add(new Stock(id, name, active));
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
