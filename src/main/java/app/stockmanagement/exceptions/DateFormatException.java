package app.stockmanagement.exceptions;

public class DateFormatException extends Exception {

	private static final long serialVersionUID = 1L;

	public DateFormatException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public DateFormatException(String message, Throwable cause) {
		super(message, cause);
	}

	public DateFormatException(String message) {
		super(message);
	}

	public DateFormatException(Throwable cause) {
		super(cause);
	}

	
}
