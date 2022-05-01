package net.thesimpleteam.simplebot.utils;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.thesimpleteam.simplebot.SimpleBot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

public class MessageHelper {

    private MessageHelper() {}

    public static String getTag(final User user) {
        return user.getName() + "#" + user.getDiscriminator();
    }

    public static String formattedMention(User user) {
        return String.format("**[**%s**]** ", user.getAsMention());
    }

    public static void syntaxError(CommandEvent event, Command command, String informations) {
        StringBuilder argumentsBuilder = new StringBuilder();
        if (command.getArguments() == null)
            argumentsBuilder.append(translateMessage(event, "error.commands.syntaxError.arguments.argumentsNull"));
        else if (!command.getArguments().startsWith("arguments."))
            argumentsBuilder.append(command.getArguments());
        else {
            if (translateMessage(event, command.getArguments()).split("²").length == 1) {
                argumentsBuilder.append(SimpleBot.getPrefix(event.getGuild())).append(command.getName()).append(" ").append(translateMessage(event, command.getArguments()));
            } else {
                int loop = 1;
                for (String arg : Arrays.stream(translateMessage(event, command.getArguments()).split("²")).toList()) {
                    loop = Math.max(loop, arg.split(">").length);
                }
                int indexList = 1;
                for (int length = 1; length <= loop; length++) {
                    int finalLength = length;
                    if (Arrays.stream(translateMessage(event, command.getArguments()).split("²")).anyMatch(arguments -> arguments.split(">").length == finalLength)) {
                        if (!Arrays.stream(translateMessage(event, command.getArguments()).split("²")).filter(arguments -> arguments != null && arguments.split(">").length == finalLength)
                                .toList().isEmpty()) {
                            argumentsBuilder.append("__");
                            switch (finalLength) {
                                case 1 -> argumentsBuilder.append(translateMessage(event, "error.commands.syntaxError.arguments.oneArgument"));
                                case 2 -> argumentsBuilder.append(translateMessage(event, "error.commands.syntaxError.arguments.twoArguments"));
                                case 3 -> argumentsBuilder.append(translateMessage(event, "error.commands.syntaxError.arguments.threeArguments"));
                                case 4 -> argumentsBuilder.append(translateMessage(event, "error.commands.syntaxError.arguments.fourArguments"));
                                default -> argumentsBuilder.append("The devs forgotten to add the syntax with the length of ").append(finalLength);
                            }
                            argumentsBuilder.append("__").append("\n\n");
                            for (int index = 0; index < Arrays.stream(translateMessage(event, command.getArguments()).split("²")).filter(arguments -> arguments != null && arguments.split(">").length == finalLength).toList().size(); index++) {
                                argumentsBuilder.append(SimpleBot.getPrefix(event.getGuild())).append(command.getName()).append(" ").append(Arrays.stream(translateMessage(event, command.getArguments()).split("²")).filter(arguments -> arguments != null && arguments.split(">").length == finalLength).toList().get(index)).append(" \u27A1 *").append(translateMessage(event, command.getHelp()).split("²")[indexList]).append("*\n");
                                indexList++;
                            }
                            argumentsBuilder.append("\n");
                        }
                    }
                }
            }
        }
        String examples;
        if (command.getExample() == null)
            examples = translateMessage(event, "error.commands.syntaxError.examples.exampleNull");
        else if (command.getExample().startsWith("example."))
            examples = Arrays.toString(Stream.of(translateMessage(event, command.getExample()).split("²")).map(example -> example = SimpleBot.getPrefix(event.getGuild()) + command.getName() + " " + example).toArray()).replace("[", "").replace("]", "").replace(",", "");
        else
            examples = Arrays.toString(Stream.of(command.getExample().split("²")).map(example -> example = SimpleBot.getPrefix(event.getGuild()) + command.getName() + " " + example).toArray()).replace("[", "").replace("]", "").replace(",", "");
        EmbedBuilder embedBuilder = getEmbed(event, "error.commands.syntaxError.syntaxError", null, null, null, command.getName())
                .addField(command.getArguments().startsWith("arguments.") ? translateMessage(event, command.getArguments()).split("²").length == 1 ? translateMessage(event, "error.commands.syntaxError.arguments.argument") : translateMessage(event, "error.commands.syntaxError.arguments.arguments") : command.getArguments().split("²").length == 1 ? translateMessage(event, "error.commands.syntaxError.arguments.argument") : translateMessage(event, "error.commands.syntaxError.arguments.arguments"), argumentsBuilder.toString(), false)
                .addField(translateMessage(event, "error.commands.syntaxError.help"), command.getHelp() == null || command.getHelp().isEmpty() ? translateMessage(event, "error.commands.syntaxError.help.helpNull") : translateMessage(event, command.getHelp()).contains("²") ? translateMessage(event, command.getHelp()).split("²")[0] : translateMessage(event, command.getHelp()), false)
                .addField(command.getExample().startsWith("example.") ? translateMessage(event, command.getExample()).split("²").length == 1 ? translateMessage(event, "error.commands.syntaxError.examples.example") : translateMessage(event, "error.commands.syntaxError.examples.examples") : command.getExample().split("²").length == 1 ? translateMessage(event, "error.commands.syntaxError.examples.example") : translateMessage(event, "error.commands.syntaxError.examples.examples"), examples, false);
        if (informations != null) {
            if (translateMessage(event, informations).length() > 1024) {
                int field = 0;
                StringBuilder informationsBuilder = new StringBuilder();
                for (char character : informations.toCharArray()) {
                    informationsBuilder.append(character);
                    if(character == '²') {
                        field++;
                        embedBuilder.addField(field == 1 ? translateMessage(event, "error.commands.syntaxError.informations") : "", informationsBuilder.toString(), false);
                    }
                }
            } else
                embedBuilder.addField(translateMessage(event, "error.commands.syntaxError.informations"), translateMessage(event, informations), false);
        }
        //TODO [REMINDER] When all syntaxError of commands are translated, remove the informations lambda thing and add "translateMessage(informations, event)"
        event.reply(new MessageBuilder(embedBuilder.build()).build());
    }

