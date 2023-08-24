package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private static final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    public TelegramBotUpdatesListener(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    private static final Pattern PATTERN = Pattern.compile("(\\d{1,2}\\.\\d{1,2}.\\d{2,4} \\d{1,2}:\\d{1,2}) ([А-я A-z\\d,\\s.!:?]+)");

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(
            "d.M.yyyy HH:mm");

    @Autowired
    private final TelegramBot telegramBot;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            Long id = update.message().chat().id();
            Message message = update.message();
            String text = message.text();
            LocalDateTime dateTime;
            if (update.message() != null && text != null) {
                Matcher matcher = PATTERN.matcher(text);
                if (text.equals("/start")) {
                    SendMessage sendMessage = new SendMessage(
                            id,
                            "Для планирования задачи, отправьте её в формате: \\ n*31.12.2022 20:00 Текст вашей задачи*");
                    sendMessage.parseMode(ParseMode.Markdown);
                    SendResponse sendResponse = telegramBot.execute(sendMessage);
                    if (!sendResponse.isOk()) {
                        logger.error("SendMessage was failed due to " + sendResponse.description());
                    }
                } else if (matcher.matches() && (dateTime=parse(matcher.group(1)))!=null) {
                    String notification = matcher.group(2);
                } else {

                }
            }

        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
    @Nullable
    private LocalDateTime parse (String dateTime) {
        try {
            return LocalDateTime.parse(dateTime, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

}
