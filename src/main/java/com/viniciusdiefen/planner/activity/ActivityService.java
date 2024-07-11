package com.viniciusdiefen.planner.activity;

import com.viniciusdiefen.planner.trip.Trip;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ActivityService {
    @Autowired
    private ActivityRepository activityRepository;

    public ActivityCreateResponse registerActivityToTrip(ActivityRequestPayload payload, Trip trip) {
        Activity newActivity = new Activity(payload.title(), payload.occurs_at(), trip);

        this.activityRepository.save(newActivity);

        return new ActivityCreateResponse(newActivity.getId());
    }

    public List<ActivityData> getAllActivitiesFromTrip(UUID id) {
        return this.activityRepository.findByTripId(id).stream().map(activity -> new ActivityData(activity.getId(),
                activity.getTitle(), activity.getOccursAt())).toList();
    }
}
