
public class MyArrays {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		System.out.println("Java Arrays Exercise 1");
		String[] cars = { "Volvo", "BMW", "Ford" };

		System.out.println("Java Arrays Exercise 2");
		System.out.println(cars[1]);

		System.out.println("Java Arrays Exercise 3");
		cars[0] = "Opel";
		System.out.println(cars[0]);

		System.out.println("Java Arrays Exercise 4");
		cars[0] = "Volvo";
		System.out.println(cars.length);

		System.out.println("Java Arrays Exercise 5");
		for (String i : cars) {
			System.out.println(i);
		}

		System.out.println("Java Arrays Exercise 6");
		int[][] myNumbers = { { 1, 2, 3, 4 }, { 5, 6, 7 } };
	}

}
