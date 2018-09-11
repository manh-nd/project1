package app.stockmanagement.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import app.stockmanagement.exceptions.DuplicateValueException;
import app.stockmanagement.models.Category;

/**
 * 
 * @author Nguyen Duc Manh
 *
 * @manhn
 */
public class CategoryDAO implements DAO<Category> {

	@Override
	public int create(Category c) throws SQLException, DuplicateValueException {
		if (c == null) {
			throw new SQLException("Bạn không thể để trống tham số truyền vào");
		}
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			
			Category category = get(c.getName());
			if (category != null) {
				throw new DuplicateValueException("Trùng tên loại hàng [" + c.getName() +"].");
			}
			String sql = "INSERT INTO Categories(CategoryName) VALUES (?)";
			
			conn = DAOFactory.openConnection();
			pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			pstmt.setString(1, c.getName());
			int rowAffected = pstmt.executeUpdate();
			if (rowAffected > 0) {
				DAOFactory.increaseQueryCount();
				System.out.printf("%d>> JDBC: %s\n", DAOFactory.getQueryCount(), sql);
				rs = pstmt.getGeneratedKeys();
				while(rs.next()) {
					return rs.getInt(1);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if(rs!=null) {
				rs.close();
			}
			if (pstmt != null) {
				pstmt.close();
			}
		}
		return 0;
	}

	@Override
	public boolean update(Category c) throws SQLException, DuplicateValueException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			
			Category category = get(c.getName());
			if (category != null) {
				if (c.getId() != category.getId()) {
					throw new DuplicateValueException("Trùng tên loại hàng [" + c.getName() +"].");
				}
			}
			String sql = "UPDATE Categories SET CategoryName = ? WHERE CategoryID = ?";
			
			conn = DAOFactory.openConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, c.getName());
			pstmt.setInt(2, c.getId());
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
	public boolean delete(Category c) throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = DAOFactory.openConnection();
			String sql = "UPDATE Categories SET active = 0 WHERE CategoryID = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, c.getId());
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
	public Category get(int id) throws SQLException {
		Category c = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = DAOFactory.openConnection();
			String sql = "SELECT * FROM Categories WHERE CategoryID = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, id);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				c = new Category(rs.getInt(1), rs.getString(2), rs.getBoolean(3));
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
		return c;
	}

	@Override
	public Category get(String uniqueColumn) throws SQLException {
		if (uniqueColumn == null)
			throw new SQLException("Bạn không thể để trống tham số truyền vào!");
		Category c = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = DAOFactory.openConnection();
			String sql = "SELECT * FROM Categories WHERE CategoryName = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, uniqueColumn);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				c = new Category(rs.getInt(1), rs.getString(2), rs.getBoolean(3));
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
		return c;
	}

	@Override
	public List<Category> find(String search) throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<Category> list = new ArrayList<>();
		try {
			conn = DAOFactory.openConnection();
			String sql = "SELECT * FROM Categories WHERE CategoryName like ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, "%" + search + "%");
			rs = pstmt.executeQuery();
			while (rs.next()) {
				int id = rs.getInt(1);
				String name = rs.getString(2);
				boolean active = rs.getBoolean(3);
				list.add(new Category(id, name, active));
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
		return list;
	}

	@Override
	public List<Category> find(String property, OrderBy orderBy) throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<Category> list = new ArrayList<>();
		try {
			conn = DAOFactory.openConnection();
			String sql = "SELECT * FROM Categories WHERE CategoryName like ? ORDER BY ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, "%" + property + "%");
			pstmt.setString(2, "%" + orderBy + "%");
			rs = pstmt.executeQuery();
			while (rs.next()) {
				int id = rs.getInt(1);
				String name = rs.getString(2);
				boolean active = rs.getBoolean(3);
				list.add(new Category(id, name, active));
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
		return list;
	}

	@Override
	public List<Category> getAll(OrderBy orderBy) throws SQLException {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		List<Category> list = new ArrayList<>();
		try {
			conn = DAOFactory.openConnection();
			String sql = "SELECT * FROM Categories";
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				int id = rs.getInt(1);
				String name = rs.getString(2);
				boolean active = rs.getBoolean(3);
				list.add(new Category(id, name, active));
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
			if (stmt != null) {
				stmt.close();
			}
		}
		return list;
	}

	@Override
	public List<Category> getAll() throws SQLException {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		List<Category> list = new ArrayList<>();
		try {
			conn = DAOFactory.openConnection();
			String sql = "SELECT * FROM Categories WHERE active = 1";
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				int id = rs.getInt(1);
				String name = rs.getString(2);
				boolean active = rs.getBoolean(3);
				list.add(new Category(id, name, active));
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
			if (stmt != null) {
				stmt.close();
			}
		}
		return list;
	}

}
