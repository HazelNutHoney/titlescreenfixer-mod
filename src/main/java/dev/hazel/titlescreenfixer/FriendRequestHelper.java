package dev.hazel.titlescreenfixer;

import net.minecraft.client.Minecraft;

public class FriendRequestHelper {
    public static int getPendingRequestCount(Minecraft minecraft) {
        var social = minecraft.getPlayerSocialManager();
        return social.getIncomingRequests().size() + social.getOutgoingRequests().size();
    }

    public static boolean hasPendingRequests(Minecraft minecraft) {
        return getPendingRequestCount(minecraft) > 0;
    }
}
