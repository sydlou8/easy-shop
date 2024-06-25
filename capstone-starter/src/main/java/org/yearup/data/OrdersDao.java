package org.yearup.data;

import org.yearup.models.Order;
import org.yearup.models.OrderLineItem;
import org.yearup.models.Profile;
import org.yearup.models.ShoppingCart;

import java.util.List;
import java.util.Map;

public interface OrdersDao {
    void add(ShoppingCart cart, Profile profile);
    Order getOrderById(int id);

    OrderLineItem getOrderLineItemById(int id);

    Map<Order, List<OrderLineItem>> getOrders();
}