    public static void sendError(Exception e, CommandEvent event, Command command) {
        EmbedBuilder embedBuilder = getEmbed(event, "error.commands.sendError.error", null, null, null)
                .addField(translateMessage(event, "error.commands.sendError.sendError"), e.getMessage(), false)
                .addField(translateMessage(event, "error.commands.sendError.command"), SimpleBot.getPrefix(event.getGuild()) + command.getName(), false);
        if (command.getArguments() == null || command.getArguments().isEmpty()) embedBuilder.addField(translateMessage(event, "error.commands.sendError.arguments"), event.getArgs(), false);
        event.reply(new MessageBuilder(embedBuilder.build()).build());
    }

    public static EmbedBuilder getEmbed(CommandEvent event, String title, @Nullable Color color, @Nullable String description, @Nullable String thumbnail, @Nullable Object... formatArgs){
        return getEmbed(event.getAuthor(), event.getMessage(), event.getGuild(), title, color, description, thumbnail, formatArgs);
    }

    public static EmbedBuilder getEmbed(@NotNull User author, @Nullable Message message, @NotNull Guild guild, @NotNull String title, @Nullable Color color, @Nullable String description, @Nullable String thumbnail, @Nullable Object... formatArgs) {
        EmbedBuilder embedBuilder = new EmbedBuilder().setTimestamp(Instant.now()).setFooter(getTag(author), author.getEffectiveAvatarUrl());
        if(title.startsWith("success.")){
            embedBuilder.setColor(Color.GREEN).setTitle(UnicodeCharacters.WHITE_HEAVY_CHECK_MARK_EMOJI + " " + (formatArgs != null ? String.format(translateMessage(author, message, guild, title, SimpleBot.getLocalizations().get(SimpleBot.getServerConfig().language().getOrDefault(guild.getId(), "en")).getAsString()), formatArgs) :
                    translateMessage(author, message, guild, title, SimpleBot.getLocalizations().get(SimpleBot.getServerConfig().language().getOrDefault(guild.getId(), "en")).getAsString())));
        } else if(title.startsWith("error.")){
            embedBuilder.setColor(Color.RED).setTitle(UnicodeCharacters.CROSS_MARK_EMOJI + " " + (formatArgs != null ? String.format(translateMessage(author, message, guild, title, SimpleBot.getLocalizations().get(SimpleBot.getServerConfig().language().getOrDefault(guild.getId(), "en")).getAsString()), formatArgs) :
                    translateMessage(author, message, guild, title, SimpleBot.getLocalizations().get(SimpleBot.getServerConfig().language().getOrDefault(guild.getId(), "en")).getAsString())));
        } else if(title.startsWith("warning.")){
            embedBuilder.setColor(0xff7f00).setTitle(UnicodeCharacters.WARNING_SIGN_EMOJI + " " + (formatArgs != null ? String.format(translateMessage(author, message, guild, title, SimpleBot.getLocalizations().get(SimpleBot.getServerConfig().language().getOrDefault(guild.getId(), "en")).getAsString()), formatArgs) :
                    translateMessage(author, message, guild, title, SimpleBot.getLocalizations().get(SimpleBot.getServerConfig().language().getOrDefault(guild.getId(), "en")).getAsString())));
        }
        if(color != null) embedBuilder.setColor(color);
        if(description != null && description.length() <= 4096) embedBuilder.setDescription(description);
        if(thumbnail != null) embedBuilder.setThumbnail(thumbnail);
        return embedBuilder;
    }

