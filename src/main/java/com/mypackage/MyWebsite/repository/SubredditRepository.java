package com.mypackage.MyWebsite.repository;

import com.mypackage.MyWebsite.model.Subreddit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubredditRepository extends JpaRepository<Subreddit, Long> {
}