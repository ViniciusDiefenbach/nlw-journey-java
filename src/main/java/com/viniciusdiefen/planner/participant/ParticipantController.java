package com.viniciusdiefen.planner.participant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/participants")
public class ParticipantController {
    @Autowired
    private ParticipantRepository participantRepository;

    @PostMapping("/{id}/confirm")
    public ResponseEntity<Participant> confirmParticipant(@PathVariable UUID id,
                                                          @RequestBody ParticipantRequestPayload payload) {
        Optional<Participant> participantOptional = this.participantRepository.findById(id);

        if (participantOptional.isPresent()) {
            Participant rawParticipant = participantOptional.get();
            rawParticipant.setIsConfirmed(true);
            rawParticipant.setName(payload.name());

            this.participantRepository.save(rawParticipant);

            return ResponseEntity.ok(rawParticipant);
        }

        return ResponseEntity.notFound().build();
    }
}
