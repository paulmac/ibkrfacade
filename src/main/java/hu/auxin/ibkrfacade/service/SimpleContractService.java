package hu.auxin.ibkrfacade.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.ib.client.CommissionReport;
import com.ib.client.Contract;
import com.ib.client.Execution;
import com.ib.client.Types;
import com.ib.client.Types.SecType;

import hu.auxin.ibkrfacade.models.json.SimpleContract;
import hu.auxin.ibkrfacade.models.json.SimpleContract.Exchange;
import hu.auxin.ibkrfacade.repositories.json.SimpleContractRepository;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.NonNull;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@Service
@DependsOn("orderManagerService")
@Scope("singleton")
public class SimpleContractService implements ExecHandler {

        @Value("${ibkr.conid.eur.cfd}")
        public Integer ibkrConidEurCfd;

        @Value("${ibkr.commission.eur.cash}")
        public BigDecimal ibkrCommissionEurCfd;

        @Value("${ibkr.commission.currency.eur.cash}")
        public String ibkrCommissionCurrencyEurCfd;

        @Value("${ibkr.conid.eur.cash}")
        public Integer ibkrConidEurCash;

        @Value("${ibkr.commission.eur.cash}")
        public BigDecimal ibkrCommissionEurCash;

        @Value("${ibkr.commission.currency.eur.cash}")
        public String ibkrCommissionCurrencyEurCash;

        @Value("${ibkr.conid.aud.nzd.cfd}")
        public Integer ibkrConidAudNzdCfd;

        @Value("${ibkr.commission.aud.nzd.cfd}")
        public BigDecimal ibkrCommissionAudNzdCfd;

        @Value("${ibkr.commission.currency.aud.nzd.cfd}")
        public String ibkrCommissionCurrencyAudNzdCfd;

        @NonNull
        private SimpleContractRepository repo;

        @NonNull
        private OrderManagerService orderManagerService;

        @NonNull
        private ContractManagerService contractManagerService;

        CompletableFuture<SimpleContract> completableFuture;

        SimpleContract simple;

        // Cannot be Synchronized as needs to give up control of CPU!!
        public SimpleContract simpleOrder(String symbol, String currency,
                        SecType secType, Integer quantity, Exchange exchange,
                        Types.Action action, String localSymbol) throws InterruptedException, ExecutionException {
                Optional<SimpleContract> optional = repo.findBySymbolAndSecTypeAndCurrency(symbol,
                                secType, currency);

                // Get the value from optional or throw an exception if empty
                simple = optional.orElseGet(() -> SimpleContract.builder()
                                .symbol(symbol)
                                .secType(secType)
                                .currency(currency)
                                .exchange(exchange)
                                .commission(BigDecimal.ZERO)
                                .commissionCurrency("AUD")
                                .build());

                if (StringUtils.hasLength(localSymbol))
                        // String is null or contains only whitespace
                        simple.setLocalSymbol(localSymbol);

                CompletableFuture<SimpleContract> future = new CompletableFuture<>();
                completableFuture = future.orTimeout(5, TimeUnit.SECONDS);

                Integer orderId = orderManagerService.placeMarketOrder(simple.toContract(),
                                action,
                                quantity, this);

                log.info("Order; ID [{}] for Contract; Symbol [{}]", orderId,
                                simple.getSymbol());
                try {
                        simple = completableFuture.get();
                        log.info("Simple Contract ID [{}] conid [{}] ", simple.getId(), simple.getConid());
                        return simple;

                } catch (InterruptedException e) {
                        // TimeoutException will be thrown if the task takes longer than the specified
                        // timeout
                        log.error("Task did not complete within the specified timeout", e);
                        throw e;
                }
        }

        public List<SimpleContract> searchSimpleContract(String search) {
                return contractManagerService.searchContract(search).stream().map(contract -> SimpleContract.builder()
                                .conid(contract.conid())
                                .symbol(contract.symbol())
                                .secType(contract.secType())
                                .currency(contract.currency())
                                .exchange(Exchange.valueOf(contract.exchange()))
                                .build())
                                .collect(Collectors.toList());
        }

        @Synchronized
        @Override
        public void execDetails(int reqId, Contract contract, Execution execution) {
                // Should fill in some aspects of the Contact that aren't easily available
                // unless executing an order
                log.info("ExecDetails; simple.id [{}] reqId [{}] conid [{}] execId [{}] orderId [{}] ",
                                this.simple.getId(), reqId, contract.conid(),
                                execution.execId(),
                                execution.orderId());
                simple.setConid(contract.conid());
        }

        @Synchronized
        @Override
        public void commissionReport(CommissionReport commissionReport) {
                // update Commission details if required
                if (simple.getCommission().compareTo(BigDecimal.valueOf(commissionReport.commission())) != 0) {
                        simple.setCommission(BigDecimal.valueOf(commissionReport.commission()));
                        simple.setCommissionCurrency(commissionReport.currency());
                        repo.save(simple);
                }
                completableFuture.complete(simple);
        }

        @Synchronized
        @Override
        public void error(int id, int errorCode, String errorMsg) {
                log.error("Simple Order Failed ID [{}] Code [{}] Msg [{}] ", id, errorCode, errorMsg);
                // don't complete, just let it fail with timeout
        }

        public List<SimpleContract> loadStatic() {

                // Load SimpleContracts
                List<SimpleContract> contracts;
                if (repo.count() == 0) {

                        SimpleContract eurCashContract = SimpleContract.builder()
                                        .conid(ibkrConidEurCash)
                                        .symbol("EUR")
                                        .secType(Types.SecType.CASH).currency("USD")
                                        .exchange(Exchange.IDEALPRO)
                                        .commission(ibkrCommissionEurCash)
                                        .commissionCurrency(ibkrCommissionCurrencyEurCash).build();

                        SimpleContract eurCfdContract = SimpleContract.builder()
                                        .conid(ibkrConidEurCfd)
                                        .symbol("EUR")
                                        .secType(Types.SecType.CFD).currency("USD").exchange(Exchange.SMART)
                                        .commission(ibkrCommissionAudNzdCfd)
                                        .commissionCurrency(ibkrCommissionCurrencyAudNzdCfd).build();

                        SimpleContract audNzdCfdContract = SimpleContract.builder()
                                        .conid(ibkrConidAudNzdCfd)
                                        .symbol("AUD")
                                        .secType(Types.SecType.CFD).currency("NZD").exchange(Exchange.SMART)
                                        .commission(ibkrCommissionAudNzdCfd)
                                        .commissionCurrency(ibkrCommissionCurrencyAudNzdCfd).build();

                        contracts = List.of(audNzdCfdContract, eurCashContract, eurCfdContract);
                        repo.saveAll(contracts);
                        log.info(String.format("✅ Created %s Contracts...", repo.count()));
                        return contracts;
                } else
                        return repo.findAll();
        }

        @PostConstruct
        void init() {
                this.loadStatic();
                log.info("🚀 ✅ Initialized {} ", this.getClass().getName());
        }
}