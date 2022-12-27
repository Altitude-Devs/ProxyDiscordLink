package com.alttd.proxydiscordlink;

import com.alttd.proxydiscordlink.bot.commandManager.CommandManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class JDAListener extends ListenerAdapter {

    private final JDA jda;

    public JDAListener(JDA jda) {
        this.jda = jda;
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        CommandManager commandManager = new CommandManager(jda);
        jda.addEventListener(commandManager);
    }

}
