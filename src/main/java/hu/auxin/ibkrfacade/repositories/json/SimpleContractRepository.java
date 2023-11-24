package hu.auxin.ibkrfacade.repositories.json;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.ib.client.Types;
import com.redis.om.spring.repository.RedisDocumentRepository;

import hu.auxin.ibkrfacade.models.json.SimpleContract;

@Repository
public interface SimpleContractRepository extends RedisDocumentRepository<SimpleContract, String> {
    // Find people by age range
    Optional<SimpleContract> findByConid(Integer conid);

    Optional<SimpleContract> findBySymbolAndSecTypeAndCurrency(String symbol, Types.SecType secType, String currency);
}
