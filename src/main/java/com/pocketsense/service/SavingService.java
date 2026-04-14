package com.pocketsense.service;

import com.pocketsense.dto.SavingRequest;
import com.pocketsense.model.Saving;
import com.pocketsense.repository.SavingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class SavingService {

    @Autowired
    private SavingRepository savingRepository;

    public Saving addSaving(SavingRequest request) {
        Saving saving = new Saving();
        saving.setUserId(request.getUserId());
        saving.setAmount(request.getAmount());
        saving.setDate(request.getDate() != null ? request.getDate() : LocalDate.now());
        return savingRepository.save(saving);
    }

    public List<Saving> getSavingsByUserId(UUID userId) {
        return savingRepository.findByUserId(userId);
    }
}
