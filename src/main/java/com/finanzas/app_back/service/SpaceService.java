package com.finanzas.app_back.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.finanzas.app_back.dto.GenericResponse;
import com.finanzas.app_back.dto.Space.AcceptInvitationRequest;
import com.finanzas.app_back.dto.Space.CreateInvitationRequest;
import com.finanzas.app_back.dto.Space.CreateSpaceRequest;
import com.finanzas.app_back.dto.Space.InvitationDto;
import com.finanzas.app_back.dto.Space.MemberDto;
import com.finanzas.app_back.model.Invitation;
import com.finanzas.app_back.model.Space;
import com.finanzas.app_back.repositories.SpaceRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetUsersResult;
import com.google.firebase.auth.UidIdentifier;
import com.google.firebase.auth.UserIdentifier;

@Service
public class SpaceService {

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private GeneralService generalService;

    public String createPersonalSpace(String uid, String spaceName) throws Exception {
        Space space = new Space();
        space.setName(spaceName);
        space.setType("personal");
        space.setOwnerId(uid);
        space.setCreatedAt(LocalDateTime.now().toString());
        return spaceRepository.createSpace(space, uid);
    }

    public GenericResponse createSpace(String uid, CreateSpaceRequest req) {
        GenericResponse response = new GenericResponse();
        try {
            Space space = new Space();
            space.setName(req.getName());
            space.setType("shared");
            space.setOwnerId(uid);
            space.setCreatedAt(LocalDateTime.now().toString());
            String spaceId = spaceRepository.createSpace(space, uid);
            response.setCoderr("0000");
            response.setMessage("Space creado exitosamente.");
            response.setData(spaceId);
        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al crear el space");
        }
        return response;
    }

    public GenericResponse getSpacesByUser(String uid) {
        GenericResponse response = new GenericResponse();
        try {
            response.setCoderr("0000");
            response.setMessage("Spaces obtenidos exitosamente.");
            response.setData(spaceRepository.getSpacesByUser(uid));
        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al obtener los spaces");
        }
        return response;
    }

    public GenericResponse getMembersBySpace(String spaceId, String uid) {
        GenericResponse response = new GenericResponse();
        try {
            spaceRepository.validateMembership(spaceId, uid);
            List<MemberDto> members = spaceRepository.getMembersBySpace(spaceId);
            if (!members.isEmpty()) {
                List<UserIdentifier> identifiers = members.stream()
                        .map(m -> new UidIdentifier(m.getUserId()))
                        .collect(Collectors.toList());
                try {
                    GetUsersResult result = FirebaseAuth.getInstance().getUsers(identifiers);
                    Map<String, String> nameByUid = result.getUsers().stream()
                            .collect(Collectors.toMap(
                                    ur -> ur.getUid(),
                                    ur -> ur.getDisplayName() != null ? ur.getDisplayName() : ""));
                    members.forEach(m -> m.setName(nameByUid.get(m.getUserId())));
                } catch (Exception ignored) {
                    // Si falla el lookup de nombres, se retornan los miembros sin nombre
                }
            }
            response.setCoderr("0000");
            response.setMessage("Miembros obtenidos exitosamente.");
            response.setData(members);
        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al obtener los miembros");
        }
        return response;
    }

    public GenericResponse createInvitation(String spaceId, String uid, CreateInvitationRequest req) {
        GenericResponse response = new GenericResponse();
        try {
            String role = spaceRepository.validateAndGetRole(spaceId, uid);
            if (!"owner".equals(role) && !"admin".equals(role)) {
                response.setCoderr("1006");
                response.setMessage("No tienes permisos para invitar miembros a este space.");
                return response;
            }

            Invitation invitation = new Invitation();
            invitation.setSpaceId(spaceId);
            invitation.setInvitedEmail(req.getEmail());
            invitation.setInvitedBy(uid);
            invitation.setRole(req.getRole());
            invitation.setStatus("pending");

            String code = spaceRepository.createInvitation(invitation);

            response.setCoderr("0000");
            response.setMessage("Invitación creada exitosamente.");
            response.setData(code);
        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al crear la invitación");
        }
        return response;
    }

