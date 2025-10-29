package com.springjwt.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Internationalization (i18n) configuration
 * Handles locale resolution from HTTP Accept-Language header
 *
 * Supported locales: English (en), Vietnamese (vi)
 * Default locale: English (en)
 *
 * Usage: Client sends Accept-Language header
 * - Accept-Language: vi -> Vietnamese messages
 * - Accept-Language: en -> English messages
 * - No header or unsupported -> English (default)
 */
@Configuration
public class I18nConfig {

    /**
     * Configure locale resolver to use Accept-Language header
     *
     * @return LocaleResolver that reads Accept-Language header
     */
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();

        // Set default locale to English
        localeResolver.setDefaultLocale(Locale.ENGLISH);

        // Define supported locales
        List<Locale> supportedLocales = Arrays.asList(
                Locale.ENGLISH,                    // en
                Locale.forLanguageTag("vi")        // vi (Vietnamese)
        );
        localeResolver.setSupportedLocales(supportedLocales);

        return localeResolver;
    }
}

