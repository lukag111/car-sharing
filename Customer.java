package carsharing;

public class Customer {
    private String name;
    private int id;

    private Integer rentedCarId;

    public void setRentedCarId(Integer rentedCarId) {
        this.rentedCarId = rentedCarId;
    }


    public Customer(String name, int id, int rentedCarId) {
        this.name = name;
        this.id = id;
        this.rentedCarId = rentedCarId;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public Integer getIdRentedCar() {
        return rentedCarId;
    }

}
