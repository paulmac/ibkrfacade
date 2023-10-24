package hu.auxin.ibkrfacade.service;

import java.util.Collection;
import java.util.List;

import com.ib.client.Contract;
import com.ib.client.ContractDetails;

import hu.auxin.ibkrfacade.models.TwsResultHolder;
import hu.auxin.ibkrfacade.models.hashes.ContractHolder;
import hu.auxin.ibkrfacade.models.json.Option;

/**
 * Publicly available methods for TWS communication
 */
public interface TwsHandler {

    /**
     * Subscribe to market data stream, returns with the stream id.
     * 
     * @param contract
     * @param tickData if true, we get tick-by-tick data
     */
    int subscribeMarketData(Contract contract, boolean tickData);

    TwsResultHolder<List<Contract>> searchContract(String search);

    /**
     * Gets the ContractDetails from TWS by the given conid, extracts the Contract
     * object from it, writes the whole
     * result to Redis, and returns with the Contract with it's details encapsulated
     * to a TwsResultHolder type.
     * 
     * @param conid
     * @return
     */
    TwsResultHolder<ContractHolder> requestContractByConid(int conid);

    TwsResultHolder<ContractDetails> requestContractDetails(Contract contract);

    Collection<Option> requestForOptionChain(Contract contract);
}
