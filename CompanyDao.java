package carsharing;

import java.sql.SQLException;
import java.util.List;

public interface CompanyDao {
    public List<Company> getAllCompany();
    public Company getCompanyById(int id) throws SQLException;

    void addCompany(Company company);

    public Company getCompanyByCarId(int carId) throws SQLException;
}
