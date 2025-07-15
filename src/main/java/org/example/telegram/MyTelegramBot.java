package org.example.telegram;

import org.example.Main;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyTelegramBot extends TelegramLongPollingBot {

    private Long chatId = null;
    private List<String> jobList = new ArrayList<>();


    @Override
    public String getBotUsername() {
        return "CristiJobBot";
    }

    @Override
    public String getBotToken() {
        return "8007363903:AAF7uUXLUYNjg9Qnf4dgXY68tK8tnYb4Fjs";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        String messageText = update.getMessage().getText().trim();
        chatId = update.getMessage().getChatId();
        System.out.println("Chat ID: " + chatId);

        if (messageText.startsWith("/find")) {
            handleFindCommand(messageText);
        } else if (messageText.startsWith("/list")) {
            handleListCommand();
        } else if (messageText.startsWith("/remove")) {
            handleRemoveCommand(messageText);
        } else {
            sendText("❓ Unknown command: " + messageText);
        }
    }

    private void handleFindCommand(String messageText) {
        String[] parts = messageText.split("\\s+", 2);
        if (parts.length < 2) {
            sendText("❗ Usage: /find <job title>");
            return;
        }

        String jobQuery = parts[1];
        sendText("🔍 Searching for jobs: " + jobQuery);
        jobList.add(jobQuery);
        Main.runJobCheck(this);
    }

    private void handleListCommand() {
        if (jobList.isEmpty()) {
            sendText("📭 No active jobs in the list.");
        } else {
            sendText("📋 Active searching jobs:\n- " + String.join("\n- ", jobList));
        }
    }

    private void handleRemoveCommand(String messageText) {
        String[] parts = messageText.split("\\s+", 2);
        if (parts.length < 2) {
            sendText("❗ Usage: /remove <job title>");
            return;
        }

        String jobToRemove = parts[1];
        if (jobList.contains(jobToRemove)) {
            jobList.remove(jobToRemove);
            sendText("🗑️ Removed job: " + jobToRemove);
        } else {
            sendText("⚠️ Job not found: " + jobToRemove);
        }
    }

    public void sendText(String text) {
        SendMessage message = new SendMessage(String.valueOf(chatId), text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public boolean hasChatId() {
        return chatId != null;
    }

    public List<String> getJobList() {
        return jobList;
    }
}