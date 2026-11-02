package com.example.placement_platform.entity;


import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class RoundResultTest {

    @ParameterizedTest
    @CsvSource({
            "PENDING, CLEARED, true",
            "PENDING, FAILED, true",
            "PENDING, PENDING, false",
            "CLEARED, FAILED, false",
            "FAILED, CLEARED, false",
            "CLEARED, PENDING, false"
    })
    void canMoveTo_followsTheRules(RoundResult from, RoundResult to, boolean expected) {
        assertThat(from.canMoveTo(to)).isEqualTo(expected);
    }
}
