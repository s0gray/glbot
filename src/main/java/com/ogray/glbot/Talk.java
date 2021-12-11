package com.ogray.glbot;

import com.ogray.glbot.utils.Utils;
import com.ogray.glc.Manager;
import com.ogray.glc.Persist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.ByteArrayInputStream;

public class Talk {
    private static final Logger log = LoggerFactory.getLogger(Talk.class);

    GLBot bot;
    Long chatId;

    Manager boss = new Manager(null);

    enum BotState {
        IDLE,
        SET_SOURCE_SIZE, SET_SOURCE_TYPE, SET_IMAGE_SIZE_PX, SET_IMAGE_SIZE_RE, SET_NG, SET_M0, SET_GAMMA, SET_SIGMAC, INTRO
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

        boss.init();
        boss.setParams(Persist.getInstance());
        boss.refreshGravs();
        boss.render();
    }

    public void onUpdateReceived(Update update) {
        String input = update.getMessage().getText();
        log.info("+onMessage, state="+state+",["+input+"]");
        try {
        //    Long userId = update.getMessage().getFrom().getId();

            switch(input) {
                case "/render":
                   // dataFromUItoPersist();
                   // boss.setParams(Persist.getInstance());
                    boss.render();
                    byte[] jpg = boss.getMap().field.getJPG();

                    bot.sendImage("" +chatId,
                            new InputFile( new ByteArrayInputStream(jpg), "image.jpg"), "image");

                    return;
                case "/info":
                    String txt = "Source: type=" + boss.getSrc().getPar().getSourceType() +" ";
                    txt += " Size=" + boss.getSrc().getPar().size +"RE\n";

                    txt += "Gravitators: N=" + boss.getGen().getPar().getNg()
                            +" Mass0="+boss.getGen().getPar().getM0() +" Seed="+ boss.getGen().getPar().getSeed()+"\n";

                    txt += "Image: Size=" + boss.getMap().getPar().getSizePX() +"px = "
                            +boss.getMap().getPar().getSizeRE()+"RE\n";

                    txt += "Background Field: gamma=" + boss.getMap().getPar().getGamma() +
                            " sigmaC="+boss.getMap().getPar().getSigmaC() +"\n";

                    sendResponse(update, txt);
                    return;

                case "/source":
                    // set source params
                    sendResponse2(update, "set-source-params");
                    return;
                case "/setsourcesize":
                    sendResponse2(update, "enter-source-size-value");
                    this.setState(BotState.SET_SOURCE_SIZE);
                    return;
                case "/setsourcetype":
                    sendResponse2(update, "choose-source-type");
                    this.setState(BotState.SET_SOURCE_TYPE);
                    return;
                case "/image":
                    sendResponse2(update, "set-image-params");
                    return;
                case "/setimagesizepx":
                    sendResponse2(update, "enter-value");
                    this.setState(BotState.SET_IMAGE_SIZE_PX);
                    return;
                case "/setimagesizere":
                    sendResponse2(update, "enter-value");
                    this.setState(BotState.SET_IMAGE_SIZE_RE);
                    return;
                case "/grav":
                    // set grav params
                    sendResponse2(update, "set-grav-params");
                    return;
                case "/setng":
                    sendResponse2(update, "enter-value");
                    this.setState(BotState.SET_NG);
                    return;
                case "/setm":
                    sendResponse2(update, "enter-value");
                    this.setState(BotState.SET_M0);
                    return;
                case "/setgamma":
                    sendResponse2(update, "enter-value");
                    this.setState(BotState.SET_GAMMA);
                    return;
                case "/setsigmac":
                    sendResponse2(update, "enter-value");
                    this.setState(BotState.SET_SIGMAC);
                    return;
            }

            switch(state) {
                case SET_SOURCE_SIZE:{
                    try {
                        float value = getFloatValue(input);
                        boss.getSrc().setParameter("size", value);
                        sendResponse2(update, "success");
                        this.setState(BotState.IDLE);

                        return;
                    } catch (NumberFormatException ex1) {
                        sendResponse2(update, "try-again");
                        return;
                    }
                }
                case SET_SOURCE_TYPE: {
                    try {
                        int value = getSourceTypValue(input);
                        boss.getSrc().setParameter("type", value);
                        sendResponse2(update, "success");
                        this.setState(BotState.IDLE);

                        return;
                    } catch (NumberFormatException ex1) {
                        sendResponse2(update, "try-again");
                        return;
                    }
                }
                case SET_IMAGE_SIZE_PX: {
                    try {
                        int value = getIntValue(input);
                        boss.getMap().setParameter("sizePX", value);
                        sendResponse2(update, "success");
                        this.setState(BotState.IDLE);

                        return;
                    } catch (NumberFormatException ex1) {
                        sendResponse2(update, "try-again");
                        return;
                    }
                }
                case SET_IMAGE_SIZE_RE: {
                    try {
                        float value = getFloatValue(input);
                        boss.getMap().setParameter("sizeRE", value);
                        sendResponse2(update, "success");
                        this.setState(BotState.IDLE);

                        return;
                    } catch (NumberFormatException ex1) {
                        sendResponse2(update, "try-again");
                        return;
                    }
                }
                case SET_NG:
                {
                    try {
                        int value = getIntValue(input);
                        boss.getGen().setParam("ng", value);
                        boss.getGen().generate();
                        sendResponse2(update, "success");
                        this.setState(BotState.IDLE);

                        return;
                    } catch (NumberFormatException ex1) {
                        sendResponse2(update, "try-again");
                        return;
                    }
                }
                case SET_M0:
                {
                    try {
                        int value = getIntValue(input);
                        boss.getGen().setParam("m0", value);
                        boss.getGen().generate();
                        sendResponse2(update, "success");
                        this.setState(BotState.IDLE);

                        return;
                    } catch (NumberFormatException ex1) {
                        sendResponse2(update, "try-again");
                        return;
                    }
                }
                case SET_GAMMA:
                {
                    try {
                        float value = getFloatValue(input);
                        boss.getMap().setParameter("gamma", value);
                        sendResponse2(update, "success");
                        this.setState(BotState.IDLE);

                        return;
                    } catch (NumberFormatException ex1) {
                        sendResponse2(update, "try-again");
                        return;
                    }
                }
                case SET_SIGMAC:
                {
                    try {
                        float value = getFloatValue(input);
                        boss.getMap().setParameter("sigma_c", value);
                        sendResponse2(update, "success");
                        this.setState(BotState.IDLE);

                        return;
                    } catch (NumberFormatException ex1) {
                        sendResponse2(update, "try-again");
                        return;
                    }
                }

                default:
                    setState(BotState.INTRO);
                    sendResponse2(update, "intro");
                    return;

            }
           // sendResponse(update, "Hello !");


        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private int getSourceTypValue(String input) {
        switch(input) {
            case "/flatsource": return 0;
            case "/gausssource": return 1;
            case "/expsource": return 2;
            case "/limbsource": return 3;
            case "/disksource": return 4;
        }
        throw new NumberFormatException();
    }

    private int getIntValue(String input) {
        return Integer.parseInt(input);
    }

    private float getFloatValue(String input) throws NumberFormatException {
        return Float.parseFloat(input);
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
