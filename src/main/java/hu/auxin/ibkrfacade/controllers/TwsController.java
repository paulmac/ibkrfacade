package hu.auxin.ibkrfacade.controllers;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import com.ib.client.Types;

import hu.auxin.ibkrfacade.models.PositionHolder;
import hu.auxin.ibkrfacade.models.PriceHolder;
import hu.auxin.ibkrfacade.models.hashes.ContractHolder;
import hu.auxin.ibkrfacade.models.hashes.OrderHolder;
import hu.auxin.ibkrfacade.models.json.Option;
import hu.auxin.ibkrfacade.service.ContractManagerService;
import hu.auxin.ibkrfacade.service.ExecHandler;
import hu.auxin.ibkrfacade.service.OrderManagerService;
import hu.auxin.ibkrfacade.service.PositionManagerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * Web endpoints for accessing TWS functionality
 */
@Data
@Slf4j
@RestController
@DependsOn("contractManagerService")
@RequestMapping("/tws/")
public class TwsController implements ExecHandler {

    @NonNull
    private ContractManagerService contractManagerService;
    @NonNull
    private OrderManagerService orderManagerService;
    @NonNull
    private PositionManagerService positionManagerService;

    /**
     * This variable decides if "high frequency" data stream is needed from IBKR.
     * It's value can be set from application.properties file.
     */
    @Value("${ibkr.tick-by-tick-stream}")
    private boolean tickByTickStream;

    @Operation(summary = "Connects to a Platfomrm ; GW TWS - PAPER LIVE", parameters = {
            @Parameter(name = "platform", description = "The type of platform GW TWS - PAPER LIVE")
    })
    @GetMapping("/connect")
    void connect(@RequestParam String platform) {
        try {
            contractManagerService.getTws().connect(platform);
        } catch (InterruptedException e) {
            log.error("Can't Connect : " + platform, e);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Can't Connect : " +
                    platform);
        }
    }

    @GetMapping("/disconnect")
    void disconnect() throws Exception {
        contractManagerService.getTws().disconnect();
    }

    @Operation(summary = "Search for an instrument by it's ticker, or part of it's name.", parameters = {
            @Parameter(description = "Ticker, or name of the traded instrument", examples = {
                    @ExampleObject(name = "Ticker", value = "AAPL"),
                    @ExampleObject(name = "Company name", value = "Apple")
            })
    })

    @GetMapping("/search")
    List<Contract> searchContract(@RequestParam String query) {
        return contractManagerService.searchContract(query);
    }

    @GetMapping("/details")
    ContractDetails details(@RequestParam Contract con) {
        return contractManagerService.getContractDetails(con);
    }

    @Operation(summary = "Subscribes to an instrument by it's conid. Subscription means the TWS starts streaming the market data for the instrument which will be saved into Redis TimeSeries", parameters = {
            @Parameter(name = "conid", description = "The conid (IBKR unique id) of the instrument")
    })
    @GetMapping("/subscribe/{conid}")
    void subscribeMarketDataByConid(@PathVariable int conid) {
        contractManagerService.subscribeMarketData(getContractByConid(conid).getContract(), tickByTickStream);
    }

    @Operation(summary = "Returns with the ContractHolder which contains the Contract descriptor itself and the streamRequestId if you are already subscribed to the instrument.", parameters = {
            @Parameter(name = "conid", description = "The conid (IBKR unique id) of the instrument")
    })

    @GetMapping("/contract/{conid}")
    ContractHolder getContractByConid(@PathVariable int conid) {
        return contractManagerService.getContractHolder(conid);
    }

    @Operation(summary = "Sends an order to the market.", parameters = {
            @Parameter(name = "conid", description = "The conid (IBKR unique id) of the instrument"),
            @Parameter(name = "action", description = "BUY or SELL"),
            @Parameter(name = "quantity", description = "Quantity"),
            @Parameter(name = "price", description = "Price value")
    })
    @PostMapping("/lmt_order")
    void placeLimitOrder(@RequestParam int conid, @RequestParam Types.Action action, @RequestParam double quantity,
            @RequestParam BigDecimal price) {
        Contract contract = contractManagerService.getContractHolder(conid).getContract();
        orderManagerService.placeLimitOrder(contract, action, quantity, price, this);
    }

    @PostMapping("/mkt_order")
    void placeMarketOrder(@RequestParam int conid, @RequestParam Types.Action action, @RequestParam double quantity) {
        Contract contract = contractManagerService.getContractHolder(conid).getContract();
        orderManagerService.placeMarketOrder(contract, action, quantity, this);
    }

    @Operation(summary = "Returns with the list of orders.")
    @GetMapping("/orders")
    Collection<OrderHolder> getAllOrders() {
        return orderManagerService.getAllOrders();
    }

    @Operation(summary = "Returns with the list of active orders.")
    @GetMapping("/orders/active")
    Collection<OrderHolder> getActiveOrders() {
        return orderManagerService.getActiveOrders();
    }

    @Operation(summary = "Returns with the list of open positions.")
    @GetMapping("/positions")
    Collection<PositionHolder> getAllPositions() {
        return positionManagerService.getAllPositions();
    }

    @Operation(summary = "Returns with the last available bid and ask for the given Contract.", parameters = {
            @Parameter(name = "contract", description = "The Contract descriptor object as a JSON")
    })
    @PostMapping("/price")
    PriceHolder getLastPriceByContract(@RequestBody Contract contract) {
        return contractManagerService.getLastPriceByContract(contract);
    }

    @Operation(summary = "Returns with the last available bid and ask by conid.", parameters = {
            @Parameter(name = "conid", description = "The conid (IBKR unique id) of the instrument")
    })
    @GetMapping("/price/{conid}")
    PriceHolder getLastPriceByConid(@PathVariable int conid) {
        return contractManagerService.getLastPriceByConid(conid);
    }

    @Operation(summary = "Returns with the option chain as the list of option typed Contracts for an underlying Contract. "
            +
            "This method won't subscribe to the changes of the certain options. If you need the option data as a market stream, "
            +
            "you have to subscribe to each option you want one-by-one.", parameters = {
                    @Parameter(name = "underlyingConid", description = "The conid (IBKR unique id) of the instrument") })
    @GetMapping("/optionChain/{underlyingConid}")
    Collection<Option> getOptionChain(@PathVariable int underlyingConid) {
        return contractManagerService.getOptionChainByConid(underlyingConid);
    }

}
