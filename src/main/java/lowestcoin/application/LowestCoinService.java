package lowestcoin.application;

import lowestcoin.application.dto.CoinResponse;
import lowestcoin.application.dto.CurrentInfoResponse;
import lowestcoin.application.dto.LowestCoinResponse;
import org.springframework.stereotype.Service;

@Service
public class LowestCoinService {

    public LowestCoinResponse makeLowestCoinInfo(
            CurrentInfoResponse currentInfoResponse,
            CoinResponse ticker
    ) {
        String market = ticker.market();
        String koreanName = ticker.koreanName();
        double openingPrice = currentInfoResponse.openingPrice();
        Double lowestPrice = ticker.lowestPrice();
        double priceGap = openingPrice - lowestPrice;

        if (priceGap <= 0) {
            return new LowestCoinResponse(
                    market, koreanName,
                    openingPrice, lowestPrice,
                    priceGap
            );
        }

        return LowestCoinResponse.ofEmpty();
    }
}
