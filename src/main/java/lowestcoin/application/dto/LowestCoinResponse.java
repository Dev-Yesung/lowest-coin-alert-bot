package lowestcoin.application.dto;

public record LowestCoinResponse(
        String market, String koreanName,
        Double openingPrice, Double lowestPrice,
        Double priceGap
) {
    public static LowestCoinResponse ofEmpty() {
        return new LowestCoinResponse(
                null, null,
                null, null, null
        );
    }
}
