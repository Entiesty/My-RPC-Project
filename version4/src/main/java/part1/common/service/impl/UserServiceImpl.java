package part1.common.service.impl;

import part1.common.pojo.User;
import part1.common.service.UserService;

import java.util.Random;
import java.util.UUID;

public class UserServiceImpl implements UserService {
    @Override
    public User getUserByUserId(Integer id) {
        //模拟数据库查询操作
        System.out.println("客服端查询了id为" + id + "的用户");
        Random random = new Random();

        //模拟从数据库中获取用户数据的操作
        return User.builder()
                .id(id)
                .userName(UUID.randomUUID().toString())
                .sex(random.nextBoolean())
                .build();
    }

    @Override
    public Integer insertUserId(User user) {
        System.out.println("插入用户名为" + user.getUserName() + "的用户成功");

        return user.getId();
    }
}
