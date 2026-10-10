package com.municipal.controller;

import com.municipal.dto.response.ApiResponse;
import com.municipal.entity.Category;
import com.municipal.entity.Department;
import com.municipal.repository.CategoryRepository;
import com.municipal.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PublicDataController {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<Category>>> getActiveCategories() {
        List<Category> categories = categoryRepository.findByActiveTrue();
        return ResponseEntity.ok(ApiResponse.success("Active categories loaded", categories));
    }

    @GetMapping("/departments")
    public ResponseEntity<ApiResponse<List<Department>>> getActiveDepartments() {
        List<Department> departments = departmentRepository.findByActiveTrue();
        return ResponseEntity.ok(ApiResponse.success("Active departments loaded", departments));
    }
}
