import java.util.Scanner;

public class MainMethod {
    public static void main(String[] args) throws OrderException, ClassNotFoundException {

        DailyTransactions dailyTransactions = new DailyTransactions();                 // Creating objects for DailyTransactions class
        //dailyTransactions.connection();

        System.out.print("Enter order number for shipped order");
        Scanner scanner = new Scanner(System.in);
        String sOrderID = scanner.next();
        dailyTransactions.Ship_order(Integer.parseInt(sOrderID));

        System.out.println("Enter date for order");
        System.out.println("Year");
        Scanner scanner2 = new Scanner(System.in);
        String year = scanner2.next();
        System.out.println("Month");
        Scanner scanner3 = new Scanner(System.in);
        String month = scanner3.next();
        System.out.println("Day");
        Scanner scanner4 = new Scanner(System.in);
        String day = scanner4.next();
        dailyTransactions.Issue_reorders(Integer.parseInt(year),Integer.parseInt(month),Integer.parseInt(day));

        System.out.println("Enter new order ID for recieved order ");
        Scanner scanner5 = new Scanner(System.in);
        String internalOrderReference = scanner5.next();
        dailyTransactions.Receive_order(Integer.parseInt(internalOrderReference));

    }
}
