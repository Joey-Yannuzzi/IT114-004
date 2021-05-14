package client;

import java.awt.Point;

import core.Game;

public interface Event {
	void onClientConnect(String clientName, String message);

	void onClientDisconnect(String clientName, String message);

	void onMessageReceive(String clientName, String message);

	void onChangeRoom();

	void onSyncDirection(String clientName, Point direction);

	void onSyncPosition(String clientName, Point position);

	void onSpawnProjectile(String name, Point direction);

	void onGameStart();

	void onSendTeammates(Game game);
}