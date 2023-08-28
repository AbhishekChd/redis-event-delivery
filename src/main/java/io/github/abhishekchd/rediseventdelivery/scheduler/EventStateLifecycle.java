package io.github.abhishekchd.rediseventdelivery.scheduler;

import org.springframework.lang.NonNull;

public interface EventStateLifecycle<T> {
    void onSuccess(@NonNull T data);

    void onFailure(@NonNull T data, @NonNull String message);
}
