package com.jobscope.domain.application.entity;

public enum StageResult {
    PENDING, PASS, FAIL;

    public ApplicationResult toApplicationResult() {
        return switch (this) {
            case PASS -> ApplicationResult.PASSED;
            case FAIL -> ApplicationResult.FAILED;
            case PENDING -> null;
        };
    }
}