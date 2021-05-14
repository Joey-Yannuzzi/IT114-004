package server;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import client.Player;
import core.BaseGamePanel;
import core.Countdown;
import core.Projectile;
import core.Team;

public class Room extends BaseGamePanel implements AutoCloseable {
	private static SocketServer server;
	private String name;
	private final static Logger log = Logger.getLogger(Room.class.getName());
	private final static String COMMAND_TRIGGER = "/";
	private final static String CREATE_ROOM = "createroom";
	private final static String JOIN_ROOM = "joinroom";
	private final static String CREATE_GAME = "creategame";
	private List<ClientPlayer> clients = new ArrayList<ClientPlayer>();
	private List<ClientPlayer> players;
	private List<ClientPlayer> spectators = new ArrayList<ClientPlayer>();
	static Dimension gameAreaSize = new Dimension(700, 600);
	long frame = 0;
	// private Game game;
	private List<Projectile> projectiles = new ArrayList<Projectile>();
	private Countdown timer;
	private Team redTeam;
	private Team blueTeam;
	private String red = "red";
	private String blue = "blue";
	private Color[] colors = { Color.BLUE, Color.CYAN, Color.GREEN, Color.MAGENTA, Color.ORANGE, Color.PINK, Color.RED,
			Color.YELLOW };

	public Room(String name, boolean delayStart) {
		super(delayStart);
		this.name = name;
		isServer = true;
	}

	public Room(String name) {
		this.name = name;
		isServer = true;
	}

	public static void setServer(SocketServer server) {
		Room.server = server;
	}

	public String getName() {
		return (name);
	}

	private static Point getRandomStartPosition() {
		Point startPos = new Point();
		startPos.x = (int) (Math.random() * gameAreaSize.width);
		startPos.y = (int) (Math.random() * gameAreaSize.height);
		return (startPos);
	}

	protected synchronized void addClient(ServerThread client) {
		client.setCurrentRoom(this);
		boolean exists = false;
		Iterator<ClientPlayer> iter = clients.iterator();

		while (iter.hasNext()) {
			ClientPlayer c = iter.next();

			if (c.client == client) {
				exists = true;

				if (c.player == null) {
					log.log(Level.WARNING, "Client " + client.getClientName() + " player was null, creating");
					Player p = new Player();
					p.setName(client.getClientName());
					c.player = p;
					syncClient(c);
				}

				break;
			}
		}

		if (exists) {
			log.log(Level.INFO, "Cloning a user is not possible");
		} else {
			Player p = new Player();
			p.setName(client.getClientName());
			ClientPlayer cp = new ClientPlayer(client, p);
			clients.add(cp);
			syncClient(cp);
		}
	}

	private void syncClient(ClientPlayer cp) {
		if (cp.client.getClientName() != null) {
			cp.client.sendClearList();
			sendConnectionStatus(cp.client, true, "joined the room " + getName());
			Point startPos = Room.getRandomStartPosition();
			cp.player.setPosition(startPos);
			cp.client.sendPosition(cp.client.getClientName(), startPos);
			sendPositionSync(cp.client, startPos);
			updateClientList(cp.client);
			updatePlayers(cp.client);
		}
	}

	private synchronized void updatePlayers(ServerThread client) {
		Iterator<ClientPlayer> iter = clients.iterator();

		while (iter.hasNext()) {
			ClientPlayer c = iter.next();

			if (c.client != client) {
				boolean messageSent = client.sendDirection(c.client.getClientName(), c.player.getDirection());

				if (messageSent) {
					messageSent = client.sendPosition(c.client.getClientName(), c.player.getPosition());
				}
			}
		}
	}

	private synchronized void updateClientList(ServerThread client) {
		Iterator<ClientPlayer> iter = clients.iterator();

		while (iter.hasNext()) {
			ClientPlayer c = iter.next();

			if (c.client != client) {
				boolean messageSent = client.sendConnectionStatus(c.client.getClientName(), true, null);
			}
		}
	}

