package com.springjwt.sample.model.request;

/**
 * Java 17+ Text Blocks - Multi-line String Literals
 *
 * Benefits:
 * - More readable
 * - No need for concatenation or escape characters
 * - Preserves formatting
 * - Better for SQL, JSON, HTML, etc.
 */
public class TextBlockExamples {

    // Traditional way (Before Java 17)
    public static final String JSON_OLD = "{\n" +
            "  \"username\": \"admin\",\n" +
            "  \"email\": \"admin@example.com\",\n" +
            "  \"roles\": [\"ADMIN\", \"USER\"]\n" +
            "}";

    // Java 17+ Text Block
    public static final String JSON_NEW = """
            {
              "username": "admin",
              "email": "admin@example.com",
              "roles": ["ADMIN", "USER"]
            }
            """;

    // SQL Query - Old way
    public static final String SQL_QUERY_OLD = "SELECT u.id, u.username, u.email, r.name as role " +
            "FROM users u " +
            "INNER JOIN user_roles ur ON u.id = ur.user_id " +
            "INNER JOIN roles r ON ur.role_id = r.id " +
            "WHERE u.id = ? AND u.active = true " +
            "ORDER BY u.username";

    // SQL Query - Text Block
    public static final String SQL_QUERY_NEW = """
            SELECT u.id, u.username, u.email, r.name as role
            FROM users u
            INNER JOIN user_roles ur ON u.id = ur.user_id
            INNER JOIN roles r ON ur.role_id = r.id
            WHERE u.id = ? AND u.active = true
            ORDER BY u.username
            """;

    // HTML Template - Old way
    public static final String HTML_OLD = "<html>\n" +
            "  <head>\n" +
            "    <title>Login</title>\n" +
            "  </head>\n" +
            "  <body>\n" +
            "    <h1>Welcome</h1>\n" +
            "  </body>\n" +
            "</html>";

    // HTML Template - Text Block
    public static final String HTML_NEW = """
            <html>
              <head>
                <title>Login</title>
              </head>
              <body>
                <h1>Welcome</h1>
              </body>
            </html>
            """;

    // Error Message Template
    public static final String ERROR_MESSAGE = """
            Authentication Error:
            ==================
            Status: %s
            Message: %s
            Timestamp: %s
            
            Please check your credentials and try again.
            """;

    // Usage example
    public String formatErrorMessage(String status, String message, String timestamp) {
        return String.format(ERROR_MESSAGE, status, message, timestamp);
    }

    // JWT Validation Error
    public static final String JWT_ERROR_HELP = """
            JWT Token Validation Failed
            ===========================
            
            Possible causes:
            1. Token has expired
            2. Token signature is invalid
            3. Token has been revoked
            4. Token format is malformed
            
            Solution:
            - Request a new token using /api/auth/signin
            - Ensure Bearer token is properly formatted in Authorization header
            """;
}

