package org.yearup.data;

import org.yearup.models.*;

import java.util.List;
import java.util.Map;

public interface OrdersDao {
    Order add(ShoppingCart cart, Profile profile);
    Order setOrderByOrderId(Profile profile);
    Order getOrderById(int id);
    //Order getOrderByUserId(Profile profile);
    OrderLineItem setLineItem(ShoppingCartItem item, Order order);
    OrderLineItem getOrderLineItemById(int id);

}
