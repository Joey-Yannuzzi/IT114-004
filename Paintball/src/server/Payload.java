package server;

import java.awt.Point;
import java.io.Serializable;

import core.Team;

//import core.Game;

public class Payload implements Serializable {

	private static final long serialVersionUID = -6687715510484845706L;
	private String clientName;
	private String message;
	private PayloadType payloadType;
	private int number;
	private Team redTeam;
	private Team blueTeam;
	// private Game game;
	int x = 0;
	int y = 0;

	public void setClientName(String s) {
		this.clientName = s;
	}

	public String getClientName() {
		return (clientName);
	}

	public void setMessage(String s) {
		this.message = s;
	}

	public String getMessage() {
		return (this.message);
	}

	public void setPayloadType(PayloadType pt) {
		this.payloadType = pt;
	}

	public PayloadType getPayloadType() {
		return (this.payloadType);
	}

	public void setNumber(int n) {
		this.number = n;
	}

	public int getNumber() {
		return (this.number);
	}

	public void setPoint(Point p) {
		x = p.x;
		y = p.y;
	}

	public Point getPoint() {
		return new Point(x, y);
	}

	@Override
	public String toString() {
		return (String.format("Type[%s], Number[%s], Message[%s]", getPayloadType().toString(), getNumber(),
				getMessage()));
	}

	public Team getRedTeam() {
		return redTeam;
	}

	public void setRedTeam(Team redTeam) {
		this.redTeam = redTeam;
	}

	public Team getBlueTeam() {
		return blueTeam;
	}

	public void setBlueTeam(Team blueTeam) {
		this.blueTeam = blueTeam;
	}

	/*
	 * public Game getGame() { return game; }
	 * 
	 * public void setGame(Game game) { this.game = game; }
	 */
}