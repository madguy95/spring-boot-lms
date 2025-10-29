package com.springjwt.common.util;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * Utility class for retrieving internationalized messages.
 *
 * This class provides a convenient way to access i18n messages from the message source
 * using the current locale from the LocaleContextHolder.
 *
 * Usage example:
 * <pre>
 * String message = messageUtil.getMessage("user.not.found");
 * String messageWithParam = messageUtil.getMessage("job.already.exists", jobName, groupName);
 * </pre>
 */
@Component
@RequiredArgsConstructor
public class MessageUtil {

    private final MessageSource messageSource;

    /**
     * Get internationalized message for the given key using current locale.
     *
     * @param key Message key from messages.properties
     * @return Localized message
     */
    public String getMessage(String key) {
        return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
    }

    /**
     * Get internationalized message with parameters for the given key using current locale.
     *
     * @param key    Message key from messages.properties
     * @param params Parameters to substitute in the message (for {0}, {1}, etc.)
     * @return Localized message with substituted parameters
     */
    public String getMessage(String key, Object... params) {
        return messageSource.getMessage(key, params, LocaleContextHolder.getLocale());
    }

    /**
     * Get internationalized message with default value if key not found.
     *
     * @param key            Message key from messages.properties
     * @param defaultMessage Default message to return if key not found
     * @return Localized message or default message
     */
    public String getMessageOrDefault(String key, String defaultMessage) {
        return messageSource.getMessage(key, null, defaultMessage, LocaleContextHolder.getLocale());
    }

    /**
     * Get internationalized message with parameters and default value if key not found.
     *
     * @param key            Message key from messages.properties
     * @param defaultMessage Default message to return if key not found
     * @param params         Parameters to substitute in the message
     * @return Localized message with substituted parameters or default message
     */
    public String getMessageOrDefault(String key, String defaultMessage, Object... params) {
        return messageSource.getMessage(key, params, defaultMessage, LocaleContextHolder.getLocale());
    }
}

