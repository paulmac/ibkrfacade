package hu.auxin.ibkrfacade.service;

import com.ib.client.CommissionReport;
import com.ib.client.Contract;
import com.ib.client.Execution;

public interface ExecHandler {

    public default void commissionReport(CommissionReport commissionReport) {
        // Simple default to allow non implemenation in all sub-classes
    }

    public default void execDetails(int reqId, Contract contract, Execution execution) {
        // Simple default to allow non implemenation in all sub-classes
    }

    public default void error(int id, int errorCode, String errorMsg) {
        // Simple default to allow non implemenation in all sub-classes
    }

}
