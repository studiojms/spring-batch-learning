package com.studiojms.batch;

import com.studiojms.batch.model.User;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author jefferson.souza
 */
public class UsersRowMapper implements RowMapper<User> {
    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        final User user = new User();
        user.setUsername(rs.getString("username"));
        user.setLoginEmail(rs.getString("login_email"));
        user.setIdentifier(rs.getString("identifier"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        return user;

    }
}
