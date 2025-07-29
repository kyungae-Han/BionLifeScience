package com.dev.BionLifeScienceWeb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dev.BionLifeScienceWeb.model.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>{

}
