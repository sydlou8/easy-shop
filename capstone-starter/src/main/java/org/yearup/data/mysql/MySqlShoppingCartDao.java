package org.yearup.data.mysql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yearup.controllers.exceptions.ProductNotFoundException;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class MySqlShoppingCartDao extends MySqlDaoBase implements ShoppingCartDao {
    @Autowired
    public MySqlShoppingCartDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public ShoppingCart getByUserId(int userId) {
        ShoppingCart cart = new ShoppingCart();
        try(Connection con = getConnection()) {
            String sql = """
                    SELECT * FROM shopping_cart
                    WHERE user_id = ?;
                    """;
            PreparedStatement statement = con.prepareStatement(sql);
            statement.setInt(1, userId);

            ResultSet row = statement.executeQuery();

            while (row.next()) {
                mapCart(row, cart);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cart;
    }

    @Override
    public ShoppingCart add(int userId, int productId) {
        try (Connection con = getConnection()) {
            // find product with ids
            String sql = "SELECT quantity FROM shopping_cart WHERE user_id = ? AND product_id = ?";
            PreparedStatement statement = con.prepareStatement(sql);
            statement.setInt(1, userId);
            statement.setInt(2, productId);

            ResultSet row = statement.executeQuery();

            if (row.next()) {
                // if product is found, send info to updatemethod
                int existingQuantity = row.getInt(1);
                ShoppingCart updated =  update(userId, productId, existingQuantity + 1);
                return updated;
            } else {
                // otherwise insert
                sql = """
                        INSERT INTO shopping_cart (user_id, product_id, quantity)
                        VALUES (?, ?, 1);
                        """;
                statement = con.prepareStatement(sql);
                statement.setInt(1, userId);
                statement.setInt(2, productId);

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return getByUserId(userId);
    }

    @Override
    public void update(int userId, int productId, ShoppingCartItem item) {
        try (Connection con = getConnection()) {
            String sql = """
                    UPDATE shopping_cart
                    SET quantity = ?
                    WHERE user_id = ?
                        AND product_id = ?;
                    """;
            PreparedStatement statement = con.prepareStatement(sql);
            statement.setInt(1, item.getQuantity());
            statement.setInt(2, userId);
            statement.setInt(3, productId);

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private ShoppingCart update(int userId, int productId, int quantity) {
        try (Connection con = getConnection()) {
            String sql = """
                    UPDATE shopping_cart
                    SET quantity = ?
                    WHERE user_id = ?
                        AND product_id = ?;
                    """;
            PreparedStatement statement = con.prepareStatement(sql);
            statement.setInt(1, quantity);
            statement.setInt(2, userId);
            statement.setInt(3, productId);

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return getByUserId(userId);
    }

    @Override
    public ShoppingCart emptyCart(int userId) {
        try (Connection con = getConnection()) {
            String sql = "DELETE FROM shopping_cart WHERE user_id = ?";
            PreparedStatement statement = con.prepareStatement(sql);
            statement.setInt(1, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return getByUserId(userId);
    }

    private Product getProductById(int productId) {
        String sql = "SELECT * FROM products WHERE product_id = ?";
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, productId);

            ResultSet row = statement.executeQuery();

            if (row.next()) {
                return mapProduct(row);
            } else throw new ProductNotFoundException(productId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    private Product mapProduct (ResultSet row) throws SQLException {
        int productId = row.getInt("product_id");
        String name = row.getString("name");
        BigDecimal price = row.getBigDecimal("price");
        int categoryId = row.getInt("category_id");
        String description = row.getString("description");
        String color = row.getString("color");
        int stock = row.getInt("stock");
        boolean isFeatured = row.getBoolean("featured");
        String imageUrl = row.getString("image_url");

        return new Product(productId, name, price, categoryId, description, color, stock, isFeatured, imageUrl);
    }
    private void mapCart(ResultSet row, ShoppingCart cart) throws SQLException {
        //int userId = row.getInt("user_id");
        int productId = row.getInt("product_id");
        int quantity = row.getInt("quantity");

        ShoppingCartItem item = new ShoppingCartItem() {{
            setProduct(getProductById(productId));
            setQuantity(quantity);
        }};

        cart.add(item);
    }
}

