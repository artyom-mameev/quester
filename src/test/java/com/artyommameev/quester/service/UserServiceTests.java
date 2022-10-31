package com.artyommameev.quester.service;

import com.artyommameev.quester.entity.User;
import com.artyommameev.quester.repository.UserRepository;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(UserService.class)
@SuppressWarnings("ConstantConditions")
public class UserServiceTests {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @Mock
    private User user;

    @Before
    public void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();

        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test(expected = UserService.UserAlreadyExistsException.class)
    @SuppressWarnings("unchecked")
    public void registerNewUserAccountThrowsUserAlreadyExistsExceptionIfUserWithThatUsernameAlreadyExists() throws Exception {
        val optionalUser = mock(Optional.class);

        when(optionalUser.isPresent()).thenReturn(true);

        when(userRepository.findUserByUsername(anyString()))
                .thenReturn(optionalUser);

        userService.registerNewUserAccount("username",
                "password", "password",
                "email@email.com");
    }

    @Test(expected = UserService.UserAlreadyExistsException.class)
    @SuppressWarnings("unchecked")
    public void registerNewUserAccountThrowsUserAlreadyExistsExceptionIfUserWithThatEmailAlreadyExists() throws Exception {
        val optionalUser = mock(Optional.class);

        when(optionalUser.isPresent()).thenReturn(true);

        when(userRepository.findUserByEmail(anyString()))
                .thenReturn(optionalUser);

        userService.registerNewUserAccount("username",
                "password", "password",
                "email@email.com");
    }

    @Test
    public void registerNewUserAccountSavesNewUser() throws Exception {
        final ArgumentCaptor<User> captor = ArgumentCaptor.forClass(
                User.class);

        when(userRepository.findUserByEmail(anyString())).
                thenReturn(Optional.empty());

        userService.registerNewUserAccount("username",
                "password", "password",
                "email@email.com");

        verify(userRepository, times(1))
                .save(captor.capture());

        val createdUser = captor.getValue();

        assertEquals("username", createdUser.getUsername());
        assertEquals("email@email.com", createdUser.getEmail());
        assertNotNull(createdUser.getPassword());
    }

    @Test(expected = UserService.VerificationException.class)
    public void registerNewUserAccountThrowsVerificationExceptionIfUserThrowsEmptyStringException() throws Exception {
        PowerMockito.whenNew(User.class).withAnyArguments()
                .thenThrow(new User.EmptyStringException(""));

        when(userRepository.findUserByEmail(anyString())).
                thenReturn(Optional.empty());

        userService.registerNewUserAccount("username",
                "password", "password",
                "email@email.com");
    }

    @Test(expected = UserService.VerificationException.class)
    public void registerNewUserAccountThrowsVerificationExceptionIfUserThrowsEmailFormatException() throws Exception {
        PowerMockito.whenNew(User.class).withAnyArguments()
                .thenThrow(new User.EmailFormatException(""));

        when(userRepository.findUserByEmail(anyString())).
                thenReturn(Optional.empty());

        userService.registerNewUserAccount("username",
                "password", "password",
                "email@email.com");
    }

    @Test(expected = UserService.VerificationException.class)
    public void registerNewUserAccountThrowsVerificationExceptionIfUserThrowsPasswordsNotMatchException() throws Exception {
        PowerMockito.whenNew(User.class).withAnyArguments()
                .thenThrow(new User.PasswordsNotMatchException(""));

        when(userRepository.findUserByEmail(anyString())).
                thenReturn(Optional.empty());

        userService.registerNewUserAccount("username",
                "password", "password",
                "email@email.com");

    }

    @Test(expected = UserService.VerificationException.class)
    public void registerNewUserAccountThrowsVerificationExceptionIfUserThrowsNullValueException() throws Exception {
        PowerMockito.whenNew(User.class).withAnyArguments()
                .thenThrow(new User.NullValueException(""));

        when(userRepository.findUserByEmail(anyString())).
                thenReturn(Optional.empty());

        userService.registerNewUserAccount("username",
                "password", "password",
                "email@email.com");
    }

    @Test(expected = NullPointerException.class)
    public void updateUserAccountThrowsNullPointerExceptionIfUserIsNull() throws Exception {
        userService.updateUserAccount(null, "email@test.com",
                "password", "password");
    }

    @Test(expected = UserService.UserAlreadyExistsException.class)
    @SuppressWarnings("unchecked")
    public void updateUserAccountThrowsUserAlreadyExistsExceptionIfUserWithThatEmailAlreadyExists() throws Exception {
        val optionalUser = mock(Optional.class);

        when(optionalUser.isPresent()).thenReturn(true);

        when(userRepository.findUserByEmail(anyString()))
                .thenReturn(optionalUser);

        userService.updateUserAccount(user, "email@email.com",
                "password", "password");
    }

