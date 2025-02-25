package com.michelin.connectedfleet.ELD_Backend.Authentication;

import com.michelin.connectedfleet.ELD_Backend.data.User.User;
import com.michelin.connectedfleet.ELD_Backend.data.User.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MongoAuthUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public MongoAuthUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException(username);
        }

        return user;
    }
}
