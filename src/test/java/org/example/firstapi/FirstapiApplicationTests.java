package org.example.firstapi;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FirstapiApplicationTests {

    @Test
    void applicationClassExists() {
        // when + then
        assertThat(FirstapiApplication.class).isNotNull();
    }
}
