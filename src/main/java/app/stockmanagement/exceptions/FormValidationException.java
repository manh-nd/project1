package app.stockmanagement.exceptions;

public class FormValidationException extends Exception {

	private static final long serialVersionUID = 1L;

	public FormValidationException() {
		super();
	}

	public FormValidationException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public FormValidationException(String message, Throwable cause) {
		super(message, cause);
	}

	public FormValidationException(String message) {
		super(message);
	}

	public FormValidationException(Throwable cause) {
		super(cause);
	}
	

}
