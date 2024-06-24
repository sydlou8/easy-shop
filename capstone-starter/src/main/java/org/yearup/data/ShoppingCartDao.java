package org.yearup.data;

import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

public interface ShoppingCartDao
{
    ShoppingCart getByUserId(int userId);
    // add additional method signatures here
    ShoppingCart add(int userId, int productId);
    void update(int userId, int productId, ShoppingCartItem item);
    void empty(int userId);
}
