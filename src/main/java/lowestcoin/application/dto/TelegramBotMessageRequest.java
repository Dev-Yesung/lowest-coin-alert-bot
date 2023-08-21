package lowestcoin.application.dto;

public record TelegramBotMessageRequest(
        String chat_id,
        String text
) {
}
