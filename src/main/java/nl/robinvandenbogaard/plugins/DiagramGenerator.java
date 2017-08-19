package nl.robinvandenbogaard.plugins;

import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

class DiagramGenerator {

	private static final String EAR = "artifact \"%s.ear\" <<ear>> as %s {%n";
	private static final String EJB = "artifact \"%s\" <<ejb>> as %s %n";
	private static final String JAR = "artifact \"%s\" <<library>> as %s %n";
	private static final String WAR = "artifact \"%s.war\" <<war>> as %s {%n";
	private static final String FILE = "file \"%s\" as %s %n";
	private static final String FOLDER = "folder \"%s\" as %s {%n";
	private static final String FOLDER_SKIP = "folder %s as \"%s\"%n";

	private final String destinationFilename;
	private final String destinationDirectory;
	private final String webApplicationFile;
	private final List<String> ejbs;
	private final List<String> excludeDirs;
	private final List<String> filterJars;

	private final Path tmpDir;
	private final StringBuilder rootBuilder;
	private String indent = "";

	DiagramGenerator(String destinationFilename, String webApplicationFile, String destinationDirectory,
					 List<String> ejbs, List<String> excludeDirs, List<String> filterJars) throws IOException {
		this.destinationFilename = destinationFilename;
		this.destinationDirectory = destinationDirectory;
		this.webApplicationFile = webApplicationFile;
		this.ejbs = ejbs;
		this.excludeDirs = excludeDirs;
		this.filterJars = filterJars;

		this.rootBuilder = new StringBuilder();
		this.tmpDir = Files.createTempDirectory(null).toAbsolutePath();
	}

	void generate() throws IOException {
		rootBuilder.append("@startuml\n");
		generateDiagram(webApplicationFile);
		rootBuilder.append("@enduml\n");
		saveDiagram();
		cleanUp();
	}

	private void generateDiagram(String archiveFile) throws IOException {
		try (FileSystem zipFileSystem = createZipFileSystem(archiveFile, false)) {
			final Path root = zipFileSystem.getPath("/");

			//generate the archive file tree and copy files to the destination
			Files.walkFileTree(root, new SimpleFileVisitor<Path>() {

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					String fileName = file.getFileName().toString();
					String alias = getAlias();
					if (fileName.endsWith("jar") && isEjbJar(fileName)) {
						rootBuilder.append(indent);
						rootBuilder.append(String.format(EJB, fileName, alias));
					} else if (fileName.endsWith("jar") && isJarIncluded(fileName)) {
						rootBuilder.append(indent);
						rootBuilder.append(String.format(JAR, fileName, alias));
					} else if (fileName.endsWith("war")) {
						String warFile = extractFile(file);
						generateDiagram(warFile);
						Files.deleteIfExists(Paths.get(warFile));
					} else if (!fileName.endsWith("jar")) {
						rootBuilder.append(indent);
						rootBuilder.append(String.format(FILE, fileName, alias));
					}
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					String alias = getAlias();

					if (dir.getNameCount() == 0) {
						String name = archiveFile.substring(archiveFile.lastIndexOf("\\") + 1, archiveFile.lastIndexOf("."));
						if (archiveFile.endsWith("ear")) {
							rootBuilder.append(String.format(EAR, name, alias));
						} else if (archiveFile.endsWith("war")) {
							rootBuilder.append(String.format(WAR, name, alias));
						}
						indent += " ";
						return FileVisitResult.CONTINUE;
					} else if (isDirectoryExcluded(dir)) {
						String fileName = dir.getFileName().toString().replace("/", "");
						rootBuilder.append(indent);
						rootBuilder.append(String.format(FOLDER_SKIP, fileName, alias));
						return FileVisitResult.SKIP_SUBTREE;
					} else {
						String fileName = dir.getFileName().toString().replace("/", "");
						rootBuilder.append(indent);
						rootBuilder.append(String.format(FOLDER, fileName, alias));
						indent += " ";
						return FileVisitResult.CONTINUE;
					}
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					indent = indent.substring(0, Math.max(0, indent.length() - 1));
					rootBuilder.append(indent);
					rootBuilder.append("}\n");
					return FileVisitResult.CONTINUE;
				}
			});
		}
	}

	private boolean isEjbJar(String jarFile) {
		return ejbs.contains(jarFile);
	}

	private boolean isDirectoryExcluded(Path dir) {
		String name = dir.getFileName().toString().replace("/", "");
		return excludeDirs.contains(name);
	}

	private boolean isJarIncluded(String jarFile) {
		return MatcherUtil.matches(filterJars, FilenameUtils.removeExtension(jarFile));
	}

	private String extractFile(Path file) throws IOException {
		final Path destFile = Paths.get(tmpDir.toString(),
				file.toString());
		Files.copy(file, destFile, StandardCopyOption.REPLACE_EXISTING);
		return destFile.toString();
	}

	private String getAlias() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

	private FileSystem createZipFileSystem(String zipFilename,
										   boolean create)
			throws IOException {
		// convert the filename to a URI
		final Path path = Paths.get(zipFilename);
		final URI uri = URI.create("jar:file:" + path.toUri().getPath());

		final Map<String, String> env = new HashMap<>();
		if (create) {
			env.put("create", "true");
		}
		return FileSystems.newFileSystem(uri, env);
	}

	private void saveDiagram() throws IOException {
		String source = rootBuilder.toString();

		SourceStringReader reader = new SourceStringReader(source);
		final ByteArrayOutputStream os = new ByteArrayOutputStream();
		// Write the first image to "os"
		reader.outputImage(os, new FileFormatOption(FileFormat.SVG));
		os.close();

		// The XML is stored into svg
		final String svg = new String(os.toByteArray(), Charset.forName("UTF-8"));
		FileUtils.writeStringToFile(new File(destinationDirectory + destinationFilename + ".svg"), svg, "utf-8");
	}

	private void cleanUp() throws IOException {
		Files.deleteIfExists(tmpDir);
	}
}
