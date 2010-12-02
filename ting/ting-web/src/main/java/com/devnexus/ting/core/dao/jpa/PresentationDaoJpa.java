package com.devnexus.ting.core.dao.jpa;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.devnexus.ting.core.dao.PresentationDao;
import com.devnexus.ting.core.model.Presentation;

@Repository("presentationDao")
public class PresentationDaoJpa extends GenericDaoJpa< Presentation, Long>
                           implements PresentationDao {

    /** Constructor. */
    public PresentationDaoJpa() {
        super(Presentation.class);
    }

	@Override
	public List<Presentation> getPresentationsForCurrentEvent() {
		return super.entityManager
			.createQuery("select p from Presentation p "
					   + "left join p.event e "
					   + "where e.current = :iscurrent "
					   + "order by p.title ASC", Presentation.class)
			.setParameter("iscurrent", true)
			.getResultList();
	}

	@Override
	public List<Presentation> getPresentationsForEvent(Long eventId) {
		return super.entityManager
		.createQuery("select p from Presentation p "
				   + "    join p.event e "
				   + "where e.id = :eventId "
				   + "order by p.title ASC", Presentation.class)
		.setParameter("eventId", eventId)
		.getResultList();
	}

}
