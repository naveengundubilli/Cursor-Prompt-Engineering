package com.securepdfeditor.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.List;

public final class NetworkGuard {
	private static final Logger logger = LoggerFactory.getLogger(NetworkGuard.class);

	private NetworkGuard() {}

	public static final class AppPolicy {
		private static volatile boolean offline = false;

		public static void setOffline(boolean value) {
			offline = value;
			if (offline) install(); else uninstall();
		}

		public static boolean isOffline() { return offline; }

		private static void install() {
			ProxySelector.setDefault(new BlockingProxySelector());
			try {
				URL.setURLStreamHandlerFactory(protocol -> new URLStreamHandler() {
					@Override
					protected URLConnection openConnection(URL u) throws IOException {
						throw new SecurityException("Network blocked in offline mode: " + u);
					}
				});
			} catch (Error ignored) {
				// Factory can be set only once per JVM; ignore if already set
			}
			logger.info("Network access blocked (offline mode)");
		}

		private static void uninstall() {
			ProxySelector.setDefault(null);
			logger.info("Network access allowed (online mode)");
		}
	}

	private static class BlockingProxySelector extends ProxySelector {
		@Override
		public List<Proxy> select(URI uri) { throw new SecurityException("Network blocked: " + uri); }
		@Override
		public void connectFailed(URI uri, SocketAddress sa, IOException ioe) { }
	}
}
