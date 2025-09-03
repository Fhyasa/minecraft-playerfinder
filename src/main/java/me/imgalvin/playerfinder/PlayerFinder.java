package me.imgalvin.playerfinder;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

// Fabric Permissions API
import me.lucko.fabric.api.permissions.v0.Permissions;

public class PlayerFinder implements ModInitializer {
    PlayerFinderUtils utils = new PlayerFinderUtils();

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                CommandManager.literal("findplayer")
                    // Root predicate:
                    // - allow if user has `playerfinder.find` (fallback: OP 2)
                    // - OR allow if user has `playerfinder.find.self` (fallback: OP 0)
                    .requires((ServerCommandSource src) ->
                        Permissions.check(src, "playerfinder.find", 2)
                        || Permissions.check(src, "playerfinder.find.self", 0)
                    )
                    .then(CommandManager.argument("player", StringArgumentType.string())
                        .suggests(new PlayerSuggestionProvider())
                        .executes(context -> {
                            String playerName = StringArgumentType.getString(context, "player");
                            ServerCommandSource source = context.getSource();

                            PlayerEntity sourcePlayer = source.getPlayer();
                            if (sourcePlayer == null) {
                                source.sendError(Text.literal("This command can only be used by a player."));
                                return 0;
                            }

                            PlayerEntity targetPlayer = source.getServer().getPlayerManager().getPlayer(playerName);
                            if (targetPlayer == null) {
                                source.sendError(Text.literal("Player '" + playerName + "' is not online."));
                                return 0;
                            }

                            boolean findingOthers = !sourcePlayer.getGameProfile().getName().equalsIgnoreCase(playerName);

                            // If finding others, require the extra node (fallback: OP 2).
                            if (findingOthers && !Permissions.check(source, "playerfinder.find.others", 2)) {
                                source.sendError(Text.literal("You don't have permission to locate other players."));
                                return 0;
                            }

                            BlockPos targetBlockPos = targetPlayer.getBlockPos();
                            BlockPos sourceBlockPos = sourcePlayer.getBlockPos();
                            RegistryKey<World> playerDimension = targetPlayer.getWorld().getRegistryKey();
                            RegistryKey<World> sourceDimension = sourcePlayer.getWorld().getRegistryKey();

                            boolean isSameDimension = sourceDimension == playerDimension;

                            source.sendFeedback(
                                () -> Text.literal(playerName + " is at ")
                                    .append(Text.literal(targetBlockPos.getX() + ", " + targetBlockPos.getY() + ", " + targetBlockPos.getZ())
                                        .formatted(utils.getDimensionColor(playerDimension)))
                                    .append(Text.literal(" in the ").formatted(Formatting.WHITE))
                                    .append(Text.literal(utils.getDimensionText(playerDimension))
                                        .formatted(utils.getDimensionColor(playerDimension)))
                                    .append(Text.literal(isSameDimension
                                            ? " (" + utils.getDistance(sourceBlockPos, targetBlockPos) + " blocks away)"
                                            : " (Player is in a different dimension)")
                                        .formatted(isSameDimension ? Formatting.GREEN : Formatting.RED)),
                                false
                            );

                            return 1;
                        })
                    )
            );
        });
    }
}