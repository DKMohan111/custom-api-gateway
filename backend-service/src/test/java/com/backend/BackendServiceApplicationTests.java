
package com.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "app.admin.password=test-admin-password",
        "app.jwt.secret=test-only-secret-key-that-is-at-least-32-bytes"
})
class BackendServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
