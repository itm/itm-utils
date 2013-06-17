package de.uniluebeck.itm.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.util.Random;

public abstract class NetworkUtils {

	private static final Logger log = LoggerFactory.getLogger(NetworkUtils.class);

	/**
	 * Tests the connectivity of the given endpoint URL ({@code endpointURL}}) by trying to establish a TCP connection to
	 * this port.
	 *
	 * @param endpointURL the endpoint URL of the Web service for which to test connectivity
	 *
	 * @return {@code true} if a connection can be established (i.e. connectivity is given), {@code false} otherwise
	 */
	public static boolean testConnectivity(String endpointURL) {

		URI uri;

		try {
			uri = URI.create(endpointURL);
		} catch (Exception e) {
			log.error("Invalid endpoint URL given in testConnectivity(): {}", endpointURL);
			return false;
		}

		try {

			Socket socket = new Socket(uri.getHost(), uri.getPort());
			boolean connected = socket.isConnected();
			socket.close();
			return connected;

		} catch (IOException e) {
			log.warn("Could not connect to controller endpoint host/port. Reason: {}", e.getMessage());
		}

		return false;
	}

	/**
	 * Calls {@link NetworkUtils#testConnectivity(String)} and throws an {@link IllegalArgumentException} if
	 * connectivity is not given.
	 *
	 * @param endpointUrl the endpoint URL to check
	 */
	public static void checkConnectivity(final String endpointUrl) {
		if (!testConnectivity(endpointUrl)) {
			throw new RuntimeException(
					"Could not connect to host/port of the given endpoint URL: \"" + endpointUrl + "\". "
							+ "Make sure you're not behind a firewall/NAT and the endpoint is already started."
			);
		}
	}

	public static int findFreePort() {
		Random random = new Random();
		boolean foundPort = false;
		int port = 0;
		ServerSocket socket = null;
		while (!foundPort) {
			try {
				port = random.nextInt(((int) Math.pow(2, 16)) - 1024) + 1024;
				socket = new ServerSocket(port);
				socket.close();
				foundPort = true;
			} catch (IOException e) {
				// go on searching for another port
			}
		}
		return port;
	}
}