    public GenericResponse getInvitationsBySpace(String spaceId, String uid) {
        GenericResponse response = new GenericResponse();
        try {
            spaceRepository.validateMembership(spaceId, uid);
            response.setCoderr("0000");
            response.setMessage("Invitaciones obtenidas exitosamente.");
            response.setData(spaceRepository.getInvitationsBySpace(spaceId));
        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al obtener las invitaciones");
        }
        return response;
    }

    public GenericResponse acceptInvitation(AcceptInvitationRequest req, String uid, String email) {
        GenericResponse response = new GenericResponse();
        try {
            InvitationDto invitation = spaceRepository.getInvitationByCode(req.getCode());

            if (invitation == null) {
                response.setCoderr("1001");
                response.setMessage("El código de invitación no existe.");
                return response;
            }
            if (!"pending".equals(invitation.getStatus())) {
                response.setCoderr("1002");
                response.setMessage("La invitación ya fue utilizada.");
                return response;
            }
            if (!invitation.getInvitedEmail().equalsIgnoreCase(email)) {
                response.setCoderr("1003");
                response.setMessage("Este código de invitación no corresponde a tu cuenta.");
                return response;
            }

            spaceRepository.acceptInvitation(req.getCode(), uid, invitation.getSpaceId(), invitation.getRole());

            response.setCoderr("0000");
            response.setMessage("Te has unido al space exitosamente.");
        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al aceptar la invitación");
        }
        return response;
    }

    public GenericResponse removeMember(String spaceId, String uid, String targetUserId) {
        GenericResponse response = new GenericResponse();
        try {
            String role = spaceRepository.validateAndGetRole(spaceId, uid);
            if (!"owner".equals(role)) {
                response.setCoderr("1006");
                response.setMessage("Solo el owner puede eliminar miembros del space.");
                return response;
            }
            if (uid.equals(targetUserId)) {
                response.setCoderr("1008");
                response.setMessage("El owner no puede eliminarse a sí mismo del space.");
                return response;
            }
            if (!spaceRepository.isMember(spaceId, targetUserId)) {
                response.setCoderr("1001");
                response.setMessage("El usuario no es miembro de este space.");
                return response;
            }
            spaceRepository.removeMember(spaceId, targetUserId);
            response.setCoderr("0000");
            response.setMessage("Miembro eliminado exitosamente.");
        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al eliminar el miembro");
        }
        return response;
    }

    public GenericResponse renameSpace(String spaceId, String uid, String newName) {
        GenericResponse response = new GenericResponse();
        try {
            String role = spaceRepository.validateAndGetRole(spaceId, uid);
            if (!"owner".equals(role)) {
                response.setCoderr("1006");
                response.setMessage("Solo el owner puede renombrar el space.");
                return response;
            }
            spaceRepository.renameSpace(spaceId, newName);
            response.setCoderr("0000");
            response.setMessage("Space renombrado exitosamente.");
        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al renombrar el space");
        }
        return response;
    }

    public GenericResponse deleteSpace(String spaceId, String uid) {
        GenericResponse response = new GenericResponse();
        try {
            String role = spaceRepository.validateAndGetRole(spaceId, uid);
            if (!"owner".equals(role)) {
                response.setCoderr("1006");
                response.setMessage("Solo el owner puede eliminar el space.");
                return response;
            }

            java.util.List<com.finanzas.app_back.dto.Space.SpaceDto> spaces = spaceRepository.getSpacesByUser(uid);
            com.finanzas.app_back.dto.Space.SpaceDto target = spaces.stream()
                    .filter(s -> spaceId.equals(s.getSpaceId()))
                    .findFirst().orElse(null);
            if (target != null && "personal".equals(target.getType())) {
                response.setCoderr("1007");
                response.setMessage("No se puede eliminar el space personal.");
                return response;
            }

            spaceRepository.deleteSpace(spaceId);
            response.setCoderr("0000");
            response.setMessage("Space eliminado exitosamente.");
        } catch (Exception e) {
            response = generalService.handleExcepcion(e, "Error al eliminar el space");
        }
        return response;
    }
}
