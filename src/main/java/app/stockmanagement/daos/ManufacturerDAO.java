package app.stockmanagement.daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import app.stockmanagement.exceptions.DuplicateValueException;
import app.stockmanagement.models.Manufacturer;

/**
 * 
 * @author Nguyen Duc Manh
 *
 * @manhn
 */
public class ManufacturerDAO implements DAO<Manufacturer> {

	@Override
	public int create(Manufacturer m) throws SQLException, DuplicateValueException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = DAOFactory.openConnection();
			Manufacturer manufacturer = get(m.getName());
			if( manufacturer!=null){
				throw new DuplicateValueException("Trùng tên nhà sản xuất!");
			}
			String sql = "INSERT INTO Manufacturers(ManufacturerName) VALUES (?)";
			
			pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			pstmt.setString(1, m.getName());
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
	public boolean update(Manufacturer m) throws SQLException, DuplicateValueException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			Manufacturer manufacturer = get(m.getName());
			if (manufacturer != null) {
				if (m.getId() != manufacturer.getId()) {
					throw new DuplicateValueException("Trùng tên hãng sản xuất [" + m.getName() +"].");
				}
			}
			String sql = "UPDATE Manufacturers SET ManufacturerName = ? WHERE ManufacturerID = ?";
			
			conn = DAOFactory.openConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, m.getName());
			pstmt.setInt(2, m.getId());
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
	public boolean delete(Manufacturer m) throws SQLException {
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = DAOFactory.openConnection();
			String sql = "UPDATE Manufacturers SET active = 0 WHERE ManufacturerID = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, m.getId());
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
	public Manufacturer get(int id) throws SQLException {
		Manufacturer m = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = DAOFactory.openConnection();
			String sql = "SELECT * FROM Manufacturers WHERE ManufacturerID = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, id);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				m = new Manufacturer(rs.getInt(1), rs.getString(2), rs.getBoolean(3));
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
		return m;
	}
	
	@Override
	public Manufacturer get(String uniqueColumn) throws SQLException {
		if (uniqueColumn == null)
			throw new SQLException("Bạn không thể để trống tham số truyền vào!");
		Manufacturer m = null;
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = DAOFactory.openConnection();
			String sql = "SELECT * FROM Manufacturers WHERE ManufacturerName = ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, uniqueColumn);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				m = new Manufacturer(rs.getInt(1), rs.getString(2), rs.getBoolean(3));
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
		return m;
	}

	@Override
	public List<Manufacturer> find(String search) throws SQLException {
		if (search == null)
			throw new SQLException("Bạn không thể để trống tham số truyền vào!");
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<Manufacturer> list = new ArrayList<>();
		try {
			conn = DAOFactory.openConnection();
			String sql = "SELECT * FROM Manufacturers WHERE ManufacturerName like ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, "%" + search + "%");
			rs = pstmt.executeQuery();
			while (rs.next()) {
				int id = rs.getInt(1);
				String name = rs.getString(2);
				boolean active = rs.getBoolean(3);
				list.add(new Manufacturer(id, name, active));
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
	public List<Manufacturer> find(String property, OrderBy orderBy) throws SQLException {
		if (property == null)
			throw new SQLException("Bạn không thể để trống tham số truyền vào!");
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<Manufacturer> list = new ArrayList<>();
		try {
			conn = DAOFactory.openConnection();
			String sql = "SELECT * FROM Manufacturers WHERE ManufacturerName like ? ORDER BY ?";
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, "%" + property + "%");
			pstmt.setString(2, "%" + orderBy + "%");
			rs = pstmt.executeQuery();
			while (rs.next()) {
				int id = rs.getInt(1);
				String name = rs.getString(2);
				boolean active = rs.getBoolean(3);
				list.add(new Manufacturer(id, name, active));
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
	public List<Manufacturer> getAll(OrderBy orderBy) throws SQLException {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		List<Manufacturer> list = new ArrayList<>();
		try {
			conn = DAOFactory.openConnection();
			String sql = "SELECT * FROM Manufacturers";
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				int id = rs.getInt(1);
				String name = rs.getString(2);
				boolean active = rs.getBoolean(3);
				list.add(new Manufacturer(id, name, active));
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
	public List<Manufacturer> getAll() throws SQLException {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		List<Manufacturer> list = new ArrayList<>();
		try {
			conn = DAOFactory.openConnection();
			String sql = "SELECT * FROM Manufacturers WHERE active = 1";
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			while (rs.next()) {
				int id = rs.getInt(1);
				String name = rs.getString(2);
				boolean active = rs.getBoolean(3);
				list.add(new Manufacturer(id, name, active));
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
