package com.eh.springbootcache.controller;

import com.eh.springbootcache.orm.bean.Employee;
import com.eh.springbootcache.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmployeeController {
    @Autowired
    EmployeeService employeeService;

    /**
     * 测试：http://localhost:8080/emp/2
     *
     * @param id
     * @return
     */
    @GetMapping("/emp/{id}")
    public Employee getEmployee(@PathVariable("id") Integer id) {
        Employee employee = employeeService.getEmpById(id);
        return employee;
    }

    /**
     * 测试：http://localhost:8080/emp?id=2&email=jimmy@gmail.com
     *
     * @param employee
     * @return
     */
    @GetMapping("/emp")
    public Employee update(Employee employee) {
        Employee emp = employeeService.updateEmp(employee);
        return emp;
    }

    /**
     * 测试：http://localhost:8080/del/2
     *
     * @param id
     * @return
     */
    @GetMapping("/del/{id}")
    public String deleteEmp(@PathVariable("id") Integer id) {
        employeeService.deleteEmpById(id);
        return "success";
    }
}
