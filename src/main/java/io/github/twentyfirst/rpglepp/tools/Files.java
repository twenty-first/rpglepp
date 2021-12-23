package io.github.twentyfirst.rpglepp.tools;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Files {

	private static Logger log = LoggerFactory.getLogger(Files.class);

	public static String readFile(String name, List<String> path) throws IOException {
		return readFile(name, path, StandardCharsets.ISO_8859_1);
	}

	/**
	 * Searches for a copybook file in a set of directories and/or classpath locations.
	 * 
	 * When searching inside directories filenames are treated as case-insensitive.
	 *
	 * @param name the name of the copybook file to be read
	 * @param path a list of directories or classpath locations where the file may be found
	 * @param charset the character set in which the file is encoded
	 * @return a string containing the file's text
	 * @throws FileNotFoundException if the file is not found
	 * @throws IOException in case of unexpected I/O errors
	 */
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
					List<String> lines = 
							new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))
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
				try ( Stream<Path> files = java.nio.file.Files.list(
						FileSystems.getDefault().getPath(d)) ) {
					Optional<Path> o = files
							.filter(p -> p.getFileName().toString().compareToIgnoreCase(name) == 0)
							.findFirst();
					if ( o.isPresent() ) {
						Path p = o.get();
						// JDK 11
						// String read = java.nio.file.Files.readString(p, charset);
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
				catch ( Exception e ) {
					log.warn("Error reading file: " + e.getMessage());
				}
			}
		}
		throw new FileNotFoundException("File " + name + " not found");
	}

}
