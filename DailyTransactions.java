import java.sql.*;
import java.util.*;

public class DailyTransactions implements inventoryControl {

    @Override
    public void Ship_order(int orderNumber) throws OrderException {
        try {
            Connection connection = connection();
            Statement statement = null;     // creating statement class object to build up SQL queries
            ResultSet resultSet = null;     // Creating ResultSet data structure to receive results from an SQL query
            // Creating instances of Statements which allows to issue queries to the DB
            statement = connection.createStatement();
            Properties identity_1 = new Properties();  //  Using a properties structure, just to hide info from other users.
            MyIdentity me_1 = new MyIdentity();        //  My own class to identify my credentials.  Ideally load Properties from a file instead and this class disappears.
            String dbName;
            me_1.setIdentity(identity_1);                   // Fill the properties structure with my info.  Ideally, load properties from a file instead to replace this bit.
            dbName = identity_1.getProperty("database");
            statement.executeQuery("use " + dbName + ";");

            String query = "update orders set ShippedDate=now() where OrderID=" + orderNumber + " and ShippedDate is null;";
            statement.executeUpdate(query);

            String query2 = "select products.ProductID, orderdetails.Quantity, products.UnitsInStock " +
                    "from products join orderdetails where products.ProductID=orderdetails.ProductID and OrderID="+orderNumber+";";
            resultSet = statement.executeQuery(query2);
            boolean negativeFlag = true;
            while(resultSet.next()){
                int unitInStock = resultSet.getInt("UnitsInStock");
                int qty = resultSet.getInt("Quantity");
                if((unitInStock-qty)<0){
                    negativeFlag = false;
                }
            }
            if(negativeFlag) {
                String query3 = "update products as pdt join orderdetails as ord set pdt.UnitsInStock = pdt.UnitsInStock - ord.Quantity " +
                        "where ord.ProductID = pdt.ProductID and ord.OrderID = " + orderNumber + ";";
                statement.executeUpdate(query3);
            }
            else{
                throw new OrderException("The following orderID has orders with quanity greater than unitsInStock", orderNumber);
            }
        } catch (ClassNotFoundException e) {

        } catch (SQLException e) {

        }
//        catch (OrderException e){
//            System.out.println(e.getMessage());
//        }
    }

