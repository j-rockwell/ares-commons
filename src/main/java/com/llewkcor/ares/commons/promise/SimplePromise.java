package com.llewkcor.ares.commons.promise;

public interface SimplePromise {
    /**
     * Simple success response
     */
    void success();

    /**
     * Error response
     * @param err Error message
     */
    void fail(String err);
}
