package core;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.io.Serializable;

public abstract class GameObject implements Serializable {

	private static final long serialVersionUID = -9145932773417678588L;
	protected Point position = new Point(0, 0);
	protected Point previousPosition = new Point(0, 0);
	protected Point speed = new Point(2, 2);
	protected Point direction = new Point(0, 0);
	protected Dimension size = new Dimension(25, 25);
	protected String name = "";
	protected boolean isActive = true;

	public void setSpeed(int x, int y) {
		if (x > -1) {
			speed.x = x;
		}

		if (y > -1) {
			speed.y = y;
		}
	}

	public void setSize(int width, int height) {
		size.width = Math.max(0, width);
		size.height = Math.max(0, height);
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return (this.name);
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public boolean isActive() {
		return (this.isActive);
	}

	public void move() {
		if (!isActive) {
			return;
		}

		previousPosition.x = position.x;
		previousPosition.y = position.y;
		position.x += (speed.x * direction.x);
		position.y += (speed.y * direction.y);
	}

	public boolean setDirection(int x, int y) {
		x = Helpers.clamp(x, -1, 1);
		y = Helpers.clamp(y, -1, 1);
		boolean changed = false;

		if (direction.x != x) {
			direction.x = x;
			changed = true;
		}

		if (direction.y != y) {
			direction.y = y;
			changed = true;
		}

		return (changed);
	}

	public Point getDirection() {
		return (direction);
	}

	public void setPosition(Point position) {
		previousPosition.x = position.x;
		previousPosition.y = position.y;
		this.position.x = position.x;
		this.position.y = position.y;
	}

	public Point getPosition() {
		return (position);
	}

	public boolean changedPosition() {
		return (previousPosition.x != position.x || previousPosition.y != position.y);
	}

	public boolean draw(Graphics g) {
		if (!isActive) {
			return (false);
		}

		return (true);
	}
}
