package core;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;

public class Projectile extends GameObject {

	private Dimension size = new Dimension(25, 25);
	private Point speed = new Point(5, 5);
	private Point position;
	private boolean isShoot = false;

	public Projectile(Color teamColor, Point position, Point direction) {
		this.color = Color.RED;
		this.position = position;
		this.direction = direction;
		setShoot(true);
	}

	@Override
	public boolean draw(Graphics g) {
		if (super.draw(g)) {
			g.setColor(color);
			g.fillRect(position.x, position.y, size.width, size.height);
		}

		return true;
	}

	public boolean getShoot() {
		return isShoot;
	}

	public void setShoot(boolean isShoot) {
		this.isShoot = isShoot;
	}
}
