package com.artyommameev.quester.aspect;

import com.artyommameev.quester.entity.User;
import com.artyommameev.quester.security.user.ActualUser;
import lombok.val;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.ui.Model;

import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class CurrentUserToModelAspectTests {
    @MockBean
    private ActualUser actualUser;
    @MockBean
    private User user;
    @MockBean
    private ProceedingJoinPoint pjp;
    @MockBean
    private Model model;

    @Before
    public void setUp() {
        when(actualUser.isLoggedIn()).thenReturn(true);
        when(actualUser.getCurrentUser()).thenReturn(user);
        when(pjp.getArgs()).thenReturn(new Object[]{model});
    }

    @Test
    public void addsUserAttributeToModelIfUserIsLoggedInAndModelIsPresent() throws Throwable {
        val currentUserToModelAspect =
                new CurrentUserToModelAspect(actualUser);

        currentUserToModelAspect.addCurrentUserToModel(pjp);

        verify(model, times(1))
                .addAttribute("user", user);
    }

    @Test(expected = NullPointerException.class)
    public void throwsNullPointerExceptionIfUserIsLoggedInAndModelIsMissing() throws Throwable {
        val currentUserToModelAspect =
                new CurrentUserToModelAspect(actualUser);

        when(pjp.getArgs()).thenReturn(new Object[]{null});

        currentUserToModelAspect.addCurrentUserToModel(pjp);
    }

    @Test
    public void proceedsWithSameArgumentsIfUserIsNotLoggedIn() throws Throwable {
        when(actualUser.isLoggedIn()).thenReturn(false);

        val currentUserToModelAspect =
                new CurrentUserToModelAspect(actualUser);

        currentUserToModelAspect.addCurrentUserToModel(pjp);

        verify(model, times(0)).addAttribute(any(), any());
    }
}