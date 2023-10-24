package hu.auxin.ibkrfacade.models;

import java.io.Serializable;

import lombok.Getter;

/**
 * Holder class for results coming through TWS API.
 * 
 * @param <T> refers to the wrapped return type
 */
@Getter
public class TwsResultHolder<T extends Object> implements Serializable {

    /**
     * Result for a request if everything was allright during the request
     */
    private T result;

    /**
     * If something went wrong during the TWS communication it holds the error
     * message
     */
    private String error;

    /**
     * Empty instantiation is forbidden
     */
    private TwsResultHolder() {
    }

    public TwsResultHolder(T result) {
        this.result = result;
    }

    TwsResultHolder(String error) {
        this.error = error;
    }
}
