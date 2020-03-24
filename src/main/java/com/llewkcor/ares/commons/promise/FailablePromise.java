package com.llewkcor.ares.commons.promise;

public interface FailablePromise<T> {
    /**
     * Success response with a returned object instance
     * @param t Object instance
     */
    void success(T t);

    /**
     * Failure response with an error message
     * @param err Error message
     */
    void fail(String err);
}
