package org.yearup.data.mysql;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.models.Profile;
import org.yearup.data.ProfileDao;

import javax.sql.DataSource;
import java.sql.*;

@Component
public class MySqlProfileDao extends MySqlDaoBase implements ProfileDao
{
    public MySqlProfileDao(DataSource dataSource)
    {
        super(dataSource);
    }

    @Override
    public Profile create(Profile profile) {
        String sql = "INSERT INTO profiles (user_id, first_name, last_name, phone, email, address, city, state, zip) " +
                " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try(Connection connection = getConnection()) {
            PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setInt(1, profile.getUserId());
            ps.setString(2, profile.getFirstName());
            ps.setString(3, profile.getLastName());
            ps.setString(4, profile.getPhone());
            ps.setString(5, profile.getEmail());
            ps.setString(6, profile.getAddress());
            ps.setString(7, profile.getCity());
            ps.setString(8, profile.getState());
            ps.setString(9, profile.getZip());

            ps.executeUpdate();

            return getByUserId(profile.getUserId());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Profile getByUserId(int id) {
        try (Connection con = getConnection()) {
            String sql = "SELECT * FROM profiles WHERE user_id = ?;";
            PreparedStatement statement = con.prepareStatement(sql);
            statement.setInt(1, id);

            ResultSet row = statement.executeQuery();
            if (row.next()) {
                int userId = row.getInt("user_id");
                String first = row.getString("first_name");
                String last = row.getString("last_name");
                String phone = row.getString("phone");
                String email = row.getString("email");
                String address = row.getString("address");
                String city = row.getString("city");
                String state = row.getString("state");
                String zip = row.getString("zip");
                return new Profile (userId, first, last, phone, email, address, city, state, zip);
            } else throw new ResponseStatusException(HttpStatus.NO_CONTENT, "No Profile found");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(int userId, Profile profile) {
        try (Connection con = getConnection()) {
            String sql = """
                    UPDATE profiles
                    SET first_name = ?
                        , last_name = ?
                        , phone = ?
                        , email = ?
                        , address = ?
                        , city = ?
                        , state = ?
                        , zip = ?
                    WHERE user_id = ?;
                    """;
            PreparedStatement statement = con.prepareStatement(sql);
            statement.setString(1, profile.getFirstName());
            statement.setString(2, profile.getLastName());
            statement.setString(3, profile.getPhone());
            statement.setString(4, profile.getEmail());
            statement.setString(5, profile.getAddress());
            statement.setString(6, profile.getCity());
            statement.setString(7, profile.getState());
            statement.setString(8, profile.getZip());
            statement.setInt(9, userId);

            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
