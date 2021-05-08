package core;

public class GameState {

	private GameStateType gameStateType;

	public GameState(GameStateType type) {
		gameStateType = type;
	}

	public GameStateType getGameStateType() {
		return gameStateType;
	}

	public void setGameStateType(GameStateType gameStateType) {
		this.gameStateType = gameStateType;
	}
}
