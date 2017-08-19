package nl.topicus.rvdb;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.List;

/**
 * Created by robin on 8/13/2017.
 * Mojo to generate a manifestation diagram for an ear or war project
 */
@Mojo(name = "manifest-diagram", defaultPhase = LifecyclePhase.SITE)
public class ManifestDiagramMojo extends AbstractMojo {

	/**
	 * The file to generate a plantUML manifestation diagram for.
	 */
	@Parameter(property = "file", required = true,
			defaultValue = "${project.build.directory}\\${project.build.finalName}.${project.packaging}")
	private String file;

	/**
	 * The name of the file the generated manifestation diagram will receive.
	 */
	@Parameter(property = "name", defaultValue = "${project.artifactId}")
	private String name;

	/**
	 * The target directory where the manifestation diagram will be stored.
	 */
	@Parameter(property = "outputDir", defaultValue = "${project.build.directory}/site/resources/ear2plantuml/")
	private String outputDir;

	/**
	 * Haven't had time to figure out ejb detection. For now let the users mark jar files as such.
	 */
	@Parameter(property = "markAsEJB")
	private List<String> markAsEJB;

	/**
	 * Not all folders are interesting to include in the diagram. I.e. classes. List directories here that will not
	 * be traversed.
	 */
	@Parameter(property = "excludeDirs")
	private List<String> excludeDirs;


	/**
	 * List with wildcard expression (i.e. "commons-*" that will be matched against jar files (without extension). It will be
	 * included if the jar file matches one of the wildcard expressions.
	 */
	@Parameter(property = "filterJars")
	private List<String> filterJars;

	public void execute() throws MojoExecutionException, MojoFailureException {
		if (isValidExtension()) {
			buildDiagram();
		} else {
			getLog().info("Project packaging not ear or war, nothing to do here.");
		}
	}

	private void buildDiagram() throws MojoFailureException {
		try {
			DiagramGenerator ddg = new DiagramGenerator(name, file, outputDir, markAsEJB, excludeDirs, filterJars);
			ddg.generate();
		} catch (Exception e) {
			getLog().error(e.getMessage());
			throw new MojoFailureException(e.getMessage(), e);
		}
	}

	private boolean isValidExtension() {
		return file.endsWith(".war") || file.endsWith(".ear");
	}
}
