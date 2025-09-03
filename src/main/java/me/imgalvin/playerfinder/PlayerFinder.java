package me.imgalvin.playerfinder;

import com.mojang.brigadier.arguments.StringArgumentType;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PlayerFinder implements ModInitializer {
    private final PlayerFinderUtils utils = new PlayerFinderUtils();

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                CommandManager.literal("findplayer")
                    // Global gate: OP level 2 by default (or via permissions provider)
                    .requires(Permissions.require("playerfinder.find", 2))
                    .then(CommandManager.argument("player", StringArgumentType.string())
                        .suggests(new PlayerSuggestionProvider())
                        .executes(context -> {
                            String playerName = StringArgumentType.getString(context, "player");
                            ServerCommandSource source = context.getSource();

                            ServerPlayerEntity sourcePlayer = source.getPlayer();
                            if (sourcePlayer == null) {
                                source.sendError(Text.literal("This command can only be used by a player."));
                                return 0;
                            }

                            ServerPlayerEntity targetPlayer = source.getServer().getPlayerManager().getPlayer(playerName);
                            if (targetPlayer == null) {
                                source.sendError(Text.literal("Player '" + playerName + "' is not online."));
                                return 0;
                            }

                            // Per-target permission: playerfinder.find.<playername>
                            String specificNode = "playerfinder.find." + playerName.toLowerCase();
                            if (!Permissions.check(source, specificNode, 2)) {
                                source.sendError(Text.literal("You don't have permission to locate " + playerName + "."));
                                return 0;
                            }

                            BlockPos targetPos = targetPlayer.getBlockPos();
                            BlockPos sourcePos = sourcePlayer.getBlockPos();
                            RegistryKey<World> targetDim = targetPlayer.getWorld().getRegistryKey();
                            RegistryKey<World> sourceDim = sourcePlayer.getWorld().getRegistryKey();
                            boolean sameDim = sourceDim == targetDim;

                            // Dimension id like "minecraft:overworld", "minecraft:the_nether", etc.
                            String dimId = targetDim.getValue().toString();

                            // Command to SUGGEST (pastes into sender chat; they press Enter)
                            String tpCmd = "/execute in " + dimId + " run tp @s "
                                + targetPos.getX() + " " + targetPos.getY() + " " + targetPos.getZ();

                            // Build [x y z] with hover/click (1.21+ API)
                            MutableText coordsClickable = Text.literal("[")
                                .formatted(Formatting.DARK_GRAY)
                                .append(
                                    Text.literal(targetPos.getX() + " " + targetPos.getY() + " " + targetPos.getZ())
                                        .styled(style -> style
                                            .withColor(utils.getDimensionColor(targetDim))
                                            .withBold(true)
                                            .withHoverEvent(new HoverEvent.ShowText(
                                                Text.literal("Click to paste:\n")
                                                    .append(Text.literal(tpCmd).formatted(Formatting.GRAY))
                                            ))
                                            .withClickEvent(new ClickEvent.SuggestCommand(tpCmd))
                                        )
                                )
                                .append(Text.literal("]").formatted(Formatting.DARK_GRAY));

                            MutableText msg = Text.literal(playerName + " is at ")
                                .append(coordsClickable)
                                .append(Text.literal(" in the ").formatted(Formatting.WHITE))
                                .append(Text.literal(utils.getDimensionText(targetDim))
                                    .formatted(utils.getDimensionColor(targetDim)))
                                .append(
                                    Text.literal(
                                        sameDim
                                            ? " (" + utils.getDistance(sourcePos, targetPos) + " blocks away)"
                                            : " (Player is in a different dimension)"
                                    ).formatted(sameDim ? Formatting.GREEN : Formatting.RED)
                                );

                            source.sendFeedback(() -> msg, false);
                            return 1;
                        })
                    )
            );
        });
    }
}
