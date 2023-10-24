package hu.auxin.ibkrfacade.models;

import java.io.Serializable;

import org.springframework.data.annotation.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "Represents a bid/ask pair.")
@Data
public class PriceHolder implements Serializable {

    @Id
    @JsonIgnore
    private Integer requestId;

    private Double bid;
    private Double ask;

    public PriceHolder(double bid, double ask) {
        this.bid = bid;
        this.ask = ask;
    }
}
