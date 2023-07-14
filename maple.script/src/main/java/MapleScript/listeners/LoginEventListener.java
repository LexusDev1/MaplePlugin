package MapleScript.listeners;

import Database.Database;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LoginEventListener implements Listener {

    private final Database database;
    private final Map<UUID, LoginState> loginStates;

    public LoginEventListener(Database database) {
        this.database = database;
        this.loginStates = new HashMap<>();
        database.createCollection("users");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (!isPlayerRegistered(playerId)) {
            sendRegistrationMessage(player);
            createLoginState(playerId, LoginState.Registration);
        } else {
            sendLoginMessage(player);
            createLoginState(playerId, LoginState.Login);
        }
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        String message = event.getMessage();

        if (loginStates.containsKey(playerId)) {
            event.setCancelled(true);

            LoginState loginState = loginStates.get(playerId);

            switch (loginState) {
                case Registration:
                    registerPlayer(player, message);
                    break;
                case Login:
                    loginPlayer(player, message);
                    break;
            }

            loginStates.remove(playerId);
        }
    }

    private boolean isPlayerRegistered(UUID playerId) {
        return database.getValue("users", playerId.toString()) != null;
    }

    private void sendRegistrationMessage(Player player) {
        player.sendMessage(ChatColor.YELLOW + "Welcome to the server!");
        player.sendMessage(ChatColor.YELLOW + "To register, please type your desired password in the chat.");
    }

    private void sendLoginMessage(Player player) {
        player.sendMessage(ChatColor.YELLOW + "Welcome back! Please log in by typing your password in the chat.");
    }

    private void registerPlayer(Player player, String password) {
        UUID playerId = player.getUniqueId();
        if (!isPlayerRegistered(playerId)) {
            database.setValue("users", playerId.toString(), password);
            player.sendMessage(ChatColor.GREEN + "Registration successful! You are now registered.");
            player.sendMessage(ChatColor.GREEN + "Please log in with your password to continue.");
        } else {
            player.sendMessage(ChatColor.RED + "You are already registered.");
        }
    }

    private void loginPlayer(Player player, String password) {
        UUID playerId = player.getUniqueId();
        String storedPassword = database.getValue("users", playerId.toString());

        if (storedPassword != null && storedPassword.equals(password)) {
            player.sendMessage(ChatColor.GREEN + "Login successful! You are now logged in.");
        } else {
            player.sendMessage(ChatColor.RED + "Incorrect password. Please try again.");
        }
    }

    private void createLoginState(UUID playerId, LoginState state) {
        loginStates.put(playerId, state);
    }

    private enum LoginState {
        Registration,
        Login
    }
}
