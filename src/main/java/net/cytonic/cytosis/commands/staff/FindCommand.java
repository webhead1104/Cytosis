package net.cytonic.cytosis.commands.staff;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.CommandUtils;
import net.cytonic.cytosis.commands.CytosisCommand;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import java.util.UUID;

/**
 * Locates a player on the network
 */
public class FindCommand extends CytosisCommand {

    /**
     * A command to find a player on the network
     */
    public FindCommand() {
        super("find");
        setCondition(CommandUtils.IS_STAFF);
        setDefaultExecutor((sender, cmdc) -> sender.sendMessage(Msg.mm("<RED>You must specify a player!")));
        var playerArgument = ArgumentType.Word("player");
        playerArgument.setSuggestionCallback((cmds, cmdc, suggestion) -> Cytosis.getCytonicNetwork().getOnlinePlayers().getValues().forEach(v -> suggestion.addEntry(new SuggestionEntry(v.toString()))));
        playerArgument.setCallback((sender, exception) -> sender.sendMessage(Component.text("The player " + exception.getInput() + " is invalid!", NamedTextColor.RED)));
        addSyntax((sender, context) -> {
            if (sender instanceof final CytosisPlayer player) {
                String playerName = context.get(playerArgument);
                UUID uuid = Cytosis.getCytonicNetwork().getLifetimeFlattened().getByValue(playerName.toLowerCase());
                if (!Cytosis.getCytonicNetwork().getOnlinePlayers().containsKey(uuid)) {
                    player.sendMessage(Component.text("The player " + playerName + " is not online!", NamedTextColor.RED));
                    return;
                }

                String server = Cytosis.getCytonicNetwork().getNetworkPlayersOnServers().get(uuid);

                Component message = Component.text("The player " + playerName + " is online on server " + server + " ", NamedTextColor.YELLOW)
                        .append(Component.text("[GO THERE]", NamedTextColor.GREEN, TextDecoration.BOLD)
                                .hoverEvent(HoverEvent.hoverEvent(HoverEvent.Action.SHOW_TEXT, Component.text("Click to travel to server: '" + server + "'"))))
                        .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/server " + server));
                player.sendMessage(message);
            }

        }, playerArgument);
    }
}
