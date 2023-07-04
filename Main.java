package carsharing;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class Main {

    private static final String DB_URL = "jdbc:h2:./src/carsharing/db/carsharing";
    private static final String DB_DRIVER = "org.h2.Driver";

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            Class.forName(DB_DRIVER);
            Connection conn = DriverManager.getConnection(DB_URL);
            createCompanyTable(conn);
            createCarTable(conn);
            createCustomerTable(conn);
            CompanyDaoImp companyDaoImp = new CompanyDaoImp();
            CustomerDaoImp cusDao = new CustomerDaoImp();
            CarDaoImp carDaoImp = new CarDaoImp();
            boolean exit = false;
            while (!exit) {
                int choice = displayMainMenu();
                switch (choice) {
                    case 1:
                        managerMenu(conn, companyDaoImp, carDaoImp);
                        break;
                    case 2:
                        customerMenu(conn, cusDao, companyDaoImp, carDaoImp);
                        break;
                    case 3:
                        createCustomer(conn);
                        break;
                    case 4:
                        printAllTables(conn);
                        break;
                    case 0:
                        //cleanUpDatabase(conn);
                        exit = true;
                        break;
                    default:
                        System.out.println("Invalid choice, please try again.");
                        break;
                }
            }
            conn.close();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }
    public static void cleanUpDatabase(Connection conn) throws SQLException {
        String sql= "DELETE FROM CUSTOMER;";
        Statement stmt = conn.createStatement();
        stmt.execute(sql);

        sql = "DELETE FROM CAR;";
        stmt = conn.createStatement();
        stmt.execute(sql);

        sql = "DELETE FROM COMPANY;";
        stmt = conn.createStatement();
        stmt.execute(sql);



        System.out.println("The database has been cleaned up!");
    }

    public static void createCustomer(Connection conn) throws SQLException {
        System.out.println("Enter the customer name:");
        String name = scanner.nextLine();

        // Get the highest existing customer ID
        String selectSql = "SELECT MAX(id) FROM CUSTOMER";
        PreparedStatement selectStatement = conn.prepareStatement(selectSql);
        ResultSet resultSet = selectStatement.executeQuery();
        resultSet.next();
        int maxId = resultSet.getInt(1);

        // Set the ID of the new customer to the next available integer
        int id = maxId + 1;

        // Insert the new customer into the database
        String insertSql = "INSERT INTO CUSTOMER (id, name, rented_car_id) VALUES (?, ?, NULL)";
        PreparedStatement insertStatement = conn.prepareStatement(insertSql);
        insertStatement.setInt(1, id);
        insertStatement.setString(2, name);
        insertStatement.executeUpdate();

        System.out.println("The customer was created with ID " + id + "!");
    }

    private static void createTables(Connection conn) throws SQLException {
        createCompanyTable(conn);
        createCarTable(conn);
        createCustomerTable(conn);
    }

    private static int displayMainMenu() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("1. Log in as a manager");
        System.out.println("2. Log in as a customer");
        System.out.println("3. Create a customer");
        System.out.println("0. Exit");
        int choice = Integer.parseInt(scanner.nextLine());
        return choice;
    }


    private static void customerMenu(Connection conn, CustomerDaoImp cusDao, CompanyDaoImp cdi, CarDaoImp carDaoImp) throws SQLException {
        boolean returned = false;
        Scanner scanner = new Scanner(System.in);
        String selectSql = "SELECT id, name, rented_car_id FROM CUSTOMER";
        PreparedStatement selectStatement = conn.prepareStatement(selectSql);
        ResultSet resultSet = selectStatement.executeQuery();
        List<Customer> customers = new ArrayList<>();
        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            String name = resultSet.getString("name");
            int idCar = resultSet.getInt("rented_car_id");
            Customer customer = new Customer(name, id, idCar);
            customers.add(customer);
        }
        if (customers.isEmpty()) {
            System.out.println("The customer list is empty!");
            return;
        }
        System.out.println("Choose the customer: ");
        for (int i = 0; i < customers.size(); i++) {
            Customer customer = customers.get(i);
            System.out.println((i + 1) + ". " + customer.getName());
        }
        System.out.println("0. Back");

        String input = scanner.nextLine();
        if (input.equals("0")) {
            return;  // exit the method and go back to the previous menu
        }
        if (input.isEmpty()) {
            System.out.println("Invalid input!");
            return;
        }
        int c = Integer.parseInt(input);
        if (c == 0) return;
        Customer cus = cusDao.getCustomerById(c);
        boolean flag = true;
        while (flag) {
            System.out.println("1. Rent a car\n" +
                    "2. Return a rented car\n" +
                    "3. My rented car\n" +
                    "0. Back");
            String input1 = scanner.nextLine();
            int o = Integer.parseInt(input1);
            switch (o) {
                case 1:
                    Integer rentedCarId = cus.getIdRentedCar();
                    if(rentedCarId == null || rentedCarId == 0) {
                        int a = showCompanyList(conn);

                        String input2 = scanner.nextLine();
                        int compID = Integer.parseInt(input2);

                        Company company = cdi.getCompanyById(compID);
                        rentCar(compID, conn, cdi, carDaoImp, cusDao, cus);
                        returned = false;
                    } else {
                        System.out.println("You've already rented a car!");
                    }
                    break;

                case 2:
                    if (returned) {
                        System.out.println("You've returned a rented car!");
                        break;
                    } else {
                        if (cus.getIdRentedCar() == 0) {
                            System.out.println("You didn't rent a car!");
                            break;
                        }else {
                            System.out.println("You've returned a rented car!");
                        }
                        int rentedCarId2 = cus.getIdRentedCar();
                        Car rentedCar = carDaoImp.getCarById(rentedCarId2);

                        if (rentedCar == null) {
                            System.out.println("The rented car is invalid!");
                            break;
                        }

                        // Set customer's rented car ID to null
                        cus.setRentedCarId(null);

                        String sql = "UPDATE CUSTOMER SET RENTED_CAR_ID = NULL WHERE ID = ?";
                        PreparedStatement statement = conn.prepareStatement(sql);
                        statement.setInt(1, cus.getId());
                        statement.executeUpdate();

                        returned = true;
                        System.out.println("You have returned the rented car '" + rentedCar.getName() + "'.");
                        break;
                    }

                case 3:
                    Car car = carDaoImp.getCarById(cus.getIdRentedCar());
                    if (car == null) {
                        System.out.println("You didn't rent a car!");
                        break;
                    }
                    System.out.println("Your rented car:\n" + car.getName());
                    Company cp = cdi.getCompanyById(car.getCarId());
                    if (cp == null) {
                        System.out.println("Company not found!");
                    }
                    System.out.println("Company:\n" + cp.getName());
                    break;

                case 0:
                    flag = false;
                    break;
            }
        }
    }

    public static void rentCar(int compID, Connection conn, CompanyDaoImp cdi, CarDaoImp carDaoImp, CustomerDaoImp customerDaoImp, Customer cus) throws SQLException {
        Company company = cdi.getCompanyById(compID);
        if (company == null) {
            System.out.println("Company not found!");
            return;
        }
        if (cus == null && cus.getIdRentedCar()==0) {
            System.out.println("Customer error!");
            return;
        }

        List<Car> carList = carDaoImp.getCarsFromC(compID);

        String sql = "SELECT * FROM CAR " +
                "WHERE COMPANY_ID = ? " +
                "AND ID NOT IN (SELECT RENTED_CAR_ID FROM CUSTOMER WHERE RENTED_CAR_ID IS NOT NULL)";
        PreparedStatement pstmt = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        pstmt.setInt(1, company.getId());
        ResultSet rs = pstmt.executeQuery();
        if (!rs.isBeforeFirst()) {
            System.out.println("The car list is empty!");
        } else {
            int i = 1;
            while (rs.next()) {
                String carName = rs.getString("name");
                System.out.println(i + ". " + carName);
                i++;
            }
            System.out.println();
        }
        Integer rentedCarId = cus.getIdRentedCar();
        if(rentedCarId != null && rentedCarId != 0) {
            System.out.println("You've already rented a car!");
            return;
        }

        int selectedCarIndex = Integer.parseInt(scanner.nextLine());
        if (selectedCarIndex < 1 || selectedCarIndex > carList.size()) {
            System.out.println("Invalid input!");
            return;
        }
        rs.absolute(selectedCarIndex); // Move cursor to the selected row
        int carId = rs.getInt("ID"); // Get the ID of the selected car
        String carName = rs.getString("name");
        System.out.println("You rented '" + carName + "'");

        // Assume conn is a valid database connection
        int customerId = cus.getId();

        // Check if the car ID exists in the CAR table
        String checkCarSql = "SELECT ID FROM CAR WHERE ID = ?";
        PreparedStatement checkCarStmt = conn.prepareStatement(checkCarSql);
        checkCarStmt.setInt(1, carId);
        ResultSet carResult = checkCarStmt.executeQuery();

        if (carResult.next()) {
            // Car ID exists, update the rented car ID for the customer
            String updateSql = "UPDATE CUSTOMER SET RENTED_CAR_ID = ? WHERE ID = ? AND RENTED_CAR_ID IS NULL";
            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setInt(1, carId);
            updateStmt.setInt(2, customerId);
            cus.setRentedCarId(carId);
            int rowsUpdated = updateStmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Rented car updated successfully.");
            } else {
                System.out.println("Failed to update rented car.");
            }
        } else {
            // Car ID does not exist, handle error
            System.out.println("Invalid car ID.");
        }
    }

    private static void createCompanyTable(Connection conn) throws SQLException {
        String createTableSql = "CREATE TABLE IF NOT EXISTS COMPANY (" +
                "id INT PRIMARY KEY AUTO_INCREMENT, " +
                "name VARCHAR(25) NOT NULL UNIQUE)";
        Statement stmt = conn.createStatement();
        stmt.execute(createTableSql);
    }
    private static void createCarTable(Connection conn) throws SQLException {
        String createTableSql = "CREATE TABLE IF NOT EXISTS CAR (" +
                "  ID INT PRIMARY KEY AUTO_INCREMENT," +
                "  NAME VARCHAR(255) NOT NULL UNIQUE," +
                "  COMPANY_ID INT NOT NULL," +
                "  FOREIGN KEY (COMPANY_ID) REFERENCES COMPANY(ID)" +
                ");";

        Statement stmt = conn.createStatement();
        stmt.execute(createTableSql);
    }
    private static void createCustomerTable(Connection conn) throws SQLException {
        String createTableSql = "CREATE TABLE IF NOT EXISTS CUSTOMER (" +
                "  ID INT PRIMARY KEY AUTO_INCREMENT," +
                "  NAME VARCHAR(255) NOT NULL," +
                "  RENTED_CAR_ID INT," +
                "  FOREIGN KEY (RENTED_CAR_ID) REFERENCES CAR(ID)" +
                ");";
        Statement stmt = conn.createStatement();
        stmt.execute(createTableSql);
    }

    private static void managerMenu(Connection conn, CompanyDaoImp cdi, CarDaoImp carDaoImp) throws SQLException {
        boolean exit = false;
        while (!exit) {
            System.out.println("1. Company list");
            System.out.println("2. Create a company");
            System.out.println("0. Back");
            int choice = Integer.parseInt(scanner.nextLine());
            switch (choice) {
                case 1:
                    int f = showCompanyList(conn);
                    if (f==1) break;
                    String input = scanner.nextLine();
                    if (input.equals("0")) {
                        break;  // exit the loop and go back to the previous menu
                    }
                    if (input.isEmpty()) {
                        System.out.println("Invalid input!");
                        break;
                    }
                    int c = Integer.parseInt(input);
                    if (c==0) break;
                    Company cp = cdi.getCompanyById(c);
                    boolean flag = true;
                    List<Car> cars = carDaoImp.getCarsFromC(cp.getId());
                    while (flag){
                        System.out.println("'"+cp.getName()+"'"+" company:");
                        System.out.println("1. Car list\n" +
                                "2. Create a car\n" +
                                "0. Back");
                        String input1 = scanner.nextLine();
                        int o = Integer.parseInt(input1);
                        switch (o){
                            case 2:
                                System.out.println("Enter the car name:");
                                String name = scanner.nextLine();
                                String insertSql = "INSERT INTO CAR (name, company_id) VALUES (?, ?)";
                                PreparedStatement insertStatement = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
                                insertStatement.setString(1, name);
                                insertStatement.setInt(2, cp.getId());
                                insertStatement.executeUpdate();

                                System.out.println("The car was created!");

                                ResultSet generatedKeys = insertStatement.getGeneratedKeys();
                                if (generatedKeys.next()) {
                                    int carId = generatedKeys.getInt(1);
                                    Car car = new Car(carId, name, cp.getId());
                                }


// Fetch the updated list of cars
                                String selectSql = "SELECT id, name FROM CAR WHERE company_id = ?";
                                PreparedStatement selectStatement = conn.prepareStatement(selectSql);
                                selectStatement.setInt(1, cp.getId());
                                ResultSet resultSet = selectStatement.executeQuery();
                                while (resultSet.next()) {
                                    int id = resultSet.getInt("id");
                                    String name1 = resultSet.getString("name");
                                    Car car = new Car(id, name1, cp.getId());
                                    cars.add(car);
                                }


                                break;
                            case 1:
                                /*selectSql = "SELECT id, name FROM CAR WHERE company_id = ?";
                                selectStatement = conn.prepareStatement(selectSql);
                                selectStatement.setInt(1, cp.getId());
                                resultSet = selectStatement.executeQuery();
                                List<Car> cars1 = new ArrayList<>();
                                while (resultSet.next()) {
                                    int id1 = resultSet.getInt("id");
                                    String name1 = resultSet.getString("name");
                                    Car car = new Car(id1, name1, cp.getId());
                                    cars1.add(car);
                                }

                                if (cars1.isEmpty()) {
                                    System.out.println("The car list is empty!");
                                } else {
                                    System.out.println("Choose the car: ");
                                    for (int i = 0; i < cars1.size(); i++) {
                                        Car car = cars1.get(i);
                                        System.out.println(car.getCarId() + ". " + car.getName());
                                    }
                                }*/

                                String sql = "SELECT * FROM CAR WHERE COMPANY_ID = ?";
                                PreparedStatement pstmt = conn.prepareStatement(sql);
                                pstmt.setInt(1, cp.getId());
                                ResultSet rs = pstmt.executeQuery();
                                if (!rs.isBeforeFirst()) {
                                    System.out.println("The car list is empty!");
                                } else {
                                    int i = 1;
                                    while (rs.next()) {
                                        String carName = rs.getString("name");
                                        System.out.println(i + ". " + carName);
                                        i++;
                                    }
                                    System.out.println();
                                }

                                break;
                            case 0:
                                flag = false;
                                break;
                        }
                    }

                    break;
                case 2:
                    createCompany(conn);
                    break;
                case 0:
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid choice, please try again.");
                    break;
            }
        }
    }

    private static int showCompanyList(Connection conn) throws SQLException {
        String selectSql = "SELECT id, name FROM COMPANY";
        PreparedStatement selectStatement = conn.prepareStatement(selectSql);
        ResultSet resultSet = selectStatement.executeQuery();
        List<Company> companies = new ArrayList<>();
        while (resultSet.next()) {
            int id = resultSet.getInt("id");
            String name = resultSet.getString("name");
            Company company = new Company(id, name);
            companies.add(company);
        }
        if (companies.isEmpty()) {
            System.out.println("The company list is empty!");
            return 1;
        } else {
            System.out.println("Choose the company: ");
            for (int i = 0; i < companies.size(); i++) {
                Company company = companies.get(i);
                System.out.println((i + 1) + ". " + company.getName());
            }
            System.out.println("0. Back");
        }
        return 0;
    }

    private static void createCompany(Connection conn) throws SQLException {
        System.out.println("Enter the company name:");
        String name = scanner.nextLine();

        // Get the number of existing companies in the database
        String countSql = "SELECT COUNT(*) FROM COMPANY";
        PreparedStatement countStatement = conn.prepareStatement(countSql);
        ResultSet countResult = countStatement.executeQuery();
        countResult.next();
        int numCompanies = countResult.getInt(1);

        // Set the ID of the new company to the next available integer
        int id = numCompanies + 1;

        // Insert the new company into the database
        String insertSql = "INSERT INTO COMPANY (id, name) VALUES (?, ?)";
        PreparedStatement insertStatement = conn.prepareStatement(insertSql);
        insertStatement.setInt(1, id);
        insertStatement.setString(2, name);
        insertStatement.executeUpdate();

        System.out.println("The company was created with ID " + id + "!");
    }

    public static void printAllTables(Connection conn) throws SQLException {
        // Print Company table
        System.out.println("COMPANY TABLE:");
        String selectCompanySql = "SELECT * FROM COMPANY";
        PreparedStatement companyStmt = conn.prepareStatement(selectCompanySql);
        ResultSet companyRs = companyStmt.executeQuery();
        while (companyRs.next()) {
            int id = companyRs.getInt("id");
            String name = companyRs.getString("name");
            System.out.println(id + "\t" + name);
        }
        System.out.println();

        // Print Car table
        System.out.println("CAR TABLE:");
        String selectCarSql = "SELECT * FROM CAR";
        PreparedStatement carStmt = conn.prepareStatement(selectCarSql);
        ResultSet carRs = carStmt.executeQuery();
        while (carRs.next()) {
            int id = carRs.getInt("id");
            String name = carRs.getString("name");
            int companyId = carRs.getInt("company_id");
            System.out.println(id + "\t" + name + "\t" + companyId);
        }
        System.out.println();

        // Print Customer table
        System.out.println("CUSTOMER TABLE:");
        String selectCustomerSql = "SELECT * FROM CUSTOMER";
        PreparedStatement customerStmt = conn.prepareStatement(selectCustomerSql);
        ResultSet customerRs = customerStmt.executeQuery();
        while (customerRs.next()) {
            int id = customerRs.getInt("id");
            String name = customerRs.getString("name");
            int rentedCarId = customerRs.getInt("rented_car_id");
            System.out.println(id + "\t" + name + "\t" + rentedCarId);
        }
        System.out.println();
    }


}





