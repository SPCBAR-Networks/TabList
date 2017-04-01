package me.dags.tabs;

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
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyle;
import org.spongepowered.api.text.format.TextStyles;

import java.util.concurrent.TimeUnit;

/**
 * @author dags <dags@dags.me>
 */
@Plugin(id = "tablist", name = "TabList", version = "0.1", description = "-_-")
public class Tabs {

    @Listener
    public void init(GameInitializationEvent event) {
        Task.builder().execute(() -> reload(null)).interval(30, TimeUnit.MINUTES).submit(this);
    }

    @Listener
    public void reload(GameReloadEvent event) {
        Sponge.getServer().getOnlinePlayers().forEach(Tabs::refreshTabName);
        Sponge.getServer().getOnlinePlayers().forEach(Tabs::syncOnlineTabs);
    }

    @Listener(order = Order.POST)
    public void onJoin(ClientConnectionEvent.Join event, @Root Player player) {
        Task.builder().execute(() -> {
            refreshTabName(player);
            syncOnlineTabs(player);
        }).submit(this);
    }

    private static void refreshTabName(Player player) {
        player.getTabList().getEntry(player.getUniqueId()).ifPresent(entry -> {
            Text name = tabName(player);
            entry.setDisplayName(name);
            player.sendMessage(name);
        });
    }

    private static void syncOnlineTabs(Player player) {
        for (Player online : Sponge.getServer().getOnlinePlayers()) {
            syncTabs(player, online);
        }
    }

    private static void syncTabs(Player one, Player two) {
        Text oneName = one.getTabList().getEntry(one.getUniqueId()).flatMap(TabListEntry::getDisplayName).orElse(tabName(one));
        Text twoName = two.getTabList().getEntry(two.getUniqueId()).flatMap(TabListEntry::getDisplayName).orElse(tabName(two));
        one.getTabList().getEntry(two.getUniqueId()).ifPresent(entry -> entry.setDisplayName(twoName));
        two.getTabList().getEntry(one.getUniqueId()).ifPresent(entry -> entry.setDisplayName(oneName));
    }

    private static Text tabName(Player player) {
        Text.Builder builder = Text.builder(player.getName());

        Sponge.getRegistry().getAllOf(TextColor.class)
                .stream()
                .filter(color -> color != TextColors.NONE && color != TextColors.RESET)
                .filter(color -> player.hasPermission("tablist.color." + color.getName().toLowerCase()))
                .findFirst()
                .ifPresent(builder::color);

        Sponge.getRegistry().getAllOf(TextStyle.Base.class)
                .stream()
                .filter(style -> style != TextStyles.NONE && style != TextStyles.RESET)
                .filter(style -> player.hasPermission("tablist.style." + style.getName().toLowerCase()))
                .map(builder.getStyle()::and)
                .forEach(builder::style);

        return builder.build();
    }
}
