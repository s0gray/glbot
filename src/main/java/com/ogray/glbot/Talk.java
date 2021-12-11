package com.ogray.glbot;

import com.ogray.glbot.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class Talk {
    private static final Logger log = LoggerFactory.getLogger(Talk.class);

    GLBot bot;
    Long chatId;

    int activeRole = 0;


    enum BotState {
        IDLE,
        INTRO,
        DANCER_MAIN_MENU,
        TEACHER_MAIN_MENU,
        ASK_ADMIN_SECRET,
    }

    BotState state = BotState.IDLE;
    void setState(BotState s) {
        log.info("setState "+s+" "+this);
        this.state = s;
    }

    /**
     * Constructor
     * @param bot
     * @param chatId
     */
    public Talk(GLBot bot, Long chatId) {
        this.bot = bot;
        this.chatId = chatId;
    }
    void createNewSession(Update update) throws TelegramApiException {
        // we need to gather info about user to start session
        switch( state ) {
            case INTRO: {
                String input = update.getMessage().getText();
                if( input.isEmpty() ) {
                    sendResponse2(update, "makeChoice");
                    return;
                }
                if("/admin".compareTo(input)==0) {
                    sendResponse2(update, "askAdminSecret");

                    setState(BotState.ASK_ADMIN_SECRET);
                }else {
                    sendResponse2(update, "makeChoice");
                    return;
                }
            }
            break;

            case IDLE:
            default:
            {
                setState(BotState.INTRO);
                sendResponse2(update, "intro");
                return;
            }
        }
    }

    public void onUpdateReceived(Update update) {
        String input = update.getMessage().getText();
        log.info("+onMessage, state="+state+",["+input+"]");
        try {
        //    Long userId = update.getMessage().getFrom().getId();
            sendResponse(update, "Hello !");
               //this.showDancerMainMenu(update);


        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }




    private int getIndexFromYes(String input) {
        String sub = input.substring( 1, input.length()-3);
   //     log.info("sub = "+sub);
        return Integer.parseInt(sub);
    }
    private int getIndexFromNo(String input) {
        String sub = input.substring( 1, input.length()-2);
    //    log.info("sub = "+sub);
        return Integer.parseInt(sub);
    }

    // is '/1YES
    private static boolean isYes(String input) {
        if(input.length()<4) return false;
        String sub = input.substring( input.length()-3);
        log.info("sub=["+sub+"]");
        if("YES".compareTo(sub)==0) return true;
        return false;
    }
    private static boolean isNo(String input) {
        if(input.length()<3) return false;
        String sub = input.substring( input.length()-2);
        log.info("sub=["+sub+"]");
        if("NO".compareTo(sub)==0) return true;
        return false;
    }

    private void setActiveRole(int value) {
        log.info("setActiveRole: "+value+" "+this);
        this.activeRole = value;
    }

    private void showDancerMainMenu(Update update) throws TelegramApiException {
        setState(BotState.DANCER_MAIN_MENU);
        sendResponse(update, Utils.getString("dancerMainMenu"));
    }

    private void showTeacherMainMenu(Update update) throws TelegramApiException {
        setState(BotState.TEACHER_MAIN_MENU);
        sendResponse(update, Utils.getString("teacherMainMenu"));
    }

    void sendResponse(Update update, String text) throws TelegramApiException {
        SendMessage sendMessage = makeResponse(update);
        sendMessage.setText(text);
        bot.execute(sendMessage);
    }

    /**
     * Send response using key in .properties
     * @param update
     * @param key
     * @throws TelegramApiException
     */
    void sendResponse2(Update update, String key) throws TelegramApiException {
        SendMessage sendMessage = makeResponse(update);
        sendMessage.setText( Utils.getString(key) );
        bot.execute(sendMessage);
    }

    SendMessage makeResponse(Update update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId().toString());
        return sendMessage;
    }


}
