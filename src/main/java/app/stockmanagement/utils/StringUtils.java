package app.stockmanagement.utils;

import java.sql.Date;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class StringUtils {
	
	public static String formatDate(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		return dateFormat.format(date);
	}
	public static int parseInt(String number) throws Exception {
		try {
			NumberFormat format = NumberFormat.getInstance(new Locale("vi", "VN"));
			Number n = format.parse(number);
			return n.intValue();
		} catch (Exception e) {
			throw e;
		}
	}

	public static String format(int number) {
		NumberFormat format = NumberFormat.getInstance(new Locale("vi", "VN"));
		return format.format(number);
	}

	public static boolean isInteger(String interger) {
		try {
			NumberFormat format = NumberFormat.getInstance(new Locale("vi", "VN"));
			format.parse(interger);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public static boolean isNumber(String number) {
		try {
			Double.parseDouble(number);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	
	
}
