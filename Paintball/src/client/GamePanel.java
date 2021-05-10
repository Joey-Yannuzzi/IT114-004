package client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;

import core.BaseGamePanel;
import core.Projectile;

public class GamePanel extends BaseGamePanel implements Event {

	private static final long serialVersionUID = -1121202275148798015L;
	List<Player> players;
	Player myPlayer;
	String playerUsername;
	private final static Logger log = Logger.getLogger(GamePanel.class.getName());
	List<Projectile> projectiles;
	// Point direction = new Point(1, 0);
	// Point position = new Point(50, 50);
	private Projectile myProjectile;
	private Dimension projectileSize = new Dimension(25, 25);
	private Point defaultDirection = new Point(1, 0);

	public void setPlayerName(String name) {
		playerUsername = name;

		if (myPlayer != null) {
			myPlayer.setName(playerUsername);
		}
	}

	@Override
	public void attachListeners() {
		InputMap im = this.getRootPane().getInputMap();
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0, false), "up_pressed");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0, true), "up_released");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0, false), "down_pressed");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0, true), "down_released");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0, false), "left_pressed");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0, true), "left_released");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0, false), "right_pressed");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0, true), "right_released");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, false), "space_pressed");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0, true), "space_released");
		ActionMap am = this.getRootPane().getActionMap();
		am.put("up_pressed", new MoveAction(KeyEvent.VK_W, true));
		am.put("up_released", new MoveAction(KeyEvent.VK_W, false));
		am.put("down_pressed", new MoveAction(KeyEvent.VK_S, true));
		am.put("down_released", new MoveAction(KeyEvent.VK_S, false));
		am.put("left_pressed", new MoveAction(KeyEvent.VK_A, true));
		am.put("left_released", new MoveAction(KeyEvent.VK_A, false));
		am.put("right_pressed", new MoveAction(KeyEvent.VK_D, true));
		am.put("right_released", new MoveAction(KeyEvent.VK_D, false));
		am.put("space_pressed", new MoveAction(KeyEvent.VK_SPACE, true));
		am.put("space_released", new MoveAction(KeyEvent.VK_SPACE, false));
	}

	@Override
	public synchronized void onClientConnect(String clientName, String message) {
		// TODO Auto-generated method stub

		System.out.println("Connected on Game Panel: " + clientName);
		boolean exists = false;
		Iterator<Player> iter = players.iterator();

		while (iter.hasNext()) {
			Player p = iter.next();

			if (p != null && p.getName().equalsIgnoreCase(clientName)) {
				exists = true;
				break;
			}
		}

		if (!exists) {
			Player p = new Player();
			p.setName(clientName);
			players.add(p);

			System.out.println(clientName + ": " + playerUsername);

			if (clientName.equals(playerUsername)) {
				System.out.println("Reset myPlayer");
				myPlayer = p;
			}
		}
	}

	@Override
	public void onClientDisconnect(String clientName, String message) {
		// TODO Auto-generated method stub

		System.out.println("Disconnected on Game Panel: " + clientName);
		Iterator<Player> iter = players.iterator();

		while (iter.hasNext()) {
			Player p = iter.next();

			if (p != null && !p.getName().equals(playerUsername) && p.getName().equalsIgnoreCase(clientName)) {
				iter.remove();
				break;
			}
		}
	}

	@Override
	public void onMessageReceive(String clientName, String message) {
		// TODO Auto-generated method stub

		System.out.println("Message on Game Panel");
	}

	@Override
	public void onChangeRoom() {
		// TODO Auto-generated method stub

		Iterator<Player> iter = players.iterator();

		while (iter.hasNext()) {
			Player p = iter.next();
			iter.remove();
		}

		myPlayer = null;
		System.out.println("Cleared players");
	}

	@Override
	public void awake() {
		// TODO Auto-generated method stub

		players = new ArrayList<Player>();
		projectiles = new ArrayList<Projectile>();
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub

		applyControls();
		localMovePlayers();
		localMoveProjectiles();

	}

	private void applyControls() {
		// System.out.println(myPlayer);
		boolean d = false;

		if (myPlayer != null) {
			int x = 0, y = 0;

			if (KeyStates.W) {
				y = -1;
			} else if (KeyStates.S) {
				y = 1;
			}

			if (!KeyStates.W && !KeyStates.S) {
				y = 0;
			}

			if (KeyStates.A) {
				x = -1;
			} else if (KeyStates.D) {
				x = 1;
			}

			if (!KeyStates.A && !KeyStates.D) {
				x = 0;
			}

			if (x != 0 || y != 0) {
				defaultDirection.x = x;
				defaultDirection.y = y;
			}

			if (x == 0 && y == 0) {
				d = true;
			}

			boolean changed = myPlayer.setDirection(x, y);

			if (KeyStates.SPACE) {
				if (d) {
					myProjectile = new Projectile(myPlayer.getColor(), myPlayer.getPosition(), defaultDirection,
							projectileSize);
				} else {
					myProjectile = new Projectile(myPlayer.getColor(), myPlayer.getPosition(), myPlayer.getDirection(),
							projectileSize);
				}
				projectiles.add(myProjectile);
			}

			if (changed) {
				System.out.println("Direction changed");
				SocketClient.INSTANCE.syncDirection(new Point(x, y));
			}

		}
	}

	private void localMovePlayers() {
		Iterator<Player> iter = players.iterator();

		while (iter.hasNext()) {
			Player p = iter.next();

			if (p != null) {
				p.move();
			}
		}
	}

	private void localMoveProjectiles() {
		Iterator<Projectile> iter = projectiles.iterator();

		while (iter.hasNext()) {
			Projectile p = iter.next();

			if (p != null) {
				p.move();
				System.out.println("Direction: " + p.getDirection());
				System.out.println("Position: " + p.getPosition());
				System.out.println("Moving Projectile");
			}
		}
	}

	@Override
	public void lateUpdate() {
		// TODO Auto-generated method stub
	}

	@Override
	public synchronized void draw(Graphics g) {
		// TODO Auto-generated method stub

		setBackground(Color.BLACK);
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		drawPlayers(g);
		drawText(g);
		drawProjectiles(g);
	}

	private synchronized void drawPlayers(Graphics g) {
		// g.setColor(Color.RED);
		// g.fillRect(0, 0, 50, 50);
		Iterator<Player> iter = players.iterator();

		while (iter.hasNext()) {
			Player p = iter.next();

			if (p != null) {
				p.draw(g);
			}
		}
	}

	private synchronized void drawProjectiles(Graphics g) {
		Iterator<Projectile> iter = projectiles.iterator();

		while (iter.hasNext()) {
			Projectile p = iter.next();

			if (p != null) {
				p.draw(g);
			}
		}
	}

	private void drawText(Graphics g) {
		g.setColor(Color.WHITE);
		g.setFont(new Font("Monospaced", Font.PLAIN, 12));

		if (myPlayer != null) {
			g.drawString("Debug MyPlayer: " + myPlayer.toString(), 10, 20);
		}
	}

	@Override
	public void quit() {
		// TODO Auto-generated method stub

		log.log(Level.INFO, "GamePanel quit");
	}

	@Override
	public void onSyncDirection(String clientName, Point direction) {
		Iterator<Player> iter = players.iterator();

		while (iter.hasNext()) {
			Player p = iter.next();

			if (p != null && p.getName().equalsIgnoreCase(clientName)) {
				System.out.println("Syncing direction: " + clientName);
				p.setDirection(direction.x, direction.y);
				System.out.println("From: " + direction);
				System.out.println("To: " + p.getDirection());
				break;
			}
		}
	}

	@Override
	public void onSyncPosition(String clientName, Point position) {
		System.out.println("Got position for " + clientName);
		Iterator<Player> iter = players.iterator();

		while (iter.hasNext()) {
			Player p = iter.next();

			if (p != null && p.getName().equalsIgnoreCase(clientName)) {
				System.out.println(clientName + " set " + position);
				p.setPosition(position);
				break;
			}
		}
	}

}
