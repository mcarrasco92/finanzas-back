package com.finanzas.app_back.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.finanzas.app_back.dto.GenericResponse;
import com.finanzas.app_back.dto.Space.AcceptInvitationRequest;
import com.finanzas.app_back.dto.Space.CreateInvitationRequest;
import com.finanzas.app_back.dto.Space.CreateSpaceRequest;
import com.finanzas.app_back.service.SpaceService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class SpaceController {

    @Autowired
    private SpaceService spaceService;

    @PostMapping("/api/spaces")
    public ResponseEntity<GenericResponse> createSpace(HttpServletRequest request, @RequestBody CreateSpaceRequest body) {
        String uid = (String) request.getAttribute("uid");
        return ResponseEntity.ok(spaceService.createSpace(uid, body));
    }

    @GetMapping("/api/spaces")
    public ResponseEntity<GenericResponse> getSpacesByUser(HttpServletRequest request) {
        String uid = (String) request.getAttribute("uid");
        return ResponseEntity.ok(spaceService.getSpacesByUser(uid));
    }

    @PutMapping("/api/spaces/{spaceId}")
    public ResponseEntity<GenericResponse> renameSpace(HttpServletRequest request, @PathVariable String spaceId, @RequestBody CreateSpaceRequest body) {
        String uid = (String) request.getAttribute("uid");
        return ResponseEntity.ok(spaceService.renameSpace(spaceId, uid, body.getName()));
    }

    @DeleteMapping("/api/spaces/{spaceId}")
    public ResponseEntity<GenericResponse> deleteSpace(HttpServletRequest request, @PathVariable String spaceId) {
        String uid = (String) request.getAttribute("uid");
        return ResponseEntity.ok(spaceService.deleteSpace(spaceId, uid));
    }

    @GetMapping("/api/spaces/{spaceId}/members")
    public ResponseEntity<GenericResponse> getMembersBySpace(HttpServletRequest request, @PathVariable String spaceId) {
        String uid = (String) request.getAttribute("uid");
        return ResponseEntity.ok(spaceService.getMembersBySpace(spaceId, uid));
    }

    @DeleteMapping("/api/spaces/{spaceId}/members/{userId}")
    public ResponseEntity<GenericResponse> removeMember(HttpServletRequest request, @PathVariable String spaceId, @PathVariable String userId) {
        String uid = (String) request.getAttribute("uid");
        return ResponseEntity.ok(spaceService.removeMember(spaceId, uid, userId));
    }

    @PostMapping("/api/spaces/{spaceId}/invitations")
    public ResponseEntity<GenericResponse> createInvitation(HttpServletRequest request, @PathVariable String spaceId, @RequestBody CreateInvitationRequest body) {
        String uid = (String) request.getAttribute("uid");
        return ResponseEntity.ok(spaceService.createInvitation(spaceId, uid, body));
    }

    @GetMapping("/api/spaces/{spaceId}/invitations")
    public ResponseEntity<GenericResponse> getInvitationsBySpace(HttpServletRequest request, @PathVariable String spaceId) {
        String uid = (String) request.getAttribute("uid");
        return ResponseEntity.ok(spaceService.getInvitationsBySpace(spaceId, uid));
    }

    @PostMapping("/api/invitations/accept")
    public ResponseEntity<GenericResponse> acceptInvitation(HttpServletRequest request, @RequestBody AcceptInvitationRequest body) {
        String uid = (String) request.getAttribute("uid");
        String email = (String) request.getAttribute("email");
        return ResponseEntity.ok(spaceService.acceptInvitation(body, uid, email));
    }
}
