package com.kuke.videomeeting.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class FriendTest {

    @PersistenceContext
    EntityManager em;

    @BeforeEach
    public void beforeEach() {
        User user1 = User.createUser("user1", "1234", "user1", "user1",  Collections.singletonList(Role.ROLE_NORMAL));
        User user2 = User.createUser("user2", "1234", "user2", "user2",  Collections.singletonList(Role.ROLE_NORMAL));
        em.persist(user1);
        em.persist(user2);
    }

    @Test
    public void createFriendTest() {

        // given
        Friend friend = Friend.createFriend(
                em.createQuery("select u from User u where u.uid = :uid", User.class)
                        .setParameter("uid", "user1")
                        .getSingleResult(),
                em.createQuery("select u from User u where u.uid = :uid", User.class)
                        .setParameter("uid", "user2")
                        .getSingleResult());

        // when
        em.persist(friend);
        em.flush();
        em.clear();

        // then
        Friend result = em.createQuery("select f from Friend f where f.from.id = :id", Friend.class)
                .setParameter("id", friend.getFrom().getId())
                .getSingleResult();
        assertThat(result.getTo().getId()).isEqualTo(friend.getTo().getId());
    }


}