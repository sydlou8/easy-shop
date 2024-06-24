package org.yearup.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.ProductDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;
import org.yearup.models.User;

import java.security.Principal;

// convert this class to a REST controller
// only logged in users should have access to these actions
@RestController
@CrossOrigin
@RequestMapping("cart")
@PreAuthorize("isAuthenticated()")
public class ShoppingCartController {
    // a shopping cart requires
    private ShoppingCartDao shoppingCartDao;
    private UserDao userDao;
    private ProductDao productDao;

    @Autowired
    public ShoppingCartController(ShoppingCartDao shoppingCartDao, UserDao userDao, ProductDao productDao) {
        this.shoppingCartDao = shoppingCartDao;
        this.userDao = userDao;
        this.productDao = productDao;
    }

    private User getUser (Principal principal) {
        // get the currently logged in username
        String userName = principal.getName();
        // find database user by userId
        return userDao.getByUserName(userName);
    }

    private int getUserId(User user) {
        return user.getId();
    }

    // each method in this controller requires a Principal object as a parameter
    @GetMapping("")
    public ShoppingCart getCart(Principal principal) {
        try {
            int userId = getUserId(getUser(principal));
            // use the shoppingcartDao to get all items in the cart and return the cart
            return shoppingCartDao.getByUserId(userId);
        } catch(Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User Unauthorized");
        }
    }

    // add a POST method to add a product to the cart - the url should be
    // https://localhost:8080/cart/products/15 (15 is the productId to be added
    @PostMapping({"products/{productId}", "products/{productId}/"})
    @ResponseStatus(HttpStatus.CREATED)
    public ShoppingCart addItem(Principal principal, @PathVariable int productId) {
        try {
            int userId = getUserId(getUser(principal));
            return shoppingCartDao.add(userId, productId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User Unauthorized");
        }
    }

    // add a PUT method to update an existing product in the cart - the url should be
    // https://localhost:8080/cart/products/15 (15 is the productId to be updated)
    // the BODY should be a ShoppingCartItem - quantity is the only value that will be updated
    @PutMapping({"products/{productId}", "products/{productId}/"})
    @ResponseStatus(HttpStatus.OK)
    public void update(Principal principal, @PathVariable int productId, @RequestBody ShoppingCartItem item) {
        try {
            int userId = getUserId(getUser(principal));
            shoppingCartDao.update(userId, productId, item);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error Detected");
        }
    }

    // add a DELETE method to clear all products from the current users cart
    // https://localhost:8080/cart
    @DeleteMapping({"", "/"})
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearCart(Principal principal) {
        try {
            int userId = getUserId(getUser(principal));
            shoppingCartDao.empty(userId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error Detected");
        }
    }
}
