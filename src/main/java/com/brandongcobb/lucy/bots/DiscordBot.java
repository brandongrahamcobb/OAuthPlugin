/*  DiscordBot.java The purpose of this class is to manage the
 *  JDA discord api.
 *
 *  Copyright (C) 2025  github.com/brandongrahamcobb
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.brandongcobb.lucy.bots;

import com.brandongcobb.lucy.cogs.*;
import com.brandongcobb.lucy.utils.handlers.*;
import com.brandongcobb.lucy.Lucy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import java.util.logging.Level;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class DiscordBot {

    private JDA api;
    private final Logger logger = Logger.getLogger("Lucy");;
    private final ReentrantLock lock = new ReentrantLock();

    public DiscordBot() {
        ConfigManager cm = ConfigManager.getInstance();
        CompletableFuture<Void> future = cm.completeGetConfigValue("DISCORD_API_KEY", String.class)
            .thenCombine(cm.completeGetConfigValue("DISCORD_OWNER_ID", Long.class), (apiKey, ownerId) -> {
                try {
                    if (apiKey == null || ownerId == null) {
                        throw new IllegalArgumentException("API Key or Owner ID is null");
                    }
                    this.api = JDABuilder.createDefault((String) apiKey,
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.MESSAGE_CONTENT,
                            GatewayIntent.GUILD_MEMBERS)
                        .setActivity(Activity.playing("I take pharmacology personally."))
                        .build();
                    List<Cog> cogs = new ArrayList<>();
                    cogs.add(new EventListeners());
                    for (Cog cog : cogs) {
                        cog.register(this.api, this);
                    }
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Error during DiscordBot setup", e);
                    throw new CompletionException(e);
                }
                return null;
            })
            .whenComplete((result, throwable) -> {
                if (throwable != null) {
                    logger.log(Level.SEVERE, "Error starting Discord bot", throwable);
                } else {
                    logger.info("Discord bot successfully initialized.");
                }
            });
        future.join();
    }
}
