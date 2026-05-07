package com.eleraky.studentexchange.repository;

import com.eleraky.studentexchange.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // JpaRepository يعطينا جاهزاً دوال مثل save, findAll, findById, delete
    // يمكننا إضافة دوال مخصصة هنا لاحقاً، مثلاً:
    // User findByUsername(String username);
}
