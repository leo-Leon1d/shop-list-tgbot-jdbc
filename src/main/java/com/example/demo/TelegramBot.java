package com.example.demo;

import lombok.SneakyThrows;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TelegramBot extends TelegramLongPollingBot {

    private final BotService botService;
    private final Map<Long, String> userState = new HashMap<>();
    private final Map<Long, String> userItem = new HashMap<>();

    public TelegramBot(DefaultBotOptions options, String botToken, BotService botService) {
        super(options, botToken);
        this.botService = botService;
        registerBotCommands();
    }

    @SneakyThrows
    private void msg(Long chatId, String message) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), message);
        sendApiMethod(sendMessage);
        System.out.println(message);
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        if(update.hasCallbackQuery()) {
            System.out.println(update.getCallbackQuery().getData());

            AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery(update.getCallbackQuery().getId());
            answerCallbackQuery.setText("Обработка...");
            execute(answerCallbackQuery);

            String callbackData = update.getCallbackQuery().getData();
            Long chatId = update.getCallbackQuery().getMessage().getChatId();

            if(callbackData.startsWith("removeAll")) {
                botService.removeALl();

                msg(chatId, "Все товары были успешно удалены.");
            }

            if(callbackData.startsWith("remove_")) {
                String item = callbackData.substring(7);
                botService.remove(item);

                msg(chatId, "Товар успешно удален.");
            }
        }

        if(update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();

            System.out.println(text);

            if (userState.containsKey(chatId) && userState.get(chatId).equals("waiting_for_amount")) {
                handleAddAmount(chatId, text);
            } else if (userState.containsKey(chatId) && userState.get(chatId).equals("waiting_for_item")) {
                handleAddItem(chatId, text);
            } else if(text.equals("/list")) {
                handleList(chatId);
            } else if(text.equals("/remove")) {
                handleRemove(chatId);
            } else if(text.equals("/add")) {
                handleAdd(chatId);
            } else {
                msg(chatId, "Введите одну из комманд бота.");
            }
        }
    }

    @SneakyThrows
    public void handleList(Long chatId) {
        List<Map<String, Object>> list = botService.listNameAmount();

        if (list.isEmpty()) {
            msg(chatId, "Ваш список покупок пуст.");
        } else {
            StringBuilder shoppingList = new StringBuilder("Список покупок:\n");
            for (Map<String, Object> row : list) {
                String name = (String) row.get("name");
                Integer amount = (Integer) row.get("amount");
                shoppingList.append(name).append(" (").append(amount).append(")\n");
            }
            msg(chatId, shoppingList.toString());
        }
    }


    public void handleRemove(Long chatId) {
        if(botService.listName().isEmpty()) {
            msg(chatId, "Ваш список пуст.");
        } else removePick(chatId, botService.listName());
    }

    @SneakyThrows
    public void removePick(Long chatId, List<String> list) {
        SendMessage sendMessage = new SendMessage(chatId.toString(),
                "Выберите предмет для удаления из списка:");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for(String item : list) {
            List<InlineKeyboardButton> row = new ArrayList<>();

            row.add(InlineKeyboardButton.builder()
                    .text(item)
                    .callbackData("remove_" + item)
                    .build());
            rows.add(row);
        }
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text("Удалить всё")
                .callbackData("removeAll")
                .build());
        rows.add(row);
        inlineKeyboardMarkup.setKeyboard(rows);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        sendApiMethod(sendMessage);
    }

    @SneakyThrows
    public void handleAdd(Long chatId) {
        msg(chatId, "Введите название продукта:");
        userState.put(chatId, "waiting_for_item");
    }

    @SneakyThrows
    public void handleAddItem(Long chatId, String itemName) {
        userItem.put(chatId, itemName);
        msg(chatId, "Введите количество для \"" + itemName + "\":");
        userState.put(chatId, "waiting_for_amount");
    }

    @SneakyThrows
    public void handleAddAmount(Long chatId, String amountText) {
        try {
            int amount = Integer.parseInt(amountText);
            String itemName = userItem.get(chatId);

            if (itemName != null) {
                botService.add(itemName, amount);
                msg(chatId, "Товар \"" + itemName + "\" в количестве " + amount + " добавлен в список.");
            }

            userState.remove(chatId);
            userItem.remove(chatId);

        } catch (NumberFormatException e) {
            SendMessage sendMessage = new SendMessage(chatId.toString(),
                    "Пожалуйста, введите корректное количество (число).");
            sendApiMethod(sendMessage);
        }
    }

    @Override
    public String getBotUsername() {
        return "Shop Assistant JDBC";
    }

    public void registerBotCommands () {
        List<BotCommand> botCommands = new ArrayList<>();
        botCommands.add(new BotCommand("/list",
                "Просмотреть список покупок"));
        botCommands.add(new BotCommand("/add",
                "Добавить в список покупок"));
        botCommands.add(new BotCommand("/remove",
                "Убрать из списка покупок"));
        try {
            this.execute(new SetMyCommands(botCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

}
