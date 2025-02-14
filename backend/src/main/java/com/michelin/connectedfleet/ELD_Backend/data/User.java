package com.michelin.connectedfleet.ELD_Backend.data;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Document("users")
public final class User implements UserDetails {
    @MongoId
    private String username;
    private String password;
    private String firstName;
    private String lastName;
    private Collection<GrantedAuthority> userRoles;

    public User(String username, String password, String firstName, String lastName) {
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return userRoles;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "User[" +
                "username=" + username + ", " +
                "password=" + password + ", " +
                "firstName=" + firstName + ", " +
                "lastName=" + lastName + ']';
    }
}
