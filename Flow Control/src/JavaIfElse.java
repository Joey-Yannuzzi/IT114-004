
public class JavaIfElse {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		System.out.println("Java If...Else Exercise 1");
		int x = 50;
		int y = 10;
		if (x > y) {
			System.out.println("Hello World");
		}

		System.out.println("Java If...Else Exercise 2");
		y = 50;
		if (x == y) {
			System.out.println("Hello World");
		}

		System.out.println("Java If...Else Exercise 3");
		if (x == y) {
			System.out.println("Yes");
		} else {
			System.out.println("No");
		}

		System.out.println("Java If...Else Exercise 4");
		if (x == y) {
			System.out.println(1);
		} else if (x > y) {
			System.out.println(2);
		} else {
			System.out.println(3);
		}

		System.out.println("Java If...Else Exercise 5");
		int time = 20;
		String result = (time < 18) ? "Good day." : "Good evening.";
		System.out.println(result);
	}

}
