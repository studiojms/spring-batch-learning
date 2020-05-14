package com.studiojms.batch;

import com.studiojms.batch.model.User;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

/**
 * @author jefferson.souza
 */
public class UserFieldSetMapper implements FieldSetMapper<User> {
    @Override
    public User mapFieldSet(FieldSet fieldSet) throws BindException {
        final User user = new User();
        user.setUsername(fieldSet.readString("Username"));
        user.setLoginEmail(fieldSet.readString("Login email"));
        user.setIdentifier(fieldSet.readString("Identifier"));
        user.setFirstName(fieldSet.readString("First name"));
        user.setLastName(fieldSet.readString("Last name"));
        return user;
    }
}
