package com.finanzas.app_back.repositories;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.finanzas.app_back.dto.Space.InvitationDto;
import com.finanzas.app_back.dto.Space.MemberDto;
import com.finanzas.app_back.dto.Space.SpaceDto;
import com.finanzas.app_back.model.Invitation;
import com.finanzas.app_back.model.Space;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteBatch;

@Repository
public class SpaceRepository {

    @Autowired
    private Firestore firestore;

    public String createSpace(Space space, String ownerId) throws ExecutionException, InterruptedException {
        String now = LocalDateTime.now().toString();

        DocumentReference spaceRef = firestore.collection("spaces").document();
        String spaceId = spaceRef.getId();

        Map<String, Object> memberData = new HashMap<>();
        memberData.put("role", "owner");
        memberData.put("joinedAt", now);

        Map<String, Object> userSpaceData = new HashMap<>();
        userSpaceData.put("spaceId", spaceId);
        userSpaceData.put("role", "owner");
        userSpaceData.put("joinedAt", now);

        WriteBatch batch = firestore.batch();
        batch.set(spaceRef, space);
        batch.set(firestore.collection("spaces").document(spaceId).collection("members").document(ownerId), memberData);
        batch.set(firestore.collection("users").document(ownerId).collection("spaces").document(spaceId), userSpaceData);
        batch.commit().get();

        return spaceId;
    }

    public List<SpaceDto> getSpacesByUser(String uid) throws ExecutionException, InterruptedException {
        List<SpaceDto> result = new ArrayList<>();

        ApiFuture<QuerySnapshot> query = firestore.collection("users").document(uid).collection("spaces").get();
        for (QueryDocumentSnapshot doc : query.get().getDocuments()) {
            String spaceId = doc.getString("spaceId");
            String role = doc.getString("role");

            if (spaceId == null) continue;

            DocumentSnapshot spaceDoc = firestore.collection("spaces").document(spaceId).get().get();
            if (spaceDoc.exists()) {
                SpaceDto dto = new SpaceDto();
                dto.setSpaceId(spaceId);
                dto.setName(spaceDoc.getString("name"));
                dto.setType(spaceDoc.getString("type"));
                dto.setOwnerId(spaceDoc.getString("ownerId"));
                dto.setRole(role);
                result.add(dto);
            }
        }

        return result;
    }

    public List<MemberDto> getMembersBySpace(String spaceId) throws ExecutionException, InterruptedException {
        List<MemberDto> result = new ArrayList<>();

        ApiFuture<QuerySnapshot> query = firestore.collection("spaces").document(spaceId).collection("members").get();
        for (QueryDocumentSnapshot doc : query.get().getDocuments()) {
            MemberDto dto = new MemberDto();
            dto.setUserId(doc.getId());
            dto.setRole(doc.getString("role"));
            dto.setJoinedAt(doc.getString("joinedAt"));
            result.add(dto);
        }

        return result;
    }

    public boolean isMember(String spaceId, String uid) throws ExecutionException, InterruptedException {
        DocumentSnapshot snap = firestore.collection("spaces").document(spaceId)
                .collection("members").document(uid).get().get();
        return snap.exists();
    }

    public String getMemberRole(String spaceId, String uid) throws ExecutionException, InterruptedException {
        DocumentSnapshot snap = firestore.collection("spaces").document(spaceId)
                .collection("members").document(uid).get().get();
        if (!snap.exists()) return null;
        return snap.getString("role");
    }

    public void validateMembership(String spaceId, String uid) throws ExecutionException, InterruptedException {
        if (!isMember(spaceId, uid)) {
            throw new RuntimeException("403: El usuario no tiene acceso al space especificado.");
        }
    }

    // Single read that validates membership AND returns role — use instead of calling both methods
    public String validateAndGetRole(String spaceId, String uid) throws ExecutionException, InterruptedException {
        DocumentSnapshot snap = firestore.collection("spaces").document(spaceId)
                .collection("members").document(uid).get().get();
        if (!snap.exists()) {
            throw new RuntimeException("403: El usuario no tiene acceso al space especificado.");
        }
        return snap.getString("role");
    }

    public String createInvitation(Invitation invitation) throws ExecutionException, InterruptedException {
        String code = generateUniqueCode();
        invitation.setCode(code);
        firestore.collection("invitations").document(code).set(invitation).get();
        return code;
    }

