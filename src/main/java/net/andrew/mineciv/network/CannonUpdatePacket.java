package net.andrew.mineciv.network;

import net.andrew.mineciv.block.entity.custom.CannonBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.network.CustomPayloadEvent;

public class CannonUpdatePacket {

    private final BlockPos pos;
    private final float pitch;
    private final float power;

    public CannonUpdatePacket(BlockPos pos, float pitch, float power) {
        this.pos = pos;
        this.pitch = pitch;
        this.power = power;
    }

    public static void encode(CannonUpdatePacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeFloat(packet.pitch);
        buf.writeFloat(packet.power);
    }

    public static CannonUpdatePacket decode(FriendlyByteBuf buf) {
        return new CannonUpdatePacket(
                buf.readBlockPos(),
                buf.readFloat(),
                buf.readFloat()
        );
    }

    public static void handle(CannonUpdatePacket packet, CustomPayloadEvent.Context ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null) {
                BlockEntity blockEntity = player.level().getBlockEntity(packet.pos);
                if (blockEntity instanceof CannonBlockEntity cannon) {
                    cannon.setPitch(packet.pitch);
                    cannon.setPower(packet.power);
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}
