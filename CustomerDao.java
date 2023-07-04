package carsharing;

import java.util.List;

public interface CustomerDao{
    public List<Customer> getAllCustomer();
    public Customer getCustomerById(int id);
    public void addCustomer(Customer customer);
}
