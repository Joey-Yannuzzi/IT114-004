package server;

import java.awt.Point;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import core.Team;

//import core.Game;

public class ServerThread extends Thread {
	private Socket client;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private boolean isRunning = false;
	private Room currentRoom;
	private String clientName;
	private final static Logger log = Logger.getLogger(ServerThread.class.getName());

	public String getClientName() {
		return clientName;
	}

	protected synchronized Room getCurrentRoom() {
		return (currentRoom);
	}

	protected synchronized void setCurrentRoom(Room room) {
		if (room != null) {
			currentRoom = room;
		} else {
			log.log(Level.INFO, "The room is null");
		}
	}

	public ServerThread(Socket myClient, Room room) throws IOException {
		this.client = myClient;
		this.currentRoom = room;
		out = new ObjectOutputStream(client.getOutputStream());
		in = new ObjectInputStream(client.getInputStream());
	}

	@Deprecated
	public boolean send(String message) {
		try {
			out.writeObject(message);
			return (true);
		} catch (IOException e) {
			log.log(Level.INFO, "Error sending a message");
			e.printStackTrace();
			cleanup();
			return (false);
		}
	}

	protected boolean send(String clientName, String message) {
		Payload payload = new Payload();
		payload.setPayloadType(PayloadType.MESSAGE);
		payload.setClientName(clientName);
		payload.setMessage(message);
		return sendPayload(payload);
	}

	protected boolean sendDirection(String clientName, Point dir) {
		Payload payload = new Payload();
		payload.setPayloadType(PayloadType.SYNC_DIRECTION);
		payload.setClientName(clientName);
		payload.setPoint(dir);
		return sendPayload(payload);
	}

	protected boolean sendPosition(String clientName, Point pos) {
		Payload payload = new Payload();
		payload.setPayloadType(PayloadType.SYNC_POSITION);
		payload.setClientName(clientName);
		payload.setPoint(pos);
		return sendPayload(payload);
	}

	protected boolean sendProjectileSpawn(ServerThread client, Point direction) {
		Payload payload = new Payload();
		payload.setPayloadType(PayloadType.PROJECTILE);
		payload.setClientName(client.getClientName());
		payload.setPoint(direction);
		return (sendPayload(payload));
	}

	protected boolean sendGameStart() {
		Payload payload = new Payload();
		payload.setPayloadType(PayloadType.START_GAME);
		return (sendPayload(payload));
	}

	/*
	 * protected boolean sendTeammates(Game game) { Payload payload = new Payload();
	 * payload.setPayloadType(PayloadType.TEAM); payload.setGame(game); return
	 * (sendPayload(payload)); }
	 */

	protected boolean sendCountdown(String message, int duration) {
		Payload payload = new Payload();
		payload.setPayloadType(PayloadType.SET_COUNTDOWN);
		payload.setMessage(message);
		payload.setNumber(duration);
		return (sendPayload(payload));
	}

	protected boolean sendEndGame() {
		Payload payload = new Payload();
		payload.setPayloadType(PayloadType.END_GAME);
		return (sendPayload(payload));
	}

	protected boolean sendDeathReport(String name) {
		Payload payload = new Payload();
		payload.setPayloadType(PayloadType.DEATH);
		payload.setClientName(name);
		return (sendPayload(payload));
	}

	protected boolean sendHitReport(String name) {
		Payload payload = new Payload();
		payload.setPayloadType(PayloadType.HIT);
		payload.setClientName(name);
		return (sendPayload(payload));
	}

	protected boolean sendGlobalDeath(String name, String message) {
		Payload payload = new Payload();
		payload.setPayloadType(PayloadType.GLOBAL_DEATH);
		payload.setClientName(name);
		payload.setMessage(message);
		return (sendPayload(payload));
	}

	protected boolean sendTeams(Team redTeam, Team blueTeam) {
		System.out.println("ServerThread");
		Payload payload = new Payload();
		payload.setPayloadType(PayloadType.TEAM);
		payload.setRedTeam(redTeam);
		payload.setBlueTeam(blueTeam);
		return (sendPayload(payload));
	}

	protected boolean sendConnectionStatus(String clientName, boolean isConnect, String message) {
		Payload payload = new Payload();

		if (isConnect) {
			payload.setPayloadType(PayloadType.CONNECT);
			payload.setMessage(message);
		} else {
			payload.setPayloadType(PayloadType.DISCONNECT);
			payload.setMessage(message);
		}

		payload.setClientName(clientName);
		return sendPayload(payload);
	}

	protected boolean sendClearList() {
		Payload payload = new Payload();
		payload.setPayloadType(PayloadType.CLEAR_PLAYERS);
		return (sendPayload(payload));
	}

	private boolean sendPayload(Payload p) {
		try {
			out.writeObject(p);
			return (true);
		} catch (IOException e) {
			log.log(Level.INFO, "Error sending a message to the user");
			e.printStackTrace();
			cleanup();
			return (false);
		}
	}

	private void processPayload(Payload p) {
		switch (p.getPayloadType()) {
		case CONNECT:
			String n = p.getClientName();

			if (n != null) {
				clientName = n;
				log.log(Level.INFO, "Set name to " + clientName);

				if (currentRoom != null) {
					currentRoom.joinLobby(this);
				}
			}

			break;

		case DISCONNECT:
			isRunning = false;
			break;

		case MESSAGE:
			currentRoom.sendMessage(this, p.getMessage());
			break;

		case CLEAR_PLAYERS:
			break;

		case SYNC_DIRECTION:
			System.out.println("Direction changed: " + p.getPoint());
			currentRoom.sendDirectionSync(this, p.getPoint());
			break;

		case SYNC_POSITION:
			currentRoom.sendPositionSync(this, p.getPoint());
			break;

		case PROJECTILE:
			// System.out.println("Position: " + p.getPosition());
			currentRoom.sendCreateProjectile(this, p.getPoint());
			break;

		case GLOBAL_DEATH:
			currentRoom.sendGlobalDeath(p.getClientName(), p.getMessage());

		default:
			log.log(Level.INFO, "Unhandled payload");
			break;
		}
	}

	@Override
	public void run() {
		try {
			isRunning = true;
			Payload fromClient;

			while (isRunning && !client.isClosed() && (fromClient = (Payload) in.readObject()) != null) {
				System.out.println("User: " + fromClient);
				processPayload(fromClient);
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.log(Level.INFO, "User Disconnected");
		} finally {
			isRunning = false;
			log.log(Level.INFO, "Cleaning up connection");
			cleanup();
		}
	}

	private void cleanup() {
		if (currentRoom != null) {
			log.log(Level.INFO, getName() + " Leaving " + currentRoom.getName());
			currentRoom.removeClient(this);
		}

		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
				log.log(Level.INFO, "Already Closed");
			}
		}

		if (out != null) {
			try {
				in.close();
			} catch (IOException e) {
				log.log(Level.INFO, "Already Closed");
			}
		}

		if (client != null && !client.isClosed()) {
			try {
				client.shutdownInput();
			} catch (IOException e) {
				log.log(Level.INFO, "Socket is already closed");
			}

			try {
				client.shutdownOutput();
			} catch (IOException e) {
				log.log(Level.INFO, "Socket is already closed");
			}

			try {
				client.close();
			} catch (IOException e) {
				log.log(Level.INFO, "Already Closed");
			}
		}
	}
}
