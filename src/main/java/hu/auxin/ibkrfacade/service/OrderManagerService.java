package hu.auxin.ibkrfacade.service;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ib.client.CommissionReport;
import com.ib.client.Contract;
import com.ib.client.EClientSocket;
import com.ib.client.Execution;
import com.ib.client.Order;
import com.ib.client.OrderState;
import com.ib.client.Types;

import hu.auxin.ibkrfacade.models.OrderSamples;
import hu.auxin.ibkrfacade.models.hashes.OrderHolder;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Service
@Scope("singleton")
public class OrderManagerService {

    private Map<Integer, OrderHolder> orders = new HashMap<>();

    private EClientSocket client;

    private Map<String, ExecHandler> handlers = new HashMap<>();

    private Integer orderId = -1;

    /**
     * Place an order on the market for a Contract
     *
     * @param contract
     * @param order
     */

    @Synchronized
    private Integer placeOrder(Contract contract, Order order, ExecHandler handler) {
        if (client != null && client.isConnected()) {
            orderId++;
            order.orderId(orderId);
            client.placeOrder(orderId, contract, order);
            handlers.put(String.valueOf(orderId), handler);
            return orderId;
        } else
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Tws Not Connected");
    }

    public Integer placeLimitOrder(Contract contract, Types.Action action, double quantity, BigDecimal limitPrice,
            ExecHandler handler) {
        Order order = OrderSamples.LimitOrder(action.getApiString(), quantity, limitPrice.doubleValue());
        return this.placeOrder(contract, order, handler);
    }

    public Integer placeMarketOrder(Contract contract, Types.Action action, double quantity, ExecHandler handler) {
        Order order = OrderSamples.MarketOrder(action.getApiString(), quantity);
        return this.placeOrder(contract, order, handler);
    }

    public Integer placeStopLimitOrder(Contract contract, Types.Action action, double quantity, BigDecimal stopPrice,
            BigDecimal limitPrice, ExecHandler handler) {
        Order order = OrderSamples.StopLimit(action.getApiString(), quantity, limitPrice.doubleValue(),
                stopPrice.doubleValue());
        return this.placeOrder(contract, order, handler);
    }

    @Synchronized
    public void execDetails(int reqId, Contract contract, Execution execution) {
        ExecHandler handler = handlers.remove(String.valueOf(execution.orderId()));
        if (handler != null) {
            log.info(
                    "ExecDetails. handler.class [{}] reqId [{}] conid [{}] execId [{}] orderId [{}] ",
                    handler.getClass(), reqId, contract.conid(),
                    execution.execId(),
                    execution.orderId());
            handler.execDetails(reqId, contract, execution);
            handlers.put(execution.execId(), handler); // put it back in but with a different key
        } else
            log.error("handler is NULL");
    }

    @Synchronized
    public void commissionReport(CommissionReport commissionReport) {
        ExecHandler handler = handlers.remove(String.valueOf(commissionReport.execId()));
        if (handler != null)
            handler.commissionReport(commissionReport);
    }

    @Synchronized
    public void error(int id, int errorCode, String errorMsg) {
        ExecHandler handler = handlers.remove(String.valueOf(id));
        if (handler != null)
            handler.error(id, errorCode, errorMsg);
    }

    /**
     * Order created, get from IB and store
     *
     * @param contract
     * @param order
     * @param orderState
     */
    public void setOrder(Contract contract, Order order, OrderState orderState) {
        orders.put(order.permId(), new OrderHolder(order.permId(), order, contract, orderState));
    }

    /**
     * Order status changed
     *
     * @param permId
     * @param status
     * @param filled
     * @param remaining
     * @param avgFillPrice
     * @param lastFillPrice
     */
    public void changeOrderStatus(int permId, String status, double filled, double remaining, double avgFillPrice,
            double lastFillPrice) {
        OrderHolder orderHolder = orders.get(permId);
        if (orderHolder != null) {
            orderHolder.getOrderState().status(status);
            orderHolder.getOrder().filledQuantity(filled);
        } else {
            throw new RuntimeException("Order empty for permId=" + permId);
        }
    }

    public Collection<OrderHolder> getAllOrders() {
        return orders.values();
    }

    public Collection<OrderHolder> getActiveOrders() {
        return orders.values().stream()
                .filter(orderHolder -> orderHolder.getOrderState().status().isActive())
                .collect(Collectors.toList());
    }

    public Collection<OrderHolder> getAllOrdersByContract(Contract contract) {
        return orders.values().stream()
                .filter(orderHolder -> orderHolder.getContract().conid() == contract.conid())
                .collect(Collectors.toList());
    }

    public Collection<OrderHolder> getActiveOrdersByContract(Contract contract) {
        return orders.values().stream()
                .filter(orderHolder -> orderHolder.getContract().conid() == contract.conid())
                .filter(orderHolder -> orderHolder.getOrderState().status().isActive())
                .collect(Collectors.toList());
    }

    @PostConstruct
    void init() {
        log.info("🚀 ✅ Initialized {} seed orderId [{}]", this.getClass().getName());
    }
}
