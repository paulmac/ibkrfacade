package hu.auxin.ibkrfacade.controllers;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ib.client.Types;
import com.ib.client.Types.SecType;


import hu.auxin.ibkrfacade.models.json.SimpleContract;
import hu.auxin.ibkrfacade.models.json.SimpleContract.Exchange;
import hu.auxin.ibkrfacade.repositories.json.SimpleContractRepository;
import hu.auxin.ibkrfacade.service.SimpleContractService;
import lombok.Data;
import lombok.NonNull;

@Data
@RestController
@RequestMapping("/contracts/")
public class SimpleContractController {

    @NonNull
    private SimpleContractRepository repo;

    @NonNull
    private SimpleContractService service;

    @GetMapping("all")
    Iterable<SimpleContract> all() {
        return repo.findAll();
    }

    @GetMapping("/search")
    List<SimpleContract> searchSimpleContract(@RequestParam String query) {
        return service.searchSimpleContract(query);
    }

    @GetMapping("{id}")
    Optional<SimpleContract> byId(@PathVariable String id) {
        return repo.findById(id);
    }

    @GetMapping("simple_order")
    SimpleContract simpleOrder(@RequestParam("symbol") String symbol, @RequestParam("currency") String currency,
            @RequestParam("secType") SecType secType, @RequestParam("quantity") Integer quantity,
            @RequestParam("exchange") Exchange exchange, @RequestParam("action") Types.Action action,
            @RequestParam(name = "localSymbol", required = false) Optional<String> localSymbol)
            throws InterruptedException, ExecutionException {
        return service.simpleOrder(symbol, currency, secType, quantity, exchange, action, localSymbol.orElse(null));
    }

    @PostMapping("new")
    SimpleContract create(@RequestBody SimpleContract contract) {
        return repo.save(contract);
    }

    @GetMapping("load_static")
    List<SimpleContract> load() {
        return service.loadStatic();
    }

    // *** DANGER ****
    @DeleteMapping("all")
    void deleteAll() {
        repo.deleteAll();
    }
}
