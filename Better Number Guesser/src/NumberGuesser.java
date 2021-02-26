import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

public class NumberGuesser {

	private int level = 1;
	private int strikes = 0;
	private int maxStrikes = 5;
	private int num;
	private int[] guesses;
	private boolean running = false;
	final String saveFile = "Save.txt";

	public static int getNumber(int level) {
		int range = 9 + ((level - 1) * 5);
		System.out.println("Guess the number I picked from 1-" + (range + 1));
		return (new Random().nextInt(range + 1) + 1);
	}

	private void win() {
		System.out.println("Correct");
		level++;
		strikes = 0;
		System.out.println("Level: " + level);
		setGuesses();
		num = getNumber(level);
		saveLevel();
	}

	private void lose() {
		System.out.println("You are out of tries");
		System.out.println("Correct Answer: " + num);
		strikes = 0;
		level--;
		if (level < 1) {
			level = 1;
		}
		setGuesses();
		saveLevel();
		num = getNumber(level);
	}

	private void processInput(String message) {
		if (message.equalsIgnoreCase("quit")) {
			System.out.println("Goodbye");
			running = false;
			return;
		}
		int guess = -1;
		try {
			guess = Integer.parseInt(message);
		} catch (Exception e) {
			System.out.println("Try again, that is not a number");
			return;
		}
		if ((!checkGuess(guess)) && (guess > 0)) {
			System.out.println("Your Guess: " + guess);
			processGuess(guess);
		} else if (guess < 1) {
			System.out.println("No negative numbers or zero");
		} else {
			System.out.println("You already guessed that number");
		}
	}

	private void processGuess(int guess) {
		if (guess == num) {
			win();
		} else {
			System.out.println("Wrong");
			guesses[strikes] = guess;
			strikes++;
			if (strikes >= maxStrikes) {
				lose();
			} else {
				System.out.println("Remaining tries: " + (maxStrikes - strikes));
				System.out.print("Guessed numbers: ");
				for (int i = 0; i < strikes; i++) {
					System.out.print(guesses[i] + ", ");
				}
				System.out.println();

				if (guess > num) {
					System.out.println("Lower");
				} else {
					System.out.println("Higher");
				}
			}
		}
	}

	private void setGuesses() {
		for (int i = 0; i < guesses.length; i++) {
			guesses[i] = 0;
		}
	}

	private boolean checkGuess(int guess) {
		boolean isSame = false;
		for (int i = 0; i < guesses.length; i++) {
			if (guess == guesses[i]) {
				isSame = true;
				return (isSame);
			}
		}
		return (isSame);
	}

	private void saveLevel() {
		try (FileWriter file = new FileWriter(saveFile)) {
			file.write(level + "\n");
			file.write(strikes + "\n");
			file.write(maxStrikes + "\n");
			for (int i = 0; i < guesses.length; i++) {
				file.write(guesses[i] + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean loadLevel() {
		File file = new File(saveFile);
		if (!file.exists()) {
			guesses = new int[maxStrikes];
			setGuesses();
			return false;
		}
		try (Scanner doabarrelroll = new Scanner(file)) {
			while (doabarrelroll.hasNextLine()) {
				int l = doabarrelroll.nextInt();
				int s = doabarrelroll.nextInt();
				int mS = doabarrelroll.nextInt();
				int gS;
				if (l > 1) {
					level = l;
					strikes = s;
					maxStrikes = mS;
					guesses = new int[maxStrikes];
					for (int i = 0; i < guesses.length; i++) {
						gS = doabarrelroll.nextInt();
						guesses[i] = gS;
					}
					break;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e2) {
			e2.printStackTrace();
			return false;
		}
		return (level > 1);
	}

	void run() {
		try (Scanner doabarrelroll = new Scanner(System.in)) {
			System.out.println("Welcome to number guesser 4000!");
			System.out.println("To leave, type 'quit'");
			if (loadLevel()) {
				System.out.println("Level loaded");
				System.out.println("Level: " + level);
			}
			num = getNumber(level);
			running = true;
			while (doabarrelroll.hasNext()) {
				String message = doabarrelroll.nextLine();
				processInput(message);
				if (!running) {
					break;
				}
			}
		} catch (Exception e) {
			System.out.println("That is not a number");
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		saveLevel();
	}

	static public void main(String[] args) {
		NumberGuesser guesser = new NumberGuesser();
		guesser.run();
	}
}
