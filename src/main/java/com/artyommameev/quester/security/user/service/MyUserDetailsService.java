package com.artyommameev.quester.security.user.service;

import com.artyommameev.quester.entity.User;
import com.artyommameev.quester.repository.UserRepository;
import lombok.val;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * A simple {@link UserDetailsService} that loads {@link User}s from
 * {@link UserRepository} by username.
 *
 * @author Artyom Mameev
 */
@Service
@Transactional
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * The constructor, through which dependencies are injected by Spring.
     *
     * @param userRepository a repository that allows to query and store
     *                       the {@link User} objects.
     * @see UserRepository
     */
    public MyUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads and returns {@link User} from the database by username.
     *
     * @param username the name of the {@link User} that should be returned.
     * @throws UsernameNotFoundException if the user is not found.
     */
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        val optionalUser = userRepository.findUserByUsername(
                username);

        return optionalUser.orElseGet(() -> {
            throw new UsernameNotFoundException(
                    "No user found with the username \"" + username + "\"");
        });
    }
}
