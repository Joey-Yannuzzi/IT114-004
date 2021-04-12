package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import server.Payload;
import server.PayloadType;

class SocketClient {

	private static Socket server;
	private static Thread clientThread;
	private static Thread fromServerThread;
	private static String clientName;
	private static ObjectOutputStream out;
	private final static Logger log = Logger.getLogger(SocketClient.class.getName());
	private static Event event;

	public static boolean connect(String address, String port) {
		try {
			server = new Socket(address, Integer.parseInt(port));
			log.log(Level.INFO, "User Connected");
			return (true);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return (false);
	}

	public static void setUsername(String username) {
		clientName = username;
		sendPayload(buildConnectionStatus(clientName, true));
	}

	public static void sendMessage(String message) {
		sendPayload(buildMessage(message));
	}

	private static Payload buildMessage(String message) {
		Payload payload = new Payload();
		payload.setPayloadType(PayloadType.MESSAGE);
		payload.setClientName(clientName);
		payload.setMessage(message);
		return (payload);
	}

	private static Payload buildConnectionStatus(String name, boolean isConnect) {
		Payload payload = new Payload();

		if (isConnect) {
			payload.setPayloadType(PayloadType.CONNECT);
		} else {
			payload.setPayloadType(PayloadType.DISCONNECT);
		}

		payload.setClientName(name);
		return (payload);
	}

	private static void sendPayload(Payload p) {
		try {
			out.writeObject(p);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void listenForServerMessage(ObjectInputStream in) {
		if (fromServerThread != null) {
			log.log(Level.INFO, "Already listening");
			return;
		}
		fromServerThread = new Thread() {
			@Override
			public void run() {
				try {
					Payload fromServer;

					while (!server.isClosed() && (fromServer = (Payload) in.readObject()) != null) {
						processPayload(fromServer);
					}
				} catch (Exception e) {
					if (!server.isClosed()) {
						e.printStackTrace();
						log.log(Level.INFO, "Server Closed");
					} else {
						log.log(Level.INFO, "Connection Closed");
					}
				} finally {
					close();
					log.log(Level.INFO, "No longer listening");
				}
			}
		};
		fromServerThread.start();
	}

	private static void processPayload(Payload p) {
		switch (p.getPayloadType()) {
		case CONNECT:
			if (event != null) {
				event.onClientConnect(p.getClientName(), p.getMessage());
			}
			break;

		case DISCONNECT:
			if (event != null) {
				event.onClientDisconnect(p.getClientName(), p.getMessage());
			}
			break;

		case MESSAGE:
			if (event != null) {
				event.onMessageReceive(p.getClientName(), p.getMessage());
			}
			break;

		case CLEAR_PLAYERS:
			if (event != null) {
				event.onChangeRoom();
			}

		default:
			log.log(Level.WARNING, "Unhandled payload: " + p);
			break;
		}
	}

	public static void callbackListener(Event e) {
		event = e;
		log.log(Level.INFO, "Added listener");
	}

	public static boolean connectAndStart(String address, String port) throws IOException {
		if (connect(address, port)) {
			return (start());
		}

		return (false);
	}

	public static boolean start() throws IOException {
		if (server == null) {
			log.log(Level.WARNING, "Server does not exist");
			return (false);
		}

		if (clientThread != null && clientThread.isAlive()) {
			log.log(Level.SEVERE, "Thread is already active");
			return (false);
		}
		if (clientThread != null) {
			clientThread.interrupt();
			clientThread = null;
		}

		log.log(Level.INFO, "Client Started");

		clientThread = new Thread() {
			@Override
			public void run() {
				try (ObjectOutputStream out = new ObjectOutputStream(server.getOutputStream());
						ObjectInputStream in = new ObjectInputStream(server.getInputStream());) {
					SocketClient.out = out;
					listenForServerMessage(in);

					while (!server.isClosed()) {
						Thread.sleep(50);
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					close();
				}
			}
		};

		clientThread.start();
		return (true);
	}

	public static void close() {
		if (server != null && !server.isClosed()) {
			try {
				server.close();
				log.log(Level.INFO, "Socket Closed");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}