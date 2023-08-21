package lowestcoin.application.dto;

public record CoinResponse(
        String market,
        String koreanName,
        Double openingPrice,
        Double lowestPrice,
        String lowestPriceDate
) {
}
