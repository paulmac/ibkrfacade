package hu.auxin.ibkrfacade.service;

import com.ib.client.CommissionReport;
import com.ib.client.Contract;
import com.ib.client.Execution;

public interface ExecHandler {

    public void execDetails(int reqId, Contract contract, Execution execution);

    public void commissionReport(CommissionReport commissionReport);

}
