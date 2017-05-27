package com.kevin.web;

import com.kevin.domain.Person;
import com.kevin.domain.PersonRepository;
import com.kevin.domain.User;
import com.kevin.domain.UserRepository;
import com.kevin.Util;
import io.swagger.annotations.ApiOperation;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.security.MessageDigest;

import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by min on 2017/5/23.
 */
@RestController
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PersonRepository personRepository;

    Util util = new Util();


    @ApiOperation(value = "查询所有用户")
    @GetMapping(value="/users/")
    public List<User> getUsersList(){
        return userRepository.findAll();
    }

    @ApiOperation(value = "查询所有未通过审核用户")
    @GetMapping(value = "/notpassedusers")
    private List<User> getNotpassedUsersList(){
        return userRepository.findAllByVerify(-1);
    }

    @ApiOperation(value = "查询所有被禁用用户")
    @GetMapping(value = "/forbiddenusers")
    private List<User> getForbiddenUsersList(){
        return userRepository.findAllByVerify(2);
    }

    @ApiOperation(value = "查询所有通过审核用户")
    @GetMapping(value = "/passedusers")
    private List<User> getPassedUsersList(){
        return userRepository.findAllByVerify(1);
    }

    @ApiOperation(value = "查询所有尚未审核用户")
    @GetMapping(value = "/waitusers")
    private List<User> getWaitUsersList(){
        return userRepository.findAllByVerify(0);
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
    public void deleteUnpassUser(@PathVariable("id") Integer id) throws JSONException {
        //PersonController personController = new PersonController();
        //Person person = personController.getPerson(id);
        User user = userRepository.findOne(id);
        Long username = user.getUsername();
        Person person = personRepository.findByStudentNum(username);
        personRepository.delete(person);
        userRepository.delete(id);
    }


    @ApiOperation(value = "查询当前登录用户")
    @GetMapping(value = "/user/{id}")
    public User getCurrentUser(@PathVariable("id") Integer id){
        return userRepository.findUserById(id);
    }


    @ApiOperation(value = "注册")
    @PostMapping(value = "/user/creation")
    public Map register(@RequestParam("username") Long username,
                               @RequestParam("password") String password) {
        Map<String,Object> map = new HashMap<>();
        if(username==null || Objects.equals(password, "")){
            map.put("success",false);
            map.put("message","用户名或密码不能为空");
            return map;
        }
        if (userRepository.findByUsername(username) == null) {
            User user =new User();
            user.setUsername(username);
            user.setPassword(password);
            user.setLoginCount(0);
            userRepository.save(user);
            Person person = personRepository.findByStudentNum(username);
            if(person==null){
                Person p = new Person();
                p.setStudentNum(username);
                personRepository.save(p);
            }

            map.put("success",true);
            return map;
        }
        else {
            map.put("success",false);
            map.put("message","该账户已注册");
            return map;
        }
    }

    @ApiOperation(value = "登陆")
    @PostMapping(value = "/user/login")
    public Map login(@RequestParam("username") Long username,
                            @RequestParam("password") String password) throws ParseException, NoSuchAlgorithmException {
        Map<String,Object> map = new HashMap<>();
        if(username==null || Objects.equals(password, "")){
            map.put("success",false);
            map.put("message","用户名或密码不能为空");
            return map;
        }
        User user = userRepository.findByUsername(username);
        if (user == null){
            map.put("success",false);
            map.put("message","用户不存在");
            return map;
        }else if (!Objects.equals(user.getPassword(), password)){
            map.put("success",false);
            map.put("message","密码错误");
            return map;
        }
        if(user.getVerify()==0){
            map.put("success",false);
            map.put("message","尚未通过审核，请等待管理员审核");
            return map;
        }else if(user.getVerify()==-1){
            map.put("success",false);
            map.put("message","审核未通过，请联系管理员");
            return map;
        }else if (user.getVerify()==2){
            map.put("success",false);
            map.put("message","账户被禁用，请联系管理员");
            return map;
        }
//        if (user.getLoginCount()==0 || user.getLoginCount()==null){
//            Person person = personRepository.findByStudentNum(username);
//            if(person==null){
//                Person p = new Person();
//                p.setStudentNum(username);
//                personRepository.save(p);
//            }
//        }

        user.setLoginCount(user.getLoginCount()+1);
        user.setLastLoginTime(new Timestamp(System.currentTimeMillis()));
        userRepository.save(user);

        // 生成token
        String token;
        Long userName = user.getUsername();
        String strLong = Long.toString(userName);
        byte[] inputData = strLong.getBytes();
        MessageDigest messageDigest = MessageDigest.getInstance("SHA");
        messageDigest.update(inputData);
        BigInteger sha = new BigInteger(messageDigest.digest());
        String strCurrentTime = Long.toString(System.currentTimeMillis());
        token = strCurrentTime + sha.toString(32);


        map.put("name",username);
        map.put("success","true");
        map.put("message","登陆成功");
        map.put("user",user);
        map.put("token",token);
        return map;
    }

    @ApiOperation(value = "查询token是否过期",notes = "返回true未过期")
    @GetMapping(value = "/user/token/{token}")
    public Map getTokenStatus(@PathVariable("token") String token){
        Map<String,Object> map = new HashMap<>();
        boolean b = util.checkToken(token);
        map.put("success",b);
        return map;
    }

}
