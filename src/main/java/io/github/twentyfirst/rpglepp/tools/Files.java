package io.github.twentyfirst.rpglepp.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Files {

	private static Logger log = LoggerFactory.getLogger(Files.class);

	public static String readFile(String name, List<String> path) throws IOException {
		return readFile(name, path, StandardCharsets.ISO_8859_1);
	}

	public static String readFile(String name, List<String> path, Charset charset) throws IOException {
		for ( String d: path ) {
			if ( d.startsWith("classpath:") ) {
				String resource = d.substring("classpath:".length());
				if ( resource.length() > 0 ) {
					resource += "/";
				}
				resource += name;
				ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
				try ( InputStream is = classLoader.getResourceAsStream(resource) ) {
					List<String> lines = new BufferedReader(
								new InputStreamReader(is, StandardCharsets.UTF_8))
							.lines()
							.collect(Collectors.toList());
					StringBuilder read = new StringBuilder();
					for ( String l: lines ) {
						read.append(l);
						read.append('\n');
					}
					log.debug("Read resource " + resource);
					return read.toString();
				}
				catch ( Exception e ) {
					log.warn("Error reading resource: " + e.getMessage());
				}
			}
			else {
				Path p = FileSystems.getDefault().getPath(d, name);
				File src = p.toFile();
				if ( src.exists() ) {
	// JDK 11
//					String read = java.nio.file.Files.readString(p, charset);
					StringBuilder read = new StringBuilder();
					List<String> lines = java.nio.file.Files.readAllLines(p, charset);
					for ( String l: lines ) {
						read.append(l);
						read.append('\n');
					}
					log.debug("Read file " + p.toString());
					return read.toString();
				}				
			}
		}
		throw new FileNotFoundException("File " + name + " not found");
	}

}
