package com.esgis2026.assigame.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.esgis2026.assigame.entity.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByConversation_IdOrderByDateEnvoiAsc(Long conversationId);

    List<Message> findByConversation_IdAndLuFalse(Long conversationId);

    long countByConversation_IdAndLuFalse(Long conversationId);
}
