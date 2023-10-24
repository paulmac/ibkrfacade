package hu.auxin.ibkrfacade.repositories.hashes;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import hu.auxin.ibkrfacade.models.hashes.ContractHolder;

@Repository
public interface ContractRepository extends CrudRepository<ContractHolder, String> {

    Optional<ContractHolder> findContractHolderByOptionChainRequestId(Integer optionChainRequestId);

    Optional<ContractHolder> findByConid(Integer conid);

    Optional<ContractHolder> findByOptionChainRequestId(int reqId);
}
