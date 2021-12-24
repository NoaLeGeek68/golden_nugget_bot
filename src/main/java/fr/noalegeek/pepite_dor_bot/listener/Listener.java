package fr.noalegeek.pepite_dor_bot.listener;

import com.google.common.base.Throwables;
import com.jagrosh.jdautilities.command.Command;
import fr.noalegeek.pepite_dor_bot.Main;
import fr.noalegeek.pepite_dor_bot.config.ServerConfig;
import fr.noalegeek.pepite_dor_bot.utils.LevenshteinDistance;
import fr.noalegeek.pepite_dor_bot.utils.MessageHelper;
import fr.noalegeek.pepite_dor_bot.utils.UnicodeCharacters;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static fr.noalegeek.pepite_dor_bot.Main.*;

public class Listener extends ListenerAdapter {

    @Override
    public void onShutdown(@NotNull ShutdownEvent event) {
        try {
            Listener.saveConfigs();
        } catch (IOException ex) {
            LOGGER.severe(Throwables.getStackTraceAsString(ex));
        }
        Main.getExecutorService().schedule(() -> System.exit(0), 3, TimeUnit.SECONDS); //JDA doesn't want to exit the JVM so we do a System.exit()
    }

    public static void saveConfigs() throws IOException {
        if (!new File(new File("config/server-config.json").toPath().toUri()).exists()) new File(new File("config/server-config.json").toPath().toUri()).createNewFile();
        if (gson.fromJson(Files.newBufferedReader(new File("config/server-config.json").toPath(), StandardCharsets.UTF_8), ServerConfig.class) == getServerConfig()) return;
        Writer writer = Files.newBufferedWriter(new File("config/server-config.json").toPath(), StandardCharsets.UTF_8, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        gson.toJson(getServerConfig(), writer);
        writer.close();
        LOGGER.info("Server config updated");
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        if (Main.getServerConfig().channelMemberJoin().containsKey(event.getGuild().getId())) {
            if(event.getGuild().getSystemChannel() == null){

                return;
            }
            try {
                event.getGuild().getSystemChannel().sendMessage(new MessageBuilder(new EmbedBuilder() // Sends a memberJoin embed
                        .setThumbnail(event.getMember().getUser().getAvatarUrl())
                        .setTitle(String.format(MessageHelper.translateMessage("listener.onGuildMemberJoin.memberJoin", event.getUser(), event.getGuild()), event.getMember().getEffectiveName(), event.getGuild().getName()))
                        .addField(MessageHelper.translateMessage("listener.onGuildMemberJoin.member", event.getUser(), event.getGuild()), event.getMember().getAsMention(), false)
                        .addField(String.format("%s %s", UnicodeCharacters.heavyPlusSign, MessageHelper.translateMessage("listener.onGuildMemberJoin.newMember", event.getUser(), event.getGuild())), String.format(MessageHelper.translateMessage("listener.onGuildMemberJoin.countMember", event.getUser(), event.getGuild()), event.getGuild().getMemberCount()), false)
                        .setTimestamp(Instant.now())
                        .setColor(Color.GREEN)
                        .build()).build()).queue();
            } catch (InsufficientPermissionException ex) {
                event.getGuild().getOwner().getUser().openPrivateChannel().queue(privateChannel ->
                        privateChannel.sendMessage(MessageHelper.formattedMention(event.getGuild().getOwner().getUser()) + MessageHelper.getTag(event.getUser()) +
                                " a rejoint votre serveur **" + event.getGuild().getName() +
                                "** mais je n'ai pas pu envoyer le message de bienvenue car je n'ai pas accès au salon mis par défaut.\n" +
                        "(Vous n'avez pas configurer le salon des messages de bienvenue, c'est pour cela que j'ai choisi le salon par défaut. " +
                        "Vous pouvez changer tout cela en faisant `" + getInfos().prefix() + "config channelmember remove <identifiant du salon>`)"));
            }
            return;
        }

        //TODO verif si le salon existe
        try {
            event.getGuild().getTextChannelById(getServerConfig().channelMemberJoin().get(event.getGuild().getId())).sendMessage(new MessageBuilder(new EmbedBuilder() // Sends a memberJoin embed
                    .setThumbnail(event.getMember().getUser().getAvatarUrl())
                    .setTitle(String.format(MessageHelper.translateMessage("listener.onGuildMemberJoin.memberJoin", event.getUser(), event.getGuild()), event.getMember().getEffectiveName(), event.getGuild().getName()))
                    .addField(MessageHelper.translateMessage("listener.onGuildMemberJoin.member", event.getUser(), event.getGuild()), event.getMember().getAsMention(), false)
                    .addField(String.format("%s %s", UnicodeCharacters.heavyPlusSign, MessageHelper.translateMessage("listener.onGuildMemberJoin.newMember", event.getUser(), event.getGuild())), String.format(MessageHelper.translateMessage("listener.onGuildMemberJoin.countMember", event.getUser(), event.getGuild()), event.getGuild().getMemberCount()), false)
                    .setTimestamp(Instant.now())
                    .setColor(Color.GREEN)
                    .build()).build()).queue();
        } catch (InsufficientPermissionException ex) {
            event.getGuild().getOwner().getUser().openPrivateChannel().queue(privateChannel ->
                    privateChannel.sendMessage(MessageHelper.formattedMention(event.getGuild().getOwner().getUser()) + MessageHelper.getTag(event.getUser()) +
                            " a rejoint votre serveur **" + event.getGuild().getName() +
                            "** mais je n'ai pas pu envoyer le message de bienvenue car je n'ai pas accès au salon configuré.\n" +
                    "(Vous avez configurer le salon des messages de bienvenue, c'est pour cela que j'ai choisi le salon configuré. Vous pouvez changer tout cela en faisant `" +
                    getInfos().prefix() + "config channelmember join reset`)"));
        }
        if (getServerConfig().guildJoinRole().containsKey(event.getGuild().getId())) {
            event.getGuild().addRoleToMember(event.getMember(), Objects.requireNonNull(event.getGuild().getRoleById(Main.getServerConfig().guildJoinRole()
                    .get(event.getGuild().getId())))).queue();
        }
        LOGGER.info(event.getUser().getName() + "#" + event.getUser().getDiscriminator() + " a rejoint le serveur " + event.getGuild().getName() + ".");
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        EmbedBuilder embedMemberLeave = new EmbedBuilder()
                .setThumbnail(event.getUser().getAvatarUrl())
                .setTitle("**" + (event.getUser()).getName() + " a quitté le serveur __" + event.getGuild().getName() + "__ !**")
                .addField("Membre", event.getUser().getAsMention(), false)
                .addField("➖ Membre perdu", "Nous sommes de nouveau à " + event.getGuild().getMemberCount() + " membres sur le serveur...", false)
                .setTimestamp(Instant.now())
                .setColor(Color.RED);
        if (!getServerConfig().channelMemberLeave().containsKey(event.getGuild().getId())) {
            try {
                event.getGuild().getDefaultChannel().sendMessage(new MessageBuilder(embedMemberLeave).build()).queue();
            } catch (InsufficientPermissionException ex) {
                event.getGuild().getOwner().getUser().openPrivateChannel().queue(privateChannel ->
                        privateChannel.sendMessage(MessageHelper.formattedMention(event.getGuild().getOwner().getUser()) + MessageHelper.getTag(event.getUser()) +
                                " a quitté votre serveur **" + event.getGuild().getName() +
                                "** mais je n'ai pas pu envoyer le message de départ car je n'ai pas accès au salon mis par défaut.\n" +
                        "(Vous n'avez pas configurer le salon des messages de départs, c'est pour cela que j'ai choisi le salon par défaut. Vous pouvez changer tout cela en faisant `"
                                + getInfos().prefix() + "config channelmember remove <identifiant du salon>`)"));
            }
            return;
        }
        try {
            event.getGuild().getTextChannelById(getServerConfig().channelMemberLeave().get(event.getGuild().getId())).sendMessage(new MessageBuilder(embedMemberLeave).build()).queue();
        } catch (InsufficientPermissionException ex) {
            event.getGuild().getOwner().getUser().openPrivateChannel().queue(privateChannel -> privateChannel.
                    sendMessage(MessageHelper.formattedMention(event.getGuild().getOwner().getUser()) + MessageHelper.getTag(event.getUser()) +
                            " a quitté votre serveur **" + event.getGuild().getName() + "** mais je n'ai pas pu envoyer le message de départ car je n'ai pas accès au salon configuré.\n" +
                    "(Vous avez configurer le salon des messages de départ, c'est pour cela que j'ai choisi le salon configuré. Vous pouvez changer tout cela en faisant `" +
                    getInfos().prefix() + "config channelmember remove reset`)"));
        }
        LOGGER.info(event.getUser().getName() + "#" + event.getUser().getDiscriminator() + " a quitté le serveur " + event.getGuild().getName() + ".");
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if(event.getMessage().getMentionedUsers().contains(event.getJDA().getSelfUser())) {
            if(Main.getServerConfig().prefix().containsKey(event.getGuild().getId())) {
                event.getMessage().reply("My prefix is **" + Main.getServerConfig().prefix().get(event.getGuild().getId()) + "**").queue();
                return;
            }
            event.getMessage().reply("My prefix is **" + Main.getClient().getPrefix() + "**").queue();
            return;
        }
        String message = event.getMessage().getContentRaw();
        LOGGER.info(String.format("%s %s:%n %s", MessageHelper.getTag(event.getAuthor()), "a dit", message));
        if (getServerConfig().prohibitWords() == null) {
            new File("config/server-config.json").delete();
            try {
                setupServerConfig();
            } catch (IOException ex) {
                LOGGER.severe(ex.getMessage());
            }
            return;
        }
        if(message.startsWith(Main.getPrefix(event.getGuild()))) {
            String[] args = message.substring(Main.getPrefix(event.getGuild()).length()).split("\\s+");
            if(args.length == 0) return;
            String cmdName = args[0];
            if(Main.getClient().getCommands().stream().anyMatch(command -> command.getName().equalsIgnoreCase(cmdName) ||
                    Arrays.stream(command.getAliases()).anyMatch(cmdName::equalsIgnoreCase)) || Main.getClient().getHelpWord().equalsIgnoreCase(cmdName)) return;
            double highestResult = 0;
            String cmd = null;
            for (Command command : getClient().getCommands()) {
                double _highestResult = LevenshteinDistance.getDistance(cmdName, command.getName());
                double b = 0;
                String _alias = command.getName();

                if(b > _highestResult) {
                    _highestResult = b;
                }
                if(highestResult < _highestResult) {
                    cmd = _alias;
                    highestResult = _highestResult;
                }
            }
            event.getChannel().sendMessage("Did you meant " + cmd).mention(event.getAuthor()).complete();
        }
        if (!getServerConfig().prohibitWords().containsKey(event.getGuild().getId())) return;
        for (String s : getServerConfig().prohibitWords().get(event.getGuild().getId())) {
            for(String alias : new String[]{"prohibitw","prohitbitwrd","pw","pwrd","pword"}){
                if(message.toLowerCase().startsWith(alias)){
                    return;
                }
            }
            if (message.toLowerCase().contains(s.toLowerCase())) {
                event.getMessage().delete().queue(unused -> event.getMessage().reply(MessageHelper.formattedMention(event.getAuthor()) + "Le mot `" + s + "` fait parti de la liste des mots interdits.").queue());
            }
        }
    }
}