package com.github.linealnan.frodhunter;

import lombok.Getter;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class TelegramLongPollingObserver extends TelegramLongPollingBot {

    @Getter
    @Value("${bot.name}")
    private String botUsername;

    @Getter
    @Value("${bot.token}")
    private String botToken;
    @Override
    public void onUpdateReceived(Update update) {
        try {
            sendMessage(update.getMessage().getChatId(), "Привет!");
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(Long chatId, String textToSend) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw e;
        }
    }

}
