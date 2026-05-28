package com.springjwt.core.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "file.storage", name = "type", havingValue = "cloudinary")
public class CloudinaryConfig {

    @Value("${file.cloudinary.cloud-name}")
    private String cloudName;

    @Value("${file.cloudinary.api-key}")
    private String apiKey;

    @Value("${file.cloudinary.api-secret}")
    private String apiSecret;

    @Value("${file.cloudinary.secure:true}")
    private boolean secure;

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", secure
        ));
    }
}
