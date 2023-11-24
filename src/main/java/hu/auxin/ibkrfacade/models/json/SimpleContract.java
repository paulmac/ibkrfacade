package hu.auxin.ibkrfacade.models.json;

import java.math.BigDecimal;

import org.springframework.data.annotation.Id;

import com.ib.client.Contract;
import com.ib.client.Types;
import com.redis.om.spring.annotations.Document;
import com.redis.om.spring.annotations.Indexed;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
@Document
public class SimpleContract {

    public enum Exchange { // State of the Trade, partial to be implemented latter
        SMART("Smart"),
        IDEALPRO("IDEALPRO"),
        NYMEX("NYMEX"),
        COMEX("COMEX"),
        CME("CME");

        String s;

        private Exchange(String s) {
            this.s = s;
        }

        public String getValue() {
            return s;
        }
    }

    @Id
    @Indexed
    private String id; // ULID
    @Indexed
    private Integer conid; // eg 143916318 conid(eurCash.intValue());
    @NonNull
    @Indexed
    private String symbol; // eg "EUR";
    @NonNull
    @Indexed
    private Types.SecType secType; // eg "CFD";
    @NonNull
    @Indexed
    private String currency; // e.g USD
    private Exchange exchange; // e.g IDEALPRO
    @NonNull
    private BigDecimal commission = BigDecimal.ZERO;
    @NonNull
    private String commissionCurrency = "USD"; // e.g USD

    private String localSymbol; // e.g USD

    public Contract toContract() {
        Contract con = new Contract();
        if (conid != null)
            con.conid(this.getConid());
        if (localSymbol != null)
            con.localSymbol(this.getLocalSymbol());
        con.secType(this.secType.toString());
        con.symbol(this.symbol);
        con.currency(this.currency);
        con.exchange(this.exchange.getValue());
        return con;
    }
}
