package net.cytonic.cytosis.commands.moderation;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.config.CytosisSnoops;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.SnoopUtils;
import net.kyori.adventure.text.Component;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import java.util.UUID;

public class UnbanCommand extends CytosisCommand {

    public UnbanCommand() {
        super("unban");
        setCondition(CommandUtils.IS_MODERATOR);
        setDefaultExecutor((sender, ignored) -> sender.sendMessage(Msg.mm("<red>Usage: /unban (player)")));
        var playerArg = ArgumentType.Word("target");
        playerArg.setSuggestionCallback((sender, ignored, suggestion) -> {
            if (sender instanceof CytosisPlayer player) {
                player.sendActionBar(Msg.mm("<green>Fetching banned players..."));
                Cytosis.getCytonicNetwork().getBannedPlayers().forEach((uuid, ignored1) -> suggestion.addEntry(new SuggestionEntry(Cytosis.getCytonicNetwork().getLifetimePlayers().getByKey(uuid))));
            }
        });
        addSyntax((sender, context) -> {
            if (!(sender instanceof CytosisPlayer actor)) {
                return;
            }

            final String player = context.get(playerArg);
            if (!Cytosis.getCytonicNetwork().getLifetimePlayers().containsValue(player)) {
                sender.sendMessage(Msg.whoops("The player %s doesn't exist!", player));
                return;
            }
            UUID uuid = Cytosis.getCytonicNetwork().getLifetimePlayers().getByValue(player);
            if (!Cytosis.getCytonicNetwork().getBannedPlayers().containsKey(uuid)) {
                sender.sendMessage(Msg.whoops("%s is not banned!", player));
                return;
            }


            Component snoop = actor.formattedName().append(Msg.mm("<gray> unbanned ")).append(SnoopUtils.toTarget(uuid)).append(Msg.mm("<gray>."));

            Cytosis.getSnooperManager().sendSnoop(CytosisSnoops.PLAYER_UNBAN, Msg.snoop(snoop));

            Cytosis.getDatabaseManager().getMysqlDatabase().unbanPlayer(uuid);
            sender.sendMessage(Msg.greenSplash("UNBANNED!", "%s was successfully unbanned!", player));
        }, playerArg);
    }
}
