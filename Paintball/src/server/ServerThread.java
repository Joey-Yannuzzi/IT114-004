package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

class ServerThread extends Thread {
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
