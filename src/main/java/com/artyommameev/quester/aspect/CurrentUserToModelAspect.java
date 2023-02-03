package com.artyommameev.quester.aspect;

import com.artyommameev.quester.aspect.annotation.CurrentUserToModel;
import com.artyommameev.quester.entity.User;
import com.artyommameev.quester.security.user.ActualUser;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

/**
 * An aspect that used to automatically add the current {@link User} object
 * from {@link ActualUser} abstraction to the Spring MVC model.<br>
 * Should be used primarily with Spring MVC controllers.
 *
 * @author Artyom Mameev
 * @see ActualUser
 */
@Slf4j
@Component
@Configurable(autowire = Autowire.BY_TYPE)
@Aspect
public class CurrentUserToModelAspect {

    private final ActualUser actualUser;

    /**
     * Constructor, through which dependencies are injected.
     *
     * @param actualUser the {@link ActualUser} abstraction which represents
     *                   current normal or oAuth2 user.
     */
    public CurrentUserToModelAspect(ActualUser actualUser) {
        this.actualUser = actualUser;
    }

    /**
     * Basic logic of the aspect. The aspect is triggered by the
     * {@link CurrentUserToModel} annotation, and if the current {@link User}
     * is authorized, the parameters of the method that triggered the aspect
     * are examined to see if the Spring MVC model object is present in it.<br>
     * If the model object is present, the current {@link User} object from
     * the {@link ActualUser} abstraction is added as the model attribute.
     *
     * @return a {@link ProceedingJoinPoint}, continues the normal operation
     * of the method.
     * @throws NullPointerException if the Spring MVC model is not found in
     *                              the parameters of the method that triggered
     *                              the aspect.
     * @throws Throwable            if some AOP exception is thrown.
     */
    @Around("@annotation(com.artyommameev.quester.aspect.annotation" +
            ".CurrentUserToModel)")
    public Object addCurrentUserToModel(ProceedingJoinPoint pjp)
            throws Throwable {
        log.debug("Trying to add current user object to MVC model");

        if (!actualUser.isLoggedIn()) {
            log.debug("User is not logged in, nothing to add to MVC model");

            // continue the normal operation of the method
            return pjp.proceed(pjp.getArgs());
        }

        val args = pjp.getArgs();

        val model = getModelFromVarArgs(args);

        if (model == null) {
            throw new NullPointerException("Model cannot be null");
        }

        model.addAttribute("user", actualUser.getCurrentUser());

        log.info("User '" + actualUser.getCurrentUser().getUsername() + "' " +
                "was successfully added to MVC model");

        return pjp.proceed(pjp.getArgs());
    }

    private Model getModelFromVarArgs(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof Model) {
                return (Model) arg;
            }
        }

        return null;
    }
}