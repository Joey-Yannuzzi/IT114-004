package client;

import java.awt.Point;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import core.Team;
import server.Payload;
import server.PayloadType;

public enum SocketClient {

	INSTANCE;

	private static Socket server;
	private static Thread fromServerThread;
	private static Thread clientThread;
	private static String clientName;
	private static ObjectOutputStream out;
	private final static Logger log = Logger.getLogger(SocketClient.class.getName());
	private static List<Event> events = new ArrayList<Event>();

	public boolean connect(String address, String port) {
		try {
			server = new Socket(address, Integer.parseInt(port));
			log.log(Level.INFO, "Client connected");
			return true;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return (false);
	}

	public void setUsername(String username) {
		clientName = username;
		sendPayload(buildConnectionStatus(clientName, true));
	}

	public void sendMessage(String message) {
		sendPayload(buildMessage(message));
	}

	private Payload buildMessage(String message) {
		Payload payload = new Payload();
		payload.setPayloadType(PayloadType.MESSAGE);
		payload.setClientName(clientName);
		payload.setMessage(message);
		return (payload);
	}

	private Payload buildConnectionStatus(String name, boolean isConnect) {
		Payload payload = new Payload();

		if (isConnect) {
			payload.setPayloadType(PayloadType.CONNECT);
		} else {
			payload.setPayloadType(PayloadType.DISCONNECT);
		}

		payload.setClientName(name);
		return (payload);
	}

	private void sendPayload(Payload p) {
		try {
			out.writeObject(p);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void listenForServerMessage(ObjectInputStream in) {
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

	private void sendOnClientConnect(String name, String message) {
		Iterator<Event> iter = events.iterator();

		while (iter.hasNext()) {
			Event e = iter.next();

			if (e != null) {
				e.onClientConnect(name, message);
			}
		}
	}

	private void sendOnClientDisconnect(String name, String message) {
		Iterator<Event> iter = events.iterator();

		while (iter.hasNext()) {
			Event e = iter.next();

			if (e != null) {
				e.onClientDisconnect(name, message);
			}
		}
	}

	private void sendOnMessage(String name, String message) {
		Iterator<Event> iter = events.iterator();

		while (iter.hasNext()) {
			Event e = iter.next();

			if (e != null) {
				e.onMessageReceive(name, message);
			}
		}
	}

	private void sendOnChangeRoom() {
		Iterator<Event> iter = events.iterator();

		while (iter.hasNext()) {
			Event e = iter.next();

			if (e != null) {
				e.onChangeRoom();
			}
		}
	}

	private void sendSyncDirection(String clientName, Point direction) {
		Iterator<Event> iter = events.iterator();

		while (iter.hasNext()) {
			Event e = iter.next();

			if (e != null) {
				e.onSyncDirection(clientName, direction);
			}
		}
	}

	private void sendSyncPosition(String clientName, Point position) {
		Iterator<Event> iter = events.iterator();

		while (iter.hasNext()) {
			Event e = iter.next();

			if (e != null) {
				e.onSyncPosition(clientName, position);
			}
		}
	}

	private void sendSpawnProjectile(String name, Point direction) {
		Iterator<Event> iter = events.iterator();

		while (iter.hasNext()) {
			Event e = iter.next();

			if (e != null) {
				e.onSpawnProjectile(name, direction);
			}
		}
	}

	private void sendGameStart() {
		Iterator<Event> iter = events.iterator();

		while (iter.hasNext()) {
			Event e = iter.next();

			if (e != null) {
				e.onGameStart();
			}
		}
	}

	/*
	 * private void sendTeammates(Game game) { Iterator<Event> iter =
	 * events.iterator();
	 * 
	 * while (iter.hasNext()) { Event e = iter.next();
	 * 
	 * if (e != null) { e.onSendTeammates(game); } } }
	 */

	private void sendCountdown(String message, int duration) {
		Iterator<Event> iter = events.iterator();

		while (iter.hasNext()) {
			Event e = iter.next();

			if (e != null) {
				e.onSetCountdown(message, duration);
			}
		}
	}

	private void sendGameEnd() {
		Iterator<Event> iter = events.iterator();

		while (iter.hasNext()) {
			Event e = iter.next();

			if (e != null) {
				e.onGameEnd();
			}
		}
	}

	private void sendDeathReport(String name) {
		Iterator<Event> iter = events.iterator();

		while (iter.hasNext()) {
			Event e = iter.next();

			if (e != null) {
				e.onDeathReport(name);
			}
		}
	}

	private void sendGlobalDeath(String name, String message) {
		Iterator<Event> iter = events.iterator();

		while (iter.hasNext()) {
			Event e = iter.next();

			if (e != null) {
				e.onGlobalDeath(name, message);
			}
		}
	}

	private void sendHitReport(String name) {
		Iterator<Event> iter = events.iterator();

		while (iter.hasNext()) {
			Event e = iter.next();

			if (e != null) {
				e.onHitReport(name);
			}
		}
	}

	private void sendTeams(Team redTeam, Team blueTeam) {
		Iterator<Event> iter = events.iterator();

		while (iter.hasNext()) {
			Event e = iter.next();

			if (e != null) {
				e.onSendTeams(redTeam, blueTeam);
			}
		}
	}

	protected void spawnProjectile(String name, Point direction) {
		// System.out.println("Position: " + position);
		Payload p = new Payload();
		p.setPayloadType(PayloadType.PROJECTILE);
		p.setPoint(direction);
		p.setClientName(name);
		sendPayload(p);
	}

	protected void globalDeathReport(String name, String message) {
		Payload p = new Payload();
		p.setPayloadType(PayloadType.GLOBAL_DEATH);
		p.setClientName(name);
		p.setMessage(message);
		sendPayload(p);
	}

	private void processPayload(Payload p) {
		switch (p.getPayloadType()) {
		case CONNECT:
			receiveClientConnect(p.getClientName(), p.getMessage());
			break;

		case DISCONNECT:
			sendOnClientDisconnect(p.getClientName(), p.getMessage());
			break;

		case MESSAGE:
			sendOnMessage(p.getClientName(), p.getMessage());
			break;

		case CLEAR_PLAYERS:
			sendOnChangeRoom();
			break;

		case SYNC_DIRECTION:
			sendSyncDirection(p.getClientName(), p.getPoint());
			break;

		case SYNC_POSITION:
			sendSyncPosition(p.getClientName(), p.getPoint());
			break;

		case PROJECTILE:
			sendSpawnProjectile(p.getClientName(), p.getPoint());
			break;

		case START_GAME:
			sendGameStart();
			break;

		case END_GAME:
			sendGameEnd();
			break;

		case TEAM:
			sendTeams(p.getRedTeam(), p.getBlueTeam());
			break;

		case SET_COUNTDOWN:
			sendCountdown(p.getMessage(), p.getNumber());
			break;

		case DEATH:
			sendDeathReport(p.getClientName());
			break;

		case GLOBAL_DEATH:
			sendGlobalDeath(p.getClientName(), p.getMessage());
			break;

		case HIT:
			sendHitReport(p.getClientName());

		default:
			log.log(Level.WARNING, "unhandled payload on client" + p);
			break;
		}
	}

	public void registerCallbackListener(Event e) {
		events.add(e);
		log.log(Level.INFO, "Attached listener");
	}

	public void removeCallbackListener(Event e) {
		events.remove(e);
	}

	public void syncDirection(Point dir) {
		Payload p = new Payload();
		p.setPayloadType(PayloadType.SYNC_DIRECTION);
		p.setPoint(dir);
		sendPayload(p);
	}

	public void syncPosition(Point position) {
		Payload p = new Payload();
		p.setPayloadType(PayloadType.SYNC_POSITION);
		p.setPoint(position);
		sendPayload(p);
	}

	public boolean connectAndStart(String address, String port) throws IOException {
		if (connect(address, port)) {
			return (start());
		}

		return (false);
	}

	public boolean start() throws IOException {
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

	private void receiveClientConnect(String name, String message) {
		Iterator<Event> iter = events.iterator();

		while (iter.hasNext()) {
			Event e = iter.next();

			if (e != null) {
				e.onClientConnect(name, message);
			}
		}
	}

	public void close() {
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