package com.github.linealnan.frodhunter;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiValidationException;
import io.reactivex.rxjava3.subjects.PublishSubject;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TelegramLongPollingObserver extends TelegramLongPollingBot {
    static final Logger log = LoggerFactory.getLogger(FrodHunterApplication.class);

    final public PublishSubject<Update> onUpdateSubject = PublishSubject.create();

    public static final String hostCheckerDevChannelId = "-1002071536642";

    private ApplicationContext context;
    @Autowired
    public void context(ApplicationContext context) { this.context = context; }

    public ApplicationContext getContext() {
        return this.context;
    }

    @Getter
    @Value("${bot.name}")
    private String botUsername;

    @Getter
    @Value("${bot.token}")
    private String botToken;
    @Override
    public void onUpdateReceived(Update update) {
        try {
            onUpdateSubject.onNext(update);
//            Long chatId = update.getChannelPost().getChatId();
//            Integer messageId = update.getChannelPost().getMessageId();
//            String messageText = update.getChannelPost().getText();
//
//            if (containsUrlString(messageText)) {
//                removeMessage(chatId, messageId);
//                log.info("Было удалено сообщение");
//            }
        } catch (Exception e) {
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

    /**
     * Use this method to delete a message, including service messages, with the following limitations:
     * - A message can only be deleted if it was sent less than 48 hours ago.
     * - Service messages about a supergroup, channel, or forum topic creation can't be deleted.
     * - A dice message in a private chat can only be deleted if it was sent more than 24 hours ago.
     * - Bots can delete outgoing messages in private chats, groups, and supergroups.
     * - Bots can delete incoming messages in private chats.
     * - Bots granted can_post_messages permissions can delete outgoing messages in channels.
     * - If the bot is an administrator of a group, it can delete any message there.
     * - If the bot has can_delete_messages permission in a supergroup or a channel, it can delete any message there.
     *
     * @param chatId
     * @param messageId
     */
    public void removeMessage(Long chatId, Integer messageId) throws TelegramApiException {
        try {
            execute(new DeleteMessage(chatId.toString(), messageId));
        } catch (TelegramApiException e) {
            throw e;
        }
    }

    private void sendMessage(String chatId, String textToSend) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(textToSend);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw e;
        }
    }

    public void sendMessageToDevChannel(String textToSend) throws TelegramApiException {
        sendMessage(hostCheckerDevChannelId, textToSend);
    }

}
