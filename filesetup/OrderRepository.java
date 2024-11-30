package filesetup;

import objects.Order;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OrderRepository {

    private static OrderRepository instance;
    private final List<Order> orders;

    private OrderRepository() {
        orders = new ArrayList<>();
    }

    public static synchronized OrderRepository getInstance() {
        if (instance == null) {
            instance = new OrderRepository();
        }
        return instance;
    }
    
    public List<Order> getAllOrders() {
        return Collections.unmodifiableList(orders);
    }

    public void addOrder(Order order) {
        orders.add(order);
    }

    public void clearOrder() {
        orders.clear();
    }

    public synchronized String deleteOrderById(int id){
        for(Order order : orders){
            if(order.getId() == id){
                orders.remove(order);
                return "Order is removed..";
            }
        }
        return "Cannot find inputed Id..";
    }
}
