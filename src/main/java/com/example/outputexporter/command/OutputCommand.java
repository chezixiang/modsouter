package com.example.outputexporter.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class OutputCommand {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("output")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("object")
                .executes(OutputCommand::exportObjects))
            .then(Commands.literal("music")
                .executes(OutputCommand::exportMusic))
            .then(Commands.literal("all")
                .executes(OutputCommand::exportAll))
        );
    }

    private static int exportObjects(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        try {
            Exporter exporter = new Exporter(source.getServer());
            int count = exporter.exportItems();
            sendSuccess(source, "export.items.success", count);
            return count;
        } catch (Exception e) {
            sendError(source, "export.items.failed", e.getMessage());
            return 0;
        }
    }

    private static int exportMusic(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        try {
            Exporter exporter = new Exporter(source.getServer());
            int count = exporter.exportSounds();
            sendSuccess(source, "export.sounds.success", count);
            return count;
        } catch (Exception e) {
            sendError(source, "export.sounds.failed", e.getMessage());
            return 0;
        }
    }

    private static int exportAll(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        try {
            Exporter exporter = new Exporter(source.getServer());
            int itemCount = exporter.exportItems();
            int soundCount = exporter.exportSounds();
            sendSuccess(source, "export.all.success", itemCount, soundCount);
            return itemCount + soundCount;
        } catch (Exception e) {
            sendError(source, "export.all.failed", e.getMessage());
            return 0;
        }
    }

    private static void sendSuccess(CommandSourceStack source, String key, Object... args) {
        source.sendSuccess(() -> Component.translatable(key, args), true);
    }

    private static void sendError(CommandSourceStack source, String key, Object... args) {
        source.sendFailure(Component.translatable(key, args));
    }
}
