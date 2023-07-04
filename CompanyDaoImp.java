package carsharing;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CompanyDaoImp implements CompanyDao {

    @Override
    public List<Company> getAllCompany() {
        List<Company> companies = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        try {
            Class.forName("org.h2.Driver");
            conn = DriverManager.getConnection("jdbc:h2:./src/carsharing/db/carsharing");
            stmt = conn.createStatement();
            String sql = "SELECT * FROM COMPANY";
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                companies.add(new Company(id, name));
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
        return companies;
    }

    @Override
    public Company getCompanyById(int id) throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:./src/carsharing/db/carsharing");
        String sql = "SELECT * FROM COMPANY WHERE ID = ?";
        PreparedStatement statement = conn.prepareStatement(sql);
        statement.setInt(1, id);
        ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                int compId = rs.getInt("ID");
                String compName = rs.getString("NAME");
                Company company = new Company(compId, compName);
                return company;
            }

            return null;
        }



        @Override
    public void addCompany(Company company) {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            Class.forName("org.h2.Driver");
            conn = DriverManager.getConnection("jdbc:h2:./src/carsharing/db/carsharing");
            String sql = "INSERT INTO COMPANY (name) VALUES (?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, company.getId());
            stmt.setString(2, company.getName());
            stmt.executeUpdate();
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Company getCompanyByCarId(int carId) throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:./src/carsharing/db/carsharing");
        String sql = "SELECT COMPANY.NAME FROM CAR JOIN COMPANY ON CAR.ID_COMPANY = COMPANY.ID WHERE CAR.ID = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, carId);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            int companyId = rs.getInt("id");
            String companyName = rs.getString("name");
            return new Company(companyId, companyName);
        }
        return null;
    }

}
