package fr.noalegeek.pepite_dor_bot.commands;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import fr.noalegeek.pepite_dor_bot.Main;
import fr.noalegeek.pepite_dor_bot.utils.helpers.MessageHelper;
import fr.noalegeek.pepite_dor_bot.utils.helpers.RequestHelper;
import net.dv8tion.jda.api.EmbedBuilder;
import okhttp3.Response;

import java.io.IOException;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Locale;

public class ConvertCommand extends Command {

    public ConvertCommand() {
        this.name = "convert";
        this.aliases = new String[]{"co"};
        this.arguments = "<type> <unité de mesure> <valeur> <convertion en unité de mesure>";
        this.help = "Convertis une unité de mesure en une autre.";
        this.example = "lenght meter 10 centimeter";
        this.category = CommandCategories.FUN.category;
    }

    //https://github.com/jrshutske/unit-conversion-api
    private final String BASE_URL = "https://java-unit-conversion.herokuapp.com/api/";

    private String capitalize(String string) {
        if (string == null || string.length() == 0) {
            return string;
        }
        StringBuilder builder = new StringBuilder();
        char[] c = string.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if(i == 0) {
                builder.append(String.valueOf(c[0]).toUpperCase());
            } else {
                builder.append(String.valueOf(c[i]).toLowerCase(Locale.ROOT));
            }
        }
        return builder.toString();
    }

    @Override
    protected void execute(CommandEvent event) {
        String syntaxError = "\nLe type de mesure doit être un de ces 4 types de mesure : \"volume\"," +
                "\"length\",\"weight\",\"temperature\".\nLes unités de mesure disponibles sont :\n**Pour le type \"volume\"** : \"gallon\",\"liter\"," +
                "\"quart\",\"pint\",\"cup\",\"milliliter\",\"fluidOnce\".\n**Pour le type \"length\"** : \"miles\",\"kilometers\",\"yards\"," +
                "\"meters\",\"feet\",\"inches\",\"centimeters\",\"millimeters\".\n**Pour le type \"weight\"** : \"stone\",\"pounds\"," +
                "\"kilograms\",\"milligrams\",\"grams\",\"ounces\".\n**Pour le type \"temperature\"** : \"fahrenheit\",\"celsius\",\"kelvin\"." +
                "\n:warning: Vous devez écrire ces arguments en anglais !";
        EmbedBuilder embedSyntaxError = new EmbedBuilder()
                .setFooter(event.getAuthor().getName()+"#"+event.getAuthor().getDiscriminator(),event.getAuthor().getAvatarUrl())
                .setTitle("Syntaxe de la commande "+Main.getInfos().prefix+name)
                .setColor(0x2f3136)
                .setTimestamp(OffsetDateTime.now(Clock.systemUTC()))
                .addField("Arguments",Main.getInfos().prefix+name+" "+arguments,false)
                .addField("Informations","Le type de mesure à spécifier doit être un de ces 4 types de mesure :\n- volume\n- length\n- weight\n- temperature\n\n" +
                        "chacun de ces types, il existe plusieurs unités de mesure qui sont listées en-dessous.",false)
                .addField("Volume","- gallon\n- liter\n- quart\n- pint\n- cup\n- milliliter\n- fluidOnce",false)
                .addField("Lenght","",false)
                .addField("Weight","",false)
                .addField("Temperature","",false);
        event.reply(embedSyntaxError.build());
        String[] args = event.getArgs().split(" \\s+");
        if(args.length < 3) {
            event.replyError(MessageHelper.syntaxError(event.getAuthor(), this)+syntaxError);
            return;
        }

        UnitType type;
        String unit = args[1];
        double value;
        String convertTo = args[3];

        try {
            type = UnitType.valueOf(args[0].toUpperCase());
        }catch (IllegalArgumentException e) {
            event.replyError(MessageHelper.syntaxError(event.getAuthor(), this)+syntaxError);
            return;
        }

        try{
            value = Double.parseDouble(args[2]);
        }catch (NumberFormatException ignored) {
            event.replyError(MessageHelper.syntaxError(event.getAuthor(), this)+syntaxError);
            return;
        }

        if(unit.isEmpty() || convertTo.isEmpty()) {
            event.replyError(MessageHelper.syntaxError(event.getAuthor(), this)+syntaxError);
            return;
        }

        try {
            Response response = RequestHelper.sendRequest(String.format("%s%s/%s/%f", BASE_URL, type.name().toLowerCase(), unit, value));
            if(!response.isSuccessful()) {
                event.replyError(MessageHelper.syntaxError(event.getAuthor(), this)+syntaxError);
                return;
            }
            JsonObject object = Main.gson.fromJson(RequestHelper.getResponseAsString(response), JsonObject.class);
            JsonElement valueConverted = object.get(convertTo.toLowerCase());
            if(valueConverted == null) {
                event.replyError(MessageHelper.syntaxError(event.getAuthor(), this)+syntaxError);
                return;
            }
            event.replySuccess("Convertion en cours...");
            event.getChannel().sendMessage(value + " " + capitalize(type.name()) + " = " + valueConverted.getAsDouble() + " " + capitalize(convertTo)).queue();
        } catch (IOException exception) {
            event.replyError(MessageHelper.syntaxError(event.getAuthor(), this)+syntaxError);
        }

    }

    private enum UnitType {
        VOLUME,
        LONGUEUR,
        POIDS,
        TEMPERATURE
    }
}