    public static String getDescription(String string, int intDelimiter) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < string.toCharArray().length; i++) {
            if (i == intDelimiter - 3) {
                stringBuilder.append("...");
                break;
            }
            stringBuilder.append(string.toCharArray()[i]);
        }
        return stringBuilder.toString();
    }

    public static String formatShortDate(LocalDateTime date) {
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yy"));
    }

    public static boolean cantInteract(Member member, Member bot, Member target, CommandEvent event) {
        if (member.canInteract(target) && bot.canInteract(target)) return false;
        event.reply(new MessageBuilder(getEmbed(event, !member.canInteract(target) ? "error.commands.cantInteract.member" : "error.commands.cantInteract.bot", null, null, null).build()).build());
        return true;
    }

    /**
     * @param key the localization key
     * @param event for getting the guild's ID
     * @return the translated key in the configured language of the guild
     * @throws NullPointerException if the key does not exist in any localization files
     */
    public static String translateMessage(@NotNull CommandEvent event, @NotNull String key) {
        return translateMessage(event.getAuthor(), event.getMessage(), event.getGuild(), key, SimpleBot.getLocalizations().get(SimpleBot.getServerConfig().language().getOrDefault(event.getGuild().getId(), "en")).getAsString());
    }

    /**
     * @param key the localization key
     * @param event for getting the guild's ID
     * @param lang the language where the key will be taken
     * @return the translated key in the specified language
     * @throws NullPointerException if the key does not exist in any localization files
     */
    public static String translateMessage(@NotNull CommandEvent event, @NotNull String key, @NotNull String lang) {
        return translateMessage(event.getAuthor(), event.getMessage(), event.getGuild(), key, lang);
    }

    /**
     * @param author used by the {@link net.thesimpleteam.simplebot.utils.MessageHelper#getEmbed(User, Message, Guild, String, Color, String, String, Object...)} function
     * @param message used to send the embedBuilder
     * @param guild used to get the configured language and to send the embedBuilder to the guild's owner's private channel
     * @param key the localization key
     * @param lang the language where the key will be taken
     * @return the translated key in the specified language
     * @throws NullPointerException if the key does not exist in any localization files
     */
    public static String translateMessage(@NotNull User author, @Nullable Message message, @NotNull Guild guild, @NotNull String key, @NotNull String lang) {
        if (Optional.ofNullable(SimpleBot.getLocalizations().get(SimpleBot.getServerConfig().language().getOrDefault(guild.getId(), "en")).get(key)).isPresent()) return Optional.ofNullable(SimpleBot.getLocalizations().get(SimpleBot.getServerConfig().language().getOrDefault(guild.getId(), "en")).get(key)).get().getAsString();
        if (SimpleBot.getLocalizations().get("en").get(key) == null) {
            long skip = 2;
            if (StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(f -> f.skip(1).findFirst().orElseThrow()).getMethodName().equalsIgnoreCase("getHelpConsumer"))
                skip++;
            final long _skip = skip;
            EmbedBuilder embedBuilder = getEmbed(author, message, guild, "error.translateMessage.error", null, null, null, key)
                    .addField(translateMessage(author, message, guild, "error.translateMessage.key", SimpleBot.getLocalizations().get(SimpleBot.getServerConfig().language().getOrDefault(guild.getId(), "en")).getAsString()), key, false)
                    .addField(translateMessage(author, message, guild, "error.translateMessage.class", SimpleBot.getLocalizations().get(SimpleBot.getServerConfig().language().getOrDefault(guild.getId(), "en")).getAsString()), StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stackFrameStream -> stackFrameStream.skip(_skip).findFirst().orElseThrow()).getDeclaringClass().getSimpleName(), false)
                    .addField(translateMessage(author, message, guild, "error.translateMessage.method", SimpleBot.getLocalizations().get(SimpleBot.getServerConfig().language().getOrDefault(guild.getId(), "en")).getAsString()), StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stackFrameStream -> stackFrameStream.skip(_skip).findFirst().orElseThrow()).getMethodName(), false)
                    .addField(translateMessage(author, message, guild, "error.translateMessage.lineNumber", SimpleBot.getLocalizations().get(SimpleBot.getServerConfig().language().getOrDefault(guild.getId(), "en")).getAsString()), String.valueOf(StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(stackFrameStream -> stackFrameStream.skip(_skip).findFirst().orElseThrow()).getLineNumber()), false);
            if (message != null)
                message.reply(new MessageBuilder(embedBuilder.build()).build()).queue();
            if (guild.getOwner() != null)
                guild.getOwner().getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessage(new MessageBuilder(embedBuilder.build()).build()).queue());
            throw new NullPointerException("The key " + key + " does not exist!");
        }
        try {
            return SimpleBot.getLocalizations().get("en").get(key).getAsString();
        } catch (NullPointerException ex) {
            return key;
        }
    }
}