package com.esgis2026.assigame.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.esgis2026.assigame.entity.Conversation;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("SELECT c FROM Conversation c WHERE " +
           "(c.participant1.id = :u1 AND c.participant2.id = :u2) OR " +
           "(c.participant1.id = :u2 AND c.participant2.id = :u1)")
    List<Conversation> findEntre(@Param("u1") Long u1, @Param("u2") Long u2);

    @Query("SELECT c FROM Conversation c WHERE c.participant1.id = :userId OR c.participant2.id = :userId")
    List<Conversation> findByParticipant(@Param("userId") Long userId);
}
