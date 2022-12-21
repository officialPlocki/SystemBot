package de.plocki;

import com.google.common.collect.Lists;
import de.plocki.commands.Embed;
import de.plocki.commands.SetupVerify;
import de.plocki.commands.StatusEmbed;
import de.plocki.util.Hooks;
import de.plocki.util.Status;
import de.plocki.util.StatusManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static java.lang.Thread.sleep;

public class Main {

    public static JDA jda;

    public static void main(String[] args) throws LoginException {
        if(!new Hooks().getFileBuilder().getYaml().isSet("discordBotToken")) new Hooks().toFile("discordBotToken", "YourDiscordBotTokenComesHere");
        if(!new Hooks().getFileBuilder().getYaml().isSet("verifiedRoleID")) new Hooks().toFile("verifiedRoleID", 123456789L);
        if(!new Hooks().getFileBuilder().getYaml().isSet("thumbnailURL")) new Hooks().toFile("thumbnailURL", "URL");
        if(!new Hooks().getFileBuilder().getYaml().isSet("author")) new Hooks().toFile("author", "Vultron Studios");
        if(!new Hooks().getFileBuilder().getYaml().isSet("statusChannelID")) new Hooks().toFile("statusChannelID", 123456789L);
        if(!new Hooks().getFileBuilder().getYaml().isSet("adminUserID")) new Hooks().toFile("adminUserID", new ArrayList<Long>(Lists.newArrayList(1L, 2L)));
        JDABuilder jdaBuilder = JDABuilder.createDefault((String) new Hooks().fromFile("discordBotToken"));
        jdaBuilder.enableIntents(GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_EMOJIS_AND_STICKERS);
        jdaBuilder.setActivity(Activity.watching("Servers"));
        jdaBuilder.setStatus(OnlineStatus.ONLINE);
        jda = jdaBuilder.build();

        jda.addEventListener(new SetupVerify());
        jda.addEventListener(new Embed());
        jda.addEventListener(new StatusEmbed());

        jda.upsertCommand("setup", "Setup Verification").queue();
        jda.upsertCommand("embed", "Build Embed").queue();
        jda.upsertCommand("statusembed", "Add status Embed").queue();

        new Thread(() -> {
            try {
                sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            new Main().startThread();
        }).start();
    }

    public void startThread() {
        Thread thread = new Thread(() -> {
            StatusManager manager = new StatusManager();
            TextChannel channel = jda.getTextChannelById((long) new Hooks().fromFile("statusChannelID"));
            while(true) {
                List<Long> ids = manager.getStatusEmbeds();
                if(ids.size() > 0) {
                    for(Long id : ids) {
                        InetAddress address;
                        try {
                            address = InetAddress.getByName(manager.getAddress(id));
                        } catch (UnknownHostException e) {
                            address = null;
                        }
                        boolean status;
                        boolean orange = false;
                        long ms;
                        if(address == null) {
                            ms = 1000;
                            status = false;
                        } else {
                            try {
                                long now = System.currentTimeMillis();
                                status = address.isReachable(60);
                                ms = System.currentTimeMillis() - now;
                                if(ms > 55) {
                                    orange = true;
                                }
                                if(ms > 500) {
                                    status = false;
                                }
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }

                        boolean edit = false;

                        EmbedBuilder builder = new EmbedBuilder();
                        builder.setTitle(manager.getName(id));
                        String e = "ERROR";
                        if(orange && !manager.getStatus(id).equals(Status.ORANGE)) {
                            builder.setColor(Color.ORANGE);
                            e = jda.getEmojisByName("unstable_connection", true).get(0).getFormatted();
                            RichCustomEmoji e2 = jda.getEmojisByName("interrupted_connection", true).get(0);
                            e = e + " " + e2.getFormatted();
                            builder.setDescription("Unstable Connection (high ping)");
                            manager.setLastStatus(id, true);
                            manager.setStatus(id, Status.ORANGE);
                            edit = true;
                        } else if(status) {
                            manager.setLastStatus(id, true);
                            if(manager.checkLastStatus(id) && !manager.getStatus(id).equals(Status.GREEN)) {
                                builder.setColor(Color.GREEN);
                                e = jda.getEmojisByName("good_connection", true).get(0).getFormatted();
                                RichCustomEmoji e2 = jda.getEmojisByName("available_connection", true).get(0);
                                e = e + " " + e2.getFormatted();
                                builder.setDescription("Available Connection");
                                manager.setStatus(id, Status.GREEN);
                                edit = true;
                            } else if(!manager.checkLastStatus(id) && !manager.getStatus(id).equals(Status.ORANGE)) {
                                builder.setColor(Color.ORANGE);
                                e = jda.getEmojisByName("unstable_connection", true).get(0).getFormatted();
                                RichCustomEmoji e2 = jda.getEmojisByName("interrupted_connection", true).get(0);
                                e = e + " " + e2.getFormatted();
                                builder.setDescription("Unstable Connection");
                                manager.setStatus(id, Status.ORANGE);
                                edit = true;
                            }
                        } else {
                            manager.setLastStatus(id, false);
                            if(manager.checkLastStatus(id) && !manager.getStatus(id).equals(Status.ORANGE)) {
                                builder.setColor(Color.ORANGE);
                                e = jda.getEmojisByName("unstable_connection", true).get(0).getFormatted();
                                RichCustomEmoji e2 = jda.getEmojisByName("interrupted_connection", true).get(0);
                                e = e + " " + e2.getFormatted();
                                builder.setDescription("Unstable Connection");
                                manager.setStatus(id, Status.ORANGE);
                                edit = true;
                            } else if(!manager.checkLastStatus(id) && !manager.getStatus(id).equals(Status.RED)) {
                                builder.setColor(Color.RED);
                                try {
                                    RichCustomEmoji emoji = jda.getEmojisByName("bad_connection", true).get(0);
                                    e = emoji.getFormatted();
                                } catch (NullPointerException ex) {
                                    ex.printStackTrace();
                                    e = "";
                                }
                                RichCustomEmoji e2 = jda.getEmojisByName("no_connection", true).get(0);
                                e = e + " " + e2.getFormatted();
                                builder.setDescription("No Connection " + jda.getEmojisByName("disconnected", true).get(0).getFormatted());
                                edit = true;
                                manager.setStatus(id, Status.RED);
                            }
                        }

                        if(edit) {
                            builder.addField("Status", e, true);
                            builder.addField("Ping", ms + "ms", true);
                            builder.setFooter("Last Updated: " + new Date(System.currentTimeMillis()));
                            assert channel != null;
                            channel.editMessageEmbedsById(id, builder.build()).queue();
                        }

                        try {
                            Thread.sleep(1500);
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
                try {
                    sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            thread.interrupt();
            jda.shutdown();
        }));
    }

}