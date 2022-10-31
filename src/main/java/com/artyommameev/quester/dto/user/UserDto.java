package com.artyommameev.quester.dto.user;

import com.artyommameev.quester.entity.User;

/**
 * A root interface for {@link User} data transfer objects.
 *
 * @author Artyom Mameev
 * @see EditProfileDto
 * @see RegisterUserDto
 */
public interface UserDto {

    String getPassword();

    String getMatchingPassword();

    String getEmail();
}
