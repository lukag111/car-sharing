package carsharing;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CarDaoImp<conn> implements CarDao{

    @Override
    public List<Car> getAllCars() {
        List<Car> cars = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        try {
            Class.forName("org.h2.Driver");
            conn = DriverManager.getConnection("jdbc:h2:./src/carsharing/db/carsharing");
            stmt = conn.createStatement();
            String sql = "SELECT * FROM CAR";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                int companyId = rs.getInt("company_id");
                cars.add(new Car(id, name, companyId));
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
        return cars;
    }

    @Override
    public List<Car> getCarsFromC(int companyId) throws SQLException {

        List<Car> carList = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection("jdbc:h2:./src/carsharing/db/carsharing")) {
            String sql = "SELECT * FROM CAR WHERE company_id=?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, companyId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                Car car = new Car(id, name, companyId);
                carList.add(car);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return carList;
    }

    @Override
    public Car getCarById(int id) throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:h2:./src/carsharing/db/carsharing");
        String sql = "SELECT * FROM CAR WHERE id=?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, id);
        ResultSet resultSet = statement.executeQuery();
        Car car = null;
        while (resultSet.next()) {
            int idCar = resultSet.getInt("id");
            String name = resultSet.getString("name");
            int idCom = resultSet.getInt("company_id");
            car = new Car(idCar, name, idCom);
        }
        return car;
    }



}
