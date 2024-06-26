package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.OrdersDao;
import org.yearup.data.ProfileDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.Order;
import org.yearup.models.OrderLineItem;
import org.yearup.models.Profile;
import org.yearup.models.ShoppingCart;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin
@PreAuthorize("isAuthenticated()")
@RequestMapping("orders")
public class OrdersController extends UserBase {
    private OrdersDao ordersDao;
    private ShoppingCartDao shoppingCartDao;
    private ProfileDao profileDao;

    private List<ShoppingCart> orders = new ArrayList<>();

    @Autowired
    public OrdersController(ShoppingCartDao shoppingCartDao, UserDao userDao, ProfileDao profileDao, OrdersDao ordersDao) {
        super(userDao);
        this.shoppingCartDao = shoppingCartDao;
        this.profileDao = profileDao;
        this.ordersDao = ordersDao;
    }

    @PostMapping({"", "/"})
    @ResponseStatus(HttpStatus.CREATED)
    public Order checkout(Principal principal) {
        try {
            int userId = getUserId(getUser(principal));
            ShoppingCart cart = shoppingCartDao.getByUserId(userId);
            Profile profile = profileDao.getProfile(userId);
            // add cart to orders
            Order order = ordersDao.add(cart, profile);
            // clear current cart
            shoppingCartDao.empty(userId);
            return order;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized action");
        }
    }

}
