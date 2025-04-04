package net.cytonic.cytosis.playerlist;

import lombok.NoArgsConstructor;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.enums.PlayerRank;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.CytosisPreferences;
import net.cytonic.cytosis.utils.DurationParser;
import net.cytonic.cytosis.utils.Msg;
import net.cytonic.cytosis.utils.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * A class providing the default player list for Cytosis
 */
@NoArgsConstructor
public class DefaultPlayerListCreator implements PlayerlistCreator {

    private final int colCount = 3;
    private final Instant start = Instant.now();

    /**
     * Creates the default player list
     *
     * @param player the player for personalization
     * @return the list of columns whose size is {@code colCount}
     */
    @Override
    public List<Column> createColumns(CytosisPlayer player) {
        List<Column> columns = new ArrayList<>();

        List<PlayerListEntry> players = new ArrayList<>();

        for (CytosisPlayer p : Cytosis.getOnlinePlayers()) {
            if (p.getPreference(CytosisPreferences.VANISHED)) {
                if (!player.isStaff()) continue;
                players.add(new PlayerListEntry(p.getRank().getPrefix().color(NamedTextColor.GRAY)
                        .decorate(TextDecoration.STRIKETHROUGH, TextDecoration.ITALIC).append(p.getName()), p.getRank().ordinal(),
                        new PlayerInfoUpdatePacket.Property("textures", p.getSkin().textures(), p.getSkin().signature())));
                continue;
            }
            PlayerRank rank = Cytosis.getRankManager().getPlayerRank(p.getUuid()).orElse(PlayerRank.DEFAULT);
            players.add(new PlayerListEntry(rank.getPrefix().append(p.getName()), rank.ordinal(),
                    new PlayerInfoUpdatePacket.Property("textures", p.getSkin().textures(), p.getSkin().signature())));
        }

        Column playerCol = new Column(Msg.mm("<dark_purple><b>        Players    "), PlayerListFavicon.PURPLE);
        if (players.size() >= 19) {
            int extra = players.size() - 19;
            players = new ArrayList<>(players.subList(0, 18));
            players.add(new PlayerListEntry(Msg.mm("<italic> + " + extra + " more"), 100));
        } else {
            playerCol.setEntries(new ArrayList<>(players));
            players.clear();
        }


        columns.add(playerCol);

        columns.add(new Column(Msg.mm("<dark_aqua><b>     Server Info"), PlayerListFavicon.BLUE,
                Utils.list(new PlayerListEntry(Msg.mm("<dark_aqua>Uptime: " + DurationParser.unparse(start, " ") + ""), 0),
                        new PlayerListEntry(Component.empty(), 1),
                        new PlayerListEntry(Msg.mm("<dark_aqua>Players: " + Cytosis.getOnlinePlayers().size() + ""), 2),
                        new PlayerListEntry(Msg.mm("<dark_aqua>Version: " + Cytosis.VERSION + ""), 3),
                        new PlayerListEntry(Msg.mm("<dark_aqua>ID: " + Cytosis.getRawID() + ""), 4),
                        new PlayerListEntry(Msg.mm("<darK_aqua>Network Players: " + Cytosis.getCytonicNetwork().getOnlinePlayers().size() + ""), 5)
                )));
        columns.add(new Column(Msg.mm("<yellow><b>     Player Info"), PlayerListFavicon.YELLOW, Utils.list(
                new PlayerListEntry(Msg.mm("<yellow>Rank: " + Cytosis.getRankManager().getPlayerRank(player.getUuid()).orElse(PlayerRank.DEFAULT).name() + ""), 0),
                new PlayerListEntry(Msg.mm("<yellow>Ping: " + player.getLatency() + "ms"), 1),
                new PlayerListEntry(Msg.mm("<yellow>Locale: " + player.getLocale() + ""), 2)
        )));

        return columns;
    }

    /**
     * Creates the header for the player
     *
     * @param player the player for personalization
     * @return the component to be displayed as the header
     */
    @Override
    public Component header(CytosisPlayer player) {
        return Msg.mm("<aqua><b>CytonicMC");
    }

    /**
     * Creates the footer for the player
     *
     * @param player the player for personalization
     * @return the component to be displayed as the footer
     */
    @Override
    public Component footer(CytosisPlayer player) {
        return Msg.mm("<aqua>mc.cytonic.net").appendNewline().append(Msg.mm("<gold>forums.cytonic.net"));
    }

    /**
     * Gets the column count
     *
     * @return the number of columns, between 1 and 4 inclusive
     */
    @Override
    public int getColumnCount() {
        return colCount;
    }
}
