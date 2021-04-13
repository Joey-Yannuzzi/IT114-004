package core;

import java.awt.Graphics;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;

public abstract class BaseGamePanel extends JPanel {

	private static final long serialVersionUID = 5L;
	protected boolean isRunning = false;
	public int SLEEP = 16;
	BaseGamePanel bgp;
	public boolean isServer = false;
	public boolean delayGameLoop = false;
	private final static Logger log = Logger.getLogger(BaseGamePanel.class.getName());
	Thread gameLoop;

	public BaseGamePanel() {
		awake();
		bgp = this;

		if (!delayGameLoop) {
			startGameLoop();
		}
	}

	public BaseGamePanel(boolean delay) {
		delayGameLoop = delay;
		awake();
		bgp = this;

		if (!delayGameLoop) {
			startGameLoop();
		}
	}

	public void startGameLoop() {
		if (gameLoop == null) {
			isRunning = true;

			gameLoop = new Thread() {
				@Override
				public void run() {
					bgp.start();

					if (!isServer) {
						bgp.attachListeners();
					}

					while (isRunning) {
						bgp.update();
						bgp.lateUpdate();

						if (!isServer) {
							bgp.repaint();
						}

						try {
							Thread.sleep(SLEEP);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

					log.log(Level.INFO, "Game ended");
					bgp.quit();
				}
			};

			gameLoop.start();
		}
	}

	public abstract void awake();

	public abstract void start();

	public abstract void update();

	public abstract void lateUpdate();

	public abstract void draw(Graphics g);

	public abstract void quit();

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		draw(g);
	}

	public abstract void attachListeners();
}