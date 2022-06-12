package it.twenfir.rpglepp.api;

public class SourceFile {
	
	private String name;
	private String text;
	
	public SourceFile(String name, String text) {
		this.name = name;
		this.text = text;
	}
	
	public String getName() {
		return name;
	}
	
	public String getText() {
		return text;
	}
}
