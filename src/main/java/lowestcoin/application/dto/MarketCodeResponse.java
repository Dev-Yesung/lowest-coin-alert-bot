package lowestcoin.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MarketCodeResponse(
        @JsonProperty("market_warning") String marketWarning,
        String market,
        @JsonProperty("korean_name") String koreanName,
        @JsonProperty("english_name") String englishName
) {
}
