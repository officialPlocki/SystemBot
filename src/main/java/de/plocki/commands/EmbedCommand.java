package de.plocki.commands;

import de.plocki.Main;
import de.plocki.util.Hooks;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.UserSnowflake;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.*;
import java.util.List;

public class EmbedCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if(event.getName().equals("embed")) {
            if(((List<Long>) new Hooks().fromFile("adminUserID")).contains(event.getInteraction().getUser().getIdLong())) {
                TextInput title = TextInput.create("title", "Title", TextInputStyle.SHORT)
                        .setRequired(true)
                        .build();

                TextInput description = TextInput.create("description", "Description", TextInputStyle.PARAGRAPH)
                        .setRequired(true)
                        .build();

                Modal modal = Modal.create("embedBuilder", "Embed Builder")
                        .addActionRow(title)
                        .addActionRow(description)
                        .build();

                event.replyModal(modal).queue();
            }
        }
    }

    private final HashMap<Long, Long> ids = new HashMap<>();

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        if(event.getModalId().equals("embedBuilder")) {
            EmbedBuilder builder = new EmbedBuilder();

            final String[] msg = {Objects.requireNonNull(event.getValue("description")).getAsString()};

            HashMap<String, String> replacement = new HashMap<>();

            List<String> emoji = new ArrayList<>();

            for (String se : msg[0].split("\n")) {
                for(String s : se.split(" ")) {
                    if(s.startsWith(":")) {
                        if(s.endsWith(":")) {
                            String str = s.replaceAll(":", "");
                            RichCustomEmoji e = Main.jda.getEmojisByName(str, true).get(0);
                            replacement.put(str, e.getFormatted());
                        }
                    }
                }
            }

            replacement.forEach((s, s2) -> {
                msg[0] = msg[0].replaceAll(":" + s + ":", s2);
            });

            builder.setDescription(msg[0]);
            builder.setTitle(Objects.requireNonNull(event.getValue("title")).getAsString());

            builder.setFooter("Powered by ClusterNode.net", "https://cdn.clusternode.net/image/s/clusternode_net.png");
            builder.setColor(Color.cyan);
            builder.setAuthor("ELIZON.");
            builder.setThumbnail((String) new Hooks().fromFile("thumbnailURL"));

            event.getChannel().sendMessageEmbeds(builder.build())
                    .queue(message -> ids.put(event.getInteraction().getUser().getIdLong(), message.getIdLong()));

            event.reply("Do you want any buttons?")
                    .setEphemeral(true)
                    .addActionRow(
                            Button.primary("button_link", "Link Button"),
                            Button.primary("button_role", "Reaction Role"))
                    .queue();
        } else if(event.getModalId().equals("button_link")) {
            event.getChannel().retrieveMessageById(ids.get(event.getInteraction().getUser().getIdLong())).queue(message -> {
                List<ItemComponent> array = new ArrayList<>();
                message.getActionRows().forEach(itemComponents -> {
                    array.addAll(itemComponents.getComponents());
                });
                array.add(Button.link(Objects.requireNonNull(event.getValue("link")).getAsString(), Objects.requireNonNull(event.getValue("name")).getAsString()));
                message.editMessageEmbeds(message.getEmbeds().get(0)).setActionRow(array).queue();
            });
            event.reply("Added.").setEphemeral(true).queue();
        } else if(event.getModalId().equals("button_role")) {
            event.getChannel().retrieveMessageById(ids.get(event.getInteraction().getUser().getIdLong())).queue(message -> {
                List<ItemComponent> array = new ArrayList<>();
                message.getActionRows().forEach(itemComponents -> {
                    array.addAll(itemComponents.getComponents());
                });
                array.add(Button.primary("role_add_" + Objects.requireNonNull(event.getValue("role")).getAsString(), Objects.requireNonNull(event.getValue("name")).getAsString()));
                message.editMessageEmbeds(message.getEmbeds().get(0)).setActionRow(array).queue();
            });
            event.reply("Added.").setEphemeral(true).queue();
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if(event.getButton().getId().equals("button_link")) {
            TextInput link = TextInput.create("link", "Link", TextInputStyle.SHORT)
                    .setRequired(true)
                    .build();

            TextInput name = TextInput.create("name", "Button name", TextInputStyle.SHORT)
                    .setRequired(true)
                    .build();

            Modal modal = Modal.create("button_link", "Create Link Button")
                    .addActionRow(link)
                    .addActionRow(name)
                    .build();
            event.replyModal(modal).queue();
        } else if(event.getButton().getId().equals("button_role")) {
            TextInput link = TextInput.create("role", "Role ID", TextInputStyle.SHORT)
                    .setRequired(true)
                    .build();

            TextInput name = TextInput.create("name", "Button name", TextInputStyle.SHORT)
                    .setRequired(true)
                    .build();

            Modal modal = Modal.create("button_role", "Create Role Button")
                    .addActionRow(link)
                    .addActionRow(name)
                    .build();
            event.replyModal(modal).queue();
        } else if(event.getButton().getId().startsWith("role_add_")) {
            long id = Long.parseLong(event.getButton().getId().replaceAll("role_add_", ""));
            Member member = event.getMember();
            if(member.getRoles().contains(event.getGuild().getRoleById(id))) {
                Objects.requireNonNull(event.getGuild()).removeRoleFromMember(UserSnowflake.fromId(event.getInteraction().getUser().getIdLong()), Objects.requireNonNull(event.getGuild().getRoleById(id))).queue();
                EmbedBuilder builder = new EmbedBuilder();
                builder.setFooter("Powered by ClusterNode.net", "https://cdn.clusternode.net/image/s/clusternode_net.png");
                builder.setDescription("Role has been removed.");
                builder.setColor(Color.cyan);
                builder.setAuthor("ELIZON.");
                builder.setThumbnail((String) new Hooks().fromFile("thumbnailURL"));
                event.replyEmbeds(builder.build())
                        .setEphemeral(true)
                        .queue();
            } else {
                Objects.requireNonNull(event.getGuild()).addRoleToMember(UserSnowflake.fromId(event.getInteraction().getUser().getIdLong()), Objects.requireNonNull(event.getGuild().getRoleById(id))).queue();
                EmbedBuilder builder = new EmbedBuilder();
                builder.setFooter("Powered by ClusterNode.net", "https://cdn.clusternode.net/image/s/clusternode_net.png");
                builder.setDescription("Role has been added.");
                builder.setColor(Color.cyan);
                builder.setAuthor("ELIZON.");
                builder.setThumbnail((String) new Hooks().fromFile("thumbnailURL"));
                event.replyEmbeds(builder.build())
                        .setEphemeral(true)
                        .queue();
            }
        }
    }
}
