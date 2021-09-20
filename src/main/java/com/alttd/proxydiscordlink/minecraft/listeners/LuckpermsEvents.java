package com.alttd.proxydiscordlink.minecraft.listeners;

import com.alttd.proxydiscordlink.DiscordLink;
import com.alttd.proxydiscordlink.bot.objects.DiscordRole;
import com.alttd.proxydiscordlink.objects.DiscordLinkPlayer;
import com.alttd.proxydiscordlink.util.Utilities;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.node.NodeAddEvent;
import net.luckperms.api.event.node.NodeRemoveEvent;
import net.luckperms.api.model.PermissionHolder;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;

import java.util.List;
import java.util.Optional;

public class LuckpermsEvents {

    public void listener()
    {
        EventBus eventBus = Utilities.getLuckPerms().getEventBus();

        eventBus.subscribe(DiscordLink.getPlugin(), NodeAddEvent.class, event -> updateRank(event.getTarget(), event.getNode(), true));
        eventBus.subscribe(DiscordLink.getPlugin(), NodeRemoveEvent.class, event -> updateRank(event.getTarget(), event.getNode(), false));
    }

    public void updateRank(PermissionHolder permissionHolder, Node node, boolean added)
    {
        if (!(node instanceof InheritanceNode inheritanceNode) || !(permissionHolder instanceof User user))
            return;
        Optional<DiscordRole> optional = DiscordRole.getDiscordRoles().stream()
                .filter(discordRole -> inheritanceNode
                        .getGroupName()
                        .equalsIgnoreCase(discordRole.getLuckpermsName()))
                .findFirst();

        if (optional.isEmpty())
            return;
        DiscordLinkPlayer discordLinkPlayer = DiscordLinkPlayer.getDiscordLinkPlayer(user.getUniqueId());
        if (discordLinkPlayer == null)
            return;

        discordLinkPlayer.updateDiscord(List.of(optional.get()), added);
    }

}
