package it.twenfir.rpglepp.exception;

public class MissingCopyBookException extends RpgleppException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String fileName;
	
	public MissingCopyBookException(String fileName) {
		super("Source file " + fileName + " not found");
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

}
