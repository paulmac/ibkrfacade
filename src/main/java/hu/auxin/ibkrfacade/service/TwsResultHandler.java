package hu.auxin.ibkrfacade.service;

import java.util.Map;
import java.util.WeakHashMap;

import hu.auxin.ibkrfacade.models.TwsResultHolder;

public final class TwsResultHandler {

    private final static Map<Integer, TwsResultHolder> results = new WeakHashMap<>();

    public void setResult(int requestId, TwsResultHolder result) {
        results.put(requestId, result);
    }

    public TwsResultHolder getResult(int requestId) {
        // TODO not the most sophisticated wait mechanism
        while (!results.containsKey(requestId)) {
            continue;
        }
        return results.get(requestId);
    }

}
