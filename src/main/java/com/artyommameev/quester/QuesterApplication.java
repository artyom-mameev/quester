package com.artyommameev.quester;

import com.artyommameev.quester.convert.LowercaseStringToSortingModeEnumConverter;
import lombok.val;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.format.FormatterRegistry;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

/**
 * The entry point and base configuration of the application.
 *
 * @author Artyom Mameev
 */
@SpringBootApplication
@Configuration
@EnableAspectJAutoProxy
public class QuesterApplication implements WebMvcConfigurer {

    /**
     * The constant that represents a maximum number of elements on page.
     */
    public static final int PAGE_SIZE = 10;

    /**
     * The constant that represents a minimum size of string that can be
     * stored into the database.
     */
    public static final int MIN_STRING_SIZE = 2;

    /**
     * The constant that represents a maximum size of short string that can be
     * stored into the database.
     */
    public static final int MAX_SHORT_STRING_SIZE = 25;

    /**
     * The constant that represents a maximum size of long string that can be
     * stored into the database.
     */
    public static final int MAX_LONG_STRING_SIZE = 255;

    /**
     * The entry point of the application.
     *
     * @param args the input arguments.
     */
    public static void main(String[] args) {
        SpringApplication.run(QuesterApplication.class, args);
    }

    /**
     * A locale resolver that sets the default locale.
     *
     * @return the session locale resolver.
     */
    @Bean
    public LocaleResolver localeResolver() {
        val slr = new SessionLocaleResolver();

        slr.setDefaultLocale(Locale.US);

        return slr;
    }

    /**
     * A locale change interceptor that sets parameter to change locale.
     *
     * @return the locale change interceptor.
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        val localeChangeInterceptor = new LocaleChangeInterceptor();

        localeChangeInterceptor.setParamName("lang");

        return localeChangeInterceptor;
    }

    /**
     * A message source that sets message resource bundle for the
     * internationalization purposes.
     *
     * @return the reloadable resource bundle message source.
     */
    @Bean
    public ReloadableResourceBundleMessageSource messageSource() {
        val messageSource = new ReloadableResourceBundleMessageSource();

        messageSource.setBasename("classpath:messages");
        messageSource.setCacheSeconds(3600);
        messageSource.setDefaultEncoding("UTF-8");

        return messageSource;
    }

    /**
     * A local validator factory bean that enables the validation mechanism.
     *
     * @return the local validator factory bean.
     */
    @Bean
    public LocalValidatorFactoryBean getValidator() {
        val bean = new LocalValidatorFactoryBean();

        bean.setValidationMessageSource(messageSource());

        return bean;
    }

    /**
     * Adds Spring interceptors.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }

    /**
     * Adds Spring formatters.
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new LowercaseStringToSortingModeEnumConverter());
    }

    /**
     * A password encoder used to securely store passwords.
     *
     * @return the {@link BCryptPasswordEncoder}
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
