package de.plocki;

import com.google.common.collect.Lists;
import de.plocki.commands.EmbedCommand;
import de.plocki.commands.SetupCommand;
import de.plocki.util.Hooks;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static JDA jda;

    public static void main(String[] args) throws LoginException {
        if(!new Hooks().getFileBuilder().getYaml().isSet("discordBotToken")) new Hooks().toFile("discordBotToken", "YourDiscordBotTokenComesHere");
        if(!new Hooks().getFileBuilder().getYaml().isSet("verifiedRoleID")) new Hooks().toFile("verifiedRoleID", 123456789L);
        if(!new Hooks().getFileBuilder().getYaml().isSet("thumbnailURL")) new Hooks().toFile("thumbnailURL", "URL");
        if(!new Hooks().getFileBuilder().getYaml().isSet("adminUserID")) new Hooks().toFile("adminUserID", new ArrayList<Long>(Lists.newArrayList(1L, 2L)));
        JDABuilder jdaBuilder = JDABuilder.createDefault((String) new Hooks().fromFile("discordBotToken"));
        jdaBuilder.enableIntents(GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_PRESENCES);
        jdaBuilder.setActivity(Activity.watching("ELIZON."));
        jdaBuilder.setStatus(OnlineStatus.ONLINE);
        jda = jdaBuilder.build();

        jda.addEventListener(new SetupCommand());
        jda.addEventListener(new EmbedCommand());

        jda.upsertCommand("setup", "Setup Verification").queue();
        jda.upsertCommand("embed", "Build Embed").queue();

    }

}