package com.kevin.web;

import com.kevin.domain.User;
import com.kevin.domain.UserRepository;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Created by min on 2017/5/23.
 */
@RestController
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @ApiOperation(value = "查询所有用户")
    @GetMapping(value="/users")
    public List<User> getUsersList(){
        return userRepository.findAll();
    }

    @ApiOperation(value = "查询所有未通过审核用户")
    @GetMapping(value = "/notpassedusers")
    private List<User> getNotpassedUsersList(){
        return userRepository.findAllByVerify(-1);
    }

    @ApiOperation(value = "查询所有通过审核用户")
    @GetMapping(value = "/passedusers")
    private List<User> getPassedUsersList(){
        return userRepository.findAllByVerify(1);
    }

    @ApiOperation(value = "删除用户")
    @DeleteMapping(value = "/user/{id}")
    public void deleteUser(@PathVariable("id") Integer id){
        userRepository.delete(id);
    }

    @ApiOperation(value = "通过审核", notes="根据url的id找到用户并通过审核")
    @PutMapping(value = "/user/{id}/verify=1")
    public void putUserToPass(@PathVariable("id") Integer id){
        User user = userRepository.findOne(id);
        user.setVerify(1);
        userRepository.save(user);
    }

    @ApiOperation(value = "不通过审核", notes="根据url的id找到用户并拒绝通过此用户的审核")
    @PutMapping(value = "/user/{id}/verify=-1")
    public void putUserToNotPass(@PathVariable("id") Integer id){
        User user = userRepository.findOne(id);
        user.setVerify(-1);
        userRepository.save(user);
    }

    @ApiOperation(value = "禁用账户", notes="根据url的id找到用户并禁用账户")
    @PutMapping(value = "/user/{id}/verify=2")
    public void putUserToForbid(@PathVariable("id") Integer id){
        User user = userRepository.findOne(id);
        user.setVerify(2);
        userRepository.save(user);
    }

    @ApiOperation(value = "删除未通过审核的用户")
    @DeleteMapping(value = "/notpasseduser/{id}")
    public void deleteUnpassUser(@PathVariable("id") Integer id){
        userRepository.delete(id);
    }


    @ApiOperation(value = "查询当前登录用户")
    @GetMapping(value = "/user/{id}")
    public User getCurrentUser(@PathVariable("id") Integer id){
        return userRepository.findUserById(id);
    }


    @ApiOperation(value = "注册")
    @PostMapping(value = "/user/creation")
    public String  register(@RequestParam("username") Long username,
                            @RequestParam("password") String password) {
        if (userRepository.findByUsername(username) == null) {
            User user =new User();
            user.setUsername(username);
            user.setPassword(password);
            user.setLoginCount(0);
            userRepository.save(user);
            return "注册成功";
        }
        else {
            return "该账户已注册";
        }
    }

    @ApiOperation(value = "登陆")
    @PostMapping(value = "/user/login")
    public String login(@RequestParam("username") Long username,
                        @RequestParam("password") String password){
        if (userRepository.findByUsername(username) == null){
            return "用户不存在";
        }else if (!Objects.equals(userRepository.findByUsername(username).getPassword(), password)){
            return "密码错误";
        }
        return "登陆成功";
    }


}
