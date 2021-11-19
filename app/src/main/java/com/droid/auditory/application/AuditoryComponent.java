package com.droid.auditory.application;

import com.droid.auditory.activities.StandAloneActivity;
import com.droid.auditory.services.AuditoryService;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = AuditoryModule.class)
public interface AuditoryComponent {
    void inject(StandAloneActivity standAloneActivity);
    void inject(AuditoryService auditoryService);
}
