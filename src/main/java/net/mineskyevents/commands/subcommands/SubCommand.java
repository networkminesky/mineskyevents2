package net.mineskyevents.commands.subcommands;

import org.bukkit.command.CommandSender;

import java.util.List;

public abstract class SubCommand {
    public abstract String getName();
    public abstract String getDescription();
    public abstract String getUsage();
    public abstract List<String> getAliases();
    public abstract String hasPermission();
    public abstract void perform(CommandSender sender, String[] args);

    /** Subcomandos que exigem jogador no mundo (entrar, mapa, GUI, etc.). */
    public boolean allowsConsole() {
        return false;
    }

    public boolean matches(String input) {
        if (input.equalsIgnoreCase(getName())) return true;
        for (String alias : getAliases()) {
            if (input.equalsIgnoreCase(alias)) return true;
        }
        return false;
    }
}
