package carsharing;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDaoImp implements CustomerDao{

    @Override
    public List<Customer> getAllCustomer() {
        List<Customer> customers = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        try {
            Class.forName("org.h2.Driver");
            conn = DriverManager.getConnection("jdbc:h2:./src/carsharing/db/carsharing");
            stmt = conn.createStatement();
            String sql = "SELECT * FROM CUSTOMER";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                int carId = rs.getInt("RENTED_CAR_ID");
                customers.add(new Customer(name, id, carId));
            }
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return customers;
    }

    @Override
    public Customer getCustomerById(int id) {
        Connection conn = null;
        PreparedStatement stmt = null;
        Customer customer = null;
        try {
            Class.forName("org.h2.Driver");
            conn = DriverManager.getConnection("jdbc:h2:./src/carsharing/db/carsharing");
            String sql = "SELECT * FROM CUSTOMER WHERE id=?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int idC = rs.getInt("id");
                String name = rs.getString("name");
                int carId = rs.getInt("RENTED_CAR_ID");
                customer = new Customer(name, idC, carId);
            }
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return customer;
    }


    @Override
    public void addCustomer(Customer customer) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            Class.forName("org.h2.Driver");
            conn = DriverManager.getConnection("jdbc:h2:./src/carsharing/db/carsharing");
            String sql = "INSERT INTO CUSTOMER (id, name, carID) VALUES (?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, customer.getId());
            stmt.setString(2, customer.getName());
            stmt.setInt(3, customer.getIdRentedCar());
            stmt.executeUpdate();
        }catch (ClassNotFoundException | SQLException e){
            throw new RuntimeException(e);
        }
    }
}
