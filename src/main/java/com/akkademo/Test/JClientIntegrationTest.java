package com.akkademo.Test;

import com.akkademo.JClient;
import org.junit.jupiter.api.Test;

/**
 * author yg
 * description
 * date 2018/12/29
 */
public class JClientIntegrationTest {
    JClient jClient = new JClient("127.0.0.1:2552");

    @Test
    public void itShouldSetRecord() {
        jClient.set("123", 123);
    }
}
