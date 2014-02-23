/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.devnexus.ting.core.dao.jpa;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.devnexus.ting.core.dao.UserCalendarDao;
import com.devnexus.ting.core.model.User;
import com.devnexus.ting.core.model.UserCalendar;

/**
 *
 * @author summers
 */
@Repository("userCalendarDao")
public class UserCalendarDaoJpa extends GenericDaoJpa< UserCalendar, Long> implements UserCalendarDao {

    public UserCalendarDaoJpa() {
        super(UserCalendar.class);
    }

    @Override
    public List<UserCalendar> getUserCalendar(User user, String eventKey) {
        return super.entityManager.createQuery("from UserCalendar where event_key = :eventKey and username = :username order by fromTime").setParameter("username", user.getUsername()).setParameter("eventKey", eventKey).getResultList();
    }

    @Override
    public List<UserCalendar> getTemplateCalendar(String eventKey) {
        return super.entityManager.createQuery("from UserCalendar where event_key = :eventKey and template = true order by fromTime").setParameter("eventKey", eventKey).getResultList();
    }
    
}