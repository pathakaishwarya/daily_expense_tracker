package com.example.daily_expense_tracker.controller;


import com.example.daily_expense_tracker.entity.DailyExpense;
import com.example.daily_expense_tracker.service.DailyExpenseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.DeflaterOutputStream;

@RestController
@RequestMapping("/expense")
@CrossOrigin(origins = "http://localhost:3000")
public class DailyExpenseController {

    private static final Logger logger = LoggerFactory.getLogger(DailyExpenseController.class);

    @Autowired
    private DailyExpenseService dailyExpenseService;

    @PostMapping(value = "/create", consumes = {"multipart/form-data"})
    public ResponseEntity<DailyExpense> createExpense(@RequestParam(name= "file", required = false) MultipartFile file, @ModelAttribute  DailyExpense dailyExpense) throws IOException {
                    if (file != null) {

//                String filename = StringUtils.cleanPath(file.getOriginalFilename());
//                dailyExpense.setDocument(filename.getBytes());
                //compressing data
                byte[] compressData = compress(file.getBytes());
                int compressedSize = compressData.length;
                logger.info("file size "+ compressedSize+" bytes");
                dailyExpense.setDocument(compressData);
            }
        if (dailyExpense.getDate() == null) {
            dailyExpense.setDate(LocalDate.now());
        }
        DailyExpense expense = dailyExpenseService.createExpense(dailyExpense);
        return new ResponseEntity<>(expense, HttpStatus.CREATED);
    }
        private byte[] compress(byte[] data) throws IOException{
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try(DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(outputStream)){
                deflaterOutputStream.write(data);
            }
            return outputStream.toByteArray();
        }

    @GetMapping("/bymonth")
    public ResponseEntity<Map<String, Object>> getAllExpenseByMonth(@RequestParam String filterType,@RequestParam(required = false) String year, @RequestParam(required = false) String month, @RequestParam(required = false) String day){
        List<DailyExpense> dailyExpenses;
        double totalExpense =0;
        try {
            switch (filterType.toLowerCase()) {
                case "day":
                    dailyExpenses = dailyExpenseService.getExpenseByDay(Integer.parseInt(year),
                            Integer.parseInt(month), Integer.parseInt(day));
                    break;

                case "month":
                    dailyExpenses = dailyExpenseService.getExpenseByMonth(Integer.parseInt(year),
                            Integer.parseInt(month));
                    break;

                case "year":
                    dailyExpenses = dailyExpenseService.getExpenseByYear(Integer.parseInt(year));
                    break;

                default:
                    throw new IllegalArgumentException("Invalid filter type...");

            }
        }catch (NumberFormatException e){
            throw new IllegalArgumentException("Invalid numeric value provided for year, month, or day.");
        }

        //total expense
        for(DailyExpense expense: dailyExpenses){
            totalExpense += expense.getExpense();
        }
        Map<String, Object> response = new HashMap<>();
        response.put("dailyExpenses", dailyExpenses);
        response.put("totalExpenses",totalExpense);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<DailyExpense> updateDailyExpense(@PathVariable Long id, @RequestBody DailyExpense dailyExpense){
       DailyExpense existingExpense = dailyExpenseService.getExpenseById(id);
       if(existingExpense == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
       }
       dailyExpense.setId(id);
       DailyExpense updateEntity = dailyExpenseService.updateDailyExpense(dailyExpense);
       return new ResponseEntity<>(updateEntity, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DailyExpense> deleteDailyExpenseEntry(@PathVariable Long id){
        boolean deleted = dailyExpenseService.deleteDailyExpenseEntry(id);
        if(!deleted){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/average-by-day")
    public  ResponseEntity<Map<String, Double>> getAverageExpensesByDay(){
        List<DailyExpense> dailyExpenses = dailyExpenseService.getAllExpenses();

        //grouping daily expenses by day
        Map<LocalDate, List<DailyExpense>> expensesByDay =
                dailyExpenses.stream().collect(Collectors.groupingBy(DailyExpense::getDate));

        //calculate total expenses and average
       Map<String, Double> averageExpensesByDay =
               expensesByDay.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey().toString(),
                entry ->{double totalExpense = entry.getValue().stream().mapToDouble(DailyExpense::getExpense).sum();
                return totalExpense / entry.getValue().size();
               }
               ));
       return new ResponseEntity<>(averageExpensesByDay, HttpStatus.OK);
    }

    @GetMapping("/get-total-expenses")
    public ResponseEntity<Double> getTotalExpense(){
        double totalExpense = dailyExpenseService.getTotalExpenses( );
        return ResponseEntity.ok(totalExpense);
    }

    @GetMapping("/getBy-category")
    public ResponseEntity<Map<String, Double>> getDataByCategory(){
        Map<String, Double> expenseDataByCategory = dailyExpenseService.getDataByCategory();
        return ResponseEntity.ok(expenseDataByCategory);
    }

    @GetMapping("/top-spending-category")
    public ResponseEntity<List<String>> getTopSpendingCategories(){
        List<String> topCategory = dailyExpenseService.getTopSpendingCategories();
        return ResponseEntity.ok(topCategory);
    }
}
