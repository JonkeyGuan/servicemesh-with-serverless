package com.demo.admin;

import java.time.Duration;

import org.reactivestreams.Publisher;

import de.codecentric.boot.admin.server.domain.events.InstanceEvent;
import de.codecentric.boot.admin.server.domain.events.InstanceStatusChangedEvent;
import de.codecentric.boot.admin.server.domain.values.InstanceId;
import de.codecentric.boot.admin.server.services.AbstractEventHandler;
import de.codecentric.boot.admin.server.services.InstanceRegistry;
import de.codecentric.boot.admin.server.services.IntervalCheck;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public class DeregisterOfflineTrigger extends AbstractEventHandler<InstanceEvent> {
    private final IntervalCheck intervalCheck;

    private InstanceRegistry instanceRegistry;

    public DeregisterOfflineTrigger(Publisher<InstanceEvent> publisher, InstanceRegistry instanceRegistry) {
        super(publisher, InstanceEvent.class);
        this.instanceRegistry = instanceRegistry;
        this.intervalCheck = new IntervalCheck("deregister", this::updateInfo, Duration.ofMinutes(5),
                Duration.ofMinutes(1));
    }

    @Override
    protected Publisher<Void> handle(Flux<InstanceEvent> publisher) {

        Scheduler scheduler = Schedulers.newSingle("offline-deregister");
        return publisher.subscribeOn(scheduler)
                .filter(event -> event instanceof InstanceStatusChangedEvent
                        && ((InstanceStatusChangedEvent) event).getStatusInfo().isOffline())
                .flatMap(event -> this.updateInfo(event.getInstance())).doFinally(s -> scheduler.dispose());
    }

    private Mono<Void> updateInfo(InstanceId instance) {

        return instanceRegistry.deregister(instance).then();

    }

    @Override
    public void start() {
        super.start();
        this.intervalCheck.start();
    }

    @Override
    public void stop() {
        super.stop();
        this.intervalCheck.stop();
    }

    public void setInterval(Duration updateInterval) {
        this.intervalCheck.setInterval(updateInterval);
    }

    public void setLifetime(Duration infoLifetime) {
        this.intervalCheck.setMinRetention(infoLifetime);
    }
}
