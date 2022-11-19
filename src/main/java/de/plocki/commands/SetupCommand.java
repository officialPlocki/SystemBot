package de.plocki.commands;

import de.plocki.Main;
import de.plocki.util.Hooks;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;
import java.util.Objects;

public class SetupCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if(event.getName().equals("setup")) {
            if(((List<Long>) new Hooks().fromFile("adminUserID")).contains(event.getInteraction().getUser().getIdLong())) {
                EmbedBuilder builder = new EmbedBuilder();
                builder.setFooter("Powered by ClusterNode.net", "https://cdn.clusternode.net/image/s/clusternode_net.png");
                builder.setDescription("Please click the button to verify.");
                builder.setColor(Color.cyan);
                builder.setAuthor("ELIZON.");
                builder.setThumbnail((String) new Hooks().fromFile("thumbnailURL"));
                event.getChannel().sendMessageEmbeds(builder.build())
                        .addActionRow(
                                Button.success("verify", "Verify")
                        ).queue();
            }
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if(Objects.equals(event.getButton().getId(), "verify")) {
            Objects.requireNonNull(event.getGuild()).addRoleToMember(UserSnowflake.fromId(event.getInteraction().getUser().getIdLong()), Objects.requireNonNull(event.getGuild().getRoleById((long) new Hooks().fromFile("verifiedRoleID")))).queue();
            EmbedBuilder builder = new EmbedBuilder();
            builder.setFooter("Powered by ClusterNode.net", "https://cdn.clusternode.net/image/s/clusternode_net.png");
            builder.setDescription("Successfully verified.");
            builder.setColor(Color.cyan);
            builder.setAuthor("ELIZON.");
            builder.setThumbnail((String) new Hooks().fromFile("thumbnailURL"));
            event.replyEmbeds(builder.build())
                    .setEphemeral(true)
                    .queue();
        }
    }
}
