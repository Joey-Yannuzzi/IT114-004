import java.util.Scanner;

public class Userinput {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try (Scanner doabarrelroll = new Scanner(System.in);) {

			System.out.print("Enter Name: ");
			String message = doabarrelroll.nextLine();

			System.out.println("Hello " + message);
		}

		catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

}