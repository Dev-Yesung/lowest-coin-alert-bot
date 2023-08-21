package lowestcoin.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CurrentInfoResponse(
        @JsonProperty("market") String market,
        @JsonProperty("candle_date_time_utc") String candleDateTimeUtc,
        @JsonProperty("candle_date_time_kst") String candleDateTimeKst,
        @JsonProperty("opening_price") double openingPrice,
        @JsonProperty("high_price") double highPrice,
        @JsonProperty("low_price") double lowPrice,
        @JsonProperty("trade_price") double tradePrice,
        @JsonProperty("timestamp") long timestamp,
        @JsonProperty("candle_acc_trade_price") double candleAccTradePrice,
        @JsonProperty("candle_acc_trade_volume") double candleAccTradeVolume,
        @JsonProperty("unit") int unit
) {
}
