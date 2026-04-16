package com.patrakosh.api.activity;

import com.patrakosh.api.auth.AuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ActivityController {

    private final AuthService authService;
    private final ActivityService activityService;

    public ActivityController(AuthService authService, ActivityService activityService) {
        this.authService = authService;
        this.activityService = activityService;
    }

    @GetMapping({"/api/activity", "/activity"})
    public List<ActivityService.ActivityEntry> listActivity(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader
    ) {
        AuthService.UserAccount user = authService.requireUser(authorizationHeader);
        return activityService.listForUser(user.id());
    }
}
