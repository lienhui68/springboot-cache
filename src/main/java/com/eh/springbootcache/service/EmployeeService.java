package com.eh.springbootcache.service;

import com.eh.springbootcache.orm.bean.Employee;
import com.eh.springbootcache.orm.dao.EmployeeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class EmployeeService {


    @Autowired
    EmployeeMapper employeeMapper;


    @Cacheable(cacheNames = {"emp"})
    public Employee getEmpById(Integer id) {
        System.out.println("查询" + id + "号员工");
        Employee emp = employeeMapper.selectByPrimaryKey(id);
        return emp;
    }

    @CachePut(value = "emp", key = "#employee.id")
    public Employee updateEmp(Employee employee) {
        System.out.println("updateEmp:" + employee);
        employeeMapper.updateByPrimaryKeySelective(employee);
        return employee;
    }


    @CacheEvict(value = "emp", key = "#id")
    public void deleteEmpById(Integer id) {
        System.out.println("deleteEmp:" + id);
        employeeMapper.deleteByPrimaryKey(id);
    }


}
