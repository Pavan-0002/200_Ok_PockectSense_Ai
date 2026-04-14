package com.pocketsense.service;

import com.pocketsense.dto.ProfileRequest;
import com.pocketsense.model.Profile;
import com.pocketsense.repository.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ProfileService {

    @Autowired
    private ProfileRepository profileRepository;

    public Profile saveOrUpdateProfile(ProfileRequest request) {
        Optional<Profile> existing = profileRepository.findByUserId(request.getUserId());
        Profile profile = existing.orElse(new Profile());
        
        profile.setUserId(request.getUserId());
        profile.setName(request.getName());
        profile.setEmail(request.getEmail());
        if(request.getMonthlyBudget() != null) profile.setMonthlyBudget(request.getMonthlyBudget());
        if(request.getSavingsGoal() != null) profile.setSavingsGoal(request.getSavingsGoal());
        if(request.getImageUrl() != null) profile.setImageUrl(request.getImageUrl());

        return profileRepository.save(profile);
    }

    public Profile getProfileByUserId(UUID userId) {
        return profileRepository.findByUserId(userId).orElse(null);
    }
}
