package com.example.daily_expense_tracker.repository;

import com.example.daily_expense_tracker.entity.DailyExpense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DailyExpenseRepository extends JpaRepository<DailyExpense, Long> {
    List<DailyExpense> findByDateBetween(LocalDate startOfMonth, LocalDate endOfMonth);
}
