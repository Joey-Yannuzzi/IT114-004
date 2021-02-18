import java.util.Random;
import java.util.Scanner;

public class NumberGuesser {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		try (Scanner scan = new Scanner(System.in)) {
			System.out.println("Guess the number I picked from 1-10");
			System.out.println("To leave, type 'quit'");

			Random rand = new Random();
			int num = rand.nextInt(9) + 1;
			// System.out.println(num);
			System.out.print("Guess a number: ");

			int level = 1, strikes = 0, maxStrikes = 5;
			int[] guesses = new int[5];
			for (int i = 0; i < guesses.length; i++) {
				guesses[i] = 0;
			}

			while (scan.hasNext()) {
				String message = scan.nextLine();

				if (message.equalsIgnoreCase("quit")) {
					System.out.println("Goodbye");
					break;
				}

				int guess = -1;

				try {
					guess = Integer.parseInt(message);
				}

				catch (Exception e) {
					System.out.println("Try again, that is not a number");
				}

				if ((guess != guesses[0]) && (guess != guesses[1]) && (guess != guesses[2]) && (guess != guesses[3])
						&& (guess != guesses[4]) && (guess > 0)) {
					System.out.println("Your Guess: " + guess);

					if (guess == num) {
						System.out.println("Correct");
						level++;
						strikes = 0;
						int range = 9 + ((level - 1) * 5);
						System.out.println("Level: " + level);
						System.out.println("Guess the number I picked from 1-" + (range + 1));
						num = rand.nextInt(range) + 1;

						for (int i = 0; i < guesses.length; i++) {
							guesses[i] = 0;
						}
					} else {
						System.out.println("Wrong");
						guesses[strikes] = guess;
						strikes++;

						if (strikes >= maxStrikes) {
							System.out.println("You are out of tries");

							strikes = 0;
							level--;
							for (int i = 0; i < guesses.length; i++) {
								guesses[i] = 0;
							}

							if (level < 1) {
								level = 1;
							}

							int range = 9 + ((level - 1) * 5);

							System.out.println("Guess the number I picked from 1-" + (range + 1));

							num = rand.nextInt(range) + 1;
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
				} else if (guess < 1) {
					System.out.println("No negative numbers or zero");
				} else {
					System.out.println("You already guessed that number");
				}
			}
		} catch (Exception e) {
			System.out.println("That is not a number");

			e.printStackTrace();

			System.out.println(e.getMessage());
		}
	}

}
