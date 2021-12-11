package com.ogray.glbot;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;

public class GLBot extends TelegramLongPollingBot {
    HashMap<Long, Talk> talks = new HashMap<Long, Talk>();
    private static final Logger log = LoggerFactory.getLogger(GLBot.class);


    /**
     * Main entry for incoming message processing
     * @param update
     */
    @Override
    public void onUpdateReceived(Update update) {

        if(update==null || update.getMessage()==null) {
            log.error("update.getMessage is null");
            return;
        }
        Long chatId = update.getMessage().getChatId();
        Talk talk = talks.get(chatId);
        if( talk==null ) {
            log.info("No talk for chatId="+chatId+", creating new one.");
            talk = new Talk(this, chatId);
            talks.put(chatId, talk);
        }
        talk.onUpdateReceived(update);
    }

    @Override
    public String getBotUsername() {
        return "GravLensBot";
    }

    @Override
    public String getBotToken() {
        return "";
    }

}
