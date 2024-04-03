package com.github.linealnan.frodhunter.detect;

import com.github.linealnan.frodhunter.FrodHunterApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import com.github.linealnan.frodhunter.TelegramLongPollingObserver;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootApplication
public class UrlFinder {
    static final Logger log = LoggerFactory.getLogger(FrodHunterApplication.class);
    TelegramLongPollingObserver telegramLongPollingObserver;
    @Autowired
    public UrlFinder(TelegramLongPollingObserver telegramLongPollingObserver) {
        this.telegramLongPollingObserver = telegramLongPollingObserver;
    }

    @EventListener({ApplicationReadyEvent.class})
    public void observeChatMessage() {
        telegramLongPollingObserver
                .onUpdateSubject
                .filter(update -> update.hasMessage())
                .subscribe(update -> messageHandler(update));
        log.info("Поиск url в сообщениях");
    }

    @EventListener({ApplicationReadyEvent.class})
    public void observeChannelPost() {
        telegramLongPollingObserver
                .onUpdateSubject
                .filter(update -> update.hasChannelPost())
                .subscribe(update -> channelPostHandler(update));
        log.info("Поиск url в канале");
    }

    private void messageHandler(Update update) throws TelegramApiException {
        String messageText = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        Integer messageId = update.getMessage().getMessageId();

        detectAndRemove(messageText, chatId, messageId);
    }

    private void channelPostHandler(Update update) throws TelegramApiException {
        String messageText = update.getChannelPost().getText();
        Long chatId = update.getChannelPost().getChatId();
        Integer messageId = update.getChannelPost().getMessageId();

        detectAndRemove(messageText, chatId, messageId);
    }

    private void detectAndRemove(String messageText, Long chatId, Integer messageId)  throws TelegramApiException {
        if (containsUrlString(messageText)) {
            telegramLongPollingObserver.removeMessage(chatId, messageId);
            String mess = "Было удалено сообщение: " + messageText;
            telegramLongPollingObserver.sendMessageToDevChannel(mess);
            log.info(mess);
        }
    }

    private boolean containsUrlString(String str) {
        ArrayList<String> urlList = new ArrayList<>();

        // Regular Expression to extract URL from the string
        String regexStr = "\\b((?:https?|ftp|file):"
                + "\\/\\/[a-zA-Z0-9+&@#\\/%?=~_|!:,.;]*"
                + "[a-zA-Z0-9+&@#\\/%=~_|])";

        Pattern pattern = Pattern.compile(regexStr, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(str);

        // Find and add all matching URLs to the ArrayList
        while (matcher.find()) {
            // Add the matched URL to the ArrayList
            urlList.add(matcher.group());
        }

        return !urlList.isEmpty();
    }
}
