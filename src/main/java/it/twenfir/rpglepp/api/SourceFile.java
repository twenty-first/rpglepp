package it.twenfir.rpglepp.api;

public class SourceFile {
	
	private String name;
	private String path;
	private String text;
	
	public SourceFile(String name, String path, String text) {
		this.name = name;
		this.path = path;
		this.text = text;
	}
	
	public String getName() {
		return name;
	}
	
	public String getPath() {
		return path;
	}
	
	public String getText() {
		return text;
	}
}
