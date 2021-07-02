package fr.noalegeek.pepite_dor_bot.listener;

import fr.noalegeek.pepite_dor_bot.Main;
import fr.noalegeek.pepite_dor_bot.config.ServerConfig;
import fr.noalegeek.pepite_dor_bot.utils.helpers.MessageHelper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

import static fr.noalegeek.pepite_dor_bot.Main.*;

public class Listener extends ListenerAdapter {

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                event.getJDA().getPresence().setActivity(Activity.playing(getInfos().activities[new Random().nextInt(getInfos().activities.length)]));
            }
        }, 0, getInfos().timeBetweenStatusChange * 1000);

        Timer autoSave = new Timer();
        autoSave.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    saveConfigs();
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, ex.getMessage());
                }
            }
        }, 120_000, getInfos().autoSaveDelay * 1000 * 60);
    }

    @Override
    public void onShutdown(@NotNull ShutdownEvent event) {
        try {
            saveConfigs();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
        }
    }

    public void saveConfigs() throws IOException {
        Path configPath = new File("config/server-config.json").toPath();
        if(!new File(configPath.toUri()).exists()) {
            new File(configPath.toUri()).createNewFile();
        }
        Reader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8);
        if(gson.fromJson(reader, ServerConfig.class) == Main.getServerConfig()) return;
        Writer writer = Files.newBufferedWriter(configPath, StandardCharsets.UTF_8, StandardOpenOption.WRITE);
        gson.toJson(Main.getServerConfig(), writer);
        writer.close();
        LOGGER.info("Server config updated");
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        MessageEmbed embedMemberJoin = new EmbedBuilder()
                .setThumbnail(event.getMember().getUser().getAvatarUrl())
                .setTitle("**" + event.getMember().getEffectiveName()+" a rejoint le serveur __"+event.getGuild().getName()+ "__ !**")
                .addField("Membre", event.getMember().getAsMention(), false)
                .addField("➕ Nouveau membre","Nous sommes maintenant "+event.getGuild().getMemberCount()+" membres sur le serveur !", false)
                .setTimestamp(Instant.now())
                .setColor(Color.GREEN)
                .build();
        if(!getServerConfig().channelMemberJoin.containsKey(event.getGuild().getId())) {
            Objects.requireNonNull(event.getGuild().getDefaultChannel()).sendMessage(embedMemberJoin).queue();
            return;
        }
        Objects.requireNonNull(event.getGuild().getTextChannelById(getServerConfig().channelMemberJoin.get(event.getGuild().getId()))).sendMessage(embedMemberJoin).queue();
        if(getServerConfig().guildJoinRole.containsKey(event.getGuild().getId())) {
            event.getGuild().addRoleToMember(event.getMember(), Objects.requireNonNull(event.getGuild().getRoleById(Main.getServerConfig().guildJoinRole.get(event.getGuild().getId())))).queue();
        }
        LOGGER.info(event.getUser().getName()+"#"+event.getUser().getDiscriminator()+" a rejoint le serveur "+event.getGuild().getName()+".");
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        MessageEmbed embedMemberRemove = new EmbedBuilder()
                .setThumbnail(event.getUser().getAvatarUrl())
                .setTitle("**"+(event.getUser()).getName() + " a quitté le serveur __" + event.getGuild().getName() + "__ !**")
                .addField("Membre",event.getUser().getAsMention(), false)
                .addField("➖ Membre perdu","Nous sommes de nouveau à "+event.getGuild().getMemberCount()+" membres sur le serveur...", false)
                .setTimestamp(OffsetDateTime.now(Clock.systemUTC()))
                .setColor(Color.RED)
                .build();
        if(!getServerConfig().channelMemberRemove.containsKey(event.getGuild().getId())) Objects.requireNonNull(event.getGuild().getDefaultChannel()).sendMessage(embedMemberRemove).queue();
        Objects.requireNonNull(event.getGuild().getTextChannelById(getServerConfig().channelMemberRemove.get(event.getGuild().getId()))).sendMessage(embedMemberRemove).queue();
        LOGGER.info(event.getUser().getName()+"#"+event.getUser().getDiscriminator()+" a quitté le serveur "+event.getGuild().getName()+".");
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if(event.getAuthor() == event.getJDA().getSelfUser()) return;
        LOGGER.info(event.getAuthor().getName() + "#" + event.getAuthor().getDiscriminator() + " a dit :\n" +
                event.getMessage().getContentRaw());
        if(getServerConfig().prohibitWords == null) {
            new File("config/server-config.json").delete();
            try {
                Main.setupServerConfig();
            } catch (IOException ex) {
                LOGGER.severe(ex.getMessage());
            }
            return;
        }
        if(!getServerConfig().prohibitWords.containsKey(event.getGuild().getId())) return;
        for (String s : getServerConfig().prohibitWords.get(event.getGuild().getId())) {
            if(event.getMessage().getContentRaw().toLowerCase().contains(s.toLowerCase())) {
                event.getMessage().delete().queue(unused -> event.getMessage().reply(MessageHelper.formattedMention(event.getAuthor()) +
                                "Le mot `" + s + "` fait parti de la liste des mots interdits.").queue(),
                        unused -> event.getMessage().addReaction("\uD83E\uDD14\n").queue());
            }
        }
    }
}