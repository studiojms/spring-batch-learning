package com.studiojms.batch.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author jefferson.souza
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class User {
    private String username;
    private String loginEmail;
    private String identifier;
    private String firstName;
    private String lastName;
}
