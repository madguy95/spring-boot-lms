package com.springjwt.core.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

/**
 * DEV-ONLY: globally trust every TLS certificate the JVM sees.
 *
 * <p>Workaround for corporate SSL-inspection proxies (Zscaler, Netskope, ...) that intercept
 * outbound HTTPS and re-sign responses with an internal CA the JVM doesn't trust, producing
 * {@code PKIX path building failed} errors when calling Cloudinary / any external HTTPS API.
 *
 * <p><b>NEVER enable in staging/prod.</b> Disabling cert validation removes MITM protection
 * for the entire JVM — any TLS connection (DB, Redis, Kafka, Cloudinary, ...) becomes trusted.
 * The proper fix is to import the corporate root CA into {@code $JAVA_HOME/lib/security/cacerts}
 * via {@code keytool -importcert}.
 *
 * <p>Toggle: set {@code app.ssl.trust-all=true} (only ever in {@code config/dev/application.yml}).
 */
@Configuration
@ConditionalOnProperty(prefix = "app.ssl", name = "trust-all", havingValue = "true")
@Slf4j
public class TrustAllSslConfig {

    @PostConstruct
    public void install() throws Exception {
        log.warn("====================================================================");
        log.warn(" ⚠  TrustAllSslConfig ENABLED — JVM accepts ANY TLS certificate.    ");
        log.warn("    DEV ONLY. Never enable in staging/prod.                          ");
        log.warn("====================================================================");

        TrustManager[] trustAll = {new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
                // accept
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
                // accept
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        }};

        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, trustAll, new SecureRandom());
        SSLContext.setDefault(ctx);
        HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier((host, session) -> true);
    }
}
