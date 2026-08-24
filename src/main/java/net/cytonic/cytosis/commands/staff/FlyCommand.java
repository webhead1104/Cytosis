package net.cytonic.cytosis.commands.staff;

import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.number.ArgumentFloat;

import net.cytonic.cytosis.commands.utils.CommandUtils;
import net.cytonic.cytosis.commands.utils.CytosisCommand;
import net.cytonic.cytosis.data.enums.PlayerRank;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.Preferences;

/**
 * The class representing the fly command
 */
public class FlyCommand extends CytosisCommand {

    /**
     * Creates a new command and sets up the consumers and execution logic
     */
    public FlyCommand() {
        super("fly");
        setCondition(CommandUtils.withRankOrStaff(PlayerRank.CORTEX, PlayerRank.SYNAPSE, PlayerRank.NEXUS));

        ArgumentFloat speedArg = ArgumentType.Float("speed");
        // arg is an unscaled multiplier applied as (arg * 0.05F); floor must stay > 0 so flight can't freeze
        speedArg.between(0.1F, 10.0F);
        addSyntax((sender, ctx) -> {
            if (!(sender instanceof final CytosisPlayer player)) return;
            float speed = ctx.get(speedArg);
            player.setFlyingSpeed(0.05F * speed);
            player.updatePreference(Preferences.FLY_SPEED, speed);
            if (player.getPreference(Preferences.FLY)) {
                player.success("Flight speed set to %.2fx speed.", speed);
                return;
            }
            player.setAllowFlying(true);
            player.setFlying(true);
            player.updatePreference(Preferences.FLY, true);
            player.success("Flight enabled at %.2fx speed.", speed);
        }, speedArg);

        addSyntax((sender, _) -> {
            if (!(sender instanceof final CytosisPlayer player)) return;
            player.togglePreference(Preferences.FLY, () -> {
                player.setAllowFlying(true);
                player.setFlying(true);
                float speed = player.getPreference(Preferences.FLY_SPEED);
                player.setFlyingSpeed(0.05F * speed);
                player.sendMessage(Msg.green("Flight enabled at %.2fx speed.", speed));
            }, () -> {
                player.setAllowFlying(false);
                player.setFlying(false);
                player.sendMessage(Msg.red("Flight disabled."));
            });
        });
    }
}
