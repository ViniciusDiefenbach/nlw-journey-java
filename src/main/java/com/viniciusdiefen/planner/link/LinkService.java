package com.viniciusdiefen.planner.link;

import com.viniciusdiefen.planner.trip.Trip;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class LinkService {
    @Autowired
    private LinkRepository linkRepository;


    public LinkCreateResponse registerLinkToTrip(LinkRequestPayload payload, Trip trip) {
        Link newLink = new Link(payload.title(), payload.url(), trip);

        this.linkRepository.save(newLink);

        return new LinkCreateResponse(newLink.getId());
    }

    public List<LinkData> getAllLinksFromTrip(UUID id) {
        return this.linkRepository.findByTripId(id).stream().map(link -> new LinkData(link.getId(), link.getTitle(),
                link.getUrl())).toList();
    }
}
