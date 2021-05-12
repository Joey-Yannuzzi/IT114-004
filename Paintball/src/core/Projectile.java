package core;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;

public class Projectile extends GameObject {

	// private Dimension size = new Dimension(25, 25);
	// private Point speed = new Point(5, 5);
	// private Point position;
	private boolean isShoot = false;

	public Projectile(Color teamColor, Point position, Point direction, Dimension size) {
		this.color = teamColor;
		this.setPosition(position);
		this.setDirection(direction.x, direction.y);
		this.setSize(size);
		this.setSpeed(10, 10);
		setShoot(true);
		isActive = true;
		// System.out.println(speed);
	}

	@Override
	public boolean draw(Graphics g) {
		if (super.draw(g)) {
			g.setColor(color);
			g.fillOval(position.x, position.y, size.width, size.height);
		}

		return true;
	}

	public boolean getShoot() {
		return isShoot;
	}

	public void setShoot(boolean isShoot) {
		this.isShoot = isShoot;
	}

	public void shootProjectile(Projectile projectile) {
		if (!getShoot()) {
			return;
		}

	}
}