    @Override
    public int Issue_reorders(int year, int month, int day) {
        int count_Orders = 0;
        try {
            Connection connection = connection();
            Statement statement = null;     // creating statement class object to build up SQL queries
            ResultSet resultSet = null;     // Creating ResultSet data structure to receive results from an SQL query
            //ResultSet resultSet2 = null;     // Creating ResultSet data structure to receive results from an SQL query
            // Creating instances of Statements which allows to issue queries to the DB
            statement = connection.createStatement();
            Properties identity_1 = new Properties();  //  Using a properties structure, just to hide info from other users.
            MyIdentity me_1 = new MyIdentity();        //  My own class to identify my credentials.  Ideally load Properties from a file instead and this class disappears.
            String dbName;
            me_1.setIdentity(identity_1);                   // Fill the properties structure with my info.  Ideally, load properties from a file instead to replace this bit.
            dbName = identity_1.getProperty("database");
            statement.executeQuery("use " + dbName + ";");

            String date = year+"-"+month+"-"+day;
            //System.out.printf(date);


//            String query = "with new_order as (select orders.OrderID,orders.ShipVia " +
//                    "from orders where OrderDate <='"+date+"' or ShippedDate <='"+date+"') " +
//                    "select s.supplierID,p.ProductID,p.ReorderLevel from new_order\n" +
//                    "join orderdetails on orderdetails.OrderID = new_order.OrderID " +
//                    "join products p on orderdetails.ProductID = p.ProductID " +
//                    "join suppliers s on p.SupplierID = s.SupplierID " +
//                    "where p.UnitsInStock<p.ReorderLevel and p.ProductID not in " +
//                    "(select ProductID from orderPurchased natural join purchaseDetails where arrivedDate is NULL) " +
//                    "group by p.ProductID;";

            String query = "select distinct p.ProductID,s.supplierID,p.ReorderLevel from orders new_order\n" +
                    "join orderdetails on orderdetails.OrderID = new_order.OrderID " +
                    "join products p on orderdetails.ProductID = p.ProductID " +
                    "join suppliers s on p.SupplierID = s.SupplierID " +
                    "where p.UnitsInStock+p.UnitsOnOrder<p.ReorderLevel";

            resultSet = statement.executeQuery(query);

            HashMap<Integer, ArrayList<String>> suEntry = new HashMap<>();
            HashMap<Integer, ArrayList<String>> prIDEntry = new HashMap<>();
            HashMap<Integer, ArrayList<String>> usEntry = new HashMap<>();
            HashMap<Integer, ArrayList<String>> prEntry = new HashMap<>();
            while (resultSet.next()){
                int supplierID = Integer.parseInt(resultSet.getString("supplierID"));
                int shipVia = 1;
                String pID = resultSet.getString("ProductID");
                String units = resultSet.getString("ReorderLevel");


                String price = null;

                if(!suEntry.containsKey(supplierID)) {
                    suEntry.put(supplierID, new ArrayList<>());
                    prIDEntry.put(supplierID, new ArrayList<>());
                    usEntry.put(supplierID, new ArrayList<>());
                    prEntry.put(supplierID, new ArrayList<>());
                }

                suEntry.get(supplierID).add(""+shipVia);
                prIDEntry.get(supplierID).add(pID);
                usEntry.get(supplierID).add(units);
                prEntry.get(supplierID).add(price);
            }

            for (Integer sID: suEntry.keySet()) {
                String query2 = "insert into orderPurchased (SupplierID, placedDate, ShipperID, trackingID) " +
                        "values ( " + sID + ", now(), " + suEntry.get(sID).get(0) + ", '" + RequiredString(20) + "');";
                statement.execute(query2);
                String query3 ="select max(OrderId) as maxOrderID from orderPurchased";
                resultSet = statement.executeQuery(query3);
                resultSet.next();
                String orderID = resultSet.getString("maxOrderID");
                for(int i = 0; i<suEntry.get(sID).size();i++)
                {
                    query2 = "insert into purchaseDetails (OrderID, ProductID, Units) " +
                            "values ( " + orderID+", "+ prIDEntry.get(sID).get(i)+ ", " + usEntry.get(sID).get(i) + ");";
                    statement.execute(query2);

                    query3 = "Update products set UnitsOnOrder=UnitsOnOrder+"+usEntry.get(sID).get(i)+" where ProductID="+prIDEntry.get(sID).get(i)+";";
                    statement.execute(query3);
                }
            }
            count_Orders = suEntry.size();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //return 0;
        return count_Orders;
    }

    //Reference https://www.edureka.co/blog/random-number-and-string-generator-in-java/#Java.util.Random
    static String RequiredString(int number)
    {
        String aString = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvxyz";   // choosing a random string
        StringBuilder stringBuilder = new StringBuilder(number);
        for ( int x = 0; x < number; x++) {
            int randomIndex = (int)(aString.length() * Math.random()); // generating random number
            stringBuilder.append(aString.charAt(randomIndex));  // add Character one by one in end of stringBuilder
        }
        return stringBuilder.toString();
    }

    @Override
    public void Receive_order(int internal_order_reference) throws OrderException {
        try {
            Connection connection = connection();
            Statement statement = null;     // creating statement class object to build up SQL queries
            ResultSet resultSet = null;     // Creating ResultSet data structure to receive results from an SQL query
            // Creating instances of Statements which allows to issue queries to the DB
            statement = connection.createStatement();
            Properties identity_1 = new Properties();  //  Using a properties structure, just to hide info from other users.
            MyIdentity me_1 = new MyIdentity();        //  My own class to identify my credentials.  Ideally load Properties from a file instead and this class disappears.
            String dbName;
            me_1.setIdentity(identity_1);                   // Fill the properties structure with my info.  Ideally, load properties from a file instead to replace this bit.
            dbName = identity_1.getProperty("database");
            statement.executeQuery("use " + dbName + ";");

            String query = "update orderPurchased set arrivedDate=now() where OrderID="+internal_order_reference+" and arrivedDate is null;;";
            statement.executeUpdate(query);

            String query2 = "update products as pdt join purchaseDetails pD set UnitsInStock = UnitsInStock + Units where " +
                            "pdt.ProductID=pD.ProductID and OrderID ="+internal_order_reference+";";
            statement.executeUpdate(query2);

            query2 = "update products as pdt join purchaseDetails pD set UnitsOnOrder = UnitsOnOrder - Units where " +
                    "pdt.ProductID=pD.ProductID and OrderID ="+internal_order_reference+";";

            statement.executeUpdate(query2);

            resultSet = statement.executeQuery("select ProductID from purchaseDetails where OrderID="+internal_order_reference);
            while(resultSet.next()) {
//                System.out.println("xyz");
                String pID = resultSet.getString("ProductID");
//                System.out.println(pID);
                Statement statement1 = connection.createStatement();

//                System.out.println("select p.UnitPrice " +
//                        "from orderdetails,products p,orders " +
//                        "where orderdetails.OrderID = orders.OrderID and p.ProductID=orderdetails.ProductID and p.ProductID=" + pID + " order by OrderID DESC LIMIT 1 ");

                ResultSet rs = statement1.executeQuery("select p.UnitPrice " +
                        "from orderdetails,products p,orders " +
                        "where orderdetails.OrderID = orders.OrderID and p.ProductID=orderdetails.ProductID and p.ProductID=" + pID + " order by orders.OrderID DESC LIMIT 1 ");

                rs.next();
                String price = "" + Float.parseFloat(rs.getString("UnitPrice")) / 1.15;
                query2 = "update purchaseDetails set productCost = "+price+" where " +
                        " OrderID ="+internal_order_reference+" and ProductID="+pID+";";

                statement1.executeUpdate(query2);


                query2 = "update products set UnitPrice = "+price+" where ProductID="+pID+";";
                statement1.executeUpdate(query2);
            }


        } catch (ClassNotFoundException e) {
        } catch (SQLException e) {
        }
    }

    public static Connection connection() throws ClassNotFoundException {
        // Variables used for connections and queries
        Connection connect = null;      // the link to the database
        Properties identity = new Properties();  //  Using a properties structure, just to hide info from other users.
        MyIdentity me = new MyIdentity();        //  My own class to identify my credentials.  Ideally load Properties from a file instead and this class disappears.

        String user;
        String password;
        String dbName;
        String query = "";              // Creating attributes for various queries.

        me.setIdentity(identity);                   // Fill the properties structure with my info.  Ideally, load properties from a file instead to replace this bit.
        user = identity.getProperty("user");
        password = identity.getProperty("password");
        dbName = identity.getProperty("database");

        try {
            // This code will load the MySQL driver and each DB has its own driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Setup the connection with the DB
            connect = DriverManager.getConnection("jdbc:mysql://db.cs.dal.ca:3306?serverTimezone=UTC&useSSL=false", user, password);


        } catch (SQLException ex) {
        } finally {
            // Closing the resultSet, statements, and connections that are open and holding resources.
            try {
//                if (connect != null) {
//                    connect.close();
//                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        return connect;
    }

}
