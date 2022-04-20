package com.libraries.sotirisapakos.msserverconnector;

public interface DefaultValues {

    /**
     * Timeout time on server request - do not need to add this at any class constructor because
     * inner variable for request timeout has, by default, this value.
     */
    int DEFAULT_REQUEST_TIMEOUT_SECONDS = 20;

}
