package com.example.daily_expense_tracker.service;

import com.example.daily_expense_tracker.entity.DailyExpense;
import com.example.daily_expense_tracker.repository.DailyExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DailyExpenseService {

    @Autowired
    private DailyExpenseRepository dailyExpenseRepository;

    public DailyExpense createExpense(DailyExpense dailyExpense) {
        return dailyExpenseRepository.save(dailyExpense);
    }


    public List<DailyExpense> getExpenseByDay(int year, int month, int day) {
       LocalDate startOfDay = LocalDate.of(year,month,day);
       LocalDate endOfDay = startOfDay.plusDays(1);
       return dailyExpenseRepository.findByDateBetween(startOfDay,endOfDay);
    }

    public List<DailyExpense> getExpenseByMonth(int year, int month) {
        if (year <= 0) {
            year = LocalDate.now().getYear();
        }
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.plusMonths(1).minusDays(1);
        return dailyExpenseRepository.findByDateBetween(startOfMonth, endOfMonth);
    }

    public List<DailyExpense> getExpenseByYear(int year) {
        LocalDate startOfYear = LocalDate.of(year, 1,1);
        LocalDate endOfYear = startOfYear.plusYears(1).minusDays(1);
        return dailyExpenseRepository.findByDateBetween(startOfYear,endOfYear);
    }

    public DailyExpense getExpenseById(Long id) {
        return dailyExpenseRepository.findById(id).orElse(null);
    }

    public DailyExpense updateDailyExpense(DailyExpense dailyExpense) {
        return dailyExpenseRepository.save(dailyExpense);
    }

    public boolean deleteDailyExpenseEntry(Long id) {
        if(dailyExpenseRepository.existsById(id)){
            dailyExpenseRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<DailyExpense> getAllExpenses() {
        return dailyExpenseRepository.findAll();
    }

    public double getTotalExpenses() {
        List<DailyExpense> dailyExpenses = dailyExpenseRepository.findAll();
        if (dailyExpenses == null || dailyExpenses.isEmpty()) {
            dailyExpenses = new ArrayList<>(); // Initialize with an empty list as a fallback
        }
        double totalExpense = 0.0;
        for(DailyExpense expense: dailyExpenses){
            totalExpense +=expense.getExpense();
        }
        return totalExpense;
    }

    public Map<String, Double> getDataByCategory() {
        List<DailyExpense> dailyExpenses = dailyExpenseRepository.findAll();
        Map<String, Double> expenseDistribution = new HashMap<>();

        for(DailyExpense expense: dailyExpenses){
            String category = expense.getCategory();
            double amount = expense.getExpense();
            expenseDistribution.put(category,expenseDistribution.getOrDefault(category,0.0)+amount);
        }
        return expenseDistribution;
    }

    public List<String> getTopSpendingCategories() {
//        List<DailyExpense> dailyExpenses = dailyExpenseRepository.findAll();
        Map<String,Double> topSpendingCategory = getDataByCategory();
        return topSpendingCategory.entrySet().stream()
                .sorted(Map.Entry.<String,Double>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
