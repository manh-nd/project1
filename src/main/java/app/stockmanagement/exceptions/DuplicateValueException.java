package app.stockmanagement.exceptions;

public class DuplicateValueException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public DuplicateValueException(String message) {
		super(message);
	}

	public DuplicateValueException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public DuplicateValueException(String message, Throwable cause) {
		super(message, cause);
	}

	public DuplicateValueException(Throwable cause) {
		super(cause);
	}

}
