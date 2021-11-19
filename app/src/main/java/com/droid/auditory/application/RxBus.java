package com.droid.auditory.application;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

public class RxBus {
    private final Subject<Object, Object> bus = new SerializedSubject<>(PublishSubject.create());

    public void post(Object o) {
        bus.onNext(o);
    }

    public Observable<Object> asObservable() {
        return bus.asObservable();
    }

    public boolean hasObservers() {
        return bus.hasObservers();
    }

    public Subscription setSubscriptions(Action1 action1) {
        return bus.asObservable().subscribe(action1);
    }
}
