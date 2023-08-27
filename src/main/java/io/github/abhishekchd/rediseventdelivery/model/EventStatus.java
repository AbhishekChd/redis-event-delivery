package io.github.abhishekchd.rediseventdelivery.model;


public enum EventStatus {
    PENDING,
    SUCCESS,
    RETRYABLE_FAILURE,
    NON_RETRYABLE_FAILURE,
}
