/*  MinecraftUser.java Likely broken. 07 05 25
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
package com.brandongcobb.oauthplugin.utils.handlers;

import com.brandongcobb.oauthplugin.OAuthPlugin;
import com.brandongcobb.oauthplugin.utils.handlers.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.function.Consumer;
import java.util.concurrent.CompletableFuture;
import java.util.HashMap;
import java.util.Map;

public class MinecraftUser implements User {

    private OAuthPlugin app;
    private ConfigManager cm;
    private Database db;
    private String minecraftId;
    public Map<MinecraftUser, OAuthUserSession> sessions;

    public MinecraftUser(String minecraftId) {
        this.db = Database.completeGetInstance();
        this.minecraftId = minecraftId;
    }

    @Override
    public CompletableFuture<Void> createUser(Timestamp timestamp, long discordId, String minecraftId, String patreonAbout, int patreonAmountCents, String patreonEmail, long patreonId, String patreonName, String patreonStatus, String patreonTier, String patreonVanity) {
        UserManager um = new UserManager(db);
        return um.createUser(timestamp, discordId, minecraftId, patreonAbout, patreonAmountCents, patreonEmail, patreonId, patreonName, patreonStatus, patreonTier, patreonVanity);
    }

    public MinecraftUser getCurrentUser() {
        return this;
    }
    /**
     * Get this user's Minecraft UUID string.
     */
    public String getMinecraftId() {
        return minecraftId;
    }

    public void userExists(String minecraftId, Consumer<Boolean> callback) {
        db.completeGetConnection(connection -> {
            try {
                boolean exists = false; // Default to false
                if (connection != null) {
                    try (PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(*) FROM users WHERE minecraft_id = ?")) {
                        stmt.setString(1, minecraftId);
                        ResultSet rs = stmt.executeQuery();
                        if (rs.next()) {
                            exists = rs.getInt(1) > 0; // Set true if count is greater than 0
                        }
                    } catch (Exception e) {
                        e.printStackTrace(); // Handle exceptions
                    }
                }
                callback.accept(exists);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    connection.close(); // Close the connection after use
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
