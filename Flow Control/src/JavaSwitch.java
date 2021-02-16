
public class JavaSwitch {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		System.out.println("Java Switch Exercise 1");
		int day = 2;
		switch (day) {
		case 1:
			System.out.println("Saturday");
			break;
		case 2:
			System.out.println("Sunday");
			break;
		}

		System.out.println("Java Switch Exercise 2");
		day = 4;
		switch (day) {
		case 1:
			System.out.println("Saturday");
			break;
		case 2:
			System.out.println("Sunday");
			break;
		default:
			System.out.println("Weekend");
		}
	}

}
