package io.github.twentyfirst.rpglepp.exception;

public class InvalidCopyBookException extends RpgleppException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String fileName;
	
	public InvalidCopyBookException(String fileName) {
		super("Invalid source filename: " + fileName);
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

}
