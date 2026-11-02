package com.example.middleworld.reputation;

/** The local player's reputation as last synced from the server. */
public class ClientReputation {
    private static ReputationData data = new ReputationData();

    public static ReputationData get() {
        return data;
    }

    public static void set(ReputationData newData) {
        data = newData;
    }
}
