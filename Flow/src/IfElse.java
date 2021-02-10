
public class IfElse {

	public static void main(String[] args) {

		if (true) {
			System.out.println("Hello");
		}

		int age = 20;

		if (age >= 21) {
			System.out.println("You are an adult that can drink");
		}

		else if (age >= 18) {
			System.out.println("You are an adult");
		}

		else {
			System.out.println("you are a child");
		}

		boolean value = Boolean.parseBoolean(null);

		if (value) {
			System.out.println("true");
		}

		else {
			System.out.println("false");
		}

		String a = "test";

		if (a == "test") {
			System.out.println("'a' is correct");
		}

		else {
			System.out.println("'a' is incorrect");
		}
	}

}
