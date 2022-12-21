package de.plocki.commands;

import de.plocki.util.Hooks;
import de.plocki.util.StatusManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.Modal;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class StatusEmbed extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if(event.getName().equals("statusembed")) {
            if(((List<Long>) new Hooks().fromFile("adminUserID")).contains(event.getInteraction().getUser().getIdLong())) {
                TextInput address = TextInput.create("address", "Address", TextInputStyle.SHORT)
                        .setPlaceholder("Enter the address of the server")
                        .setRequired(true)
                        .build();
                TextInput name = TextInput.create("name", "Name", TextInputStyle.SHORT)
                        .setPlaceholder("Enter the name of the status")
                        .setRequired(true)
                        .build();
                Modal modal = Modal.create("statusEmbed", "Status Embed")
                        .addActionRow(address)
                        .addActionRow(name)
                        .build();
                event.replyModal(modal).queue();
            }
        }
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        if(event.getModalId().equals("statusEmbed")) {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("Loading...");
            event.getChannel().sendMessageEmbeds(builder.build()).queue(message -> {
                new StatusManager().registerStatusEmbed(message.getIdLong(), event.getValue("address").getAsString(), event.getValue("name").getAsString());
                new StatusManager().setLastStatus(message.getIdLong(), false);
            });
            event.reply("Embed created!").setEphemeral(true).queue();
        }
    }
}
