package com.circle.service;

import com.circle.entity.TimeOffRequest;
import com.circle.exception.InvalidDateException;
import com.circle.exception.TimeOffException;
import com.circle.repository.EmployeeRepository;
import com.circle.repository.RequestCategoryRepository;
import com.circle.repository.TimeOffRequestRepository;
import com.circle.dto.TimeOffRequestDto;
import com.circle.types.MessageTypes;
import com.circle.types.RequestCategoryEnum;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
public class TimeOffRequestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeOffRequestService.class);
    private final TimeOffRequestRepository timeOffRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final RequestCategoryRepository requestCategoryRepository;

    public TimeOffRequestService(TimeOffRequestRepository timeOffRequestRepository, EmployeeRepository employeeRepository, RequestCategoryRepository requestCategoryRepository) {
        this.timeOffRequestRepository = timeOffRequestRepository;
        this.employeeRepository = employeeRepository;
        this.requestCategoryRepository = requestCategoryRepository;
    }

    public String createTimeOffRequest(@Valid TimeOffRequestDto timeOffRequestDto){
        var timeOffRequest = new TimeOffRequest();
        if(timeOffRequestDto.getEndDate().isBefore(timeOffRequestDto.getStartDate())){
            throw new InvalidDateException(MessageTypes.END_DATE_EARLIER.getMessage());
        }

        var hasTimeOff= timeOffRequestRepository.findTimeOffRequestsByStartDateBetween(
                timeOffRequestDto.getStartDate(),timeOffRequestDto.getEndDate()
        ).getRequestCategory().getRequestCategoryType();

        if(!Objects.equals(hasTimeOff, RequestCategoryEnum.REMOTE_WORKING.getRequestCategoryType())){
            throw new TimeOffException(MessageTypes.EMPLOYEE_HAS_TIMEOFF.getMessage());
        }

        timeOffRequest.setRequestCategory(requestCategoryRepository.findById(timeOffRequestDto.getRequestCategoryId()).get());
        timeOffRequest.setId(UUID.randomUUID());
        timeOffRequest.setEmployee(employeeRepository.findById(timeOffRequestDto.getEmployeeId()).get());
        timeOffRequest.setStartDate(timeOffRequestDto.getStartDate());
        timeOffRequest.setEndDate(timeOffRequestDto.getEndDate());
        timeOffRequestRepository.save(timeOffRequest);

    return MessageTypes.TIMEOFF_REQUEST_CREATED.getMessage() ;

    }
}
