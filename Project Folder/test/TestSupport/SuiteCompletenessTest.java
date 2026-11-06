package TestSupport;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runners.Suite;

public class SuiteCompletenessTest {

	@Test
	public void everyTestClassIsPartOfTheSuite() throws IOException {
		Set<String> onDisk = new TreeSet<>();
		try (Stream<java.nio.file.Path> files = Files.walk(Paths.get("test"))) {
			files.map(path -> path.getFileName().toString())
					.filter(name -> name.endsWith("Test.java"))
					.forEach(name -> onDisk.add(name.replace(".java", "")));
		}
		Set<String> inSuite = new TreeSet<>();
		for (Class<?> test : AllTests.class.getAnnotation(Suite.SuiteClasses.class).value()) {
			inSuite.add(test.getSimpleName());
		}
		assertEquals(onDisk, inSuite);
	}
}
