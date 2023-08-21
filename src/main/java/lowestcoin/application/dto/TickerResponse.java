package lowestcoin.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TickerResponse(
        @JsonProperty("market") String market,
        @JsonProperty("trade_date") String tradeDate,
        @JsonProperty("trade_time") String tradeTime,
        @JsonProperty("trade_date_kst") String tradeDateKst,
        @JsonProperty("trade_time_kst") String tradeTimeKst,
        @JsonProperty("trade_timestamp") Long tradeTimestamp,
        @JsonProperty("opening_price") Double openingPrice,
        @JsonProperty("high_price") Double highPrice,
        @JsonProperty("low_price") Double lowPrice,
        @JsonProperty("trade_price") Double tradePrice,
        @JsonProperty("prev_closing_price") Double prevClosingPrice,
        @JsonProperty("change") String change,
        @JsonProperty("change_price") Double changePrice,
        @JsonProperty("change_rate") Double changeRate,
        @JsonProperty("signed_change_price") Double signedChangePrice,
        @JsonProperty("signed_change_rate") Double signedChangeRate,
        @JsonProperty("trade_volume") Double tradeVolume,
        @JsonProperty("acc_trade_price") Double accTradePrice,
        @JsonProperty("acc_trade_price_24h") Double accTradePrice24h,
        @JsonProperty("acc_trade_volume") Double accTradeVolume,
        @JsonProperty("acc_trade_volume_24h") Double accTradeVolume24h,
        @JsonProperty("highest_52_week_price") Double highest52WeekPrice,
        @JsonProperty("highest_52_week_date") String highest52WeekDate,
        @JsonProperty("lowest_52_week_price") Double lowest52WeekPrice,
        @JsonProperty("lowest_52_week_date") String lowest52WeekDate,
        @JsonProperty("timestamp") Long timestamp
) {
}