    @Test(expected = UserService.VerificationException.class)
    public void updateUserAccountThrowsVerificationExceptionIfUserThrowsEmailFormatException() throws Exception {
        doThrow(new User.EmailFormatException(""))
                .when(user).updateEmail(anyString());

        userService.updateUserAccount(user, "email@test.com",
                "password", "password");

    }

    @Test
    public void updateUserAccountUpdatesEmail() throws Exception {
        userService.updateUserAccount(user, "email@test.com",
                "password", "password");

        verify(user, times(1))
                .updateEmail("email@test.com");
        verify(userRepository, times(1))
                .save(user);
    }

    @Test(expected = UserService.VerificationException.class)
    public void updateUserAccountThrowsVerificationExceptionIfUser_updatePasswordThrowsNullValueException() throws Exception {
        doThrow(new User.NullValueException(""))
                .when(user).updatePassword(anyString(), anyString(), any());

        userService.updateUserAccount(user, "email@test.com",
                "password", "password");
    }

    @Test(expected = UserService.VerificationException.class)
    public void updateUserAccountThrowsVerificationExceptionIfUser_updateEmailThrowsNullValueException() throws Exception {
        doThrow(new User.NullValueException(""))
                .when(user).updateEmail(anyString());

        userService.updateUserAccount(user, "email@test.com",
                null, null);

        verify(user, times(0))
                .updatePassword(any(), any(), any());
    }

    @Test
    public void updateUserAccountUpdatesPassword() throws Exception {
        userService.updateUserAccount(user, "email@test.com",
                "password", "password");

        verify(user, times(1))
                .updatePassword("password", "password",
                        passwordEncoder);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void updateUserAccountSavesUserIfUpdatePasswordThrowsEmptyStringException() throws Exception {
        doThrow(new User.EmptyStringException(""))
                .when(user).updatePassword(anyString(), anyString(), any());

        userService.updateUserAccount(user, "email@test.com",
                "password", "password");

        verify(user, times(1))
                .updatePassword("password", "password",
                        passwordEncoder);
        verify(userRepository, times(1))
                .save(user);
    }

    @Test(expected = UserService.VerificationException.class)
    public void updateUserAccountThrowsVerificationExceptionIfUserThrowsPasswordsNotMatchException() throws Exception {
        doThrow(new User.PasswordsNotMatchException(""))
                .when(user).updatePassword(anyString(), anyString(), any());

        userService.updateUserAccount(user, "email@test.com",
                "password", "password");

    }

    @Test
    public void getUserReturnsUser() throws Exception {
        ReflectionTestUtils.setField(user, "id", 1);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertSame(user, userService.getUser(1L));
    }

    @Test(expected = UserService.UserNotFoundException.class)
    public void getUserThrowsUserNotFoundExceptionIfUserIsNotFound() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        userService.getUser(1L);
    }

    @Test(expected = NullPointerException.class)
    public void getGoogleUserThrowsNullPointerExceptionIfGoogleEmailIsNull() throws Exception {
        userService.getGoogleUser(null);
    }

    @Test
    public void getGoogleUserReturnsGoogleUser() throws Exception {
        ReflectionTestUtils.setField(user, "id", 1);

        when(userRepository.findUserByGoogleEmail("test@gmail.com"))
                .thenReturn(Optional.of(user));

        assertSame(user, userService.getGoogleUser("test@gmail.com"));
    }

    @Test(expected = UserService.UserNotFoundException.class)
    public void getGoogleUserThrowsUserNotFoundExceptionIfGoogleUserIsNotFound() throws Exception {
        when(userRepository.findUserByGoogleEmail("test@gmail.com"))
                .thenReturn(Optional.empty());

        userService.getGoogleUser("test@gmail.com");
    }

    @Test(expected = NullPointerException.class)
    public void usernameExistsThrowsNullPointerExceptionIfUsernameIsNull() {
        userService.usernameExists(null);
    }

    @Test
    public void usernameExistsReturnsTrueIfUsernameExists() throws Exception {
        when(userRepository.findUserByUsername("username"))
                .thenReturn(Optional.of(new User("testName",
                        "test@email.com", "testPassword",
                        "testPassword",
                        new BCryptPasswordEncoder(),
                        Collections.singletonList("ROLE_USER"))));

        assertTrue(userService.usernameExists("username"));
    }

    @Test
    public void usernameExistsReturnsFalseIfUsernameNotExists() {
        when(userRepository.findUserByUsername("username"))
                .thenReturn(Optional.empty());

        assertFalse(userService.usernameExists("username"));
    }

    @Test(expected = NullPointerException.class)
    public void emailExistsThrowsNullPointerExceptionIfEmailIsNull() {
        userService.emailExists(null);
    }

    @Test
    public void emailExistsReturnsTrueIfEmailExists() throws Exception {
        when(userRepository.findUserByEmail("email@test.com"))
                .thenReturn(Optional.of(new User("testName",
                        "test@email.com", "testPassword",
                        "testPassword",
                        new BCryptPasswordEncoder(),
                        Collections.singletonList("ROLE_USER"))));

        assertTrue(userService.emailExists("email@test.com"));
    }

