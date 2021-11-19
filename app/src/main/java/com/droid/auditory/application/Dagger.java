package com.droid.auditory.application;

public class Dagger {
    private static AuditoryComponent COMPONENT_INSTANCE;

    private Dagger() {
    }

    public static AuditoryComponent getDagger() {
        return COMPONENT_INSTANCE;
    }

    public static void setDagger(AuditoryComponent component) {
        COMPONENT_INSTANCE = component;
    }
}
