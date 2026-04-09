package org.example.aicareernav1.repository;

import org.example.aicareernav1.entity.VideoTask;
import org.springframework.data.jpa.repository.JpaRepository;

//String, a не Long тк на python uuid генерация id => генерирует String
public interface VideoTaskRepository extends JpaRepository<VideoTask, String> {}
