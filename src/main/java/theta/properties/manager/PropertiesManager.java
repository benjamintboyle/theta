package theta.managers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesManager implements Runnable {
	private final Logger logger = LoggerFactory.getLogger(PropertiesManager.class);

	private Boolean running = true;
	private Path configPath;
	private Properties properties = new Properties();

	public PropertiesManager(String configFilename) {
		this.getConfigResource(configFilename);
		this.readProperties();
	}

	@Override
	public void run() {
		final WatchService watchService = this.getWatchService();

		WatchKey wk = null;
		while (this.running) {
			try {
				wk = watchService.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			this.evaluateWatchEvents(wk.pollEvents());

			boolean valid = wk.reset();
			if (!valid) {
				this.logger.error("Key has been unregistered");
			}
		}
	}

	private void getConfigResource(String configFilename) {
		try {
			this.configPath = Paths.get(this.getClass().getResource(configFilename).toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	private WatchService getWatchService() {
		WatchService watchService = null;
		try {
			watchService = FileSystems.getDefault().newWatchService();

			this.configPath.getParent().register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
			this.configPath.getParent().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
		} catch (IOException e) {
			this.logger.error("Failed to register directory {} with WatchService", this.configPath.getParent(), e);
		}

		return watchService;
	}

	private void evaluateWatchEvents(List<WatchEvent<?>> watchEvents) {
		for (WatchEvent<?> event : watchEvents) {
			// Cast to Path ok, as only registered ENTRY_CREATE and ENTRY_MODIFY
			final Path changed = (Path) event.context();
			if (this.configPath.equals(changed)) {
				readProperties();
			}
		}
	}

	private void readProperties() {
		try {
			this.properties.load(Files.newInputStream(this.configPath));
			this.logger.debug("Config keys: {}", this.properties.entrySet().toString());
		} catch (FileNotFoundException e) {
			this.logger.error("File does not exist", e);
		} catch (IOException e) {
			this.logger.error("Can not read file", e);
		}
	}

	public String getProperty(String key) throws InvalidParameterException {
		String property = this.properties.getProperty(key);

		if (property != null) {
			return property;
		} else {
			throw new InvalidParameterException();
		}
	}

	public void shutdown() {
		this.running = false;
	}
}
