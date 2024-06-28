package org.yearup.data.mysql;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.yearup.data.CategoryDao;
import org.yearup.models.Category;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class MySqlCategoryDao extends MySqlDaoBase implements CategoryDao {

    private final JdbcTemplate jdbcTemplate;

    public MySqlCategoryDao(DataSource dataSource) {
        super(dataSource);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public List<Category> getAllCategories() {
        String sql = "SELECT * FROM categories";
        try {
            return jdbcTemplate.query(sql, (resultSet, rowNum) -> mapRow(resultSet));
        } catch (DataAccessException e) {
            handleDataAccessException("Failed to fetch all categories", e);
            return new ArrayList<>(); // Return empty list or handle appropriately
        }
    }

    @Override
    public Category getById(int categoryId) {
        String sql = "SELECT * FROM categories WHERE category_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{categoryId}, (resultSet, rowNum) -> mapRow(resultSet));
        } catch (DataAccessException e) {
            handleDataAccessException("Failed to fetch category with id: " + categoryId, e);
            return null; // Handle appropriately
        }
    }

    @Override
    public Category create(Category category) {
        String sql = "INSERT INTO categories (name, description) VALUES (?, ?)";
        try {
            jdbcTemplate.update(sql, category.getName(), category.getDescription());
            return category;
        } catch (DataAccessException e) {
            handleDataAccessException("Failed to create category: " + category.getName(), e);
            return null; // Handle appropriately
        }
    }

    @Override
    public void update(int categoryId, Category category) {
        String sql = "UPDATE categories SET name = ?, description = ? WHERE category_id = ?";
        try {
            jdbcTemplate.update(sql, category.getName(), category.getDescription(), categoryId);
        } catch (DataAccessException e) {
            handleDataAccessException("Failed to update category with id: " + categoryId, e);
        }
    }

    @Override
    public void delete(int categoryId) {
        String sql = "DELETE FROM categories WHERE category_id = ?";
        try {
            jdbcTemplate.update(sql, categoryId);
        } catch (DataAccessException e) {
            handleDataAccessException("Failed to delete category with id: " + categoryId, e);
        }
    }

    private Category mapRow(ResultSet resultSet) throws SQLException {
        Category category = new Category();
        category.setCategoryId(resultSet.getInt("category_id"));
        category.setName(resultSet.getString("name"));
        category.setDescription(resultSet.getString("description"));
        return category;
    }

    private void handleDataAccessException(String message, DataAccessException e) {
        // Here you can handle the exception as per your application's requirement
        // For example, print the message or throw a custom exception
        System.err.println(message);
        e.printStackTrace(); // Or handle it differently based on your needs
        throw new RuntimeException(message, e);
    }
}




