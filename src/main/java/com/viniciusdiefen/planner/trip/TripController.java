package com.viniciusdiefen.planner.trip;

import com.viniciusdiefen.planner.activity.ActivityCreateResponse;
import com.viniciusdiefen.planner.activity.ActivityData;
import com.viniciusdiefen.planner.activity.ActivityRequestPayload;
import com.viniciusdiefen.planner.activity.ActivityService;
import com.viniciusdiefen.planner.link.LinkCreateResponse;
import com.viniciusdiefen.planner.link.LinkData;
import com.viniciusdiefen.planner.link.LinkRequestPayload;
import com.viniciusdiefen.planner.link.LinkService;
import com.viniciusdiefen.planner.participant.ParticipantCreateResponse;
import com.viniciusdiefen.planner.participant.ParticipantData;
import com.viniciusdiefen.planner.participant.ParticipantRequestPayload;
import com.viniciusdiefen.planner.participant.ParticipantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/trips")
public class TripController {
    @Autowired
    private TripRepository tripRepository;
    @Autowired
    private ParticipantService participantService;
    @Autowired
    private ActivityService activityService;
    @Autowired
    private LinkService linkService;

    @PostMapping
    public ResponseEntity<TripCreateResponse> createTrip(@RequestBody TripRequestPayload payload) {
        Trip newTrip = new Trip(payload);
        this.tripRepository.save(newTrip);
        this.participantService.registerParticipantsToTrip(payload.emails_to_invite(), newTrip);
        return ResponseEntity.ok(new TripCreateResponse(newTrip.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Trip> getTripDetails(@PathVariable UUID id) {
        Optional<Trip> tripOptional = this.tripRepository.findById(id);

        return tripOptional.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Trip> updateTrip(@PathVariable UUID id, @RequestBody TripRequestPayload payload) {
        Optional<Trip> tripOptional = this.tripRepository.findById(id);

        if (tripOptional.isPresent()) {
            Trip rawTrip = tripOptional.get();
            rawTrip.setEndsAt(LocalDateTime.parse(payload.ends_at(), DateTimeFormatter.ISO_DATE_TIME));
            rawTrip.setStartsAt(LocalDateTime.parse(payload.starts_at(), DateTimeFormatter.ISO_DATE_TIME));
            rawTrip.setDestination(payload.destination());

            this.tripRepository.save(rawTrip);
            this.participantService.triggerConfirmationEmailToParticipants(id);

            return ResponseEntity.ok(rawTrip);
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/confirm")
    public ResponseEntity<Trip> confirmTrip(@PathVariable UUID id) {
        Optional<Trip> tripOptional = this.tripRepository.findById(id);

        if (tripOptional.isPresent()) {
            Trip rawTrip = tripOptional.get();
            rawTrip.setIsConfirmed(true);

            this.tripRepository.save(rawTrip);

            return ResponseEntity.ok(rawTrip);
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/invite")
    public ResponseEntity<ParticipantCreateResponse> inviteParticipant(@PathVariable UUID id,
                                                                       @RequestBody ParticipantRequestPayload payload) {
        Optional<Trip> tripOptional = this.tripRepository.findById(id);

        if (tripOptional.isPresent()) {
            Trip rawTrip = tripOptional.get();

            ParticipantCreateResponse participantResponse =
                    this.participantService.registerParticipantToTrip(payload.email(), rawTrip);

            if (rawTrip.getIsConfirmed())
                this.participantService.triggerConfirmationEmailToParticipant(payload.email());

            return ResponseEntity.ok(participantResponse);
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/participants")
    public ResponseEntity<List<ParticipantData>> getAllParticipants(@PathVariable UUID id) {
        List<ParticipantData> participants = this.participantService.getAllParticipantsFromTrip(id);

        return ResponseEntity.ok(participants);
    }

    @PostMapping("/{id}/activities")
    public ResponseEntity<ActivityCreateResponse> registerActivity(@PathVariable UUID id,
                                                                       @RequestBody ActivityRequestPayload payload) {
        Optional<Trip> tripOptional = this.tripRepository.findById(id);

        if (tripOptional.isPresent()) {
            Trip rawTrip = tripOptional.get();

            ActivityCreateResponse activityCreateResponse = this.activityService.registerActivityToTrip(payload,
                    rawTrip);

            return ResponseEntity.ok(activityCreateResponse);
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/activities")
    public ResponseEntity<List<ActivityData>> getAllActivities(@PathVariable UUID id) {
        List<ActivityData> activities = this.activityService.getAllActivitiesFromTrip(id);

        return ResponseEntity.ok(activities);
    }

    @PostMapping("/{id}/links")
    public ResponseEntity<LinkCreateResponse> registerLink(@PathVariable UUID id,
                                                                   @RequestBody LinkRequestPayload payload) {
        Optional<Trip> tripOptional = this.tripRepository.findById(id);

        if (tripOptional.isPresent()) {
            Trip rawTrip = tripOptional.get();

            LinkCreateResponse linkCreateResponse = this.linkService.registerLinkToTrip(payload,
                    rawTrip);

            return ResponseEntity.ok(linkCreateResponse);
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/links")
    public ResponseEntity<List<LinkData>> getAllLinks(@PathVariable UUID id) {
        List<LinkData> links = this.linkService.getAllLinksFromTrip(id);

        return ResponseEntity.ok(links);
    }
}
