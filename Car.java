package carsharing;

public class Car {
    private String name;
    private int carId;
    private int compId;

    public Car(int compId,String name, int carId) {
        this.name = name;
        this.carId = carId;
        this.compId = compId;
    }

    public String getName() {
        return name;
    }

    public int getCarId() {
        return carId;
    }

    public int getCompId() {
        return compId;
    }
}
