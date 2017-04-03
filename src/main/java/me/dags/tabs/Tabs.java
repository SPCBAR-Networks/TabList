package me.dags.tabs;

import me.dags.textmu.MarkupSpec;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.tab.TabListEntry;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;

import java.util.concurrent.TimeUnit;

/**
 * @author dags <dags@dags.me>
 */
@Plugin(id = "tablist", name = "TabList", version = "0.1", description = "-_-")
public final class Tabs {

    private static final String FORMAT_OPTION = "tablist:format";
    private static final MarkupSpec spec = MarkupSpec.create();

    @Listener
    public void init(GameInitializationEvent event) {
        Task.builder().execute(() -> reload(null)).interval(30, TimeUnit.MINUTES).submit(this);
    }

    @Listener
    public void reload(GameReloadEvent event) {
        Sponge.getServer().getOnlinePlayers().forEach(Tabs::refreshTabName);
        Sponge.getServer().getOnlinePlayers().forEach(Tabs::syncTabs);
    }

    @Listener(order = Order.POST)
    public void onJoin(ClientConnectionEvent.Join event, @Root Player player) {
        Task.builder().execute(() -> {
            refreshTabName(player);
            syncTabs(player);
        }).submit(this);
    }

    private static void refreshTabName(Player player) {
        player.getTabList().getEntry(player.getUniqueId()).ifPresent(entry -> entry.setDisplayName(loadTabName(player)));
    }

    private static void syncTabs(Player player) {
        for (Player online : Sponge.getServer().getOnlinePlayers()) {
            syncTabNames(player, online);
        }
    }

    private static void syncTabNames(Player one, Player two) {
        one.getTabList().getEntry(two.getUniqueId()).ifPresent(entry -> entry.setDisplayName(getTabName(two)));
        two.getTabList().getEntry(one.getUniqueId()).ifPresent(entry -> entry.setDisplayName(getTabName(one)));
    }

    private static Text getTabName(Player player) {
        return player.getTabList().getEntry(player.getUniqueId())
                .flatMap(TabListEntry::getDisplayName)
                .orElse(loadTabName(player));
    }

    private static Text loadTabName(Player player) {
        return player.getOption(Tabs.FORMAT_OPTION)
                .map(format -> spec.template(format).with("name", player.getName()).render())
                .orElse(Text.of(player.getName()));
    }
}