	protected synchronized void removeClient(ServerThread client) {
		Iterator<ClientPlayer> iter = clients.iterator();

		while (iter.hasNext()) {
			ClientPlayer c = iter.next();

			if (c.client == client) {
				iter.remove();
				log.log(Level.INFO, "Removed client " + c.client.getClientName() + " from " + getName());
			}
		}

		if (clients.size() > 0) {
			sendConnectionStatus(client, false, "left the room " + getName());
		} else {
			cleanupEmptyRoom();
		}
	}

	private void cleanupEmptyRoom() {
		if (name == null || name.equalsIgnoreCase(SocketServer.LOBBY)) {
			return;
		}

		try {
			log.log(Level.INFO, "Closing " + name);
			close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void joinRoom(String room, ServerThread client) {
		server.joinRoom(room, client);
	}

	protected void joinLobby(ServerThread client) {
		server.joinLobby(client);
	}

	private boolean processCommands(String message, ServerThread client) {
		boolean wasCommand = false;

		try {
			if (message.indexOf(COMMAND_TRIGGER) > -1) {
				String[] comm = message.split(COMMAND_TRIGGER);
				log.log(Level.INFO, message);
				String part1 = comm[1];
				String[] comm2 = part1.split(" ");
				String command = comm2[0];

				if (command != null) {
					command = command.toLowerCase();
				}

				String roomName;

				switch (command) {
				case CREATE_ROOM:
					roomName = comm2[1];

					if (server.createNewRoom(roomName)) {
						joinRoom(roomName, client);
					}

					wasCommand = true;
					break;

				case JOIN_ROOM:
					roomName = comm2[1];
					joinRoom(roomName, client);
					wasCommand = true;
					break;
				case CREATE_GAME:
					wasCommand = true;

					if (this.getName().equalsIgnoreCase("lobby")) {
						System.out.println("Cannot create game in the Lobby");
						break;
					}

					// makeTeams();
					gameStart();
					this.setDelay(false);
					// game.setGameState(GameState.GAME);
					// sendGameState(game.getGameState());
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return (wasCommand);
	}

	private void gameStart() {
		Iterator<ClientPlayer> iter = clients.iterator();

		while (iter.hasNext()) {
			ClientPlayer player = iter.next();
			boolean messageSent = player.client.sendGameStart();

			if (!messageSent) {
				iter.remove();
				log.log(Level.INFO, "Removed " + player.client.getId());
			}
		}
	}

	private void makeTeams() {
		Random rand = new Random();
		List<ClientPlayer> temp = players;
		List<ClientPlayer> team1 = new ArrayList<ClientPlayer>();
		List<ClientPlayer> team2 = new ArrayList<ClientPlayer>();
		int size = players.size() / 2;

		for (int i = 0; i < size; i++) {
			int r = rand.nextInt(temp.size());
			team1.add(temp.get(r));
			temp.remove(r);
		}

		redTeam = new Team(team1, red);
		size = players.size() - size;

		for (int i = 0; i < size; i++) {
			int r = rand.nextInt(temp.size());
			team2.add(temp.get(r));
			temp.remove(r);
		}

		blueTeam = new Team(team2, blue);
		System.out.println("Teams created");
		sendTeams();
	}

	private void sendTeams() {
		Iterator<ClientPlayer> iter = clients.iterator();

		while (iter.hasNext()) {
			ClientPlayer player = iter.next();
			boolean messageSent = player.client.sendTeams(redTeam, blueTeam);

			if (!messageSent) {
				iter.remove();
			}
		}
	}

	protected void sendConnectionStatus(ServerThread client, boolean isConnect, String message) {
		Iterator<ClientPlayer> iter = clients.iterator();

		while (iter.hasNext()) {
			ClientPlayer c = iter.next();
			boolean messageSent = c.client.sendConnectionStatus(client.getClientName(), isConnect, message);

			if (!messageSent) {
				iter.remove();
				log.log(Level.INFO, "Removed " + client.getId());
			}
		}
	}

	protected void sendMessage(ServerThread sender, String message) {
		log.log(Level.INFO, getName() + " sending message to " + clients.size() + " users");

		if (processCommands(message, sender)) {
			return;
		}

		Iterator<ClientPlayer> iter = clients.iterator();

		while (iter.hasNext()) {
			ClientPlayer client = iter.next();
			boolean messageSent = client.client.send(sender.getClientName(), message);

			if (!messageSent) {
				iter.remove();
				log.log(Level.INFO, "Removed " + client.client.getId());
			}
		}
	}

	protected void sendDirectionSync(ServerThread sender, Point dir) {
		boolean changed = false;
		Iterator<ClientPlayer> iter = clients.iterator();

		while (iter.hasNext()) {
			ClientPlayer client = iter.next();

			if (client.client == sender) {
				changed = client.player.setDirection(dir.x, dir.y);
				break;
			}
		}

		if (changed) {
			iter = clients.iterator();

			while (iter.hasNext()) {
				ClientPlayer client = iter.next();
				boolean messageSent = client.client.sendDirection(sender.getClientName(), dir);

				if (!messageSent) {
					iter.remove();
					log.log(Level.INFO, "Removed client " + client.client.getId());
				}
			}
		}
	}

	protected void sendPositionSync(ServerThread sender, Point pos) {
		Iterator<ClientPlayer> iter = clients.iterator();

		while (iter.hasNext()) {
			ClientPlayer client = iter.next();
			boolean messageSent = client.client.sendPosition(sender.getClientName(), pos);

			if (!messageSent) {
				iter.remove();
				log.log(Level.INFO, "Removed client " + client.client.getId());
			}
		}
	}

	protected void sendCreateProjectile(ServerThread player, Point direction) {
		createProjectile(player, direction);
		// System.out.println("Position: " + position);
		Iterator<ClientPlayer> iter = clients.iterator();

		while (iter.hasNext()) {
			ClientPlayer c = iter.next();
			boolean messageSent = c.client.sendProjectileSpawn(player, direction);

			if (!messageSent) {
				iter.remove();
				log.log(Level.INFO, "Removed client " + c.client.getId());
			}
		}
	}

	protected void sendGlobalDeath(String name, String message) {
		Iterator<ClientPlayer> iter = clients.iterator();

		while (iter.hasNext()) {
			ClientPlayer player = iter.next();

			if (player.player.getName().equalsIgnoreCase(message)) {
				iter.remove();
			} else {
				boolean messageSent = player.client.sendGlobalDeath(name, message);

				if (!messageSent) {
					iter.remove();
				}
			}
		}
	}

	/*
	 * protected void sendTeams(Game game) { Iterator<ClientPlayer> iter =
	 * clients.iterator();
	 * 
	 * while (iter.hasNext()) { ClientPlayer player = iter.next(); boolean
	 * messageSent = player.client.sendTeammates(game);
	 * 
	 * if (!messageSent) { iter.remove(); log.log(Level.INFO, "Removed client " +
	 * player.client.getId()); } } }
	 */

	protected void sendCountdown(String message, int duration) {
		Iterator<ClientPlayer> iter = clients.iterator();

		while (iter.hasNext()) {
			ClientPlayer player = iter.next();
			boolean messageSent = player.client.sendCountdown(message, duration);

			if (!messageSent) {
				iter.remove();
			}
		}
	}

	protected void sendEndGame() {
		Iterator<ClientPlayer> iter = clients.iterator();

		while (iter.hasNext()) {
			ClientPlayer player = iter.next();
			boolean messageSent = player.client.sendEndGame();

			if (!messageSent) {
				iter.remove();
			}
		}
	}

	private void createProjectile(ServerThread player, Point direction) {
		// TODO Auto-generated method stub
		Random rand = new Random();
		int randColor = rand.nextInt(colors.length);
		Iterator<ClientPlayer> iter = clients.iterator();

		while (iter.hasNext()) {
			ClientPlayer c = iter.next();

			if (c.client.getClientName().equalsIgnoreCase(player.getClientName())) {
				Projectile p = new Projectile(colors[randColor], c.player.getPosition(), direction, c.player);
				projectiles.add(p);
			}
		}
	}

	/*
	 * protected void sendGameState(GameState gameState) { switch (gameState) { case
	 * LOBBY: break;
	 * 
	 * case GAME: game.createGame(clients);
	 * System.out.println("Teams have been created"); sendTeams(game); break;
	 * 
	 * case END: break; } }
	 */

	@Override
	public void close() throws Exception {
		int clientCount = clients.size();

		if (clientCount > 0) {
			log.log(Level.INFO, "Sending " + clients.size() + " to lobby");
			Iterator<ClientPlayer> iter = clients.iterator();
			Room lobby = server.getLobby();

			while (iter.hasNext()) {
				ClientPlayer client = iter.next();
				lobby.addClient(client.client);
				iter.remove();
			}

			log.log(Level.INFO, "Sent " + clientCount + " to lobby");
		}

		server.cleanupRoom(this);
		name = null;
		isRunning = false;
	}

	@Override
	public void awake() {
		// TODO Auto-generated method stub
		players = clients;

		// game = new Game();
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		timer = new Countdown("Test", 60);
		log.log(Level.INFO, getName() + " start called");
	}

	void checkPositionSync(ClientPlayer cp) {
		if (frame % 120 == 0) {
			if (cp.player.changedPosition()) {
				sendPositionSync(cp.client, cp.player.getPosition());
			}
		}
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub
		movePlayers();
		moveProjectiles();
		checkProjectilePosition();
	}

	private synchronized void movePlayers() {
		Iterator<ClientPlayer> iter = clients.iterator();

		while (iter.hasNext()) {
			ClientPlayer p = iter.next();

			if (p != null) {
				p.player.move();
				checkPositionSync(p);
			}
		}
	}

	private void moveProjectiles() {
		Iterator<Projectile> iter = projectiles.iterator();

		while (iter.hasNext()) {
			Projectile p = iter.next();

			if (p != null) {
				p.move();
			}
		}
	}

	private synchronized void checkProjectilePosition() {
		Iterator<Projectile> iter = projectiles.iterator();
		Point lowerBounds = new Point(0, 0);
		Point upperBounds = new Point(gameAreaSize.width, gameAreaSize.height);

		while (iter.hasNext()) {
			Projectile p = iter.next();

			if (p != null) {
				if (p.getPosition().x <= lowerBounds.x || p.getPosition().x >= upperBounds.x
						|| p.getPosition().y <= lowerBounds.y || p.getPosition().y >= upperBounds.y) {
					iter.remove();
					projectiles.remove(p);
					// System.out.println("Deleted Projectile");
				}
			}
		}
	}

	private void checkCountdown() {
		if (timer.getTime() < 1) {
			this.isRunning = false;
			sendEndGame();
		}
	}

	private void nextFrame() {
		if (Long.MAX_VALUE - 5 <= frame) {
			frame = Long.MIN_VALUE;
		}

		frame++;
	}

	@Override
	public void lateUpdate() {
		// TODO Auto-generated method stub
		checkCountdown();
		projectileCollisionCheck();
		nextFrame();
	}

	private void projectileCollisionCheck() {
		Iterator<ClientPlayer> iter = clients.iterator();

		while (iter.hasNext()) {
			ClientPlayer player = iter.next();

			if (player != null) {
				Iterator<Projectile> it = projectiles.iterator();

				while (it.hasNext()) {
					Projectile projectile = it.next();

					if (projectile != null && projectile.getPosition() == player.player.getPosition()) {
						it.remove();
						projectiles.remove(projectile);
						boolean alive = player.player.reduceLife();
						boolean messageSent;

						if (!alive) {
							iter.remove();
							clients.remove(player);
							spectators.add(player);
							messageSent = player.client.sendDeathReport(player.player.getName());
						} else {
							messageSent = player.client.sendHitReport(player.player.getName());
						}

						if (!messageSent) {
							iter.remove();
						}
					}
				}
			}
		}
	}

	@Override
	public void draw(Graphics g) {
		// TODO Auto-generated method stub

	}

	@Override
	public void quit() {
		// TODO Auto-generated method stub
		this.setLoop(null);
		log.log(Level.WARNING, getName() + " quit() ");
	}

	@Override
	public void attachListeners() {
		// TODO Auto-generated method stub

	}
}
