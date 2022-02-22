package fr.noalegeek.pepite_dor_bot.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import fr.noalegeek.pepite_dor_bot.enums.CommandCategories;
import fr.noalegeek.pepite_dor_bot.utils.MessageHelper;
import fr.noalegeek.pepite_dor_bot.utils.UnicodeCharacters;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;

import java.awt.Color;
import java.time.Instant;

public class ShutdownCommand extends Command {

    public ShutdownCommand() {
        this.name = "shutdown";
        this.help = "help.shutdown";
        this.aliases = new String[]{"sd","shutd","sdown"};
        this.guildOnly = false;
        this.ownerCommand = true;
        this.category = CommandCategories.MISC.category;
    }

    @Override
    protected void execute(CommandEvent event) {
        event.getChannel().sendMessage(new MessageBuilder(MessageHelper.getEmbed(MessageHelper.translateMessage("success.shutdown", event), event, Color.GREEN, null, null, (Object[]) null).build()).build()).queue(e -> event.getJDA().shutdown());
    }
}
