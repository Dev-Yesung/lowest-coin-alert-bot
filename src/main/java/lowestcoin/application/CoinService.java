package lowestcoin.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lowestcoin.application.dto.CoinResponse;
import lowestcoin.application.dto.CurrentInfoResponse;
import lowestcoin.application.dto.MarketCodeResponse;
import lowestcoin.application.dto.TelegramBotMessageRequest;
import lowestcoin.application.dto.TickerResponse;
import lowestcoin.repository.MarketCodeRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@EnableAsync
public class CoinService {
    private final Map<String, MarketCodeResponse> marketCodes;
    private static final int lastReceivedId = 0;

    public CoinService(MarketCodeRepository marketCodeRepository) {
        marketCodeRepository.saveCurrentKRWMarketCodeLists();
        this.marketCodes = marketCodeRepository.getMarketCodes();
    }

    @Async
    @Scheduled(fixedDelay = 1000 * 65 * 3)
    public void doFixedDelayCoinRequest() {
        log.info("3분마다 요청을 보냅니다. 현재시작 {}", LocalDateTime.now());
        String result = findAllLowestPriceCoin();

        String requestMessage;
        try {
            TelegramBotMessageRequest telegramBotMessageRequest = new TelegramBotMessageRequest("-1001710973678", result);
            ObjectMapper objectMapper = new ObjectMapper();
            requestMessage = objectMapper.writeValueAsString(telegramBotMessageRequest);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(requestMessage))
                .uri(URI.create("https://api.telegram.org/bot6689494303:AAFlAH_MdhCncfvPws9D3Y0FZZaQhpqhUfQ/sendMessage"))
                .header("Content-Type", "application/json")
                .build();

        try {
            log.info("텔레그램 봇에게 메시지를 보냅니다...");

            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            log.info(response.body());

            log.info("메시지 전송에 성공했습니다. 다음 메시지는 3분 후에 전송합니다...");
        } catch (IOException | InterruptedException e) {
            log.warn("텔레그램 봇에게 메시지를 전송하던 도중 오류가 발생했습니다!");

            throw new RuntimeException(e);
        }
    }

    @Async
    @Scheduled(fixedDelay = 2500)
    public void doPollingChatCommand() {
        findChatBotCommand();
    }

    private void findChatBotCommand() {
        HttpRequest messageRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://api.telegram.org/bot6689494303:AAFlAH_MdhCncfvPws9D3Y0FZZaQhpqhUfQ/getUpdates"))
                .header("accept", "application/json")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();

        try {
            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(messageRequest, HttpResponse.BodyHandlers.ofString());
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(response.body());

            if (rootNode.has("result") && rootNode.get("result").isArray()) {
                JsonNode resultArray = rootNode.get("result");

                for (JsonNode result : resultArray) {
                    // 가장 마지막으로 받은 메시지에 대한 정보가 필요함
                    int updateId = result.get("update_id").asInt();
                    String text = result.get("channel_post").get("text").asText();

                    if (text.startsWith("/") && updateId > lastReceivedId) {

                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String findAllLowestPriceCoin() {
        // 1) 현재가 정보 중에 코인 이름과 52주 신저가 정보를 반환한다.
        String queryParam = String.join(",", marketCodes.keySet());

        HttpRequest tickerRequest = HttpRequest.newBuilder()
                .uri(URI.create(MessageFormat.format("https://api.upbit.com/v1/ticker?markets={0}", queryParam)))
                .header("accept", "application/json")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();

        Map<String, CoinResponse> coinResponseMap;
        try {
            var response = HttpClient.newHttpClient()
                    .send(tickerRequest, HttpResponse.BodyHandlers.ofString());
            var objectMapper = new ObjectMapper();
            coinResponseMap = Arrays.stream(objectMapper.readValue(response.body(), TickerResponse[].class))
                    .map(info -> {
                        String market = info.market();
                        String koreanName = marketCodes.get(market).koreanName();
                        Double openingPrice = info.openingPrice();
                        Double lowestPrice = info.lowest52WeekPrice();
                        String lowestPriceDate = info.lowest52WeekDate();

                        return new CoinResponse(market, koreanName,
                                openingPrice, lowestPrice,
                                lowestPriceDate);
                    })
                    .collect(Collectors.toMap(CoinResponse::market, value -> value));
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        // 2) (3분 봉 기준)받아온 정보를 바탕으로 바닥 가격인 코인의 정보를 찾아낸다.
        StringBuilder results = new StringBuilder();
        int apiRequestCount = 0;
        for (String market : coinResponseMap.keySet()) {
            apiRequestCount++;
            if (apiRequestCount % 10 == 0) {
                if (apiRequestCount % 30 == 0) {
                    log.info("너무 많이 요청하면 안되니까 잠시 쉬셈 => 지금까지 총 요청 횟수 : {}", apiRequestCount);
                }

                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            String requestParam = MessageFormat.format("3?market={0}", market);
            HttpRequest currentInfoRequest = HttpRequest.newBuilder()
                    .uri(URI.create(MessageFormat.format("https://api.upbit.com/v1/candles/minutes/{0}", requestParam)))
                    .header("accept", "application/json")
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();

            try {
                var response = HttpClient.newHttpClient()
                        .send(currentInfoRequest, HttpResponse.BodyHandlers.ofString());
                var objectMapper = new ObjectMapper();
                CurrentInfoResponse currentInfoResponse = objectMapper.readValue(response.body(), CurrentInfoResponse[].class)[0];
                CoinResponse ticker = coinResponseMap.get(market);

                String koreanName = ticker.koreanName();
                double openingPrice = currentInfoResponse.openingPrice();
                Double lowestPrice = ticker.lowestPrice();
                double priceGap = openingPrice - lowestPrice;

                if (priceGap <= 0) {
                    results.append("""
                            코인 이름 : %s
                            현재 가격 : %.6f
                            최저 가격 : %.6f
                            가격 차이 : %.6f
                                                       
                            """.formatted(koreanName, openingPrice, lowestPrice, Math.abs(priceGap))
                    );
                }
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        if (!results.isEmpty()) {
            results.append("""
                    유저 : 여기가 코인 장례식장인가요?⚰️
                    업비트 : 아니요~ 바겐세일입니다!🥳
                                        
                    """);

            return results.toString();
        }
        return """
                바겐세일하는 코인이 없습니다!!!🥲
                """;
    }
}
