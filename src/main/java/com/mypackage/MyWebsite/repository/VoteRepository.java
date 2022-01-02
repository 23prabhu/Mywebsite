package com.mypackage.MyWebsite.repository;

import com.mypackage.MyWebsite.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoteRepository extends JpaRepository<Vote, Long> {
}