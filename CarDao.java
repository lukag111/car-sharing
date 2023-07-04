package carsharing;

import java.sql.SQLException;
import java.util.List;

public interface CarDao {

    public List<Car> getAllCars();

    public List<Car> getCarsFromC(int id) throws SQLException;

    public Car getCarById(int id) throws SQLException;

}
