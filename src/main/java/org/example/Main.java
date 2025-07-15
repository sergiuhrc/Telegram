package org.example;

import org.example.telegram.MyTelegramBot;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class Main {

    public static void main(String[] args) throws Exception {

        TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        MyTelegramBot bot = new MyTelegramBot();
        botsApi.registerBot(bot);

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runJobCheck(bot); // 🔁 Periodic check
            }
        }, 0, 3_600_000); // every 1 hour
    }

    public static void runJobCheck(MyTelegramBot bot) {
        try {
            if (bot.getJobList() == null || bot.getJobList().isEmpty()) return;

            Document doc = Jsoup.connect("https://civic.md/anunturi/angajare.html").get();
            Element table = doc.selectFirst("table");
            if (table == null) return;

            Elements rows = table.select("tr");
            List<String> matchedJobs = new ArrayList<>();

            for (Element row : rows) {
                Elements cells = row.select("td, th");

                for (Element cell : cells) {
                    Element link = cell.selectFirst("a[href]");
                    if (link != null) {
                        String linkText = link.text();
                        String href = link.absUrl("href");

                        for (String keyword : bot.getJobList()) {
                            if (linkText.toLowerCase().contains(keyword.toLowerCase())) {
                                String jobInfo = "✅ " + linkText + "\n🔗 " + href;
                                matchedJobs.add(jobInfo);
                            }
                        }
                    }
                }
            }

            if (bot.hasChatId() && !matchedJobs.isEmpty()) {
                bot.sendText("⏰ Found matching jobs:");
                for (String job : matchedJobs) {
                    bot.sendText(job);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}