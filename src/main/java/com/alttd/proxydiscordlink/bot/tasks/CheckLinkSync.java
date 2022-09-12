package com.alttd.proxydiscordlink.bot.tasks;

import com.alttd.proxydiscordlink.DiscordLink;
import com.alttd.proxydiscordlink.bot.Bot;
import com.alttd.proxydiscordlink.config.BotConfig;
import com.alttd.proxydiscordlink.util.ALogger;
import net.dv8tion.jda.api.entities.Member;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CheckLinkSync implements Runnable {

    private final DiscordLink plugin;
    private final Bot bot;

    public CheckLinkSync() {
        plugin = DiscordLink.getPlugin();
        bot = plugin.getBot();
    }

    @Override
    public void run() {
        List<Member> members = bot.getMembersWithRole(BotConfig.GUILD_ID, BotConfig.LINKED_ROLE_ID);
        HashSet<Long> dbIdSet = plugin.getDatabase().getLinkedUsers();
        HashSet<Long> membersIdSet = members.stream().map(Member::getIdLong).collect(Collectors.toCollection(HashSet::new));

        //give these people the link role in discord, if they are not in the discord unlink them in game
        HashSet<Long> noRoleIds = dbIdSet.stream().filter(id -> !membersIdSet.contains(id)).collect(Collectors.toCollection(HashSet::new));

        //remove the linked role from these people
        HashSet<Long> notInDbIds = membersIdSet.stream().filter(id -> !dbIdSet.contains(id)).collect(Collectors.toCollection(HashSet::new));

        fixNotInDb(members, notInDbIds);
        fixNoLinkRole(members, noRoleIds);
    }

    private void fixNotInDb(List<Member> members, Set<Long> notInDbIds) {
        for (Long id : notInDbIds) {
            members.stream()
                    .filter(member -> member.getIdLong() == id)
                    .findAny()
                    .ifPresent(member -> {
                        ALogger.info("Removing linked role from user with discord id: [" + id + "] due to them not being in the database.");
//                        bot.removeRole(id, BotConfig.LINKED_ROLE_ID, member.getGuild().getIdLong());
                    });
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void fixNoLinkRole(List<Member> members, Set<Long> noRoleIds) {
        for (Long id : noRoleIds) {
            members.stream()
                    .filter(member -> member.getIdLong() == id)
                    .findAny()
                    .ifPresentOrElse(
                            member -> {
                                ALogger.info("Adding role to user with discord id: [" + id + "] due to them being in the database without having a role in discord.");
//                                bot.addRole(id, BotConfig.LINKED_ROLE_ID, member.getGuild().getIdLong());
                            }, () -> {
                                ALogger.info("Removing user with discord id: [" + id + "] from the database due to them not being in the discord.");
//                                plugin.getDatabase().removeLinkedAccount(id);
                    });
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
