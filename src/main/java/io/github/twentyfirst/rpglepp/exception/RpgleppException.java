package io.github.twentyfirst.rpglepp.exception;

public class RpgleppException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RpgleppException() {
	}

	public RpgleppException(String message) {
		super(message);
	}

	public RpgleppException(Throwable cause) {
		super(cause);
	}

	public RpgleppException(String message, Throwable cause) {
		super(message, cause);
	}

	public RpgleppException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
