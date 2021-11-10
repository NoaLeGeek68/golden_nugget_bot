package fr.noalegeek.pepite_dor_bot.commands;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import fr.noalegeek.pepite_dor_bot.enums.CommandCategories;
import fr.noalegeek.pepite_dor_bot.utils.MessageHelper;
import fr.noalegeek.pepite_dor_bot.utils.UnicodeCharacters;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import org.mariuszgromada.math.mxparser.Expression;
import org.mariuszgromada.math.mxparser.mXparser;

import java.awt.*;
import java.time.Instant;
import java.util.List;

public class MathsCommand extends Command {

    public MathsCommand(){
        this.category = CommandCategories.FUN.category;
        this.aliases = new String[]{"math","m"};
        this.name = "maths";
        this.arguments = "<nombre entier>";
        this.help = "help.maths";
        this.cooldown = 5;
        this.example = "14";
    }

    @Override
    protected void execute(CommandEvent event) {
        String[] args = event.getArgs().split("\\s+");
        if(args.length != 1 && args.length != 2 && args.length != 3){
            MessageHelper.syntaxError(event, this, null);
            return;
        }
        switch (args.length){
            case 1 -> { //Calculate the math expression
                mXparser.disableAlmostIntRounding();
                mXparser.disableCanonicalRounding();
                mXparser.disableUlpRounding();
                for(char c : args[0].toCharArray()){
                    if (List.of(UnicodeCharacters.getAllExponentsCharacters()).contains(c)){
                        EmbedBuilder errorExponentsCharactersEmbed = new EmbedBuilder()
                                .setColor(Color.RED)
                                .setFooter(MessageHelper.getTag(event.getAuthor()), event.getAuthor().getEffectiveAvatarUrl())
                                .setTimestamp(Instant.now())
                                .setTitle(UnicodeCharacters.crossMarkEmoji + " " + MessageHelper.translateMessage("error.maths.calculate.exponentsCharacters", event));
                        event.reply(new MessageBuilder(errorExponentsCharactersEmbed.build()).build());
                    }
                }
                if (!new Expression(calculateReplaceArgs(args[0].replaceAll("\\s+", ""))).checkSyntax()) {
                    EmbedBuilder errorSyntaxEmbed = new EmbedBuilder()
                            .setColor(Color.RED)
                            .setFooter(MessageHelper.getTag(event.getAuthor()), event.getAuthor().getEffectiveAvatarUrl())
                            .setTimestamp(Instant.now())
                            .setTitle(UnicodeCharacters.crossMarkEmoji + " " + String.format(MessageHelper.translateMessage("error.maths.calculate.syntax", event), calculateReplaceArgs(args[0].replaceAll("\\s+", ""))));
                    event.reply(new MessageBuilder(errorSyntaxEmbed.build()).build());
                    return;
                }
                EmbedBuilder successEmbed = new EmbedBuilder()
                        .setColor(Color.GREEN)
                        .setFooter(MessageHelper.getTag(event.getAuthor()), event.getAuthor().getEffectiveAvatarUrl())
                        .setTimestamp(Instant.now())
                        .setTitle(UnicodeCharacters.whiteHeavyCheckMarkEmoji + " " + MessageHelper.translateMessage("success.maths.calculate.success", event))
                        .addField(MessageHelper.translateMessage("success.maths.calculate.mathematicalExpression", event), calculateReplaceArgs(args[0].replaceAll("\\s+", "")), false)
                        .addField(MessageHelper.translateMessage("success.maths.calculate.result", event), String.valueOf(new Expression(calculateReplaceArgs(args[0].replaceAll("\\s+", ""))).calculate()), false);
                event.reply(new MessageBuilder(successEmbed.build()).build());
            }
            case 2 -> {

            }
            case 3 -> {
                switch (args[0]){
                    case "primenumber", "primeNumber" -> { //Verify if the number is a prime number
                        if(notAnNumber(event, args[2])) return;
                        switch (args[1]){
                            case "number" -> {
                                EmbedBuilder successCalculateNumberEmbed = new EmbedBuilder()
                                        .setTimestamp(Instant.now())
                                        .setColor(numberIsPrime(Long.parseLong(args[2])) ? Color.GREEN : Color.RED)
                                        .setFooter(MessageHelper.getTag(event.getAuthor()), event.getAuthor().getEffectiveAvatarUrl())
                                        .setTitle(String.format("%s %s", numberIsPrime(Long.parseLong(args[2])) ? UnicodeCharacters.whiteHeavyCheckMarkEmoji : UnicodeCharacters.crossMarkEmoji, numberIsPrime(Long.parseLong(args[2])) ? String.format(MessageHelper.translateMessage("success.maths.calculate.isPrime", event), args[2]) : String.format(MessageHelper.translateMessage("success.maths.calculate.isNotPrime", event), args[2])));
                                event.reply(new MessageBuilder(successCalculateNumberEmbed.build()).build());
                            }
                            case "list" -> {

                            }
                        }
                    }
                }
            }
        }
    }
    public static String calculateReplaceArgs(String calculation) {
        StringBuilder builder = new StringBuilder();
        for (char c : calculation.toCharArray()) {
            switch (c) {
                case '₋' -> builder.append('-');
                case '₊' -> builder.append('+');
                case '÷' -> builder.append('/');
                case ',' -> builder.append('.');
                case 'x', '×' -> builder.append('*');
                default -> builder.append(c);
            }
        }
        return builder.toString();
    }

    public static boolean notAnNumber(CommandEvent event, String string){
        if(string.chars().allMatch(Character::isDigit)) return false;
        EmbedBuilder notAnNumberEmbed = new EmbedBuilder()
                .setColor(Color.RED)
                .setFooter(MessageHelper.getTag(event.getAuthor()), event.getAuthor().getEffectiveAvatarUrl())
                .setTimestamp(Instant.now())
                .setTitle(UnicodeCharacters.crossMarkEmoji + " " + MessageHelper.translateMessage("error.commands.notAnNumber", event));
        event.reply(new MessageBuilder(notAnNumberEmbed.build()).build());
        return true;
    }

    public static boolean notAn(CommandEvent event, String string){
        if(string.chars().allMatch(Character::isDigit)) return false;
        EmbedBuilder notAnNumberEmbed = new EmbedBuilder()
                .setColor(Color.RED)
                .setFooter(MessageHelper.getTag(event.getAuthor()), event.getAuthor().getEffectiveAvatarUrl())
                .setTimestamp(Instant.now())
                .setTitle(UnicodeCharacters.crossMarkEmoji + " " + MessageHelper.translateMessage("error.commands.notAnNumber", event));
        event.reply(new MessageBuilder(notAnNumberEmbed.build()).build());
        return true;
    }

    public static boolean numberIsPrime(long number){
        if(number <= 1) return false;
        for(long i = 2; i <= Math.sqrt(number); i++){
            if(number % i == 0) return false;
        }
        return true;
    }
}
