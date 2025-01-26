package net.cytonic.cytosis.commands.chatting;

import net.cytonic.cytosis.data.containers.IgnoredChatChannelContainer;
import net.cytonic.cytosis.data.enums.ChatChannel;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.CytosisPreferences;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;

import static net.cytonic.cytosis.utils.MiniMessageTemplate.MM;

public class IgnoreChatChannelCommand extends Command {

    public IgnoreChatChannelCommand() {
        super("ignorechatchannel");
        var chatChannelArgument = ArgumentType.Word("channel").from("mod", "admin", "staff", "all", "m", "ad", "s", "a");
        chatChannelArgument.setCallback((sender, exception) -> sender.sendMessage(STR."The channel \{exception.getInput()} is invalid!"));
        chatChannelArgument.setSuggestionCallback((sender, _, suggestion) -> {
            if (!(sender instanceof CytosisPlayer player)) {
                return;
            }
            if (player.isModerator()) {
                suggestion.addEntry(new SuggestionEntry("mod"));
                suggestion.addEntry(new SuggestionEntry("m"));
            }
            if (player.isAdmin()) {
                suggestion.addEntry(new SuggestionEntry("admin"));
                suggestion.addEntry(new SuggestionEntry("ad"));
            }
            if (player.isStaff()) {
                suggestion.addEntry(new SuggestionEntry("staff"));
                suggestion.addEntry(new SuggestionEntry("s"));
            }
            suggestion.addEntry(new SuggestionEntry("all"));
            suggestion.addEntry(new SuggestionEntry("a"));
        });

        addSyntax(((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) {
                return;
            }
            ChatChannel channel = switch (context.get(chatChannelArgument).toLowerCase()) {
                case "all", "a" -> ChatChannel.ALL;
                case "admin", "ad" -> ChatChannel.ADMIN;
                case "mod", "m" -> ChatChannel.MOD;
                case "staff", "s" -> ChatChannel.STAFF;
                default ->
                        throw new IllegalStateException(STR."Unexpected value: \{context.get(chatChannelArgument).toLowerCase()}");
            };

            if (!player.canUseChannel(channel)) {
                player.sendMessage(MM."<red><b>WHOOPS!</b></red> <gray>You cannot ignore the \{channel.name().toLowerCase()} because you don't have access to it!");
                return;
            }

            IgnoredChatChannelContainer container = player.getPreference(CytosisPreferences.IGNORED_CHAT_CHANNELS);
            container = container.withForChannel(channel, !container.getForChannel(channel));
            player.updatePreference(CytosisPreferences.IGNORED_CHAT_CHANNELS, container);

            if (!container.getForChannel(channel)) {
                player.sendMessage(MM."<gray><b>UNIGNORED!</b> You are no longer ignoring the <gold>\{channel.name()}</gold> chat.");
            } else {
                player.sendMessage(MM."<gray><b>IGNORED!</b> You successfully muted <gold>\{channel.name()}</gold> chat.");
            }
        }), chatChannelArgument);
    }
}