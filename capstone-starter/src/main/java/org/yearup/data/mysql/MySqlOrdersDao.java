package org.yearup.data.mysql;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.OrdersDao;
import org.yearup.models.*;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
public class MySqlOrdersDao extends MySqlDaoBase implements OrdersDao {
    public MySqlOrdersDao(DataSource dataSource) {
        super(dataSource);
    }
    @Override
    public Order add(ShoppingCart cart, Profile profile) {
        Map<Integer, ShoppingCartItem> items = cart.getItems();
        try {
            Order order = setOrderByOrderId(profile);
            for(ShoppingCartItem item : items.values()) {
                OrderLineItem lineItem = setLineItem(item, order);
                order.add(lineItem);
            }
            return order;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public Order setOrderByOrderId(Profile profile) {
        String sql = """
                INSERT INTO orders (
                    user_id
                    , date
                    , address
                    , city
                    , state
                    , zip
                    , shipping_amount
                ) VALUES (?, ?, ?, ?, ?, ?, ?);
                """; // add shipping later...
        int newID = 0;
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = now.format(formatter);

        try (Connection con = getConnection()){
            PreparedStatement statement = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            statement.setInt(1, profile.getUserId());
            statement.setString(2, formattedDateTime);
            statement.setString(3, profile.getAddress());
            statement.setString(4, profile.getCity());
            statement.setString(5, profile.getState());
            statement.setString(6, profile.getZip());
            statement.setBigDecimal(7, BigDecimal.valueOf(0));

            statement.executeUpdate();

            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) newID = generatedKeys.getInt(1);
            else throw new RuntimeException("Failed to retrieve generated category ID");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return getOrderById(newID);
    }

    @Override
    public OrderLineItem setLineItem(ShoppingCartItem item, Order order) {
        int newID = 0;
        String sql = """
                INSERT INTO order_line_items (
                    order_id
                    , product_id
                    , sales_price
                    , quantity
                    , discount
                ) VALUES (?, ?, ?, ?, ?);
                """;
        try (Connection con = getConnection()) {
            PreparedStatement statement = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            statement.setInt(1, order.getOrderId());
            statement.setInt(2, item.getProductId());
            statement.setBigDecimal(3, item.getLineTotal());
            statement.setInt(4, item.getQuantity());
            statement.setBigDecimal(5, item.getDiscountPercent());

            statement.executeUpdate();

            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) newID = generatedKeys.getInt(1);
            else throw new RuntimeException("Failed to retrieve generated category ID");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return getOrderLineItemById(newID);
    }

    @Override
    public Order getOrderById(int id) {
        String sql = "SELECT * FROM orders WHERE order_id = ?;";
        try (Connection con = getConnection()) {
            PreparedStatement statement = con.prepareStatement(sql);
            statement.setInt(1, id);

            ResultSet row = statement.executeQuery();
            if (row.next()) {
                return mapToOrder(row);
            } else throw new ResponseStatusException(HttpStatus.NOT_FOUND, "not found");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public OrderLineItem getOrderLineItemById(int id) {
        String sql = "SELECT * FROM order_line_items WHERE order_line_item_id = ?";
        try (Connection con = getConnection()){
            PreparedStatement statement = con.prepareStatement(sql);
            statement.setInt(1, id);

            ResultSet row = statement.executeQuery();
            if (row.next()) {
                int orderId = row.getInt("order_id");
                int productId = row.getInt("product_id");
                BigDecimal salesPrice = row.getBigDecimal("sales_price");
                int quantity = row.getInt("quantity");
                BigDecimal discount = row.getBigDecimal("discount");
                return new OrderLineItem(id, orderId, productId, salesPrice, quantity, discount);
            } else throw new ResponseStatusException(HttpStatus.NOT_FOUND, "not found)");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    private Order mapToOrder(ResultSet row) throws SQLException {
        int orderId  = row.getInt("order_id");
        int userId = row.getInt("user_id");
        String date = row.getString("date");
        String address = row.getString("address");
        String city = row.getString("city");
        String state = row.getString("state");
        String zip = row.getString("zip");
        BigDecimal shipping = row.getBigDecimal("shipping_amount");
        return new Order(orderId, userId, date, address, city, state, zip, shipping);
    }
}
