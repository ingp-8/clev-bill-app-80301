package com.clevstack.clevbill.controller;

import com.clevstack.clevbill.dto.MeResponse;
import com.clevstack.clevbill.dto.PropertyResponse;
import com.clevstack.clevbill.service.MeService;
import java.security.Principal;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
public class MeController {

    private final MeService meService;

    public MeController(MeService meService) {
        this.meService = meService;
    }

    @GetMapping
    public MeResponse profile(Principal principal) {
        return meService.getProfile(principal.getName());
    }

    @GetMapping("/properties")
    public List<PropertyResponse> properties(Principal principal) {
        return meService.getAccessibleProperties(principal.getName());
    }
}
