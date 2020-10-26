package com.eh.springbootcache.service;

import com.eh.springbootcache.orm.bean.Department;
import com.eh.springbootcache.orm.dao.DepartmentMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;


@Service
public class DeptService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeptService.class);

    @Autowired
    DepartmentMapper departmentMapper;

    @Autowired
    CacheManager cacheManager;

    /**
     * 使用编码方式开发
     *
     * @param id
     * @return
     */
    public Department getDeptById(Integer id) {
        LOGGER.info("查询部门:{}", id);
        //获取某个缓存
        Cache cache = cacheManager.getCache("dept");
        Department department = cache.get(id, Department.class);
        if (department != null) {
            return department;
        }
        department = departmentMapper.selectByPrimaryKey(id);
        cache.put(id, department);
        return department;
    }


}
