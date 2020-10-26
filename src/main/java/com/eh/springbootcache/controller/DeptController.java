package com.eh.springbootcache.controller;

import com.eh.springbootcache.orm.bean.Department;
import com.eh.springbootcache.service.DeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DeptController {

    @Autowired
    private DeptService deptService;

    /**
     * 测试：
     * @param id
     * @return
     */
    @GetMapping("/dept/{id}")
    public Department getDept(@PathVariable("id") Integer id) {
        return deptService.getDeptById(id);
    }
}
