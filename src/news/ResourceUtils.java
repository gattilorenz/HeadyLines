package news;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ResourceUtils {

	static InputStreamReader getReaderForResource(String resource_path) {
		return new InputStreamReader(ResourceUtils.getResourceAsInputStream(resource_path));
	}

	static InputStream getResourceAsInputStream(String resource_path) {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		InputStream input = loader.getResourceAsStream(resource_path);
		return input;
	}
}
