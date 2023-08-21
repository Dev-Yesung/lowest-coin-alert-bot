package lowestcoin.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lowestcoin.application.dto.MarketCodeResponse;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class MarketCodeRepository {
    private Map<String, MarketCodeResponse> marketCodes = new HashMap<>();

    public void saveCurrentKRWMarketCodeLists() {
        ObjectMapper objectMapper = new ObjectMapper();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.upbit.com/v1/market/all?isDetails=true"))
                .header("accept", "application/json")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();

        try {
            log.info("업비트 서버로부터 마켓코드를 받아옵니다...");

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            marketCodes = Arrays.stream(objectMapper.readValue(response.body(), MarketCodeResponse[].class))
                    .filter(marketCode -> marketCode.market().contains("KRW"))
                    .collect(Collectors.toMap(
                            MarketCodeResponse::market,
                            value -> value
                    ));

            log.info("마켓코드 데이터를 가져오는데 성공했습니다.");
        } catch (IOException | InterruptedException e) {
            log.warn("마켓코드를 받아오는 도중 오류가 발생했습니다.");

            throw new RuntimeException("마켓 코드를 받아오는 도중 오류가 발생했습니다.");
        }
    }

    public Map<String, MarketCodeResponse> getMarketCodes() {
        return marketCodes;
    }
}