    private String generateUniqueCode() throws ExecutionException, InterruptedException {
        Random random = new Random();
        String code;
        do {
            code = String.format("%05d", random.nextInt(100000));
        } while (firestore.collection("invitations").document(code).get().get().exists());
        return code;
    }

    public List<InvitationDto> getInvitationsBySpace(String spaceId) throws ExecutionException, InterruptedException {
        List<InvitationDto> result = new ArrayList<>();
        ApiFuture<QuerySnapshot> query = firestore.collection("invitations")
                .whereEqualTo("spaceId", spaceId)
                .get();
        for (QueryDocumentSnapshot doc : query.get().getDocuments()) {
            InvitationDto dto = new InvitationDto();
            dto.setCode(doc.getId());
            dto.setSpaceId(doc.getString("spaceId"));
            dto.setInvitedEmail(doc.getString("invitedEmail"));
            dto.setRole(doc.getString("role"));
            dto.setStatus(doc.getString("status"));
            result.add(dto);
        }
        return result;
    }

    public InvitationDto getInvitationByCode(String code) throws ExecutionException, InterruptedException {
        DocumentSnapshot snap = firestore.collection("invitations").document(code).get().get();
        if (!snap.exists()) return null;
        InvitationDto dto = new InvitationDto();
        dto.setCode(code);
        dto.setSpaceId(snap.getString("spaceId"));
        dto.setInvitedEmail(snap.getString("invitedEmail"));
        dto.setRole(snap.getString("role"));
        dto.setStatus(snap.getString("status"));
        return dto;
    }

    public void removeMember(String spaceId, String userId) throws ExecutionException, InterruptedException {
        WriteBatch batch = firestore.batch();
        batch.delete(firestore.collection("spaces").document(spaceId).collection("members").document(userId));
        batch.delete(firestore.collection("users").document(userId).collection("spaces").document(spaceId));
        batch.commit().get();
    }

    public void renameSpace(String spaceId, String newName) throws ExecutionException, InterruptedException {
        firestore.collection("spaces").document(spaceId).update("name", newName).get();
    }

    public void deleteSpace(String spaceId) throws ExecutionException, InterruptedException {
        String[] subcollections = { "members", "cuentas", "transacciones", "tarjetas",
                "categorias", "transaccionesRecurrentes", "transferencias", "msi" };

        // 1. Fire all subcollection list queries in parallel
        List<ApiFuture<QuerySnapshot>> futures = new ArrayList<>();
        for (String sub : subcollections) {
            futures.add(firestore.collection("spaces").document(spaceId).collection(sub).get());
        }

        // 2. Collect all document refs to delete; extract member IDs from the first future ("members")
        List<DocumentReference> refsToDelete = new ArrayList<>();
        List<String> memberIds = new ArrayList<>();
        for (int i = 0; i < futures.size(); i++) {
            for (QueryDocumentSnapshot doc : futures.get(i).get().getDocuments()) {
                refsToDelete.add(doc.getReference());
                if (i == 0) memberIds.add(doc.getId()); // "members" subcollection
            }
        }

        // 3. Add user→space index refs and the space document itself
        for (String memberId : memberIds) {
            refsToDelete.add(firestore.collection("users").document(memberId).collection("spaces").document(spaceId));
        }
        refsToDelete.add(firestore.collection("spaces").document(spaceId));

        // 4. Batch delete in chunks of 500 (Firestore limit)
        for (int i = 0; i < refsToDelete.size(); i += 500) {
            WriteBatch batch = firestore.batch();
            for (DocumentReference ref : refsToDelete.subList(i, Math.min(i + 500, refsToDelete.size()))) {
                batch.delete(ref);
            }
            batch.commit().get();
        }
    }

    public void acceptInvitation(String code, String uid, String spaceId, String role) throws ExecutionException, InterruptedException {
        String now = LocalDateTime.now().toString();

        Map<String, Object> memberData = new HashMap<>();
        memberData.put("role", role);
        memberData.put("joinedAt", now);

        Map<String, Object> userSpaceData = new HashMap<>();
        userSpaceData.put("spaceId", spaceId);
        userSpaceData.put("role", role);
        userSpaceData.put("joinedAt", now);

        WriteBatch batch = firestore.batch();
        batch.set(firestore.collection("spaces").document(spaceId).collection("members").document(uid), memberData);
        batch.set(firestore.collection("users").document(uid).collection("spaces").document(spaceId), userSpaceData);
        batch.delete(firestore.collection("invitations").document(code));
        batch.commit().get();
    }
}
