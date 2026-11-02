package com.example.placement_platform.entity;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationStatusTest {

    @ParameterizedTest
    @CsvSource({
            "APPLIED, SHORTLISTED, true",
            "APPLIED, REJECTED, true",
            "APPLIED, WITHDRAWN, true",
            "APPLIED, OFFERED, false",
            "APPLIED, APPLIED, false",
            "SHORTLISTED, OFFERED, true",
            "SHORTLISTED, REJECTED, true",
            "SHORTLISTED, WITHDRAWN, true",
            "SHORTLISTED, APPLIED, false",
            "OFFERED, REJECTED, false",
            "REJECTED, SHORTLISTED, false",
            "WITHDRAWN, APPLIED, false"
    })
    void canMoveTo_followsTheRules(ApplicationStatus from, ApplicationStatus to, boolean expected) {
        assertThat(from.canMoveTo(to)).isEqualTo(expected);
    }
}