    @Test
    public void emailExistsReturnsFalseIfEmailNotExists() {
        when(userRepository.findUserByEmail("email@test.com"))
                .thenReturn(Optional.empty());

        assertFalse(userService.emailExists("email@test.com"));
    }

    @Test(expected = NullPointerException.class)
    public void confirmUsernameOfOauth2UserThrowsNullPointerException() throws Exception {
        userService.confirmUsernameOfOauth2User(null,
                "username");
    }

    @Test
    public void confirmUsernameOfOauth2UserConfirmsUsername() throws Exception {
        userService.confirmUsernameOfOauth2User(user, "username");

        verify(user, times(1))
                .confirmUsername("username");
        verify(userRepository, times(1)).save(user);
    }

    @Test(expected = UserService.VerificationException.class)
    public void confirmUsernameOfOauth2UserThrowsVerificationExceptionIfUserThrowsEmptyStringException() throws Exception {
        doThrow(new User.EmptyStringException(""))
                .when(user).confirmUsername("username");

        userService.confirmUsernameOfOauth2User(user, "username");
    }

    @Test(expected = UserService.VerificationException.class)
    public void confirmUsernameOfOauth2UserThrowsVerificationExceptionIfUserThrowsNullValueException() throws Exception {
        doThrow(new User.NullValueException(""))
                .when(user).confirmUsername("username");

        userService.confirmUsernameOfOauth2User(user, "username");
    }

    @Test(expected = UserService.AlreadyConfirmedException.class)
    public void confirmUsernameOfOauth2UserThrowsAlreadyConfirmedExceptionIfUserThrowsUsernameExistsException() throws Exception {
        doThrow(new User.UsernameExistsException(""))
                .when(user).confirmUsername("username");

        userService.confirmUsernameOfOauth2User(user, "username");
    }

    @Test(expected = NullPointerException.class)
    public void setEnabledThrowsNullPointerExceptionIfUserIsNull() throws Exception {
        userService.setEnabled(1L, true, null);
    }

    @Test
    public void setEnabledChangesEnabledStatus() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.setEnabled(1L, true, user);

        verify(user, times(1))
                .setEnabled(true, user);
        verify(userRepository, times(1)).save(user);

        userService.setEnabled(1L, false, user);

        verify(user, times(1))
                .setEnabled(false, user);
        verify(userRepository, times(2)).save(user);
    }

    @Test(expected = UserService.ForbiddenException.class)
    public void setEnabledThrowsForbiddenExceptionIfUserThrowsForbiddenManipulationException() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        doThrow(new User.ForbiddenManipulationException(""))
                .when(user).setEnabled(true, user);

        userService.setEnabled(1L, true, user);
    }

    @Test(expected = NullPointerException.class)
    public void deleteUserGamesThrowsNullPointerExceptionIfUserIsNull() throws Exception {
        userService.deleteUserGames(1L, null);
    }

    @Test
    public void deleteUserGamesDeletesUserGames() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUserGames(1L, user);

        verify(user, times(1)).clearGames(user);
        verify(userRepository, times(1)).save(user);
    }

    @Test(expected = UserService.ForbiddenException.class)
    public void deleteUserGamesThrowsForbiddenExceptionIfUserThrowForbiddenManipulationException() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        doThrow(new User.ForbiddenManipulationException(""))
                .when(user).clearGames(user);

        userService.deleteUserGames(1L, user);
    }

    @Test(expected = NullPointerException.class)
    public void deleteUserReviewsThrowsNullPointerExceptionIfUserIsNull() throws Exception {
        userService.deleteUserReviews(1L, null);
    }


    @Test
    public void deleteUserReviewsDeletesUserReviews() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUserReviews(1L, user);

        verify(user, times(1))
                .clearReviews(user);
        verify(userRepository, times(1)).save(user);
    }

    @Test(expected = UserService.ForbiddenException.class)
    public void deleteUserReviewsThrowsForbiddenExceptionIfUserThrowsForbiddenManipulationException() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        doThrow(new User.ForbiddenManipulationException(""))
                .when(user).clearReviews(user);

        userService.deleteUserReviews(1L, user);
    }

    @Test(expected = NullPointerException.class)
    public void deleteUserCommentsThrowsNullPointerExceptionIfUserIsNull() throws Exception {
        userService.deleteUserComments(1L, null);
    }

    @Test
    public void deleteUserCommentsDeletesUserComments() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUserComments(1L, user);

        verify(user, times(1))
                .clearComments(user);
        verify(userRepository, times(1)).save(user);
    }

    @Test(expected = UserService.ForbiddenException.class)
    public void deleteUserCommentsThrowsForbiddenExceptionIfUserThrowsForbiddenManipulationException() throws Exception {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        doThrow(new User.ForbiddenManipulationException(""))
                .when(user).clearComments(user);

        userService.deleteUserComments(1L, user);
    }
}
