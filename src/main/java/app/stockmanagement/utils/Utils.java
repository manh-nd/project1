package app.stockmanagement.utils;

import java.awt.Rectangle;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.swing.JTable;
import javax.swing.JViewport;

import app.stockmanagement.exceptions.DateFormatException;

public class Utils {

	public static Date parseDateInput(String date) throws DateFormatException {
		if (date == null) {
			throw new DateFormatException("Bạn chưa nhập hạn sử dụng!");
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		try {
			long dateInput = dateFormat.parse(date).getTime();
			long currentDate = System.currentTimeMillis();
			if (dateInput <= currentDate) {
				throw new DateFormatException("Không thể nhập hạn sử dụng trước ngày hôm nay được!");
			}

			String temp[] = date.split("/");
			String day = temp[0];
			String month = temp[1];
			String year = temp[2];

			int dd = Integer.parseInt(day);
			int MM = Integer.parseInt(month);
			int yyyy = Integer.parseInt(year);

			switch (MM) {
			case 1:
			case 3:
			case 5:
			case 7:
			case 8:
			case 10:
			case 12:
				if (dd < 1 || dd > 31) {
					throw new DateFormatException(
							"Ngày không hợp lệ!\n[Tháng " + MM + " năm " + yyyy + "] sẽ bắt đầu từ ngày 1 đến ngày 31");
				}
				break;
			case 2:
				if ((yyyy % 400 == 0 && yyyy % 100 != 0) || yyyy % 400 == 0) {
					if (dd < 1 || dd > 29) {
						throw new DateFormatException("Ngày không hợp lệ!\n[Tháng " + MM + " năm " + yyyy
								+ "] sẽ bắt đầu từ ngày 1 đến ngày 29");
					}
				} else {
					if (dd < 1 || dd > 28) {
						throw new DateFormatException("Ngày không hợp lệ!\n[Tháng " + MM + " năm " + yyyy
								+ "] sẽ bắt đầu từ ngày 1 đến ngày 28");
					}
				}
				break;
			case 4:
			case 6:
			case 9:
			case 11:
				if (dd < 1 || dd > 30) {
					throw new DateFormatException(
							"Ngày không hợp lệ!\n[Tháng " + MM + " năm " + yyyy + "] sẽ bắt đầu từ ngày 1 đến ngày 30");
				}
				break;
			default:
				throw new DateFormatException("Tháng không hợp lệ!\nKhông có tháng " + MM);
			}
			return new Date(dateInput);
		} catch (ParseException e) {
			throw new DateFormatException("Hạn sử dụng không hợp lệ!");
		}
	}

	public static void scrollToVisible(JTable table, int rowIndex, int vColIndex) {
		if (!(table.getParent() instanceof JViewport)) {
			return;
		}
		Rectangle rect = table.getCellRect(rowIndex, vColIndex, true);
		table.scrollRectToVisible(rect);
		if (rowIndex > -1) {
			table.setRowSelectionInterval(rowIndex, rowIndex);
		}
	}

}
