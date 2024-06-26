package org.yearup.data;

import org.yearup.models.*;


public interface OrdersDao {
    Order add(ShoppingCart cart, Profile profile);
    Order setOrderByOrderId(Profile profile);
    Order getOrderById(int id);
    OrderLineItem setLineItem(ShoppingCartItem item, Order order);
    OrderLineItem getOrderLineItemById(int id);

}
