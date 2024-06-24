package org.yearup.data.mysql;

import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.yearup.controllers.exceptions.ProductNotFoundException;
import org.yearup.models.Product;
import org.yearup.data.ProductDao;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class MySqlProductDao extends MySqlDaoBase implements ProductDao
{
    @Autowired
    public MySqlProductDao(DataSource dataSource)
    {
        super(dataSource);
    }

    @Override
    public List<Product> search(Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice, String color) {
        List<Product> products = new ArrayList<>();

        String sql = "SELECT * FROM products WHERE 1=1";

        String categorySQL = (categoryId != null) ? " AND category_id = ?" : "";
        String minPriceSQL = (minPrice != null) ? " AND price >= ?" : "";
        String maxPriceSQL = (maxPrice != null) ? " AND price <= ?" : "";
        String colorSQL = (color != null && !color.isEmpty()) ? " AND color = ?" : "";

        String sqlFinal = String.format("%s %s %s %s %s", sql, categorySQL, minPriceSQL, maxPriceSQL, colorSQL);

        try (Connection connection = getConnection()) {
            int i = 1;
            PreparedStatement statement = connection.prepareStatement(sqlFinal);
            if (categoryId != null) statement.setInt(i++, categoryId);
            if (minPrice != null) statement.setBigDecimal(i++, minPrice);
            if (maxPrice != null) statement.setBigDecimal(i++, maxPrice);
            if (color != null && !color.isEmpty()) statement.setString(i, color);

            ResultSet row = statement.executeQuery();

            while (row.next()) {
                Product product = mapRow(row);
                products.add(product);
            }
        }
        catch (SQLException | NullPointerException e) {
            throw new RuntimeException(e);
        }

        return products;
    }

    @Override
    public List<Product> listByCategoryId(int categoryId) {
        List<Product> products = new ArrayList<>();
        String sql = """
                    SELECT * FROM products
                    WHERE category_id = ?;
                    """;
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, categoryId);

            ResultSet row = statement.executeQuery();

            while (row.next()) {
                Product product = mapRow(row);
                products.add(product);
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return products;
    }

    @Override
    public Product getById(int productId) {
        String sql = "SELECT * FROM products WHERE product_id = ?";
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, productId);

            ResultSet row = statement.executeQuery();

            if (row.next()) {
                return mapRow(row);
            } else throw new ProductNotFoundException(productId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Product create(Product product) {
        int newID = 0;
        String sql = """
                INSERT INTO products (name, price, category_id, description, color, image_url, stock, featured)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?);
                """;

        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            statement.setString(1, product.getName());
            statement.setBigDecimal(2, product.getPrice());
            statement.setInt(3, product.getCategoryId());
            statement.setString(4, product.getDescription());
            statement.setString(5, product.getColor());
            statement.setString(6, product.getImageUrl());
            statement.setInt(7, product.getStock());
            statement.setBoolean(8, product.isFeatured());

            int affectedRows = statement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating product failed, no rows affected.");
            }

            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) newID = generatedKeys.getInt(1);
            else throw new RuntimeException("Failed to retrieve generated category ID");
        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create product: " + e.getMessage());
        } return getById(newID);
    }

    @Override
    public void update(int productId, Product product) {
        String sql = """
                UPDATE products
                SET name = ?
                    , price = ?
                    , category_id = ?
                    , description = ?
                    , color = ?
                    , image_url = ?
                    , stock = ?
                    , featured = ?
                WHERE product_id = ?;
                """;

        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, product.getName());
            statement.setBigDecimal(2, product.getPrice());
            statement.setInt(3, product.getCategoryId());
            statement.setString(4, product.getDescription());
            statement.setString(5, product.getColor());
            statement.setString(6, product.getImageUrl());
            statement.setInt(7, product.getStock());
            statement.setBoolean(8, product.isFeatured());
            statement.setInt(9, productId);

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(int productId) {
        String sql = "DELETE FROM products WHERE product_id = ?;";
        try (Connection connection = getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, productId);

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected static Product mapRow(ResultSet row) throws SQLException {
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
}
