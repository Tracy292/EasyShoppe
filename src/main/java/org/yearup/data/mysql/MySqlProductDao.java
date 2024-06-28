package org.yearup.data.mysql;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.yearup.data.ProductDao;
import org.yearup.models.Product;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class MySqlProductDao extends MySqlDaoBase implements ProductDao {

    private final JdbcTemplate jdbcTemplate;

    public MySqlProductDao(DataSource dataSource) {
        super(dataSource);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public List<Product> search(Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice, String color) {
        List<Product> products = new ArrayList<>();

        String sql = "SELECT * FROM products " +
                "WHERE (category_id = ? OR ? = -1) " +
                "   AND (price >= ? OR ? = -1) " +
                "   AND (color = ? OR ? = '') ";

        categoryId = (categoryId == null) ? -1 : categoryId;
        minPrice = (minPrice == null) ? BigDecimal.valueOf(-1) : minPrice;
        color = (color == null) ? "" : color;

        try {
            products = jdbcTemplate.query(sql,
                    (resultSet, rowNum) -> mapRow(resultSet),
                    categoryId, categoryId, minPrice, minPrice, color, color);
        } catch (DataAccessException e) {
            handleDataAccessException("Failed to search products", e);
        }
        return products;
    }

    @Override
    public List<Product> listByCategoryId(int categoryId) {
        List<Product> products = new ArrayList<>();

        String sql = "SELECT * FROM products WHERE category_id = ?";

        try {
            products = jdbcTemplate.query(sql,
                    (resultSet, rowNum) -> mapRow(resultSet),
                    categoryId);
        } catch (DataAccessException e) {
            handleDataAccessException("Failed to fetch products by category id: " + categoryId, e);
        }
        return products;
    }

    @Override
    public Product getById(int productId) {
        String sql = "SELECT * FROM products WHERE product_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql,
                    (resultSet, rowNum) -> mapRow(resultSet),
                    productId);
        } catch (DataAccessException e) {
            handleDataAccessException("Failed to fetch product with id: " + productId, e);
            return null; // Handle appropriately
        }
    }

    @Override
    public Product create(Product product) {
        String sql = "INSERT INTO products(name, price, category_id, description, color, image_url, stock, featured) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            jdbcTemplate.update(sql,
                    product.getName(),
                    product.getPrice(),
                    product.getCategoryId(),
                    product.getDescription(),
                    product.getColor(),
                    product.getImageUrl(),
                    product.getStock(),
                    product.isFeatured());

            return product;
        } catch (DataAccessException e) {
            handleDataAccessException("Failed to create product: " + product.getName(), e);
            return null; // Handle appropriately
        }
    }

    @Override
    public void update(int productId, Product product) {
        String sql = "UPDATE products " +
                "SET name = ?, " +
                "    price = ?, " +
                "    category_id = ?, " +
                "    description = ?, " +
                "    color = ?, " +
                "    image_url = ?, " +
                "    stock = ?, " +
                "    featured = ? " +
                "WHERE product_id = ?";

        try {
            // Ensure productId in the product object matches the method parameter
            product.setProductId(productId);

            jdbcTemplate.update(sql,
                    product.getName(),
                    product.getPrice(),
                    product.getCategoryId(),
                    product.getDescription(),
                    product.getColor(),
                    product.getImageUrl(),
                    product.getStock(),
                    product.isFeatured(),
                    productId);  // Use productId here for the WHERE clause

        } catch (DataAccessException e) {
            handleDataAccessException("Failed to update product with id: " + productId, e);
        }
    }


    @Override
    public void delete(int productId) {
        String sql = "DELETE FROM products WHERE product_id = ?";

        try {
            jdbcTemplate.update(sql, productId);
        } catch (DataAccessException e) {
            handleDataAccessException("Failed to delete product with id: " + productId, e);
        }
    }

    protected Product mapRow(ResultSet resultSet) throws SQLException {
        Product product = new Product();
        product.setProductId(resultSet.getInt("product_id"));
        product.setName(resultSet.getString("name"));
        product.setPrice(resultSet.getBigDecimal("price"));
        product.setCategoryId(resultSet.getInt("category_id"));
        product.setDescription(resultSet.getString("description"));
        product.setColor(resultSet.getString("color"));
        product.setStock(resultSet.getInt("stock"));
        product.setFeatured(resultSet.getBoolean("featured"));
        product.setImageUrl(resultSet.getString("image_url"));

        return product;
    }

    private void handleDataAccessException(String message, DataAccessException e) {
        // Here you can handle the exception as per your application's requirement
        // For example, print the message or throw a custom exception
        System.err.println(message);
        e.printStackTrace(); // Or handle it differently based on your needs
        throw new RuntimeException(message, e);
    }
}


