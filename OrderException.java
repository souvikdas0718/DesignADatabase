//Exception class requested during throws method
public class OrderException extends Exception {
    int orderNumber;
    public OrderException(String message, int orderNumber) {
        super(message);
        this.orderNumber=orderNumber;
    }

    public int getReference(){
        return orderNumber;
    }

//
//    @Override
//    public String getMessage() {
//        return super.getMessage();
//    }
}
