package hu.auxin.ibkrfacade.service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import com.ib.client.TickType;

import hu.auxin.ibkrfacade.models.PriceHolder;
import hu.auxin.ibkrfacade.models.TwsResultHolder;
import hu.auxin.ibkrfacade.models.hashes.ContractHolder;
import hu.auxin.ibkrfacade.models.json.Option;
import hu.auxin.ibkrfacade.repositories.hashes.ContractRepository;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.timeseries.TSElement;

/**
 * Contract related operations. The service acts as a bridge between the TWS
 * itself and the other parts of the system,
 * eg. the REST API or the strategy implementations.
 */
@Data
@Slf4j
@Service
@DependsOn("TWS")
@Scope("singleton")
public class ContractManagerService {

    @NonNull
    private TWS tws;

    @NonNull
    private final TimeSeriesHandler timeSeriesHandler;

    @NonNull
    private final ContractRepository contractRepository;

    public List<Contract> searchContract(String search) {
        TwsResultHolder resultHolder = tws.searchContract(search);
        if (StringUtils.hasLength(resultHolder.getError())) {
            throw new RuntimeException();
        }
        return (List<Contract>) resultHolder.getResult();
    }

    public ContractDetails getContractDetails(Contract contract) {
        TwsResultHolder resultHolder = tws.requestContractDetails(contract);
        if (StringUtils.hasLength(resultHolder.getError())) {
            throw new RuntimeException();
        }
        return (ContractDetails) resultHolder.getResult();
    }

    public int subscribeMarketData(Contract contract) {
        return subscribeMarketData(contract, false);
    }

    public int subscribeMarketData(Contract contract, boolean tickByTick) {
        return tws.subscribeMarketData(contract, tickByTick);
    }

    /**
     * Returns the latest available ask and bid price for the given conid
     *
     * @param conid
     * @return
     */
    public PriceHolder getLastPriceByConid(Integer conid) {
        ContractHolder contractHolder = contractRepository.findByConid(conid)
                .orElseThrow(() -> new RuntimeException("No conid found"));
        return getLastPriceByContractHolder(contractHolder);
    }

    /**
     * Returns the latest available ask and bid price for the given Contract
     *
     * @param contract
     * @return
     */
    public PriceHolder getLastPriceByContract(Contract contract) {
        ContractHolder contractHolder = contractRepository.findByConid(contract.conid())
                .orElseThrow(() -> new RuntimeException("No Contract found"));
        return getLastPriceByContractHolder(contractHolder);
    }

    /**
     * Getting the option chain for an underlying instrument
     *
     * @param underlyingConid
     * @return
     */
    public Collection<Option> getOptionChainByConid(Integer underlyingConid) {
        ContractHolder underlying = getContractHolder(underlyingConid);
        return tws.requestForOptionChain(underlying.getContract());
    }

    /**
     * Returns the latest available ask and bid price for the given ContractHolder
     * 
     * @param contractHolder
     * @return
     */
    public PriceHolder getLastPriceByContractHolder(ContractHolder contractHolder) {
        TSElement bidTsElement = timeSeriesHandler.getInstance()
                .tsGet(TimeSeriesHandler.STREAM_STRING + contractHolder.getStreamRequestId() + ":" + TickType.BID);
        TSElement askTsElement = timeSeriesHandler.getInstance()
                .tsGet(TimeSeriesHandler.STREAM_STRING + contractHolder.getStreamRequestId() + ":" + TickType.ASK);
        return new PriceHolder(bidTsElement.getValue(), askTsElement.getValue());
    }

    /**
     * Checking in Redis if the requested Contract already has a data stream.
     * If so, return with the already stored ContractHolder instead of creating a
     * new one.
     *
     * @param contract
     * @return
     */
    public ContractHolder getContractHolder(Contract contract) {
        return getContractHolder(contract.conid());
    }

    /**
     * Checking in Redis if the requested Contract already has a data stream.
     * If so, return with the already stored ContractHolder instead of creating a
     * new one.
     *
     * @param conid
     * @return
     */
    public ContractHolder getContractHolder(Integer conid) {
        Optional<ContractHolder> contractHolder = contractRepository.findByConid(conid);
        return contractHolder.orElseGet(() -> {
            TwsResultHolder<ContractHolder> twsResult = tws.requestContractByConid(conid);
            if (!StringUtils.hasLength(twsResult.getError())) {
                contractRepository.save(twsResult.getResult());
                return twsResult.getResult();
            }
            throw new RuntimeException(twsResult.getError());
        });
    }

    @PostConstruct
    void init() {
        log.info("🚀 ✅ Initialized {} ", this.getClass().getName());
    }
}
