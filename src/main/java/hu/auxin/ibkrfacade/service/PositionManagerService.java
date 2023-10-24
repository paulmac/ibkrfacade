package hu.auxin.ibkrfacade.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.ib.client.Contract;

import hu.auxin.ibkrfacade.models.PositionHolder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Scope("singleton")
public class PositionManagerService {

    private Map<Integer, PositionHolder> positions = new HashMap<>();

    public void addPosition(PositionHolder positionHolder) {
        if (positionHolder.getQuantity() == 0) {
            positions.remove(positionHolder.getContract().conid());
        } else {
            positions.put(positionHolder.getContract().conid(), positionHolder);
        }
    }

    public Collection<PositionHolder> getAllPositions() {
        return positions.values();
    }

    public PositionHolder getPositionByContract(Contract contract) {
        return positions.get(contract.conid());
    }

    public PositionHolder getPositionByConid(int conid) {
        return positions.get(conid);
    }
}
